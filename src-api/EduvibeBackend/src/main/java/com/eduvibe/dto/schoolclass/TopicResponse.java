package com.eduvibe.dto.schoolclass;

import java.util.UUID;

import com.eduvibe.model.Topic;

public record TopicResponse(UUID id, String title, int sortOrder) {

    public static TopicResponse de(Topic tema) {
        return new TopicResponse(tema.getId(), tema.getTitle(), tema.getSortOrder());
    }
}
