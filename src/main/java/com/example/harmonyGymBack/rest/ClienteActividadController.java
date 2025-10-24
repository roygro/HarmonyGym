package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Actividad;
import com.example.harmonyGymBack.model.RealizaActividad;
import com.example.harmonyGymBack.service.ClienteActividadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cliente-actividades")
@CrossOrigin(origins = "*")
public class ClienteActividadController {

    @Autowired
    private ClienteActividadService clienteActividadService;

    // ==================== INSCRIPCIÓN ====================

    @PostMapping("/inscribir")
    public ResponseEntity<?> inscribirEnActividad(
            @RequestParam String folioCliente,
            @RequestParam String idActividad) {
        try {
            RealizaActividad inscripcion = clienteActividadService
                    .inscribirClienteEnActividad(folioCliente, idActividad);

            // Obtener cupos disponibles después de la inscripción
            Integer cuposDisponibles = clienteActividadService.obtenerCuposDisponibles(idActividad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inscripción exitosa. Cupos restantes: " + cuposDisponibles);
            response.put("data", inscripcion);
            response.put("cuposDisponibles", cuposDisponibles);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== CANCELAR INSCRIPCIÓN ====================

    @PutMapping("/cancelar")
    public ResponseEntity<?> cancelarInscripcion(
            @RequestParam String folioCliente,
            @RequestParam String idActividad,
            @RequestParam(required = false) String motivo) {
        try {
            RealizaActividad inscripcion = clienteActividadService
                    .cancelarInscripcion(folioCliente, idActividad, motivo);

            // Obtener cupos disponibles después de la cancelación
            Integer cuposDisponibles = clienteActividadService.obtenerCuposDisponibles(idActividad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inscripción cancelada exitosamente. Cupos disponibles: " + cuposDisponibles);
            response.put("data", inscripcion);
            response.put("cuposDisponibles", cuposDisponibles);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== CONSULTAS DE ACTIVIDADES ====================

    @GetMapping("/disponibles")
    public ResponseEntity<?> obtenerActividadesDisponibles() {
        try {
            List<Actividad> actividades = clienteActividadService.obtenerActividadesDisponibles();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", actividades);
            response.put("total", actividades.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener actividades disponibles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/disponibles-con-cupo")
    public ResponseEntity<?> obtenerActividadesDisponiblesConCupo() {
        try {
            List<ClienteActividadService.ActividadConCupo> actividades =
                    clienteActividadService.obtenerActividadesDisponiblesConCupo();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", actividades);
            response.put("total", actividades.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener actividades con cupo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{folioCliente}/inscritas")
    public ResponseEntity<?> obtenerActividadesInscritas(@PathVariable String folioCliente) {
        try {
            List<RealizaActividad> actividades = clienteActividadService
                    .obtenerActividadesInscritas(folioCliente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", actividades);
            response.put("total", actividades.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener actividades inscritas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{folioCliente}/historial")
    public ResponseEntity<?> obtenerHistorialActividades(@PathVariable String folioCliente) {
        try {
            List<RealizaActividad> historial = clienteActividadService
                    .obtenerHistorialActividades(folioCliente);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", historial);
            response.put("total", historial.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener historial de actividades: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== VERIFICACIONES ====================

    @GetMapping("/verificar-inscripcion")
    public ResponseEntity<?> verificarInscripcion(
            @RequestParam String folioCliente,
            @RequestParam String idActividad) {
        try {
            boolean estaInscrito = clienteActividadService
                    .estaInscritoEnActividad(folioCliente, idActividad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("estaInscrito", estaInscrito);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al verificar inscripción: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{idActividad}/cupos-disponibles")
    public ResponseEntity<?> obtenerCuposDisponibles(@PathVariable String idActividad) {
        try {
            Integer cuposDisponibles = clienteActividadService.obtenerCuposDisponibles(idActividad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cuposDisponibles", cuposDisponibles);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{idActividad}/informacion-cupo")
    public ResponseEntity<?> obtenerInformacionCupo(@PathVariable String idActividad) {
        try {
            Map<String, Object> infoCupo = clienteActividadService.obtenerInformacionCupo(idActividad);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", infoCupo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== CALIFICACIÓN ====================

    @PutMapping("/calificar")
    public ResponseEntity<?> calificarActividad(
            @RequestParam String folioCliente,
            @RequestParam String idActividad,
            @RequestParam Integer calificacion,
            @RequestParam(required = false) String comentario) {
        try {
            // Buscar la inscripción
            List<RealizaActividad> inscripciones = clienteActividadService
                    .obtenerHistorialActividades(folioCliente);

            RealizaActividad inscripcion = inscripciones.stream()
                    .filter(ra -> ra.getIdActividad().equals(idActividad))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No tienes una inscripción para esta actividad"));

            // Actualizar calificación
            inscripcion.setCalificacion(calificacion);
            inscripcion.setComentarios(comentario);
            // Aquí deberías tener un servicio para guardar la actualización

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Calificación registrada exitosamente");
            response.put("data", inscripcion);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ESTADÍSTICAS ====================

    @GetMapping("/{folioCliente}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasCliente(@PathVariable String folioCliente) {
        try {
            List<RealizaActividad> historial = clienteActividadService
                    .obtenerHistorialActividades(folioCliente);

            long totalActividades = historial.size();
            long actividadesCompletadas = historial.stream()
                    .filter(ra -> "Completado".equals(ra.getEstatus()))
                    .count();
            long actividadesInscritas = historial.stream()
                    .filter(ra -> "Inscrito".equals(ra.getEstatus()))
                    .count();
            double promedioCalificacion = historial.stream()
                    .filter(ra -> ra.getCalificacion() != null)
                    .mapToInt(RealizaActividad::getCalificacion)
                    .average()
                    .orElse(0.0);

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalActividades", totalActividades);
            estadisticas.put("actividadesCompletadas", actividadesCompletadas);
            estadisticas.put("actividadesInscritas", actividadesInscritas);
            estadisticas.put("promedioCalificacion", Math.round(promedioCalificacion * 100.0) / 100.0);
            estadisticas.put("porcentajeAsistencia", totalActividades > 0 ?
                    Math.round((actividadesCompletadas * 100.0 / totalActividades) * 100.0) / 100.0 : 0.0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", estadisticas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}