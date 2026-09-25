package com.eduvibe.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.resource.ResourceResponse;
import com.eduvibe.dto.resource.SaveResourceRequest;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Resource;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.Topic;
import com.eduvibe.model.enums.ResourceType;
import com.eduvibe.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

/**
 * Materiales de una clase: apuntes, enlaces, vídeos.
 *
 * Como con avisos y tareas, solo el profesorado de la clase (o administración)
 * gestiona los materiales; lo decide {@link ClassAccessService#exigirEditable}.
 */
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ClassAccessService acceso;
    private final TopicService topicService;

    @Transactional(readOnly = true)
    public List<ResourceResponse> listar(UUID classId) {
        acceso.exigirVisible(classId);

        return resourceRepository.findBySchoolClassIdOrderBySortOrderAscTitleAsc(classId)
                .stream()
                .map(ResourceResponse::de)
                .toList();
    }

    /** El orden es automático: cada material nuevo va al final de la lista de la clase. */
    @Transactional
    public ResourceResponse crear(UUID classId, SaveResourceRequest peticion) {
        SchoolClass clase = acceso.exigirEditable(classId);
        Topic tema = topicService.resolverDeClase(peticion.topicId(), classId);

        int orden = resourceRepository.findBySchoolClassIdOrderBySortOrderAscTitleAsc(classId).size();

        Resource material = new Resource(clase, peticion.title().trim(), normalizar(peticion.fileUrl()),
                tipoOSinTipo(peticion.type()), orden);
        material.setTopic(tema);
        resourceRepository.saveAndFlush(material);

        return ResourceResponse.de(material);
    }

    @Transactional
    public ResourceResponse actualizar(UUID resourceId, SaveResourceRequest peticion) {
        Resource material = resourceRepository.findById(resourceId)
                .orElseThrow(() -> NotFoundException.de("Material", resourceId));

        UUID classId = material.getSchoolClass().getId();
        acceso.exigirEditable(classId);

        material.setTitle(peticion.title().trim());
        material.setFileUrl(normalizar(peticion.fileUrl()));
        material.setType(tipoOSinTipo(peticion.type()));
        material.setTopic(topicService.resolverDeClase(peticion.topicId(), classId));
        resourceRepository.save(material);

        return ResourceResponse.de(material);
    }

    @Transactional
    public void eliminar(UUID resourceId) {
        Resource material = resourceRepository.findById(resourceId)
                .orElseThrow(() -> NotFoundException.de("Material", resourceId));

        acceso.exigirEditable(material.getSchoolClass().getId());

        resourceRepository.delete(material);
    }

    private ResourceType tipoOSinTipo(String type) {
        return (type == null || type.isBlank()) ? null : ResourceType.desdeValor(type);
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
