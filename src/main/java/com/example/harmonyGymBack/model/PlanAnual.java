package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("ANUAL")
public class PlanAnual extends PlanPago {
    public PlanAnual() {
        setNombre("Plan Anual");
        setDescripcion("Pago anual con 20% de descuento");
        setDuracionDias(365);
        setFactorDescuento(0.8);
    }

    @Override
    public LocalDate calcularFechaFin(LocalDate fechaInicio) {
        return fechaInicio.plusDays(Math.max(0, getDuracionDias() - 1));
    }

    @Override
    public String getTipoPlan() {
        return "ANUAL";
    }
}