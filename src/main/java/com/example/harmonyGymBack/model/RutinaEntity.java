package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RUTINA")
public class RutinaEntity {

    @Id
    @Column(name = "folio_rutina", length = 50)
    private String folioRutina;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "nivel", length = 20)
    private String nivel;

    @Column(name = "objetivo", length = 100)
    private String objetivo;

    @Column(name = "duracion_estimada")
    private Integer duracionEstimada;

    @Column(name = "estatus", length = 10)
    private String estatus = "Activa";

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "folio_instructor", length = 50)
    private String folioInstructor;

    // Lista de ejercicios (relación virtual para consultas)
    @Transient
    private List<EjercicioRutinaDTO> ejercicios = new ArrayList<>();

    // Constructores
    public RutinaEntity() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public RutinaEntity(String folioRutina, String nombre, String descripcion, String nivel,
                        String objetivo, Integer duracionEstimada, String folioInstructor) {
        this.folioRutina = folioRutina;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.objetivo = objetivo;
        this.duracionEstimada = duracionEstimada;
        this.folioInstructor = folioInstructor;
        this.fechaCreacion = LocalDateTime.now();
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

    public List<EjercicioRutinaDTO> getEjercicios() { return ejercicios; }
    public void setEjercicios(List<EjercicioRutinaDTO> ejercicios) { this.ejercicios = ejercicios; }

    // ===== CLASES DTO INTERNAS =====

    // DTO para agregar ejercicios a rutina
    public static class AgregarEjercicioRequest {
        private String idEjercicio;
        private Integer orden;
        private Integer seriesEjercicio;
        private Integer repeticionesEjercicio;
        private Integer descansoEjercicio;
        private String observaciones;

        public AgregarEjercicioRequest() {}

        public AgregarEjercicioRequest(String idEjercicio, Integer orden, Integer seriesEjercicio,
                                       Integer repeticionesEjercicio, Integer descansoEjercicio, String observaciones) {
            this.idEjercicio = idEjercicio;
            this.orden = orden;
            this.seriesEjercicio = seriesEjercicio;
            this.repeticionesEjercicio = repeticionesEjercicio;
            this.descansoEjercicio = descansoEjercicio;
            this.observaciones = observaciones;
        }

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
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }

    // DTO para respuesta de ejercicios en rutina
    public static class EjercicioRutinaDTO {
        private String idEjercicio;
        private String nombre;
        private Integer tiempo;
        private Integer series;
        private Integer repeticiones;
        private Integer descanso;
        private String equipoNecesario;
        private String grupoMuscular;
        private String instrucciones;
        private Integer orden;
        private Integer seriesEjercicio;
        private Integer repeticionesEjercicio;
        private Integer descansoEjercicio;
        private String observaciones;

        public EjercicioRutinaDTO() {}

        public EjercicioRutinaDTO(String idEjercicio, String nombre, Integer tiempo, Integer series,
                                  Integer repeticiones, Integer descanso, String equipoNecesario,
                                  String grupoMuscular, String instrucciones, Integer orden,
                                  Integer seriesEjercicio, Integer repeticionesEjercicio,
                                  Integer descansoEjercicio, String observaciones) {
            this.idEjercicio = idEjercicio;
            this.nombre = nombre;
            this.tiempo = tiempo;
            this.series = series;
            this.repeticiones = repeticiones;
            this.descanso = descanso;
            this.equipoNecesario = equipoNecesario;
            this.grupoMuscular = grupoMuscular;
            this.instrucciones = instrucciones;
            this.orden = orden;
            this.seriesEjercicio = seriesEjercicio;
            this.repeticionesEjercicio = repeticionesEjercicio;
            this.descansoEjercicio = descansoEjercicio;
            this.observaciones = observaciones;
        }

        // Getters y Setters
        public String getIdEjercicio() { return idEjercicio; }
        public void setIdEjercicio(String idEjercicio) { this.idEjercicio = idEjercicio; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Integer getTiempo() { return tiempo; }
        public void setTiempo(Integer tiempo) { this.tiempo = tiempo; }
        public Integer getSeries() { return series; }
        public void setSeries(Integer series) { this.series = series; }
        public Integer getRepeticiones() { return repeticiones; }
        public void setRepeticiones(Integer repeticiones) { this.repeticiones = repeticiones; }
        public Integer getDescanso() { return descanso; }
        public void setDescanso(Integer descanso) { this.descanso = descanso; }
        public String getEquipoNecesario() { return equipoNecesario; }
        public void setEquipoNecesario(String equipoNecesario) { this.equipoNecesario = equipoNecesario; }
        public String getGrupoMuscular() { return grupoMuscular; }
        public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }
        public String getInstrucciones() { return instrucciones; }
        public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
        public Integer getOrden() { return orden; }
        public void setOrden(Integer orden) { this.orden = orden; }
        public Integer getSeriesEjercicio() { return seriesEjercicio; }
        public void setSeriesEjercicio(Integer seriesEjercicio) { this.seriesEjercicio = seriesEjercicio; }
        public Integer getRepeticionesEjercicio() { return repeticionesEjercicio; }
        public void setRepeticionesEjercicio(Integer repeticionesEjercicio) { this.repeticionesEjercicio = repeticionesEjercicio; }
        public Integer getDescansoEjercicio() { return descansoEjercicio; }
        public void setDescansoEjercicio(Integer descansoEjercicio) { this.descansoEjercicio = descansoEjercicio; }
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }

    // DTO para ejercicio simple
    public static class EjercicioSimpleDTO {
        private String idEjercicio;
        private String nombre;
        private Integer tiempo;
        private Integer series;
        private Integer repeticiones;
        private Integer descanso;
        private String equipoNecesario;
        private String grupoMuscular;
        private String instrucciones;

        public EjercicioSimpleDTO() {}

        public EjercicioSimpleDTO(String idEjercicio, String nombre, Integer tiempo, Integer series,
                                  Integer repeticiones, Integer descanso, String equipoNecesario,
                                  String grupoMuscular, String instrucciones) {
            this.idEjercicio = idEjercicio;
            this.nombre = nombre;
            this.tiempo = tiempo;
            this.series = series;
            this.repeticiones = repeticiones;
            this.descanso = descanso;
            this.equipoNecesario = equipoNecesario;
            this.grupoMuscular = grupoMuscular;
            this.instrucciones = instrucciones;
        }

        // Getters y Setters
        public String getIdEjercicio() { return idEjercicio; }
        public void setIdEjercicio(String idEjercicio) { this.idEjercicio = idEjercicio; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Integer getTiempo() { return tiempo; }
        public void setTiempo(Integer tiempo) { this.tiempo = tiempo; }
        public Integer getSeries() { return series; }
        public void setSeries(Integer series) { this.series = series; }
        public Integer getRepeticiones() { return repeticiones; }
        public void setRepeticiones(Integer repeticiones) { this.repeticiones = repeticiones; }
        public Integer getDescanso() { return descanso; }
        public void setDescanso(Integer descanso) { this.descanso = descanso; }
        public String getEquipoNecesario() { return equipoNecesario; }
        public void setEquipoNecesario(String equipoNecesario) { this.equipoNecesario = equipoNecesario; }
        public String getGrupoMuscular() { return grupoMuscular; }
        public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }
        public String getInstrucciones() { return instrucciones; }
        public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
    }
}