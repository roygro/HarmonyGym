package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "venta_id")
    private List<DetalleVenta> productos = new ArrayList<>();

    @Column(nullable = false)
    private Double total;

    @CreationTimestamp
    private LocalDateTime fecha;

    private String vendedor;
    private String metodoPago;

    // Constructores
    public Venta() {}

    public Venta(Double total, String vendedor, String metodoPago) {
        this.total = total;
        this.vendedor = vendedor;
        this.metodoPago = metodoPago;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<DetalleVenta> getProductos() { return productos; }
    public void setProductos(List<DetalleVenta> productos) { this.productos = productos; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}