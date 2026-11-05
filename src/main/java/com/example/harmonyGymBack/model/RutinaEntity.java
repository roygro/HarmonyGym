package com.example.harmonyGymBack.model;

import com.example.harmonyGymBack.model.enums.EstatusRutina;
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
    private EstatusRutina estatus = EstatusRutina.Activa;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "folio_instructor", nullable = false)
    private String folioInstructor;

    // Relación con Instructor (solo para consultas)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "folio_instructor", referencedColumnName = "folio_instructor", insertable = false, updatable = false)
    private Instructor instructor;

    // Ejercicios integrados
    @ElementCollection
    @CollectionTable(
            name = "contiene_rutina",
            joinColumns = @JoinColumn(name = "folio_rutina")
    )
    private List<EjercicioRutina> ejercicios = new ArrayList<>();

    // Constructores
    public RutinaEntity() {
        this.fechaCreacion = LocalDateTime.now();
        this.estatus = EstatusRutina.Activa;
    }

    public RutinaEntity(String folioRutina, String nombre, String descripcion,
                        String nivel, String objetivo, String folioInstructor) {
        this();
        this.folioRutina = folioRutina;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.objetivo = objetivo;
        this.folioInstructor = folioInstructor;
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

    public EstatusRutina getEstatus() { return estatus; }
    public void setEstatus(EstatusRutina estatus) { this.estatus = estatus; }
    public void setEstatus(String estatus) { this.estatus = EstatusRutina.fromString(estatus); }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFolioInstructor() { return folioInstructor; }
    public void setFolioInstructor(String folioInstructor) { this.folioInstructor = folioInstructor; }

    public Instructor getInstructor() { return instructor; }
    public void setInstructor(Instructor instructor) { this.instructor = instructor; }

    public List<EjercicioRutina> getEjercicios() { return ejercicios; }
    public void setEjercicios(List<EjercicioRutina> ejercicios) { this.ejercicios = ejercicios; }

    public boolean isActiva() {
        return EstatusRutina.Activa.equals(this.estatus);
    }

    @Embeddable
    public static class EjercicioRutina {
        @Column(name = "id_ejercicio")
        private String idEjercicio;

        @Column(name = "orden")
        private Integer orden;

        @Column(name = "series_ejercicio")
        private Integer seriesEjercicio;

        @Column(name = "repeticiones_ejercicio")
        private Integer repeticionesEjercicio;

        @Column(name = "peso_ejercicio")
        private Double pesoEjercicio;

        @Column(name = "descanso_ejercicio")
        private Integer descansoEjercicio;

        public EjercicioRutina() {}

        public EjercicioRutina(String idEjercicio, Integer orden, Integer seriesEjercicio,
                               Integer repeticionesEjercicio, Double pesoEjercicio, Integer descansoEjercicio) {
            this.idEjercicio = idEjercicio;
            this.orden = orden;
            this.seriesEjercicio = seriesEjercicio;
            this.repeticionesEjercicio = repeticionesEjercicio;
            this.pesoEjercicio = pesoEjercicio;
            this.descansoEjercicio = descansoEjercicio;
        }

        public String getIdEjercicio() { return idEjercicio; }
        public void setIdEjercicio(String idEjercicio) { this.idEjercicio = idEjercicio; }

        public Integer getOrden() { return orden; }
        public void setOrden(Integer orden) { this.orden = orden; }

        public Integer getSeriesEjercicio() { return seriesEjercicio; }
        public void setSeriesEjercicio(Integer seriesEjercicio) { this.seriesEjercicio = seriesEjercicio; }

        public Integer getRepeticionesEjercicio() { return repeticionesEjercicio; }
        public void setRepeticionesEjercicio(Integer repeticionesEjercicio) { this.repeticionesEjercicio = repeticionesEjercicio; }

        public Double getPesoEjercicio() { return pesoEjercicio; }
        public void setPesoEjercicio(Double pesoEjercicio) { this.pesoEjercicio = pesoEjercicio; }

        public Integer getDescansoEjercicio() { return descansoEjercicio; }
        public void setDescansoEjercicio(Integer descansoEjercicio) { this.descansoEjercicio = descansoEjercicio; }
    }
}
