package com.eduvibe.model.converter;

import com.eduvibe.model.enums.UserStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Equivalente a {@link UserRoleConverter} para el estado de la cuenta.
 */
@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {

    @Override
    public String convertToDatabaseColumn(UserStatus estado) {
        return estado == null ? null : estado.getValor();
    }

    @Override
    public UserStatus convertToEntityAttribute(String valor) {
        return valor == null ? null : UserStatus.desdeValor(valor);
    }
}
