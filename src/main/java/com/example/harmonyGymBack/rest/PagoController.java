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

    // POST - Crear nuevo pago (SIN DTO)
    @PostMapping
    public ResponseEntity<?> crearPago(@RequestBody Pago pago) {
        try {
            // Validaciones básicas
            if (pago.getFolioCliente() == null || pago.getFolioCliente().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El folio del cliente es requerido");
            }
            if (pago.getCodigoProducto() == null || pago.getCodigoProducto().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El código de producto es requerido");
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

    // GET - Obtener todos los pagos (ordenados por fecha descendente)
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
    public ResponseEntity<?> obtenerPagosPorCliente(@PathVariable String folioCliente) {
        try {
            if (folioCliente == null || folioCliente.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El folio del cliente es requerido");
            }

            List<Pago> pagos = pagoService.obtenerPagosPorCliente(folioCliente);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener pagos del cliente: " + e.getMessage());
        }
    }

    // GET - Obtener pagos por recepcionista
    @GetMapping("/recepcionista/{idRecepcionista}")
    public ResponseEntity<?> obtenerPagosPorRecepcionista(@PathVariable String idRecepcionista) {
        try {
            if (idRecepcionista == null || idRecepcionista.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El ID del recepcionista es requerido");
            }

            List<Pago> pagos = pagoService.obtenerPagosPorRecepcionista(idRecepcionista);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener pagos del recepcionista: " + e.getMessage());
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
                    .body("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    // GET - Obtener pagos por rango de fechas
    @GetMapping("/rango-fechas")
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

    // GET - Obtener tipos de membresía disponibles (para el frontend)
    @GetMapping("/tipos-membresia")
    public ResponseEntity<?> obtenerTiposMembresia() {
        try {
            // Esto podría venir de otra tabla o ser estático
            java.util.Map<String, Object>[] tipos = new java.util.Map[]{
                    java.util.Map.of("codigo", "MEM_MENSUAL", "nombre", "Membresía Mensual", "precio", 500.00),
                    java.util.Map.of("codigo", "MEM_TRIMESTRAL", "nombre", "Membresía Trimestral", "precio", 1350.00),
                    java.util.Map.of("codigo", "MEM_ANUAL", "nombre", "Membresía Anual", "precio", 5000.00),
                    java.util.Map.of("codigo", "MEM_INSCRIPCION", "nombre", "Inscripción", "precio", 250.00)
            };
            return ResponseEntity.ok(tipos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener tipos de membresía: " + e.getMessage());
        }
    }
}