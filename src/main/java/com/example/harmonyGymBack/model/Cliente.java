package com.example.harmonyGymBack.model;

import com.example.harmonyGymBack.model.enums.EstatusPersona;
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
    private EstatusPersona estatus = EstatusPersona.Activo;

    @Column(name = "Nombre_Archivo_Foto", length = 1000)
    private String nombreArchivoFoto;

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
        this.estatus = EstatusPersona.Activo;
    }

    public Cliente(String folioCliente, String nombre, String telefono, String email,
                   LocalDate fechaNacimiento, String genero, EstatusPersona estatus, String nombreArchivoFoto) {
        this(folioCliente, nombre, telefono, email, fechaNacimiento, genero);
        this.estatus = estatus != null ? estatus : EstatusPersona.Activo;
        this.nombreArchivoFoto = nombreArchivoFoto;
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

    public EstatusPersona getEstatus() { return estatus; }
    public void setEstatus(EstatusPersona estatus) { this.estatus = estatus; }
    public void setEstatus(String estatus) { this.estatus = EstatusPersona.fromString(estatus); }

    public String getNombreArchivoFoto() { return nombreArchivoFoto; }
    public void setNombreArchivoFoto(String nombreArchivoFoto) { this.nombreArchivoFoto = nombreArchivoFoto; }

    @Override
    public String toString() {
        return "Cliente{" +
                "folioCliente='" + folioCliente + '\'' +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", genero='" + genero + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                ", estatus=" + estatus +
                ", nombreArchivoFoto='" + nombreArchivoFoto + '\'' +
                '}';
    }
}
