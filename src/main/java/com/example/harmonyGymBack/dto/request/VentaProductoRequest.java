package com.example.harmonyGymBack.dto.request;

import com.example.harmonyGymBack.model.enums.MetodoPago;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class VentaProductoRequest {

    @NotBlank(message = "El folio del cliente es obligatorio")
    private String folioCliente;

    @NotBlank(message = "El código del producto es obligatorio")
    private String codigoProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @PositiveOrZero(message = "El monto recibido no puede ser negativo")
    private BigDecimal montoRecibido;

    private String idRecepcionista;

    public VentaProductoRequest() {}

    public String getFolioCliente() { return folioCliente; }
    public void setFolioCliente(String folioCliente) { this.folioCliente = folioCliente; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(BigDecimal montoRecibido) { this.montoRecibido = montoRecibido; }

    public String getIdRecepcionista() { return idRecepcionista; }
    public void setIdRecepcionista(String idRecepcionista) { this.idRecepcionista = idRecepcionista; }
}
