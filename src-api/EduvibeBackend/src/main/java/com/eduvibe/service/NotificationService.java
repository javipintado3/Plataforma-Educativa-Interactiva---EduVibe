package com.eduvibe.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.notification.NotificationResponse;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Notification;
import com.eduvibe.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

/**
 * Notificaciones dirigidas a quien está autenticado.
 *
 * Nadie gestiona notificaciones ajenas —ni siquiera administración—, así que a
 * diferencia de avisos, materiales o eventos, aquí no hace falta
 * {@link ClassAccessService}: basta con comprobar que la notificación es de
 * quien pregunta.
 *
 * Quién las emite (una tarea nueva, una nota publicada) queda para cuando esos
 * flujos lo necesiten: este servicio es, por ahora, solo el lado de lectura.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<NotificationResponse> misNotificaciones() {
        UUID userId = authService.identidadActual().id();

        return notificationRepository.findTop30ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public long noLeidas() {
        return notificationRepository.countByUserIdAndReadAtIsNull(authService.identidadActual().id());
    }

    @Transactional
    public void marcarLeida(UUID notificationId) {
        Notification aviso = notificationRepository.findById(notificationId)
                .orElseThrow(() -> NotFoundException.de("Notificación", notificationId));

        if (!aviso.getUser().getId().equals(authService.identidadActual().id())) {
            throw new AccessDeniedException("Esta notificación no es tuya");
        }

        aviso.marcarComoLeida();
        notificationRepository.save(aviso);
    }

    @Transactional
    public void marcarTodasLeidas() {
        notificationRepository.marcarTodasLeidas(authService.identidadActual().id());
    }
}
