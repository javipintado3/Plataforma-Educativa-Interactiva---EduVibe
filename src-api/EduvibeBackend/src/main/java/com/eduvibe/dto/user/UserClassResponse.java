package com.eduvibe.dto.user;

import java.util.UUID;

import com.eduvibe.model.Enrollment;

/** Una clase en la que participa la persona, para su ficha en el panel de administración. */
public record UserClassResponse(
        UUID classId,
        String name,
        String subject,
        String color,
        String roleInClass) {

    public static UserClassResponse de(Enrollment matricula) {
        return new UserClassResponse(
                matricula.getSchoolClass().getId(),
                matricula.getSchoolClass().getName(),
                matricula.getSchoolClass().getSubject(),
                matricula.getSchoolClass().getColor(),
                matricula.getRoleInClass().getValor());
    }
}
