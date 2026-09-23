package com.eduvibe.model.converter;

import com.eduvibe.model.enums.ResourceType;

import jakarta.persistence.Converter;

/**
 * Conversión del tipo de material. La lógica está en {@link EnumConValorConverter}.
 */
@Converter(autoApply = true)
public class ResourceTypeConverter extends EnumConValorConverter<ResourceType> {

    public ResourceTypeConverter() {
        super(ResourceType.class);
    }
}
