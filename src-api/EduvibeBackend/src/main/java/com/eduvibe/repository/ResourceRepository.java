package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.Resource;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    List<Resource> findBySchoolClassIdOrderBySortOrderAscTitleAsc(UUID classId);

    long countByTopicId(UUID topicId);
}
