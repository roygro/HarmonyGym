package com.example.harmonyGymBack.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EstatusUsuario {
    Activo("Activo"),
    Inactivo("Inactivo"),
    Bloqueado("Bloqueado");

    private final String valor;

    EstatusUsuario(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstatusUsuario fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        for (EstatusUsuario e : values()) {
            if (e.valor.equalsIgnoreCase(valor.trim())) {
                return e;
            }
        }
        if (valor.trim().equalsIgnoreCase("ACTIVO") || valor.trim().equalsIgnoreCase("ACTIVA")) {
            return Activo;
        }
        if (valor.trim().equalsIgnoreCase("INACTIVO") || valor.trim().equalsIgnoreCase("INACTIVA")) {
            return Inactivo;
        }
        if (valor.trim().equalsIgnoreCase("BLOQUEADO")) {
            return Bloqueado;
        }
        throw new IllegalArgumentException("EstatusUsuario no válido: " + valor);
    }

    @Converter(autoApply = true)
    public static class EstatusUsuarioConverter implements AttributeConverter<EstatusUsuario, String> {
        @Override
        public String convertToDatabaseColumn(EstatusUsuario attribute) {
            return attribute != null ? attribute.getValor() : null;
        }

        @Override
        public EstatusUsuario convertToEntityAttribute(String dbData) {
            return dbData != null ? EstatusUsuario.fromString(dbData) : null;
        }
    }
}
