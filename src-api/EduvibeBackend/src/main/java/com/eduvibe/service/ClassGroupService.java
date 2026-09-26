package com.eduvibe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.classgroup.ClassGroupResponse;
import com.eduvibe.dto.classgroup.SaveClassGroupRequest;
import com.eduvibe.exception.BadRequestException;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.ClassGroup;
import com.eduvibe.model.ClassGroupMember;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.User;
import com.eduvibe.model.enums.EnrollmentRole;
import com.eduvibe.repository.ClassGroupMemberRepository;
import com.eduvibe.repository.ClassGroupRepository;
import com.eduvibe.repository.EnrollmentRepository;
import com.eduvibe.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Subgrupos de una clase: quién trabaja con quién.
 *
 * Solo el profesorado de la clase (o administración) los gestiona, igual que
 * avisos, materiales o tareas; lo alumnado los ve, pero no los edita.
 */
@Service
@RequiredArgsConstructor
public class ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final ClassGroupMemberRepository classGroupMemberRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ClassAccessService acceso;

    @Transactional(readOnly = true)
    public List<ClassGroupResponse> listar(UUID classId) {
        acceso.exigirVisible(classId);

        List<ClassGroup> grupos = classGroupRepository.findBySchoolClassIdOrderByNameAsc(classId);
        if (grupos.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = grupos.stream().map(ClassGroup::getId).toList();
        Map<UUID, List<ClassGroupMember>> miembrosPorGrupo = classGroupMemberRepository.findByClassGroupIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(m -> m.getClassGroup().getId()));

        return grupos.stream()
                .map(grupo -> ClassGroupResponse.de(grupo, miembrosPorGrupo.getOrDefault(grupo.getId(), List.of())))
                .toList();
    }

    @Transactional
    public ClassGroupResponse crear(UUID classId, SaveClassGroupRequest peticion) {
        SchoolClass clase = acceso.exigirEditable(classId);

        ClassGroup grupo = new ClassGroup(clase, peticion.name().trim());
        classGroupRepository.saveAndFlush(grupo);

        List<ClassGroupMember> miembros = asignarMiembros(grupo, classId, peticion.memberIds());
        return ClassGroupResponse.de(grupo, miembros);
    }

    /** Reemplaza el nombre y los miembros enteros, igual que una rúbrica reemplaza sus criterios. */
    @Transactional
    public ClassGroupResponse actualizar(UUID groupId, SaveClassGroupRequest peticion) {
        ClassGroup grupo = classGroupRepository.findById(groupId)
                .orElseThrow(() -> NotFoundException.de("Subgrupo", groupId));

        UUID classId = grupo.getSchoolClass().getId();
        acceso.exigirEditable(classId);

        grupo.setName(peticion.name().trim());
        classGroupRepository.save(grupo);

        classGroupMemberRepository.deleteByClassGroupId(groupId);
        List<ClassGroupMember> miembros = asignarMiembros(grupo, classId, peticion.memberIds());

        return ClassGroupResponse.de(grupo, miembros);
    }

    /**
     * El subgrupo de un alumno en una clase, para adjuntarlo a una tarea
     * grupal. Null si no está en ninguno todavía.
     */
    @Transactional(readOnly = true)
    public ClassGroupResponse miGrupoEnClase(UUID userId, UUID classId) {
        return classGroupMemberRepository.findDeAlumnoEnClase(userId, classId)
                .map(membresia -> {
                    ClassGroup grupo = membresia.getClassGroup();
                    return ClassGroupResponse.de(grupo, classGroupMemberRepository.findByClassGroupId(grupo.getId()));
                })
                .orElse(null);
    }

    @Transactional
    public void eliminar(UUID groupId) {
        ClassGroup grupo = classGroupRepository.findById(groupId)
                .orElseThrow(() -> NotFoundException.de("Subgrupo", groupId));

        acceso.exigirEditable(grupo.getSchoolClass().getId());
        classGroupRepository.delete(grupo);
    }

    /**
     * Valida que cada miembro esté matriculado como alumno de la clase —sin
     * esto, se podría meter en un subgrupo a cualquiera solo con conocer su
     * identificador— y crea las filas de pertenencia.
     */
    private List<ClassGroupMember> asignarMiembros(ClassGroup grupo, UUID classId, List<UUID> memberIds) {
        List<ClassGroupMember> miembros = new ArrayList<>();
        for (UUID userId : memberIds) {
            boolean esAlumno = enrollmentRepository
                    .existsBySchoolClassIdAndUserIdAndRoleInClass(classId, userId, EnrollmentRole.STUDENT);
            if (!esAlumno) {
                throw new BadRequestException("Todos los miembros deben ser alumnado matriculado en esta clase");
            }
            User alumno = userRepository.findById(userId)
                    .orElseThrow(() -> NotFoundException.de("Usuario", userId));
            miembros.add(new ClassGroupMember(grupo, alumno));
        }
        classGroupMemberRepository.saveAll(miembros);
        return miembros;
    }
}
