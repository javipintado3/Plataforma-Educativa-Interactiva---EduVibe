package com.eduvibe.model.converter;

import com.eduvibe.model.enums.UserRole;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Traduce entre el enumerado Java y el valor que guarda la base de datos.
 *
 * Con @Enumerated(EnumType.STRING) Hibernate guardaría el nombre de la
 * constante ("ADMIN"), pero la restricción CHECK de la columna users.role
 * espera minúsculas ("admin"). Un convertidor deja cada lado con la forma que
 * le corresponde sin ensuciar ninguno de los dos.
 *
 * autoApply = true hace que se use en cualquier campo de tipo UserRole, sin
 * tener que anotarlos uno a uno.
 */
@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole rol) {
        return rol == null ? null : rol.getValor();
    }

    @Override
    public UserRole convertToEntityAttribute(String valor) {
        return valor == null ? null : UserRole.desdeValor(valor);
    }
}
