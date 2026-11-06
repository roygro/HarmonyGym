package com.example.harmonyGymBack.model.enums;

public enum EstatusPago {
    PENDIENTE,
    PAGADO,
    CANCELADO;

    public static EstatusPago fromString(String val) {
        if (val == null) return null;
        for (EstatusPago e : values()) {
            if (e.name().equalsIgnoreCase(val.trim())) return e;
        }
        return PAGADO;
    }
}
