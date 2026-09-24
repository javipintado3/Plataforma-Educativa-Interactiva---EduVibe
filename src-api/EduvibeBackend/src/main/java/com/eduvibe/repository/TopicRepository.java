package com.eduvibe.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.Topic;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findBySchoolClassIdOrderBySortOrderAscTitleAsc(UUID classId);

    /**
     * Un tema, pero solo si es de esa clase.
     *
     * Sin esta comprobación, cualquier servicio que cuelgue contenido de un
     * tema (tareas, materiales) podría colgarlo de un tema de otra clase sin
     * más que conocer su identificador.
     */
    Optional<Topic> findByIdAndSchoolClassId(UUID id, UUID classId);
}
