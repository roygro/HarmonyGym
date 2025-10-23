package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "ROL")
public class Rol {
    @Id
    @Column(name = "id_rol")
    private String idRol;

    @Column(name = "nombre_rol", nullable = false)
    private String nombreRol;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "permisos", columnDefinition = "jsonb")
    private String permisos;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Constructores
    public Rol() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Rol(String idRol, String nombreRol, String descripcion, String permisos) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
        this.permisos = permisos;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public String getIdRol() { return idRol; }
    public void setIdRol(String idRol) { this.idRol = idRol; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}