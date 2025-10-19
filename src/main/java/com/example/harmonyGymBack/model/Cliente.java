package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLIENTE")
public class Cliente {
    @Id
    @Column(name = "Folio_Cliente")
    private String folioCliente;

    @Column(name = "Nombre", nullable = false)
    private String nombre;

    @Column(name = "Telefono")
    private String telefono;

    @Column(name = "Email")
    private String email;

    @Column(name = "Fecha_Nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "Genero")
    private String genero;

    @Column(name = "Fecha_Registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "Estatus")
    private String estatus = "Activo";

    // Constructores
    public Cliente() {}

    public Cliente(String folioCliente, String nombre, String telefono, String email,
                   LocalDate fechaNacimiento, String genero) {
        this.folioCliente = folioCliente;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public String getFolioCliente() { return folioCliente; }
    public void setFolioCliente(String folioCliente) { this.folioCliente = folioCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}