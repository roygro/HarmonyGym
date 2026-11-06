package com.example.harmonyGymBack.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EstatusInscripcion {
    Inscrito("Inscrito"),
    Cancelado("Cancelado"),
    Completado("Completado");

    private final String valor;

    EstatusInscripcion(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstatusInscripcion fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        for (EstatusInscripcion e : values()) {
            if (e.valor.equalsIgnoreCase(valor.trim())) {
                return e;
            }
        }
        if (valor.trim().equalsIgnoreCase("INSCRITO") || valor.trim().equalsIgnoreCase("INSCRITA")) {
            return Inscrito;
        }
        if (valor.trim().equalsIgnoreCase("CANCELADO") || valor.trim().equalsIgnoreCase("CANCELADA")) {
            return Cancelado;
        }
        if (valor.trim().equalsIgnoreCase("COMPLETADO") || valor.trim().equalsIgnoreCase("COMPLETADA")) {
            return Completado;
        }
        throw new IllegalArgumentException("EstatusInscripcion no válido: " + valor);
    }

    @Converter(autoApply = true)
    public static class EstatusInscripcionConverter implements AttributeConverter<EstatusInscripcion, String> {
        @Override
        public String convertToDatabaseColumn(EstatusInscripcion attribute) {
            return attribute != null ? attribute.getValor() : null;
        }

        @Override
        public EstatusInscripcion convertToEntityAttribute(String dbData) {
            return dbData != null ? EstatusInscripcion.fromString(dbData) : null;
        }
    }
}
