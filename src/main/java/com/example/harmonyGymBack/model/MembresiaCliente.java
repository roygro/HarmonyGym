package com.example.harmonyGymBack.model;

import com.example.harmonyGymBack.model.enums.EstatusMembresiaCliente;
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

    @ManyToOne
    @JoinColumn(name = "id_plan_pago")
    private PlanPago planPago;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "estatus", nullable = false)
    private EstatusMembresiaCliente estatus; // Activa, Inactiva, Expirada, Cancelada

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructores
    public MembresiaCliente() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.estatus = EstatusMembresiaCliente.Activa;
    }

    public MembresiaCliente(Cliente cliente, Membresia membresia, LocalDate fechaInicio, PlanPago planPago) {
        this();
        this.cliente = cliente;
        this.membresia = membresia;
        this.planPago = planPago;
        this.fechaInicio = fechaInicio;
        if (planPago != null && fechaInicio != null) {
            this.fechaFin = planPago.calcularFechaFin(fechaInicio);
        }
    }

    // Getters y Setters
    public Long getIdMembresiaCliente() {
        return idMembresiaCliente;
    }

    public void setIdMembresiaCliente(Long idMembresiaCliente) {
        this.idMembresiaCliente = idMembresiaCliente;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Membresia getMembresia() {
        return membresia;
    }

    public void setMembresia(Membresia membresia) {
        this.membresia = membresia;
    }

    public PlanPago getPlanPago() {
        return planPago;
    }

    public void setPlanPago(PlanPago planPago) {
        this.planPago = planPago;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstatusMembresiaCliente getEstatus() {
        return estatus;
    }

    public void setEstatus(EstatusMembresiaCliente estatus) {
        this.estatus = estatus;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void setEstatus(String estatus) {
        this.estatus = EstatusMembresiaCliente.fromString(estatus);
        this.fechaActualizacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public boolean isActiva() {
        return EstatusMembresiaCliente.Activa.equals(this.estatus) &&
                fechaFin != null && (LocalDate.now().isBefore(fechaFin) || LocalDate.now().isEqual(fechaFin));
    }

    public boolean isExpirada() {
        return (EstatusMembresiaCliente.Expirada.equals(this.estatus)) ||
                (EstatusMembresiaCliente.Activa.equals(this.estatus) && fechaFin != null && LocalDate.now().isAfter(this.fechaFin));
    }

    public double calcularPrecioFinal() {
        if (this.membresia == null || this.planPago == null) return 0.0;
        int duracionMembresia = this.membresia.getDuracion() != null && this.membresia.getDuracion() > 0 ? this.membresia.getDuracion() : 30;
        int duracionPlan = this.planPago.getDuracionDias() != null ? this.planPago.getDuracionDias() : 30;
        int meses = Math.max(1, (int) Math.ceil((double) duracionPlan / duracionMembresia));
        double factor = this.planPago.getFactorDescuento() != null ? this.planPago.getFactorDescuento() : 1.0;
        double precioBase = this.membresia.getPrecio() != null ? this.membresia.getPrecio() : 0.0;
        return Math.round((precioBase * meses * factor) * 100.0) / 100.0;
    }

    public void calcularFechaFin() {
        if (this.planPago != null && this.fechaInicio != null) {
            this.fechaFin = this.planPago.calcularFechaFin(this.fechaInicio);
        }
    }

    @Override
    public String toString() {
        return "MembresiaCliente{" +
                "idMembresiaCliente=" + idMembresiaCliente +
                ", cliente=" + (cliente != null ? cliente.getFolioCliente() : "null") +
                ", membresia=" + (membresia != null ? membresia.getIdMembresia() : "null") +
                ", planPago=" + (planPago != null ? planPago.getNombrePlan() : "null") +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", estatus=" + estatus +
                '}';
    }
}
