package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    /**
     * Muro de la clase: primero los fijados, y dentro de cada grupo lo más
     * reciente arriba. Se trae el autor en la misma consulta porque siempre se
     * muestra su nombre.
     */
    @EntityGraph(attributePaths = "author")
    List<Announcement> findBySchoolClassIdOrderByPinnedDescCreatedAtDesc(UUID classId);
}
