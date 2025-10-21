package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vende") // Nombre exacto de tu tabla
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "id_recepcionista", nullable = false, length = 50)
    private String idRecepcionista;

    @Column(name = "codigo_producto", nullable = false, length = 50)
    private String codigoProducto;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "folio_cliente", nullable = false, length = 50)
    private String folioCliente;

    // Constructores
    public Pago() {
        this.fechaVenta = LocalDateTime.now();
        this.cantidad = 1; // Por defecto 1
    }

    public Pago(String idRecepcionista, String codigoProducto,
                Integer cantidad, Double precioUnitario, Double total,
                String folioCliente) {
        this();
        this.idRecepcionista = idRecepcionista;
        this.codigoProducto = codigoProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = total;
        this.folioCliente = folioCliente;
    }

    // Getters y Setters
    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public String getIdRecepcionista() {
        return idRecepcionista;
    }

    public void setIdRecepcionista(String idRecepcionista) {
        this.idRecepcionista = idRecepcionista;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
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

    public String getFolioCliente() {
        return folioCliente;
    }

    public void setFolioCliente(String folioCliente) {
        this.folioCliente = folioCliente;
    }

    // Método toString para debugging
    @Override
    public String toString() {
        return "Pago{" +
                "idVenta=" + idVenta +
                ", idRecepcionista='" + idRecepcionista + '\'' +
                ", codigoProducto='" + codigoProducto + '\'' +
                ", fechaVenta=" + fechaVenta +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", total=" + total +
                ", folioCliente='" + folioCliente + '\'' +
                '}';
    }
}