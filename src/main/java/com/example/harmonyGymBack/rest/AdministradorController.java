package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Administrador;
import com.example.harmonyGymBack.service.AdministradorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/administradores")
@CrossOrigin(origins = "*")
public class AdministradorController {

    @Autowired
    private AdministradorServiceImpl administradorService;

    // ==================== CREAR ADMINISTRADOR ====================

    @PostMapping
    public ResponseEntity<?> crearAdministrador(
            @RequestParam("nombreCom") String nombreCom,
            @RequestParam(value = "app", required = false) String app,
            @RequestParam(value = "apm", required = false) String apm) {

        try {
            Administrador administrador = administradorService.crearAdministrador(nombreCom, app, apm);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Administrador creado exitosamente");
            response.put("administrador", administrador);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear administrador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ACTUALIZAR ADMINISTRADOR ====================

    @PutMapping("/{folioAdmin}")
    public ResponseEntity<?> actualizarAdministrador(
            @PathVariable String folioAdmin,
            @RequestParam(value = "nombreCom", required = false) String nombreCom,
            @RequestParam(value = "app", required = false) String app,
            @RequestParam(value = "apm", required = false) String apm) {

        try {
            Administrador administrador = administradorService.actualizarAdministrador(
                    folioAdmin, nombreCom, app, apm);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Administrador actualizado exitosamente");
            response.put("administrador", administrador);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar administrador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ENDPOINTS EXISTENTES ====================

    @GetMapping
    public ResponseEntity<List<Administrador>> obtenerTodosLosAdministradores() {
        try {
            List<Administrador> administradores = administradorService.obtenerTodosLosAdministradores();
            return ResponseEntity.ok(administradores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{folioAdmin}")
    public ResponseEntity<?> obtenerAdministradorPorId(@PathVariable String folioAdmin) {
        try {
            Administrador administrador = administradorService.obtenerAdministradorPorId(folioAdmin);
            return ResponseEntity.ok(administrador);
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
    public ResponseEntity<List<Administrador>> obtenerAdministradoresFiltrados(
            @RequestParam(value = "nombreCom", required = false) String nombreCom,
            @RequestParam(value = "app", required = false) String app,
            @RequestParam(value = "apm", required = false) String apm) {
        try {
            List<Administrador> administradores = administradorService.obtenerAdministradoresFiltrados(nombreCom, app, apm);
            return ResponseEntity.ok(administradores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{folioAdmin}")
    public ResponseEntity<?> eliminarAdministrador(@PathVariable String folioAdmin) {
        try {
            administradorService.eliminarAdministrador(folioAdmin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Administrador eliminado exitosamente");

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

    @GetMapping("/{folioAdmin}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasAdministrador(@PathVariable String folioAdmin) {
        try {
            Map<String, Object> estadisticas = administradorService.obtenerEstadisticasAdministrador(folioAdmin);
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

    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<Administrador>> buscarAdministradoresPorNombre(@RequestParam String nombreCom) {
        try {
            List<Administrador> administradores = administradorService.buscarAdministradoresPorNombre(nombreCom);
            return ResponseEntity.ok(administradores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar/app")
    public ResponseEntity<List<Administrador>> buscarAdministradoresPorApp(@RequestParam String app) {
        try {
            List<Administrador> administradores = administradorService.buscarAdministradoresPorApp(app);
            return ResponseEntity.ok(administradores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar/apm")
    public ResponseEntity<List<Administrador>> buscarAdministradoresPorApm(@RequestParam String apm) {
        try {
            List<Administrador> administradores = administradorService.buscarAdministradoresPorApm(apm);
            return ResponseEntity.ok(administradores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recientes")
    public ResponseEntity<List<Administrador>> obtenerAdministradoresRecientes(
            @RequestParam(defaultValue = "30") int dias) {
        try {
            List<Administrador> administradores = administradorService.obtenerAdministradoresRecientes(dias);
            return ResponseEntity.ok(administradores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/por-fechas")
    public ResponseEntity<List<Administrador>> obtenerAdministradoresPorRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio);
            LocalDateTime fin = LocalDateTime.parse(fechaFin);
            List<Administrador> administradores = administradorService.obtenerAdministradoresPorRangoFechas(inicio, fin);
            return ResponseEntity.ok(administradores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/estadisticas-generales")
    public ResponseEntity<?> obtenerEstadisticasGenerales() {
        try {
            Map<String, Object> estadisticas = administradorService.obtenerEstadisticasGenerales();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== ENDPOINTS DE PRUEBA ====================

    @PostMapping("/crear-con-json")
    public ResponseEntity<?> crearAdministradorConJson(@RequestBody Administrador administrador) {
        try {
            Administrador nuevoAdministrador = administradorService.crearAdministrador(administrador);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Administrador creado exitosamente");
            response.put("administrador", nuevoAdministrador);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear administrador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{folioAdmin}/actualizar-con-json")
    public ResponseEntity<?> actualizarAdministradorConJson(
            @PathVariable String folioAdmin,
            @RequestBody Administrador administrador) {
        try {
            Administrador administradorActualizado = administradorService.actualizarAdministrador(folioAdmin, administrador);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Administrador actualizado exitosamente");
            response.put("administrador", administradorActualizado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar administrador: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}