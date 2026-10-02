package com.krs.backend.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ThemeModeConverter implements AttributeConverter<ThemeMode, String> {

    @Override
    public String convertToDatabaseColumn(ThemeMode attribute) {
        if (attribute == null) {
            return ThemeMode.LIGHT.getValue();
        }
        return attribute.getValue();
    }

    @Override
    public ThemeMode convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return ThemeMode.LIGHT;
        }
        return ThemeMode.fromValue(dbData);
    }
}
