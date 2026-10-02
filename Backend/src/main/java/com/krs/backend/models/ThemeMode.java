package com.krs.backend.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ThemeMode {
    LIGHT("light"),
    DARK("dark");

    private final String value;

    ThemeMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ThemeMode fromValue(String text) {
        if (text == null) return LIGHT;
        for (ThemeMode b : ThemeMode.values()) {
            if (b.value.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return LIGHT;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
