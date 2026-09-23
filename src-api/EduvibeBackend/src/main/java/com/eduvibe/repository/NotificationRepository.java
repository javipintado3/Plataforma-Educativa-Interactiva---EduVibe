package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eduvibe.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findTop30ByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadAtIsNull(UUID userId);

    /**
     * Marca como leídas todas las pendientes de una persona en una sola
     * sentencia, en lugar de traerlas y guardarlas una a una.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP "
         + "WHERE n.user.id = :userId AND n.readAt IS NULL")
    int marcarTodasLeidas(@Param("userId") UUID userId);
}
