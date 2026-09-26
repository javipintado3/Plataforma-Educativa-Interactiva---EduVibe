package com.eduvibe.dto.classgroup;

import java.util.List;
import java.util.UUID;

import com.eduvibe.model.ClassGroup;
import com.eduvibe.model.ClassGroupMember;

public record ClassGroupResponse(
        UUID id,
        String name,
        List<Miembro> members) {

    public static ClassGroupResponse de(ClassGroup grupo, List<ClassGroupMember> miembros) {
        return new ClassGroupResponse(
                grupo.getId(),
                grupo.getName(),
                miembros.stream().map(Miembro::de).toList());
    }

    public record Miembro(UUID id, String name) {
        public static Miembro de(ClassGroupMember miembro) {
            return new Miembro(miembro.getUser().getId(), miembro.getUser().getName());
        }
    }
}
