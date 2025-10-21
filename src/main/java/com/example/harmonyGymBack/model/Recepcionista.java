package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "RECEPCIONISTA")
public class Recepcionista {
    @Id
    @Column(name = "Id_Recepcionista")
    private String idRecepcionista;

    @Column(name = "Nombre", nullable = false)
    private String nombre;

    @Column(name = "Telefono")
    private String telefono;

    @Column(name = "Email")
    private String email;

    @Column(name = "Fecha_Contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "Estatus")
    private String estatus = "Activo";

    @Column(name = "Fecha_Registro")
    private LocalDateTime fechaRegistro;

    // Constructores
    public Recepcionista() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Recepcionista(String idRecepcionista, String nombre, String telefono,
                         String email, LocalDate fechaContratacion) {
        this.idRecepcionista = idRecepcionista;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.fechaContratacion = fechaContratacion;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Recepcionista(String idRecepcionista, String nombre, String telefono,
                         String email, LocalDate fechaContratacion, String estatus) {
        this.idRecepcionista = idRecepcionista;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.fechaContratacion = fechaContratacion;
        this.estatus = estatus;
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public String getIdRecepcionista() { return idRecepcionista; }
    public void setIdRecepcionista(String idRecepcionista) { this.idRecepcionista = idRecepcionista; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        return "Recepcionista{" +
                "idRecepcionista='" + idRecepcionista + '\'' +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", fechaContratacion=" + fechaContratacion +
                ", estatus='" + estatus + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}