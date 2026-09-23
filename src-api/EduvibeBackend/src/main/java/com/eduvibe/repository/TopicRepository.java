package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.Topic;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findBySchoolClassIdOrderBySortOrderAscTitleAsc(UUID classId);
}
