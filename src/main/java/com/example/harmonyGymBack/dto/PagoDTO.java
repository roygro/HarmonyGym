package com.example.harmonyGymBack.dto;

public class PagoDTO {
    private String idRecepcionista;
    private String codigoProducto;
    private Integer cantidad;
    private Double precioUnitario;
    private Double total;
    private String folioCliente;

    // Constructores
    public PagoDTO() {}

    public PagoDTO(String idRecepcionista, String codigoProducto,
                   Integer cantidad, Double precioUnitario, Double total,
                   String folioCliente) {
        this.idRecepcionista = idRecepcionista;
        this.codigoProducto = codigoProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.total = total;
        this.folioCliente = folioCliente;
    }

    // Getters y Setters
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
}