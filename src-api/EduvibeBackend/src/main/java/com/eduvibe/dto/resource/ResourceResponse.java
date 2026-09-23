package com.eduvibe.dto.resource;

import java.time.Instant;
import java.util.UUID;

import com.eduvibe.model.Resource;

public record ResourceResponse(
        UUID id,
        UUID topicId,
        String title,
        String fileUrl,
        String type,
        int sortOrder,
        Instant createdAt) {

    public static ResourceResponse de(Resource material) {
        return new ResourceResponse(
                material.getId(),
                material.getTopic() == null ? null : material.getTopic().getId(),
                material.getTitle(),
                material.getFileUrl(),
                material.getType() == null ? null : material.getType().getValor(),
                material.getSortOrder(),
                material.getCreatedAt());
    }
}
