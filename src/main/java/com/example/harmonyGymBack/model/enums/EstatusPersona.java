package com.example.harmonyGymBack.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EstatusPersona {
    Activo("Activo"),
    Inactivo("Inactivo");

    private final String valor;

    EstatusPersona(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstatusPersona fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        for (EstatusPersona e : values()) {
            if (e.valor.equalsIgnoreCase(valor.trim())) {
                return e;
            }
        }
        // Fallback tolerante para datos legacy
        if (valor.trim().equalsIgnoreCase("ACTIVO") || valor.trim().equalsIgnoreCase("ACTIVA")) {
            return Activo;
        }
        if (valor.trim().equalsIgnoreCase("INACTIVO") || valor.trim().equalsIgnoreCase("INACTIVA")) {
            return Inactivo;
        }
        throw new IllegalArgumentException("EstatusPersona no válido: " + valor);
    }

    @Converter(autoApply = true)
    public static class EstatusPersonaConverter implements AttributeConverter<EstatusPersona, String> {
        @Override
        public String convertToDatabaseColumn(EstatusPersona attribute) {
            return attribute != null ? attribute.getValor() : null;
        }

        @Override
        public EstatusPersona convertToEntityAttribute(String dbData) {
            return dbData != null ? EstatusPersona.fromString(dbData) : null;
        }
    }
}
