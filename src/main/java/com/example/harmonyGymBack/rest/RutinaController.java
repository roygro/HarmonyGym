package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.RutinaEntity;
import com.example.harmonyGymBack.service.RutinaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/rutinas")
@CrossOrigin(origins = "*")
public class RutinaController {

    @Autowired
    private RutinaServiceImpl rutinaService;

    // ===== ENDPOINTS PARA INSTRUCTORES =====

    // Crear rutina (solo instructores)
    @PostMapping("/instructor/{folioInstructor}")
    public ResponseEntity<?> crearRutina(@PathVariable String folioInstructor,
                                         @RequestBody RutinaEntity rutina) {
        try {
            RutinaEntity rutinaCreada = rutinaService.crearRutinaPorInstructor(rutina, folioInstructor);
            return ResponseEntity.status(HttpStatus.CREATED).body(rutinaCreada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Asignar rutina a un cliente
    @PostMapping("/{folioRutina}/asignar-cliente/{folioCliente}")
    public ResponseEntity<?> asignarRutinaACliente(@PathVariable String folioRutina,
                                                   @PathVariable String folioCliente) {
        try {
            rutinaService.asignarRutinaACliente(folioRutina, folioCliente);
            return ResponseEntity.ok().body("Rutina asignada al cliente correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Asignar rutina a múltiples clientes
    @PostMapping("/{folioRutina}/asignar-multiples-clientes")
    public ResponseEntity<?> asignarRutinaAMultiplesClientes(@PathVariable String folioRutina,
                                                             @RequestBody List<String> foliosClientes) {
        try {
            rutinaService.asignarRutinaAMultiplesClientes(folioRutina, foliosClientes);
            return ResponseEntity.ok().body("Rutina asignada a múltiples clientes correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Remover asignación de rutina a cliente
    @DeleteMapping("/{folioRutina}/desasignar-cliente/{folioCliente}")
    public ResponseEntity<?> removerAsignacionRutina(@PathVariable String folioRutina,
                                                     @PathVariable String folioCliente) {
        try {
            rutinaService.removerAsignacionRutina(folioRutina, folioCliente);
            return ResponseEntity.ok().body("Asignación removida correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Obtener rutinas creadas por un instructor
    @GetMapping("/instructor/{folioInstructor}")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinasPorInstructor(@PathVariable String folioInstructor) {
        List<RutinaEntity> rutinas = rutinaService.obtenerRutinasPorInstructor(folioInstructor);
        return ResponseEntity.ok(rutinas);
    }

    // Obtener clientes asignados a una rutina
    @GetMapping("/{folioRutina}/clientes")
    public ResponseEntity<Set<Cliente>> obtenerClientesDeRutina(@PathVariable String folioRutina) {
        try {
            Set<Cliente> clientes = rutinaService.obtenerClientesDeRutina(folioRutina);
            return ResponseEntity.ok(clientes);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Obtener rutinas disponibles de un instructor para un cliente específico
    @GetMapping("/instructor/{folioInstructor}/disponibles-para-cliente/{folioCliente}")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinasDisponiblesParaCliente(
            @PathVariable String folioInstructor,
            @PathVariable String folioCliente) {
        List<RutinaEntity> rutinas = rutinaService.obtenerRutinasDisponiblesParaCliente(folioInstructor, folioCliente);
        return ResponseEntity.ok(rutinas);
    }

    // ===== ENDPOINTS PARA CLIENTES =====

    // Obtener rutinas asignadas a un cliente
    @GetMapping("/cliente/{folioCliente}")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinasPorCliente(@PathVariable String folioCliente) {
        List<RutinaEntity> rutinas = rutinaService.obtenerRutinasPorCliente(folioCliente);
        return ResponseEntity.ok(rutinas);
    }

    // Obtener rutinas activas asignadas a un cliente
    @GetMapping("/cliente/{folioCliente}/activas")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinasActivasPorCliente(@PathVariable String folioCliente) {
        List<RutinaEntity> rutinas = rutinaService.obtenerRutinasActivasPorCliente(folioCliente);
        return ResponseEntity.ok(rutinas);
    }

    // ===== ENDPOINTS GENERALES =====

    // Obtener todas las rutinas
    @GetMapping
    public ResponseEntity<List<RutinaEntity>> obtenerTodasLasRutinas() {
        List<RutinaEntity> rutinas = rutinaService.obtenerTodasLasRutinas();
        return ResponseEntity.ok(rutinas);
    }

    // Obtener rutina por folio
    @GetMapping("/{folioRutina}")
    public ResponseEntity<?> obtenerRutinaPorId(@PathVariable String folioRutina) {
        return rutinaService.obtenerRutinaPorId(folioRutina)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Actualizar rutina
    @PutMapping("/{folioRutina}")
    public ResponseEntity<?> actualizarRutina(@PathVariable String folioRutina, @RequestBody RutinaEntity rutina) {
        try {
            RutinaEntity rutinaActualizada = rutinaService.actualizarRutina(folioRutina, rutina);
            return ResponseEntity.ok(rutinaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Cambiar estatus de rutina
    @PatchMapping("/{folioRutina}/estatus")
    public ResponseEntity<?> cambiarEstatusRutina(@PathVariable String folioRutina, @RequestBody String estatus) {
        try {
            rutinaService.cambiarEstatusRutina(folioRutina, estatus);
            return ResponseEntity.ok().body("Estatus actualizado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Verificar si rutina está asignada a cliente
    @GetMapping("/{folioRutina}/asignada-a-cliente/{folioCliente}")
    public ResponseEntity<Boolean> estaRutinaAsignadaACliente(@PathVariable String folioRutina,
                                                              @PathVariable String folioCliente) {
        boolean asignada = rutinaService.estaRutinaAsignadaACliente(folioRutina, folioCliente);
        return ResponseEntity.ok(asignada);
    }
}