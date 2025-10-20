package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "INSTRUCTOR")
public class Instructor {
    @Id
    @Column(name = "Folio_Instructor")
    private String folioInstructor;

    @Column(name = "Nombre", nullable = false)
    private String nombre;

    @Column(name = "APP")
    private String app;

    @Column(name = "APM")
    private String apm;

    @Column(name = "Hora_Entrada")
    private LocalTime horaEntrada;

    @Column(name = "Hora_Salida")
    private LocalTime horaSalida;

    @Column(name = "Especialidad")
    private String especialidad;

    @Column(name = "Fecha_Contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "Estatus")
    private String estatus = "Activo";

    @Column(name = "Fecha_Registro")
    private LocalDateTime fechaRegistro;

    // Constructores
    public Instructor() {}

    public Instructor(String folioInstructor, String nombre, String app, String apm,
                      LocalTime horaEntrada, LocalTime horaSalida, String especialidad,
                      LocalDate fechaContratacion) {
        this.folioInstructor = folioInstructor;
        this.nombre = nombre;
        this.app = app;
        this.apm = apm;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.especialidad = especialidad;
        this.fechaContratacion = fechaContratacion;
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public String getFolioInstructor() { return folioInstructor; }
    public void setFolioInstructor(String folioInstructor) { this.folioInstructor = folioInstructor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }

    public String getApm() { return apm; }
    public void setApm(String apm) { this.apm = apm; }

    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}