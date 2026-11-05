package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.enums.MetodoPago;
import com.example.harmonyGymBack.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    public static class VentaMembresiaRequest {
        public String folioCliente;
        public String idMembresia;
        public Long idPlanPago;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate fechaInicio;
        public String metodoPago;
        public BigDecimal montoRecibido;
        public Long idMembresiaCliente; // para renovar
    }

    public static class VentaProductoRequest {
        public String folioCliente;
        public String codigoProducto;
        public Integer cantidad;
        public String metodoPago;
        public BigDecimal montoRecibido;
    }

    public static class CambioMembresiaRequest {
        public Long idMembresiaCliente;
        public String nuevaIdMembresia;
        public String metodoPago;
        public BigDecimal montoRecibido;
    }

    public static class CancelarMembresiaRequest {
        public Long idMembresiaCliente;
        public String motivo;
        public String usuarioCancelo;
    }

    @PostMapping("/membresia")
    public ResponseEntity<?> venderMembresia(@RequestBody VentaMembresiaRequest request) {
        try {
            MetodoPago metodo = MetodoPago.fromString(request.metodoPago);
            Map<String, Object> resp = ventaService.venderMembresia(
                    request.folioCliente,
                    request.idMembresia,
                    request.idPlanPago,
                    request.fechaInicio != null ? request.fechaInicio : LocalDate.now(),
                    metodo,
                    request.montoRecibido
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{idPago}/paypal/capturar")
    public ResponseEntity<?> capturarPaypal(@PathVariable Integer idPago) {
        try {
            Map<String, Object> resp = ventaService.capturarPaypal(idPago);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/membresia/renovar")
    public ResponseEntity<?> renovarMembresia(@RequestBody VentaMembresiaRequest request) {
        try {
            MetodoPago metodo = MetodoPago.fromString(request.metodoPago);
            Map<String, Object> resp = ventaService.renovarMembresia(
                    request.idMembresiaCliente,
                    request.idMembresia,
                    request.idPlanPago,
                    metodo,
                    request.montoRecibido
            );
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/membresia/cambiar")
    public ResponseEntity<?> cambiarMembresia(@RequestBody CambioMembresiaRequest request) {
        try {
            MetodoPago metodo = MetodoPago.fromString(request.metodoPago);
            Map<String, Object> resp = ventaService.cambiarMembresia(
                    request.idMembresiaCliente,
                    request.nuevaIdMembresia,
                    metodo,
                    request.montoRecibido
            );
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/membresia/cancelar")
    public ResponseEntity<?> cancelarMembresia(@RequestBody CancelarMembresiaRequest request) {
        try {
            Map<String, Object> resp = ventaService.cancelarMembresia(
                    request.idMembresiaCliente,
                    request.motivo,
                    request.usuarioCancelo != null ? request.usuarioCancelo : "Sistema"
            );
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/producto")
    public ResponseEntity<?> venderProducto(@RequestBody VentaProductoRequest request) {
        try {
            MetodoPago metodo = MetodoPago.fromString(request.metodoPago);
            Map<String, Object> resp = ventaService.venderProducto(
                    request.folioCliente,
                    request.codigoProducto,
                    request.cantidad,
                    metodo,
                    request.montoRecibido
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
