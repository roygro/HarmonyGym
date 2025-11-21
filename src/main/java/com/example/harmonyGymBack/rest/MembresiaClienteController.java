package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.MembresiaCliente;
import com.example.harmonyGymBack.model.PlanPago;
import com.example.harmonyGymBack.service.MembresiaClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/membresias-clientes")
@CrossOrigin(origins = "*")
public class MembresiaClienteController {

    @Autowired
    private MembresiaClienteService membresiaClienteService;

    // ==================== ASIGNAR MEMBRESÍA A CLIENTE ====================

    // ✅ MODIFICADO: Ahora recibe idPlanPago
    @PostMapping
    public ResponseEntity<?> asignarMembresiaACliente(
            @RequestParam String folioCliente,
            @RequestParam String idMembresia,
            @RequestParam Long idPlanPago, // ✅ NUEVO: parámetro para plan de pago
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio) {

        try {
            // ✅ MODIFICADO: Pasar idPlanPago al servicio
            MembresiaCliente membresia = membresiaClienteService.asignarMembresiaACliente(
                    folioCliente, idMembresia, idPlanPago, fechaInicio);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Membresía asignada exitosamente");
            response.put("membresia", membresia);
            response.put("precioFinal", membresia.calcularPrecioFinal()); // ✅ NUEVO: precio con descuento

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al asignar membresía: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== RENOVAR MEMBRESÍA ====================

    @PostMapping("/{id}/renovar")
    public ResponseEntity<?> renovarMembresia(@PathVariable Long id) {
        try {
            MembresiaCliente membresia = membresiaClienteService.renovarMembresia(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Membresía renovada exitosamente");
            response.put("membresia", membresia);
            response.put("precioFinal", membresia.calcularPrecioFinal()); // ✅ NUEVO: precio con descuento

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al renovar membresía: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== CANCELAR MEMBRESÍA ====================

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarMembresia(@PathVariable Long id) {
        try {
            MembresiaCliente membresia = membresiaClienteService.cancelarMembresia(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Membresía cancelada exitosamente");
            response.put("membresia", membresia);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cancelar membresía: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== OBTENER MEMBRESÍA ACTIVA ====================

    @GetMapping("/cliente/{folioCliente}/activa")
    public ResponseEntity<?> obtenerMembresiaActiva(@PathVariable String folioCliente) {
        try {
            MembresiaCliente membresia = membresiaClienteService.obtenerMembresiaActiva(folioCliente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("membresia", membresia);
            response.put("precioFinal", membresia.calcularPrecioFinal()); // ✅ NUEVO: precio con descuento

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // ==================== OBTENER HISTORIAL ====================

    @GetMapping("/cliente/{folioCliente}/historial")
    public ResponseEntity<?> obtenerHistorialMembresias(@PathVariable String folioCliente) {
        try {
            List<MembresiaCliente> historial = membresiaClienteService.obtenerHistorialMembresias(folioCliente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("historial", historial);
            response.put("total", historial.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener historial: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== VERIFICAR ACCESO ====================

    @GetMapping("/cliente/{folioCliente}/verificar-acceso")
    public ResponseEntity<?> verificarAcceso(@PathVariable String folioCliente) {
        try {
            Map<String, Object> resultado = membresiaClienteService.verificarAccesoCliente(folioCliente);
            resultado.put("success", true);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al verificar acceso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== OBTENER MEMBRESÍAS ACTIVAS ====================

    @GetMapping("/activas")
    public ResponseEntity<?> obtenerMembresiasActivas() {
        try {
            List<MembresiaCliente> membresias = membresiaClienteService.obtenerTodasMembresiasActivas();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("membresias", membresias);
            response.put("total", membresias.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener membresías activas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== OBTENER MEMBRESÍAS POR EXPIRAR ====================

    @GetMapping("/por-expirar")
    public ResponseEntity<?> obtenerMembresiasPorExpirar(@RequestParam(defaultValue = "7") int dias) {
        try {
            List<MembresiaCliente> membresias = membresiaClienteService.obtenerMembresiasPorExpirar(dias);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("membresias", membresias);
            response.put("total", membresias.size());
            response.put("dias", dias);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener membresías por expirar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== ACTUALIZAR EXPIRADAS ====================

    @PostMapping("/actualizar-expiradas")
    public ResponseEntity<?> actualizarMembresiasExpiradas() {
        try {
            int actualizadas = membresiaClienteService.actualizarMembresiasExpiradas();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Membresías expiradas actualizadas: " + actualizadas);
            response.put("actualizadas", actualizadas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar membresías expiradas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== ESTADÍSTICAS ====================

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Map<String, Object> estadisticas = membresiaClienteService.obtenerEstadisticasMembresias();
            estadisticas.put("success", true);

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== CAMBIAR MEMBRESÍA ====================

    @PutMapping("/{id}/cambiar")
    public ResponseEntity<?> cambiarMembresia(
            @PathVariable Long id,
            @RequestParam String nuevaIdMembresia) {

        try {
            MembresiaCliente membresia = membresiaClienteService.cambiarMembresiaCliente(id, nuevaIdMembresia);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Membresía cambiada exitosamente");
            response.put("membresia", membresia);
            response.put("precioFinal", membresia.calcularPrecioFinal()); // ✅ NUEVO: precio con descuento

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cambiar membresía: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== OBTENER TODAS LAS MEMBRESÍAS ====================

    @GetMapping
    public ResponseEntity<?> obtenerTodasLasMembresias() {
        try {
            List<MembresiaCliente> membresias = membresiaClienteService.obtenerTodasLasMembresias();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("membresias", membresias);
            response.put("total", membresias.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener todas las membresías: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== ENDPOINTS NUEVOS PARA PLANES DE PAGO ====================

    // ✅ NUEVO: Crear plan de pago
    @PostMapping("/planes-pago")
    public ResponseEntity<?> crearPlanPago(@RequestBody Map<String, Object> planData) {
        try {
            String nombre = (String) planData.get("nombre");
            String descripcion = (String) planData.get("descripcion");
            Integer duracionDias = (Integer) planData.get("duracionDias");
            Double factorDescuento = (Double) planData.get("factorDescuento");

            PlanPago plan = membresiaClienteService.crearPlanPago(nombre, descripcion, duracionDias, factorDescuento);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Plan de pago creado exitosamente");
            response.put("plan", plan);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear plan de pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ✅ NUEVO: Obtener planes disponibles
    @GetMapping("/planes-pago")
    public ResponseEntity<?> obtenerPlanesDisponibles() {
        try {
            List<PlanPago> planes = membresiaClienteService.obtenerPlanesDisponibles();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("planes", planes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener planes de pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ NUEVO: Cambiar plan de pago de una membresía
    @PutMapping("/{id}/cambiar-plan")
    public ResponseEntity<?> cambiarPlanPago(@PathVariable Long id, @RequestParam Long idPlanPago) {
        try {
            MembresiaCliente membresia = membresiaClienteService.cambiarPlanPago(id, idPlanPago);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Plan de pago cambiado exitosamente");
            response.put("membresia", membresia);
            response.put("precioFinal", membresia.calcularPrecioFinal());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cambiar plan de pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ✅ NUEVO: Crear promoción especial
    @PostMapping("/promociones")
    public ResponseEntity<?> crearPromocion(@RequestBody Map<String, Object> promocionData) {
        try {
            String nombrePromocion = (String) promocionData.get("nombrePromocion");
            Integer duracionDias = (Integer) promocionData.get("duracionDias");

            // ✅ MANEJAR DIFERENTES TIPOS PARA porcentajeDescuento
            Double porcentajeDescuento;
            Object descuentoObj = promocionData.get("porcentajeDescuento");

            if (descuentoObj instanceof Integer) {
                porcentajeDescuento = ((Integer) descuentoObj).doubleValue();
            } else if (descuentoObj instanceof Double) {
                porcentajeDescuento = (Double) descuentoObj;
            } else {
                throw new RuntimeException("Tipo de dato no válido para porcentajeDescuento");
            }

            PlanPago promocion = membresiaClienteService.crearPromocion(
                    nombrePromocion, porcentajeDescuento, duracionDias);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Promoción creada exitosamente");
            response.put("promocion", promocion);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear promoción: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ✅ NUEVO: Obtener planes con descuento
    @GetMapping("/planes-pago/descuentos")
    public ResponseEntity<?> obtenerPlanesConDescuento() {
        try {
            List<PlanPago> planes = membresiaClienteService.obtenerPlanesConDescuento();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("planes", planes);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener planes con descuento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ NUEVO: Estadísticas de planes de pago
    @GetMapping("/planes-pago/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasPlanesPago() {
        try {
            Map<String, Object> estadisticas = membresiaClienteService.obtenerEstadisticasPlanesPago();
            estadisticas.put("success", true);

            return ResponseEntity.ok(estadisticas);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener estadísticas de planes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}