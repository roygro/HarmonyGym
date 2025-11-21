package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("TRIMESTRAL")
public class PlanTrimestral extends PlanPago {
    public PlanTrimestral() {
        setNombre("Plan Trimestral");
        setDescripcion("Pago cada 3 meses con 10% de descuento");
        setDuracionDias(90);
        setFactorDescuento(0.9);
    }

    @Override
    public LocalDate calcularFechaFin(LocalDate fechaInicio) {
        return fechaInicio.plusDays(getDuracionDias());
    }

    @Override
    public String getTipoPlan() {
        return "TRIMESTRAL";
    }
}