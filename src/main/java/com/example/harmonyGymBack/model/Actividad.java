package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ACTIVIDAD")
public class Actividad {

    @Id
    @Column(name = "Id_Actividad")
    private String idActividad;

    @Column(name = "Nombre_Actividad", nullable = false)
    private String nombreActividad;

    @Column(name = "Fecha_Actividad", nullable = false)
    private LocalDate fechaActividad;

    @Column(name = "Hora_Inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "Hora_Fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "Descripcion", length = 1000)
    private String descripcion;

    @Column(name = "Cupo", nullable = false)
    private Integer cupo;

    @Column(name = "Lugar")
    private String lugar;

    @Column(name = "Imagen_URL", length = 500)
    private String imagenUrl;

    @Column(name = "Folio_Instructor", nullable = false)
    private String folioInstructor;

    @Column(name = "Estatus")
    private String estatus = "Activa";

    @Column(name = "Fecha_Creacion")
    private LocalDateTime fechaCreacion;

    // Relación con instructor - IGNORAR para JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Folio_Instructor", referencedColumnName = "Folio_Instructor", insertable = false, updatable = false)
    @JsonIgnore // ✅ Esto evita que Jackson intente serializar la relación lazy
    private Instructor instructor;

    // Constructores
    public Actividad() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActividad = LocalDate.now();
    }

    public Actividad(String idActividad, String nombreActividad, LocalDate fechaActividad,
                     LocalTime horaInicio, LocalTime horaFin, String descripcion,
                     Integer cupo, String lugar, String imagenUrl, String folioInstructor) {
        this();
        this.idActividad = idActividad;
        this.nombreActividad = nombreActividad;
        this.fechaActividad = fechaActividad;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.descripcion = descripcion;
        this.cupo = cupo;
        this.lugar = lugar;
        this.imagenUrl = imagenUrl;
        this.folioInstructor = folioInstructor;
    }

    // Getters y Setters (mantener todos)
    public String getIdActividad() { return idActividad; }
    public void setIdActividad(String idActividad) { this.idActividad = idActividad; }

    public String getNombreActividad() { return nombreActividad; }
    public void setNombreActividad(String nombreActividad) { this.nombreActividad = nombreActividad; }

    public LocalDate getFechaActividad() { return fechaActividad; }
    public void setFechaActividad(LocalDate fechaActividad) { this.fechaActividad = fechaActividad; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCupo() { return cupo; }
    public void setCupo(Integer cupo) { this.cupo = cupo; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getFolioInstructor() { return folioInstructor; }
    public void setFolioInstructor(String folioInstructor) { this.folioInstructor = folioInstructor; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Instructor getInstructor() { return instructor; }
    public void setInstructor(Instructor instructor) { this.instructor = instructor; }
}