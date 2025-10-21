package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Recepcionista;
import com.example.harmonyGymBack.service.RecepcionistaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recepcionistas")
@CrossOrigin(origins = "*")
public class RecepcionistaController {

    @Autowired
    private RecepcionistaServiceImpl recepcionistaService;

    // ==================== CREAR RECEPCIONISTA ====================

    @PostMapping
    public ResponseEntity<?> crearRecepcionista(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "fechaContratacion", required = false) String fechaContratacion,
            @RequestParam(value = "estatus", required = false) String estatus) {

        try {
            Recepcionista recepcionista = recepcionistaService.crearRecepcionista(
                    nombre, telefono, email, fechaContratacion, estatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recepcionista creado exitosamente");
            response.put("recepcionista", recepcionista);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear recepcionista: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ACTUALIZAR RECEPCIONISTA ====================

    @PutMapping("/{idRecepcionista}")
    public ResponseEntity<?> actualizarRecepcionista(
            @PathVariable String idRecepcionista,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "fechaContratacion", required = false) String fechaContratacion,
            @RequestParam(value = "estatus", required = false) String estatus) {

        try {
            Recepcionista recepcionista = recepcionistaService.actualizarRecepcionista(
                    idRecepcionista, nombre, telefono, email, fechaContratacion, estatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recepcionista actualizado exitosamente");
            response.put("recepcionista", recepcionista);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar recepcionista: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ENDPOINTS EXISTENTES ====================

    @GetMapping
    public ResponseEntity<List<Recepcionista>> obtenerTodosLosRecepcionistas() {
        try {
            List<Recepcionista> recepcionistas = recepcionistaService.obtenerTodosLosRecepcionistas();
            return ResponseEntity.ok(recepcionistas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{idRecepcionista}")
    public ResponseEntity<?> obtenerRecepcionistaPorId(@PathVariable String idRecepcionista) {
        try {
            Recepcionista recepcionista = recepcionistaService.obtenerRecepcionistaPorId(idRecepcionista);
            return ResponseEntity.ok(recepcionista);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filtros")
    public ResponseEntity<List<Recepcionista>> obtenerRecepcionistasFiltrados(
            @RequestParam(value = "estatus", required = false) String estatus) {
        try {
            List<Recepcionista> recepcionistas = recepcionistaService.obtenerRecepcionistasFiltrados(estatus);
            return ResponseEntity.ok(recepcionistas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{idRecepcionista}/estatus")
    public ResponseEntity<?> cambiarEstatusRecepcionista(
            @PathVariable String idRecepcionista,
            @RequestParam String estatus) {
        try {
            Recepcionista recepcionista = recepcionistaService.cambiarEstatusRecepcionista(idRecepcionista, estatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estatus actualizado exitosamente");
            response.put("recepcionista", recepcionista);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{idRecepcionista}")
    public ResponseEntity<?> eliminarRecepcionista(@PathVariable String idRecepcionista) {
        try {
            recepcionistaService.eliminarRecepcionista(idRecepcionista);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recepcionista desactivado exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{idRecepcionista}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasRecepcionista(@PathVariable String idRecepcionista) {
        try {
            Map<String, Object> estadisticas = recepcionistaService.obtenerEstadisticasRecepcionista(idRecepcionista);
            return ResponseEntity.ok(estadisticas);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Recepcionista>> buscarRecepcionistasPorNombre(@RequestParam String nombre) {
        try {
            List<Recepcionista> recepcionistas = recepcionistaService.buscarRecepcionistasPorNombre(nombre);
            return ResponseEntity.ok(recepcionistas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Recepcionista>> obtenerRecepcionistasActivos() {
        try {
            List<Recepcionista> recepcionistas = recepcionistaService.obtenerRecepcionistasActivos();
            return ResponseEntity.ok(recepcionistas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/contratados-rango")
    public ResponseEntity<List<Recepcionista>> obtenerRecepcionistasContratadosEnRango(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            LocalDate inicio = LocalDate.parse(fechaInicio);
            LocalDate fin = LocalDate.parse(fechaFin);
            List<Recepcionista> recepcionistas = recepcionistaService.obtenerRecepcionistasContratadosEnRango(inicio, fin);
            return ResponseEntity.ok(recepcionistas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/estadisticas-generales")
    public ResponseEntity<?> obtenerEstadisticasGenerales() {
        try {
            Map<String, Object> estadisticas = recepcionistaService.obtenerEstadisticasGenerales();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ELIMINACIÓN COMPLETA ====================

    @DeleteMapping("/{idRecepcionista}/completo")
    public ResponseEntity<?> eliminarRecepcionistaCompleto(@PathVariable String idRecepcionista) {
        try {
            recepcionistaService.eliminarRecepcionistaCompleto(idRecepcionista);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Recepcionista eliminado completamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}