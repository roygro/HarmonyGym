package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("SEMESTRAL")
public class PlanSemestral extends PlanPago {
    public PlanSemestral() {
        setNombre("Plan Semestral");
        setDescripcion("Pago cada 6 meses con 15% de descuento");
        setDuracionDias(180);
        setFactorDescuento(0.85);
    }

    @Override
    public LocalDate calcularFechaFin(LocalDate fechaInicio) {
        return fechaInicio.plusDays(getDuracionDias());
    }

    @Override
    public String getTipoPlan() {
        return "SEMESTRAL";
    }
}