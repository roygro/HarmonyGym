package com.example.harmonyGymBack.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EstatusMembresiaCliente {
    Activa("Activa"),
    Inactiva("Inactiva"),
    Expirada("Expirada"),
    Cancelada("Cancelada");

    private final String valor;

    EstatusMembresiaCliente(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstatusMembresiaCliente fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        for (EstatusMembresiaCliente e : values()) {
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
        throw new IllegalArgumentException("EstatusMembresiaCliente no válido: " + valor);
    }

    @Converter(autoApply = true)
    public static class EstatusMembresiaClienteConverter implements AttributeConverter<EstatusMembresiaCliente, String> {
        @Override
        public String convertToDatabaseColumn(EstatusMembresiaCliente attribute) {
            return attribute != null ? attribute.getValor() : null;
        }

        @Override
        public EstatusMembresiaCliente convertToEntityAttribute(String dbData) {
            return dbData != null ? EstatusMembresiaCliente.fromString(dbData) : null;
        }
    }
}
