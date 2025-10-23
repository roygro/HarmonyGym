package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.RutinaEntity;
import com.example.harmonyGymBack.service.RutinaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutinas")
@CrossOrigin(origins = "*")
public class RutinaController {

    @Autowired
    private RutinaServiceImpl rutinaService;

    @GetMapping
    public List<RutinaEntity> getAllRutinas() {
        return rutinaService.findAll();
    }

    @GetMapping("/{folioRutina}")
    public ResponseEntity<RutinaEntity> getRutinaByFolio(@PathVariable String folioRutina) {
        return rutinaService.findByFolioRutina(folioRutina)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/instructor/{folioInstructor}")
    public List<RutinaEntity> getRutinasByInstructor(@PathVariable String folioInstructor) {
        return rutinaService.findByInstructor(folioInstructor);
    }

    @PostMapping
    public RutinaEntity createRutina(@RequestBody RutinaEntity rutina) {
        return rutinaService.createRutina(rutina);
    }

    @PutMapping("/{folioRutina}")
    public ResponseEntity<RutinaEntity> updateRutina(@PathVariable String folioRutina, @RequestBody RutinaEntity rutina) {
        RutinaEntity rutinaActualizada = rutinaService.updateRutina(folioRutina, rutina);
        return rutinaActualizada != null ? ResponseEntity.ok(rutinaActualizada) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{folioRutina}")
    public ResponseEntity<Void> deleteRutina(@PathVariable String folioRutina) {
        boolean deleted = rutinaService.deleteRutina(folioRutina);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{folioRutina}/ejercicios")
    public ResponseEntity<RutinaEntity> agregarEjercicio(
            @PathVariable String folioRutina,
            @RequestBody RutinaEntity.EjercicioRutina ejercicioRutina) {
        RutinaEntity rutina = rutinaService.agregarEjercicio(folioRutina, ejercicioRutina);
        return rutina != null ? ResponseEntity.ok(rutina) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{folioRutina}/ejercicios/{idEjercicio}")
    public ResponseEntity<RutinaEntity> eliminarEjercicio(
            @PathVariable String folioRutina,
            @PathVariable String idEjercicio) {
        RutinaEntity rutina = rutinaService.eliminarEjercicio(folioRutina, idEjercicio);
        return rutina != null ? ResponseEntity.ok(rutina) : ResponseEntity.notFound().build();
    }

    // Endpoints para asignación de rutinas a clientes (individuales)
    @PostMapping("/{folioRutina}/asignar-cliente/{folioCliente}")
    public ResponseEntity<?> asignarRutinaACliente(
            @PathVariable String folioRutina,
            @PathVariable String folioCliente,
            @RequestParam String folioInstructor) {

        try {
            rutinaService.asignarRutinaACliente(folioRutina, folioCliente, folioInstructor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rutina asignada al cliente exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/{folioRutina}/clientes-no-asignados")
    public ResponseEntity<List<Cliente>> getClientesNoAsignadosByRutina(@PathVariable String folioRutina) {
        try {
            List<Cliente> clientesNoAsignados = rutinaService.getClientesNoAsignadosByRutina(folioRutina);
            return ResponseEntity.ok(clientesNoAsignados);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @DeleteMapping("/{folioRutina}/desasignar-cliente/{folioCliente}")
    public ResponseEntity<?> desasignarRutinaDeCliente(
            @PathVariable String folioRutina,
            @PathVariable String folioCliente) {

        try {
            rutinaService.desasignarRutinaDeCliente(folioRutina, folioCliente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rutina desasignada del cliente exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/cliente/{folioCliente}")
    public ResponseEntity<List<RutinaEntity>> getRutinasByCliente(@PathVariable String folioCliente) {
        try {
            List<RutinaEntity> rutinas = rutinaService.getRutinasByCliente(folioCliente);
            return ResponseEntity.ok(rutinas);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{folioRutina}/clientes-asignados")
    public ResponseEntity<List<Cliente>> getClientesByRutina(@PathVariable String folioRutina) {
        try {
            List<Cliente> clientes = rutinaService.getClientesByRutina(folioRutina);
            return ResponseEntity.ok(clientes);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== NUEVOS ENDPOINTS PARA ASIGNACIÓN MÚLTIPLE ====================

    @PostMapping("/{folioRutina}/asignar-multiples-clientes")
    public ResponseEntity<?> asignarRutinaAMultiplesClientes(
            @PathVariable String folioRutina,
            @RequestBody AsignacionMultiplesClientesRequest request) {

        try {
            Map<String, Object> resultado = rutinaService.asignarRutinaAMultiplesClientes(
                    folioRutina, request.getFoliosClientes(), request.getFolioInstructor());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Proceso de asignación completado");
            response.put("resultado", resultado);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{folioRutina}/desasignar-multiples-clientes")
    public ResponseEntity<?> desasignarRutinaDeMultiplesClientes(
            @PathVariable String folioRutina,
            @RequestBody List<String> foliosClientes) {

        try {
            Map<String, Object> resultado = rutinaService.desasignarRutinaDeMultiplesClientes(
                    folioRutina, foliosClientes);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Proceso de desasignación completado");
            response.put("resultado", resultado);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Clase DTO para la request de asignación múltiple
    public static class AsignacionMultiplesClientesRequest {
        private List<String> foliosClientes;
        private String folioInstructor;

        // Getters y Setters
        public List<String> getFoliosClientes() {
            return foliosClientes;
        }

        public void setFoliosClientes(List<String> foliosClientes) {
            this.foliosClientes = foliosClientes;
        }

        public String getFolioInstructor() {
            return folioInstructor;
        }

        public void setFolioInstructor(String folioInstructor) {
            this.folioInstructor = folioInstructor;
        }
    }

    // ==================== ENDPOINTS PARA GESTIÓN DE ESTATUS ====================

    @PatchMapping("/{folioRutina}/estatus")
    public ResponseEntity<?> cambiarEstatusRutina(
            @PathVariable String folioRutina,
            @RequestBody CambioEstatusRequest request) {

        try {
            RutinaEntity rutinaActualizada = rutinaService.cambiarEstatusRutina(folioRutina, request.getEstatus());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estatus de la rutina actualizado exitosamente");
            response.put("rutina", rutinaActualizada);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/{folioRutina}/activar")
    public ResponseEntity<?> activarRutina(@PathVariable String folioRutina) {
        try {
            RutinaEntity rutinaActualizada = rutinaService.activarRutina(folioRutina);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rutina activada exitosamente");
            response.put("rutina", rutinaActualizada);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/{folioRutina}/inactivar")
    public ResponseEntity<?> inactivarRutina(@PathVariable String folioRutina) {
        try {
            RutinaEntity rutinaActualizada = rutinaService.inactivarRutina(folioRutina);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rutina inactivada exitosamente");
            response.put("rutina", rutinaActualizada);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/estatus/{estatus}")
    public ResponseEntity<List<RutinaEntity>> getRutinasByEstatus(@PathVariable String estatus) {
        try {
            List<RutinaEntity> rutinas = rutinaService.findByEstatus(estatus);
            return ResponseEntity.ok(rutinas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Clase DTO para cambio de estatus
    public static class CambioEstatusRequest {
        private String estatus;

        // Getters y Setters
        public String getEstatus() {
            return estatus;
        }

        public void setEstatus(String estatus) {
            this.estatus = estatus;
        }
    }
}