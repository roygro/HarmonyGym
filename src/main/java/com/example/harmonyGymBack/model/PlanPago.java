package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_plan")
@Table(name = "PLANES_PAGO")
public abstract class PlanPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @Column(name = "factor_descuento")
    private Double factorDescuento = 1.0;

    @Column(name = "estatus")
    private String estatus = "Activo";

    public abstract LocalDate calcularFechaFin(LocalDate fechaInicio);
    public abstract String getTipoPlan();

    public double aplicarDescuento(double precioBase) {
        return precioBase * factorDescuento;
    }

    // Getters y Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getDuracionDias() { return duracionDias; }
    public void setDuracionDias(Integer duracionDias) { this.duracionDias = duracionDias; }
    public Double getFactorDescuento() { return factorDescuento; }
    public void setFactorDescuento(Double factorDescuento) { this.factorDescuento = factorDescuento; }
    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}