package com.example.harmonyGymBack.model;

import com.example.harmonyGymBack.model.enums.EstatusCatalogo;
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
    private EstatusCatalogo estatus;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    // Constructores
    public Membresia() {
        this.fechaCreacion = LocalDateTime.now();
        this.estatus = EstatusCatalogo.Activa;
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

    public Membresia(String idMembresia, TipoMembresia tipo, Double precio,
                     Integer duracion, String descripcion, String beneficios,
                     EstatusCatalogo estatus) {
        this(tipo, precio, duracion, descripcion, beneficios);
        this.idMembresia = idMembresia;
        this.estatus = estatus != null ? estatus : EstatusCatalogo.Activa;
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

    public EstatusCatalogo getEstatus() {
        return estatus;
    }

    public void setEstatus(EstatusCatalogo estatus) {
        this.estatus = estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = EstatusCatalogo.fromString(estatus);
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActiva() {
        return EstatusCatalogo.Activa.equals(this.estatus);
    }

    @Override
    public String toString() {
        return "Membresia{" +
                "idMembresia='" + idMembresia + '\'' +
                ", tipo=" + tipo +
                ", precio=" + precio +
                ", duracion=" + duracion +
                ", descripcion='" + descripcion + '\'' +
                ", beneficios='" + beneficios + '\'' +
                ", estatus=" + estatus +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
