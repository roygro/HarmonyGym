package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Instructor;
import com.example.harmonyGymBack.service.InstructorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/instructores")
@CrossOrigin(origins = "*")
public class InstructorController {

    @Autowired
    private InstructorServiceImpl instructorService;

    // ==================== CREAR INSTRUCTOR ====================

    @PostMapping
    public ResponseEntity<?> crearInstructor(
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "app", required = false) String app,
            @RequestParam(value = "apm", required = false) String apm,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "horaEntrada", required = false) String horaEntrada,
            @RequestParam(value = "horaSalida", required = false) String horaSalida,
            @RequestParam(value = "especialidad", required = false) String especialidad,
            @RequestParam(value = "fechaContratacion", required = false) String fechaContratacion,
            @RequestParam(value = "estatus", required = false) String estatus) {

        try {
            Instructor instructor = instructorService.crearInstructor(
                    nombre, app, apm, email, horaEntrada, horaSalida, especialidad,
                    fechaContratacion, estatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Instructor creado exitosamente");
            response.put("instructor", instructor);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear instructor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ACTUALIZAR INSTRUCTOR ====================

    @PutMapping("/{folioInstructor}")
    public ResponseEntity<?> actualizarInstructor(
            @PathVariable String folioInstructor,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "app", required = false) String app,
            @RequestParam(value = "apm", required = false) String apm,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "horaEntrada", required = false) String horaEntrada,
            @RequestParam(value = "horaSalida", required = false) String horaSalida,
            @RequestParam(value = "especialidad", required = false) String especialidad,
            @RequestParam(value = "fechaContratacion", required = false) String fechaContratacion,
            @RequestParam(value = "estatus", required = false) String estatus) {

        try {
            Instructor instructor = instructorService.actualizarInstructor(
                    folioInstructor, nombre, app, apm, email, horaEntrada, horaSalida,
                    especialidad, fechaContratacion, estatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Instructor actualizado exitosamente");
            response.put("instructor", instructor);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar instructor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== CREAR INSTRUCTOR CON OBJETO (para compatibilidad) ====================

    @PostMapping("/crear")
    public ResponseEntity<?> crearInstructor(@RequestBody Instructor instructor) {
        try {
            Instructor instructorCreado = instructorService.crearInstructor(instructor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Instructor creado exitosamente");
            response.put("instructor", instructorCreado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear instructor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ACTUALIZAR INSTRUCTOR CON OBJETO (para compatibilidad) ====================

    @PutMapping("/actualizar/{folioInstructor}")
    public ResponseEntity<?> actualizarInstructor(@PathVariable String folioInstructor,
                                                  @RequestBody Instructor instructor) {
        try {
            Instructor instructorActualizado = instructorService.actualizarInstructor(folioInstructor, instructor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Instructor actualizado exitosamente");
            response.put("instructor", instructorActualizado);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar instructor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ENDPOINTS EXISTENTES ====================

    @GetMapping
    public ResponseEntity<List<Instructor>> obtenerTodosLosInstructores() {
        try {
            List<Instructor> instructores = instructorService.obtenerTodosLosInstructores();
            return ResponseEntity.ok(instructores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{folioInstructor}")
    public ResponseEntity<?> obtenerInstructorPorId(@PathVariable String folioInstructor) {
        try {
            Instructor instructor = instructorService.obtenerInstructorPorId(folioInstructor);
            return ResponseEntity.ok(instructor);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /* NUEVO: Obtener instructor por email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> obtenerInstructorPorEmail(@PathVariable String email) {
        try {
            Instructor instructor = instructorService.obtenerInstructorPorEmail(email);
            return ResponseEntity.ok(instructor);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }*/

    @GetMapping("/filtros")
    public ResponseEntity<List<Instructor>> obtenerInstructoresFiltrados(
            @RequestParam(value = "estatus", required = false) String estatus,
            @RequestParam(value = "especialidad", required = false) String especialidad) {
        try {
            List<Instructor> instructores = instructorService.obtenerInstructoresFiltrados(estatus, especialidad);
            return ResponseEntity.ok(instructores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{folioInstructor}/estatus")
    public ResponseEntity<?> cambiarEstatusInstructor(
            @PathVariable String folioInstructor,
            @RequestParam String estatus) {
        try {
            Instructor instructor = instructorService.cambiarEstatusInstructor(folioInstructor, estatus);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estatus actualizado exitosamente");
            response.put("instructor", instructor);

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

    @DeleteMapping("/{folioInstructor}")
    public ResponseEntity<?> eliminarInstructor(@PathVariable String folioInstructor) {
        try {
            instructorService.eliminarInstructor(folioInstructor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Instructor desactivado exitosamente");

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

    @GetMapping("/{folioInstructor}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasInstructor(@PathVariable String folioInstructor) {
        try {
            Map<String, Object> estadisticas = instructorService.obtenerEstadisticasInstructor(folioInstructor);
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

    @GetMapping("/activos")
    public ResponseEntity<List<Instructor>> obtenerInstructoresActivos() {
        try {
            List<Instructor> instructores = instructorService.obtenerInstructoresActivos();
            return ResponseEntity.ok(instructores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Instructor>> buscarInstructoresPorNombre(
            @RequestParam("nombre") String nombre) {
        try {
            List<Instructor> instructores = instructorService.buscarInstructoresPorNombre(nombre);
            return ResponseEntity.ok(instructores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // NUEVO: Buscar instructores por email
    @GetMapping("/buscar-email")
    public ResponseEntity<List<Instructor>> buscarInstructoresPorEmail(
            @RequestParam("email") String email) {
        try {
            List<Instructor> instructores = instructorService.buscarInstructoresPorEmail(email);
            return ResponseEntity.ok(instructores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/contar-activos")
    public ResponseEntity<Long> contarInstructoresActivos() {
        try {
            Long total = instructorService.contarInstructoresActivos();
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<Instructor>> obtenerInstructoresPorEspecialidad(
            @PathVariable String especialidad) {
        try {
            List<Instructor> instructores = instructorService.obtenerInstructoresPorEspecialidad(especialidad);
            return ResponseEntity.ok(instructores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/eliminar/{folioInstructor}")
    public ResponseEntity<?> eliminarInstructorCompleto(@PathVariable String folioInstructor) {
        try {
            instructorService.eliminarInstructorCompleto(folioInstructor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Instructor eliminado completamente");

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

    // NUEVO: Verificar si existe instructor por email
    @GetMapping("/existe-email/{email}")
    public ResponseEntity<Boolean> existeInstructorPorEmail(@PathVariable String email) {
        try {
            boolean existe = instructorService.existeInstructorPorEmail(email);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==================== CREAR INSTRUCTOR CON EMAIL Y CREDENCIALES ====================

    @PostMapping("/crear-con-email")
    public ResponseEntity<?> crearInstructorConEmail(@RequestBody CrearInstructorConEmailRequest request) {
        try {
            Instructor instructorCreado = instructorService.crearInstructorConEmail(
                    request.getInstructor(),
                    request.getEmail(),
                    request.getUsername()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Instructor creado exitosamente con credenciales");
            response.put("instructor", instructorCreado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear instructor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Clase DTO para la request
    public static class CrearInstructorConEmailRequest {
        private Instructor instructor;
        private String email;
        private String username;

        // Constructores
        public CrearInstructorConEmailRequest() {}

        public CrearInstructorConEmailRequest(Instructor instructor, String email, String username) {
            this.instructor = instructor;
            this.email = email;
            this.username = username;
        }

        // Getters y Setters
        public Instructor getInstructor() { return instructor; }
        public void setInstructor(Instructor instructor) { this.instructor = instructor; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
}