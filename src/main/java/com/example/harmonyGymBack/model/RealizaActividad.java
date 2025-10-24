package com.example.harmonyGymBack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "REALIZA_ACTIVIDAD")
public class RealizaActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Long idRegistro;

    @Column(name = "folio_cliente", nullable = false)
    private String folioCliente;

    @Column(name = "id_actividad", nullable = false)
    private String idActividad;

    @Column(name = "fecha_inscripcion")
    private LocalDateTime fechaInscripcion = LocalDateTime.now();

    @Column(name = "fecha_participacion")
    private LocalDate fechaParticipacion;

    @Column(name = "estatus")
    private String estatus = "Inscrito"; // Inscrito, Completado, Cancelado, NoShow

    @Column(name = "asistio")
    private Boolean asistio = false;

    @Column(name = "calificacion")
    private Integer calificacion;

    @Column(name = "comentarios", length = 500)
    private String comentarios;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folio_cliente", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Actividad actividad;

    // Constructores
    public RealizaActividad() {}

    public RealizaActividad(String folioCliente, String idActividad) {
        this.folioCliente = folioCliente;
        this.idActividad = idActividad;
        this.fechaInscripcion = LocalDateTime.now();
        this.estatus = "Inscrito";
        this.asistio = false;
    }

    public RealizaActividad(String folioCliente, String idActividad, LocalDate fechaParticipacion) {
        this(folioCliente, idActividad);
        this.fechaParticipacion = fechaParticipacion;
    }

    // Getters y Setters
    public Long getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Long idRegistro) { this.idRegistro = idRegistro; }

    public String getFolioCliente() { return folioCliente; }
    public void setFolioCliente(String folioCliente) { this.folioCliente = folioCliente; }

    public String getIdActividad() { return idActividad; }
    public void setIdActividad(String idActividad) { this.idActividad = idActividad; }

    public LocalDateTime getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDateTime fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }

    public LocalDate getFechaParticipacion() { return fechaParticipacion; }
    public void setFechaParticipacion(LocalDate fechaParticipacion) { this.fechaParticipacion = fechaParticipacion; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public Boolean getAsistio() { return asistio; }
    public void setAsistio(Boolean asistio) { this.asistio = asistio; }

    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(LocalDateTime fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Actividad getActividad() { return actividad; }
    public void setActividad(Actividad actividad) { this.actividad = actividad; }

    // Métodos de negocio
    public void cancelarInscripcion(String motivo) {
        this.estatus = "Cancelado";
        this.fechaCancelacion = LocalDateTime.now();
        this.motivoCancelacion = motivo;
    }

    public void marcarComoCompletado(Boolean asistio, Integer calificacion, String comentarios) {
        this.estatus = "Completado";
        this.asistio = asistio;
        this.calificacion = calificacion;
        this.comentarios = comentarios;
    }

    public void marcarComoNoShow() {
        this.estatus = "NoShow";
        this.asistio = false;
    }

    @Override
    public String toString() {
        return "RealizaActividad{" +
                "idRegistro=" + idRegistro +
                ", folioCliente='" + folioCliente + '\'' +
                ", idActividad='" + idActividad + '\'' +
                ", fechaInscripcion=" + fechaInscripcion +
                ", fechaParticipacion=" + fechaParticipacion +
                ", estatus='" + estatus + '\'' +
                ", asistio=" + asistio +
                ", calificacion=" + calificacion +
                '}';
    }
}