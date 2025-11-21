package com.example.harmonyGymBack.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("PERSONALIZADO")
public class PlanPersonalizado extends PlanPago {

    @Override
    public LocalDate calcularFechaFin(LocalDate fechaInicio) {
        return fechaInicio.plusDays(getDuracionDias());
    }

    @Override
    public String getTipoPlan() {
        return "PERSONALIZADO";
    }
}