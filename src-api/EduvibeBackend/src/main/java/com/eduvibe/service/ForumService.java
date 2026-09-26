package com.eduvibe.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.forum.ForumPostResponse;
import com.eduvibe.dto.forum.ForumThreadDetailResponse;
import com.eduvibe.dto.forum.ForumThreadResponse;
import com.eduvibe.dto.forum.SaveForumPostRequest;
import com.eduvibe.dto.forum.SaveForumThreadRequest;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.ForumPost;
import com.eduvibe.model.ForumThread;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.User;
import com.eduvibe.repository.ForumPostRepository;
import com.eduvibe.repository.ForumThreadRepository;
import com.eduvibe.repository.UserRepository;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

/**
 * Foro de debate de una clase: hilos y sus mensajes.
 *
 * Cualquier persona matriculada (alumnado o profesorado) puede abrir un hilo
 * y responder; lo decide {@link ClassAccessService#exigirVisible}, no
 * {@code exigirEditable} como en avisos o materiales, porque el foro es un
 * espacio de la clase entera, no algo que solo publique el profesorado.
 *
 * La moderación —borrar el mensaje de otra persona— sí es solo del
 * profesorado de la clase o administración; borrar el propio mensaje lo
 * puede hacer cualquiera, sea o no profesorado.
 */
@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumThreadRepository forumThreadRepository;
    private final ForumPostRepository forumPostRepository;
    private final UserRepository userRepository;
    private final ClassAccessService acceso;
    private final AuthService authService;
    private final TopicService topicService;

    @Transactional(readOnly = true)
    public List<ForumThreadResponse> listar(UUID classId) {
        acceso.exigirVisible(classId);

        List<ForumThread> hilos = forumThreadRepository.findBySchoolClassId(classId);
        if (hilos.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = hilos.stream().map(ForumThread::getId).toList();
        Map<UUID, ForumPostRepository.EstadisticasDeHilo> estadisticas = new HashMap<>();
        for (var fila : forumPostRepository.estadisticasDe(ids)) {
            estadisticas.put(fila.getThreadId(), fila);
        }

        return hilos.stream()
                .map(hilo -> {
                    var stats = estadisticas.get(hilo.getId());
                    long total = stats != null ? stats.getTotal() : 0;
                    Instant ultima = stats != null ? stats.getUltima() : null;
                    return ForumThreadResponse.de(hilo, total, ultima);
                })
                // Los hilos con actividad más reciente van primero, como en cualquier foro
                .sorted((a, b) -> b.lastActivityAt().compareTo(a.lastActivityAt()))
                .toList();
    }

    @Transactional
    public ForumThreadDetailResponse crear(UUID classId, SaveForumThreadRequest peticion) {
        SchoolClass clase = acceso.exigirVisible(classId);
        User autor = usuarioActual();

        ForumThread hilo = new ForumThread(clase, autor, peticion.title().trim());
        hilo.setTopic(topicService.resolverDeClase(peticion.topicId(), classId));
        forumThreadRepository.saveAndFlush(hilo);

        ForumPost primerMensaje = new ForumPost(hilo, autor, peticion.content().trim());
        forumPostRepository.save(primerMensaje);

        return detalle(hilo.getId());
    }

    @Transactional(readOnly = true)
    public ForumThreadDetailResponse detalle(UUID threadId) {
        ForumThread hilo = forumThreadRepository.findById(threadId)
                .orElseThrow(() -> NotFoundException.de("Hilo", threadId));

        UUID classId = hilo.getSchoolClass().getId();
        acceso.exigirVisible(classId);

        AuthenticatedUser autenticado = authService.identidadActual();
        boolean puedoModerar = acceso.puedeCalificarEn(classId);
        boolean puedoBorrarHilo = puedoModerar || hilo.getAuthor().getId().equals(autenticado.id());

        List<ForumPostResponse> posts = forumPostRepository.findByThreadIdOrderByCreatedAtAsc(threadId).stream()
                .map(mensaje -> ForumPostResponse.de(mensaje,
                        puedoModerar || mensaje.getAuthor().getId().equals(autenticado.id())))
                .toList();

        return ForumThreadDetailResponse.de(hilo, puedoModerar, puedoBorrarHilo, posts);
    }

    @Transactional
    public ForumPostResponse responder(UUID threadId, SaveForumPostRequest peticion) {
        ForumThread hilo = forumThreadRepository.findById(threadId)
                .orElseThrow(() -> NotFoundException.de("Hilo", threadId));

        acceso.exigirVisible(hilo.getSchoolClass().getId());
        User autor = usuarioActual();

        ForumPost mensaje = new ForumPost(hilo, autor, peticion.content().trim());
        // saveAndFlush, no save: createdAt lo rellena la base de datos al
        // insertar (@Generated), y se lee aquí mismo para la respuesta sin
        // ninguna consulta después que fuerce el flush.
        forumPostRepository.saveAndFlush(mensaje);

        return ForumPostResponse.de(mensaje, true);
    }

    @Transactional
    public void eliminarHilo(UUID threadId) {
        ForumThread hilo = forumThreadRepository.findById(threadId)
                .orElseThrow(() -> NotFoundException.de("Hilo", threadId));

        exigirAutorOModerador(hilo.getSchoolClass().getId(), hilo.getAuthor().getId());
        forumThreadRepository.delete(hilo);
    }

    @Transactional
    public void eliminarMensaje(UUID postId) {
        ForumPost mensaje = forumPostRepository.findById(postId)
                .orElseThrow(() -> NotFoundException.de("Mensaje", postId));

        exigirAutorOModerador(mensaje.getThread().getSchoolClass().getId(), mensaje.getAuthor().getId());
        forumPostRepository.delete(mensaje);
    }

    /** Solo el autor del mensaje/hilo, o el profesorado de la clase como moderación. */
    private void exigirAutorOModerador(UUID classId, UUID autorId) {
        AuthenticatedUser autenticado = authService.identidadActual();
        acceso.exigirVisible(classId);

        boolean esAutor = autorId.equals(autenticado.id());
        if (!esAutor && !acceso.puedeCalificarEn(classId)) {
            throw new AccessDeniedException("Solo quien lo escribió o el profesorado puede borrarlo");
        }
    }

    private User usuarioActual() {
        AuthenticatedUser autenticado = authService.identidadActual();
        return userRepository.findById(autenticado.id())
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));
    }
}
