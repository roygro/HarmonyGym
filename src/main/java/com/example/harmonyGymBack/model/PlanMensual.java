package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MENSUAL")
public class PlanMensual extends PlanPago {
    public PlanMensual() {
        setNombre("Plan Mensual");
        setDescripcion("Pago mensual estándar");
        setDuracionDias(30);
        setFactorDescuento(1.0);
    }

    @Override
    public LocalDate calcularFechaFin(LocalDate fechaInicio) {
        return fechaInicio.plusDays(getDuracionDias());
    }

    @Override
    public String getTipoPlan() {
        return "MENSUAL";
    }
}