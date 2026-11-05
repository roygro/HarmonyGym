package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Pago;
import com.example.harmonyGymBack.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    // POST - Crear nuevo pago
    @PostMapping
    public ResponseEntity<?> crearPago(@RequestBody Pago pago) {
        try {
            if (pago.getFolioCliente() == null || pago.getFolioCliente().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El folio del cliente es requerido");
            }

            if (pago.getTipoPago() == null || pago.getTipoPago().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El tipo de pago es requerido");
            }

            if ("producto".equalsIgnoreCase(pago.getTipoPago())) {
                if (pago.getCodigoProducto() == null || pago.getCodigoProducto().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("El código de producto es requerido para pagos de producto");
                }
            } else if ("membresia".equalsIgnoreCase(pago.getTipoPago())) {
                if (pago.getIdMembresia() == null || pago.getIdMembresia().trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("El ID de membresía es requerido para pagos de membresía");
                }
                pago.setCantidad(1);
            }

            if (pago.getTotal() == null || pago.getTotal() <= 0) {
                return ResponseEntity.badRequest().body("El total debe ser mayor a 0");
            }

            Pago nuevoPago = pagoService.crearPago(pago);
            return ResponseEntity.ok(nuevoPago);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al crear el pago: " + e.getMessage());
        }
    }

    // GET - Obtener todos los pagos
    @GetMapping
    public ResponseEntity<List<Pago>> obtenerTodosLosPagos() {
        try {
            List<Pago> pagos = pagoService.obtenerTodosLosPagos();
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener pagos por cliente
    @GetMapping("/cliente/{folioCliente}")
    public ResponseEntity<List<Pago>> obtenerPagosPorCliente(@PathVariable String folioCliente) {
        try {
            List<Pago> pagos = pagoService.obtenerPagosPorCliente(folioCliente);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener pagos por recepcionista
    @GetMapping("/recepcionista/{idRecepcionista}")
    public ResponseEntity<List<Pago>> obtenerPagosPorRecepcionista(@PathVariable String idRecepcionista) {
        try {
            List<Pago> pagos = pagoService.obtenerPagosPorRecepcionista(idRecepcionista);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener estadísticas del día
    @GetMapping("/estadisticas/dia")
    public ResponseEntity<?> obtenerEstadisticasDelDia() {
        try {
            PagoService.EstadisticasDiaDTO estadisticas = pagoService.obtenerEstadisticasDelDia();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener estadísticas del día: " + e.getMessage());
        }
    }

    // GET - Obtener pagos por rango de fechas
    @GetMapping("/rango")
    public ResponseEntity<?> obtenerPagosPorRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            if (fechaInicio == null || fechaFin == null) {
                return ResponseEntity.badRequest().body("Ambas fechas (inicio y fin) son requeridas");
            }

            List<Pago> pagos = pagoService.obtenerPagosPorRangoFechas(fechaInicio, fechaFin);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener pagos por rango de fechas: " + e.getMessage());
        }
    }

    // GET - Obtener pago por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPagoPorId(@PathVariable Integer id) {
        try {
            Pago pago = pagoService.obtenerPagoPorId(id);
            return ResponseEntity.ok(pago);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener el pago: " + e.getMessage());
        }
    }
}
