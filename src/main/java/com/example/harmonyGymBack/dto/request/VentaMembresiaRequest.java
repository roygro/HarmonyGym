package com.example.harmonyGymBack.dto.request;

import com.example.harmonyGymBack.model.enums.MetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class VentaMembresiaRequest {

    @NotBlank(message = "El folio del cliente es obligatorio")
    private String folioCliente;

    @NotBlank(message = "El ID de la membresía es obligatorio")
    private String idMembresia;

    private Long idPlanPago;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @PositiveOrZero(message = "El monto recibido no puede ser negativo")
    private BigDecimal montoRecibido;

    private String idRecepcionista;

    public VentaMembresiaRequest() {}

    public String getFolioCliente() { return folioCliente; }
    public void setFolioCliente(String folioCliente) { this.folioCliente = folioCliente; }

    public String getIdMembresia() { return idMembresia; }
    public void setIdMembresia(String idMembresia) { this.idMembresia = idMembresia; }

    public Long getIdPlanPago() { return idPlanPago; }
    public void setIdPlanPago(Long idPlanPago) { this.idPlanPago = idPlanPago; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public BigDecimal getMontoRecibido() { return montoRecibido; }
    public void setMontoRecibido(BigDecimal montoRecibido) { this.montoRecibido = montoRecibido; }

    public String getIdRecepcionista() { return idRecepcionista; }
    public void setIdRecepcionista(String idRecepcionista) { this.idRecepcionista = idRecepcionista; }
}
