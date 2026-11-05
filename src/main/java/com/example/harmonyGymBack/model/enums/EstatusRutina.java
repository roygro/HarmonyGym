package com.example.harmonyGymBack.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EstatusRutina {
    Activa("Activa"),
    Inactiva("Inactiva");

    private final String valor;

    EstatusRutina(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstatusRutina fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        for (EstatusRutina e : values()) {
            if (e.valor.equalsIgnoreCase(valor.trim())) {
                return e;
            }
        }
        if (valor.trim().equalsIgnoreCase("ACTIVO") || valor.trim().equalsIgnoreCase("ACTIVA")) {
            return Activa;
        }
        if (valor.trim().equalsIgnoreCase("INACTIVO") || valor.trim().equalsIgnoreCase("INACTIVA")) {
            return Inactiva;
        }
        throw new IllegalArgumentException("EstatusRutina no válido: " + valor);
    }

    @Converter(autoApply = true)
    public static class EstatusRutinaConverter implements AttributeConverter<EstatusRutina, String> {
        @Override
        public String convertToDatabaseColumn(EstatusRutina attribute) {
            return attribute != null ? attribute.getValor() : null;
        }

        @Override
        public EstatusRutina convertToEntityAttribute(String dbData) {
            return dbData != null ? EstatusRutina.fromString(dbData) : null;
        }
    }
}
