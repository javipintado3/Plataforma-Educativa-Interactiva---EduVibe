package com.eduvibe.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.announcement.AnnouncementResponse;
import com.eduvibe.dto.announcement.SaveAnnouncementRequest;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Announcement;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.User;
import com.eduvibe.repository.AnnouncementRepository;
import com.eduvibe.repository.UserRepository;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

/**
 * Muro de avisos de una clase.
 *
 * Solo el profesorado de la clase (o administración) publica y retira avisos;
 * lo decide {@link ClassAccessService#exigirEditable}, igual que para tareas y
 * temas. El alumnado matriculado solo lee.
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final ClassAccessService acceso;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listar(UUID classId) {
        acceso.exigirVisible(classId);
        boolean puedoBorrar = acceso.puedeCalificarEn(classId);

        return announcementRepository.findBySchoolClassIdOrderByPinnedDescCreatedAtDesc(classId)
                .stream()
                .map(aviso -> AnnouncementResponse.de(aviso, puedoBorrar))
                .toList();
    }

    @Transactional
    public AnnouncementResponse crear(UUID classId, SaveAnnouncementRequest peticion) {
        SchoolClass clase = acceso.exigirEditable(classId);
        AuthenticatedUser autenticado = authService.identidadActual();

        User autor = userRepository.findById(autenticado.id())
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));

        Announcement aviso = new Announcement(clase, autor, peticion.content().trim(), peticion.estaFijado());
        announcementRepository.saveAndFlush(aviso);

        return AnnouncementResponse.de(aviso, true);
    }

    @Transactional
    public void eliminar(UUID announcementId) {
        Announcement aviso = announcementRepository.findById(announcementId)
                .orElseThrow(() -> NotFoundException.de("Aviso", announcementId));

        acceso.exigirEditable(aviso.getSchoolClass().getId());

        announcementRepository.delete(aviso);
    }
}
