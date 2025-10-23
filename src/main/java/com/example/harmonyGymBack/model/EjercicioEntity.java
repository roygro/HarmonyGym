package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "ejercicio")
public class EjercicioEntity {
    @Id
    @Column(name = "id_ejercicio")
    private String idEjercicio;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "tiempo")
    private Integer tiempo;

    @Column(name = "series")
    private Integer series;

    @Column(name = "repeticiones")
    private Integer repeticiones;

    @Column(name = "descanso")
    private Integer descanso;

    @Column(name = "duracion_estimada")
    private Integer duracionEstimada;

    @Column(name = "equipo_necesario")
    private String equipoNecesario;

    @Column(name = "grupo_muscular")
    private String grupoMuscular;

    @Column(name = "instrucciones")
    private String instrucciones;

    @Column(name = "estatus")
    private String estatus;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Relación con Instructor (debes agregar este campo a tu tabla)
    @Column(name = "folio_instructor")
    private String folioInstructor;

    // Constructores
    public EjercicioEntity() {
        this.fechaCreacion = LocalDateTime.now();
        this.estatus = "Activo";
    }

    // Getters y Setters
    public String getIdEjercicio() { return idEjercicio; }
    public void setIdEjercicio(String idEjercicio) { this.idEjercicio = idEjercicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getTiempo() { return tiempo; }
    public void setTiempo(Integer tiempo) { this.tiempo = tiempo; }

    public Integer getSeries() { return series; }
    public void setSeries(Integer series) { this.series = series; }

    public Integer getRepeticiones() { return repeticiones; }
    public void setRepeticiones(Integer repeticiones) { this.repeticiones = repeticiones; }

    public Integer getDescanso() { return descanso; }
    public void setDescanso(Integer descanso) { this.descanso = descanso; }

    public Integer getDuracionEstimada() { return duracionEstimada; }
    public void setDuracionEstimada(Integer duracionEstimada) { this.duracionEstimada = duracionEstimada; }

    public String getEquipoNecesario() { return equipoNecesario; }
    public void setEquipoNecesario(String equipoNecesario) { this.equipoNecesario = equipoNecesario; }

    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }

    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFolioInstructor() { return folioInstructor; }
    public void setFolioInstructor(String folioInstructor) { this.folioInstructor = folioInstructor; }
}