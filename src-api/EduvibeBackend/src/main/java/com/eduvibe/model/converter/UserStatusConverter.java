package com.eduvibe.model.converter;

import com.eduvibe.model.enums.UserStatus;

import jakarta.persistence.Converter;

/**
 * Conversión del estado de cuenta. La lógica está en {@link EnumConValorConverter};
 * aquí solo se fija el tipo concreto, que es lo que JPA necesita para poder
 * aplicarlo automáticamente.
 */
@Converter(autoApply = true)
public class UserStatusConverter extends EnumConValorConverter<UserStatus> {

    public UserStatusConverter() {
        super(UserStatus.class);
    }
}
