package com.example.harmonyGymBack.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EstatusCatalogo {
    Activa("Activa"),
    Inactiva("Inactiva");

    private final String valor;

    EstatusCatalogo(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstatusCatalogo fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        for (EstatusCatalogo e : values()) {
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
        throw new IllegalArgumentException("EstatusCatalogo no válido: " + valor);
    }

    @Converter(autoApply = true)
    public static class EstatusCatalogoConverter implements AttributeConverter<EstatusCatalogo, String> {
        @Override
        public String convertToDatabaseColumn(EstatusCatalogo attribute) {
            return attribute != null ? attribute.getValor() : null;
        }

        @Override
        public EstatusCatalogo convertToEntityAttribute(String dbData) {
            return dbData != null ? EstatusCatalogo.fromString(dbData) : null;
        }
    }
}
