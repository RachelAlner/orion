package com.orion.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;

@Converter
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Short> {

    @Override
    public Short convertToDatabaseColumn(DayOfWeek dayOfWeek) {
        if(dayOfWeek == null) {
            return null;
        }

        return (short) dayOfWeek.getValue();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(Short value) {
        if (value == null) {
            return null;
        }

        return DayOfWeek.of(value);
    }
}