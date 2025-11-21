package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "membresia")
public class Membresia {
    @Id
    @Column(name = "id_membresia")
    private String idMembresia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMembresia tipo;

    @Column(name = "precio", nullable = false)
    private Double precio;

    @Column(name = "duracion", nullable = false)
    private Integer duracion; // En días

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "beneficios", length = 1000)
    private String beneficios;

    @Column(name = "estatus", nullable = false)
    private String estatus; // ACTIVO, INACTIVO, etc.

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // Constructores
    public Membresia() {
        this.fechaCreacion = LocalDateTime.now();
        this.estatus = "ACTIVO";
    }

    public Membresia(TipoMembresia tipo, Double precio, Integer duracion,
                     String descripcion, String beneficios) {
        this();
        this.tipo = tipo;
        this.precio = precio;
        this.duracion = duracion;
        this.descripcion = descripcion;
        this.beneficios = beneficios;
    }

    // ✅ NUEVO MÉTODO: Para compatibilidad con el patrón Bridge
    public Double getPrecioBase() {
        return this.precio != null ? this.precio : 0.0;
    }

    // Getters y Setters
    public String getIdMembresia() {
        return idMembresia;
    }

    public void setIdMembresia(String idMembresia) {
        this.idMembresia = idMembresia;
    }

    public TipoMembresia getTipo() {
        return tipo;
    }

    public void setTipo(TipoMembresia tipo) {
        this.tipo = tipo;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getBeneficios() {
        return beneficios;
    }

    public void setBeneficios(String beneficios) {
        this.beneficios = beneficios;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    // ✅ OPCIONAL: Método para verificar si está activa
    public boolean isActiva() {
        return "ACTIVO".equalsIgnoreCase(this.estatus);
    }

    // ✅ OPCIONAL: Método toString para debugging
    @Override
    public String toString() {
        return "Membresia{" +
                "idMembresia='" + idMembresia + '\'' +
                ", tipo=" + tipo +
                ", precio=" + precio +
                ", duracion=" + duracion +
                ", estatus='" + estatus + '\'' +
                '}';
    }
}