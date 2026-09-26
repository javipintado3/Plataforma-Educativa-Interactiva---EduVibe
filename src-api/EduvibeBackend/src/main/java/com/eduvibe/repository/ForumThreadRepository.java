package com.eduvibe.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.ForumThread;

public interface ForumThreadRepository extends JpaRepository<ForumThread, UUID> {

    List<ForumThread> findBySchoolClassId(UUID classId);
}
