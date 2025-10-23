package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CLIENTE_MEMBRESIA")
public class MembresiaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_membresia_cliente")
    private Long idMembresiaCliente;

    @ManyToOne
    @JoinColumn(name = "folio_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_membresia", nullable = false)
    private Membresia membresia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "estatus", nullable = false)
    private String estatus; // Activa, Inactiva, Expirada, Cancelada

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructores
    public MembresiaCliente() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estatus = "Activa";
    }

    public MembresiaCliente(Cliente cliente, Membresia membresia, LocalDate fechaInicio) {
        this();
        this.cliente = cliente;
        this.membresia = membresia;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaInicio.plusDays(membresia.getDuracion());
    }

    // Getters y Setters
    public Long getIdMembresiaCliente() { return idMembresiaCliente; }
    public void setIdMembresiaCliente(Long idMembresiaCliente) { this.idMembresiaCliente = idMembresiaCliente; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Membresia getMembresia() { return membresia; }
    public void setMembresia(Membresia membresia) {
        this.membresia = membresia;
        if (this.fechaInicio != null && membresia != null) {
            this.fechaFin = this.fechaInicio.plusDays(membresia.getDuracion());
        }
    }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
        if (this.membresia != null) {
            this.fechaFin = fechaInicio.plusDays(this.membresia.getDuracion());
        }
    }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) {
        this.estatus = estatus;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    // Método para verificar si la membresía está activa y vigente
    public boolean isVigente() {
        return "Activa".equals(this.estatus) &&
                LocalDate.now().isBefore(this.fechaFin) &&
                !LocalDate.now().isAfter(this.fechaFin);
    }

    // Método para verificar si está expirada
    public boolean isExpirada() {
        return "Activa".equals(this.estatus) && LocalDate.now().isAfter(this.fechaFin);
    }
}