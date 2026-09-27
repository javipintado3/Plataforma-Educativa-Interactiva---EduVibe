package com.eduvibe.model.converter;

import com.eduvibe.model.enums.ClassViewMode;

import jakarta.persistence.Converter;

/**
 * Conversión del modo de vista de la clase. La lógica está en {@link EnumConValorConverter}.
 */
@Converter(autoApply = true)
public class ClassViewModeConverter extends EnumConValorConverter<ClassViewMode> {

    public ClassViewModeConverter() {
        super(ClassViewMode.class);
    }
}
