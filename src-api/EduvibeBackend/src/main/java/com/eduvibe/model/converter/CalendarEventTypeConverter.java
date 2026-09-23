package com.eduvibe.model.converter;

import com.eduvibe.model.enums.CalendarEventType;

import jakarta.persistence.Converter;

/**
 * Conversión del tipo de evento de calendario. La lógica está en {@link EnumConValorConverter}.
 */
@Converter(autoApply = true)
public class CalendarEventTypeConverter extends EnumConValorConverter<CalendarEventType> {

    public CalendarEventTypeConverter() {
        super(CalendarEventType.class);
    }
}
