package com.example.harmonyGymBack.model;

import com.example.harmonyGymBack.model.enums.EstatusPago;
import com.example.harmonyGymBack.model.enums.MetodoPago;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vende")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recepcionista", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Recepcionista recepcionista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_producto", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folio_cliente", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_membresia_cliente", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MembresiaCliente membresiaCliente;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta = LocalDateTime.now();

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario = 0.0;

    @Column(name = "total", nullable = false)
    private Double total = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago = MetodoPago.EFECTIVO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus_pago")
    private EstatusPago estatusPago = EstatusPago.PAGADO;

    @Column(name = "monto_recibido", precision = 10, scale = 2)
    private BigDecimal montoRecibido;

    @Column(name = "cambio", precision = 10, scale = 2)
    private BigDecimal cambio;

    @Column(name = "referencia_externa", length = 255)
    private String referenciaExterna;

    @Column(name = "tipo_pago")
    private String tipoPago;

    @Column(name = "id_membresia", nullable = true)
    private String idMembresia;

    // Constructores
    public Pago() {
        this.fechaVenta = LocalDateTime.now();
        this.cantidad = 1;
        this.metodoPago = MetodoPago.EFECTIVO;
        this.estatusPago = EstatusPago.PAGADO;
    }

    public Pago(Recepcionista recepcionista, Producto producto, Integer cantidad,
                Double precioUnitario, Double total, Cliente cliente) {
        this();
        this.recepcionista = recepcionista;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = total;
        this.cliente = cliente;
    }

    // Getters y Setters
    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public Recepcionista getRecepcionista() {
        return recepcionista;
    }

    public void setRecepcionista(Recepcionista recepcionista) {
        this.recepcionista = recepcionista;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public MembresiaCliente getMembresiaCliente() {
        return membresiaCliente;
    }

    public void setMembresiaCliente(MembresiaCliente membresiaCliente) {
        this.membresiaCliente = membresiaCliente;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstatusPago getEstatusPago() {
        return estatusPago;
    }

    public void setEstatusPago(EstatusPago estatusPago) {
        this.estatusPago = estatusPago;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(BigDecimal montoRecibido) {
        this.montoRecibido = montoRecibido;
    }

    public BigDecimal getCambio() {
        return cambio;
    }

    public void setCambio(BigDecimal cambio) {
        this.cambio = cambio;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public String getTipoPago() {
        return tipoPago != null ? tipoPago : (metodoPago != null ? metodoPago.name() : null);
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
        if (tipoPago != null) {
            try {
                this.metodoPago = MetodoPago.fromString(tipoPago);
            } catch (Exception ignored) {}
        }
    }

    public String getIdMembresia() {
        return idMembresia;
    }

    public void setIdMembresia(String idMembresia) {
        this.idMembresia = idMembresia;
    }

        @Transient
    private String tempCodigoProducto;
    @Transient
    private String tempFolioCliente;
    @Transient
    private String tempIdRecepcionista;

    public void setCodigoProducto(String codigoProducto) {
        this.tempCodigoProducto = codigoProducto;
    }

    public void setFolioCliente(String folioCliente) {
        this.tempFolioCliente = folioCliente;
    }

    public void setIdRecepcionista(String idRecepcionista) {
        this.tempIdRecepcionista = idRecepcionista;
    }

    // Métodos de compatibilidad para evitar romper código existente
    public String getFolioCliente() {
        return cliente != null ? cliente.getFolioCliente() : tempFolioCliente;
    }

    public String getIdRecepcionista() {
        return recepcionista != null ? recepcionista.getIdRecepcionista() : tempIdRecepcionista;
    }

    public String getCodigoProducto() {
        return producto != null ? producto.getCodigo() : tempCodigoProducto;
    }
}