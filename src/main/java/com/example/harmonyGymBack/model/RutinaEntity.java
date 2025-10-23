package com.example.harmonyGymBack.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutina")
public class RutinaEntity {
    @Id
    @Column(name = "folio_rutina")
    private String folioRutina;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "nivel")
    private String nivel;

    @Column(name = "objetivo")
    private String objetivo;

    @Column(name = "duracion_estimada")
    private Integer duracionEstimada;

    @Column(name = "estatus")
    private String estatus;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "folio_instructor", nullable = false)

    private String folioInstructor;

    // Relación con Instructor (solo para consultas)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "folio_instructor", referencedColumnName = "folio_instructor", insertable = false, updatable = false)
    private Instructor instructor;

    // Ejercicios integrados - CORREGIDO: Quitar @OrderColumn
    @ElementCollection
    @CollectionTable(
            name = "contiene_rutina",
            joinColumns = @JoinColumn(name = "folio_rutina")
    )
    private List<EjercicioRutina> ejercicios = new ArrayList<>();

    // Constructores
    public RutinaEntity() {
        this.fechaCreacion = LocalDateTime.now();
        this.estatus = "Activa";
    }




    // Clase embeddable para los ejercicios de la rutina
    @Embeddable
    public static class EjercicioRutina {
        @Column(name = "id_ejercicio", nullable = false)
        private String idEjercicio;

        // CORREGIDO: Esta es la única propiedad que mapea a la columna 'orden'
        @Column(name = "orden", nullable = false)
        private Integer orden;

        @Column(name = "series_ejercicio")
        private Integer seriesEjercicio;

        @Column(name = "repeticiones_ejercicio")
        private Integer repeticionesEjercicio;

        @Column(name = "descanso_ejercicio")
        private Integer descansoEjercicio;

        @Column(name = "instrucciones")
        private String instrucciones;

        // Getters y Setters
        public String getIdEjercicio() { return idEjercicio; }
        public void setIdEjercicio(String idEjercicio) { this.idEjercicio = idEjercicio; }

        public Integer getOrden() { return orden; }
        public void setOrden(Integer orden) { this.orden = orden; }

        public Integer getSeriesEjercicio() { return seriesEjercicio; }
        public void setSeriesEjercicio(Integer seriesEjercicio) { this.seriesEjercicio = seriesEjercicio; }

        public Integer getRepeticionesEjercicio() { return repeticionesEjercicio; }
        public void setRepeticionesEjercicio(Integer repeticionesEjercicio) { this.repeticionesEjercicio = repeticionesEjercicio; }

        public Integer getDescansoEjercicio() { return descansoEjercicio; }
        public void setDescansoEjercicio(Integer descansoEjercicio) { this.descansoEjercicio = descansoEjercicio; }

        public String getInstrucciones() { return instrucciones; }
        public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
    }

    // Getters y Setters
    public String getFolioRutina() { return folioRutina; }
    public void setFolioRutina(String folioRutina) { this.folioRutina = folioRutina; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public Integer getDuracionEstimada() { return duracionEstimada; }
    public void setDuracionEstimada(Integer duracionEstimada) { this.duracionEstimada = duracionEstimada; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFolioInstructor() { return folioInstructor; }
    public void setFolioInstructor(String folioInstructor) { this.folioInstructor = folioInstructor; }

    public Instructor getInstructor() { return instructor; }
    public void setInstructor(Instructor instructor) { this.instructor = instructor; }

    public List<EjercicioRutina> getEjercicios() { return ejercicios; }
    public void setEjercicios(List<EjercicioRutina> ejercicios) { this.ejercicios = ejercicios; }


}