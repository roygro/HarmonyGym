package com.example.harmonyGymBack.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("PERSONALIZADO")
public class PlanPersonalizado extends PlanPago {

    @Override
    public LocalDate calcularFechaFin(LocalDate fechaInicio) {
        return fechaInicio.plusDays(Math.max(0, getDuracionDias() - 1));
    }

    @Override
    public String getTipoPlan() {
        return "PERSONALIZADO";
    }
}