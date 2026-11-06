package com.example.harmonyGymBack.model.enums;

public enum MetodoPago {
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA,
    PAYPAL;

    public static MetodoPago fromString(String val) {
        if (val == null) return null;
        for (MetodoPago m : values()) {
            if (m.name().equalsIgnoreCase(val.trim())) return m;
        }
        return EFECTIVO;
    }
}
