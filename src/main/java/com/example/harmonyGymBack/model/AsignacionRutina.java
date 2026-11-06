package com.example.harmonyGymBack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asigna", uniqueConstraints = {
    @UniqueConstraint(name = "uk_asigna_rutina_cliente", columnNames = {"folio_rutina", "cliente_asignado"})
})
public class AsignacionRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Long idAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folio_rutina", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ejercicios"})
    private RutinaEntity rutina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_asignado", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folio_instructor", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Instructor instructor;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    public AsignacionRutina() {}

    public AsignacionRutina(RutinaEntity rutina, Cliente cliente, Instructor instructor) {
        this.rutina = rutina;
        this.cliente = cliente;
        this.instructor = instructor;
        this.fechaAsignacion = LocalDateTime.now();
    }

    public Long getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(Long idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public RutinaEntity getRutina() {
        return rutina;
    }

    public void setRutina(RutinaEntity rutina) {
        this.rutina = rutina;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
}
