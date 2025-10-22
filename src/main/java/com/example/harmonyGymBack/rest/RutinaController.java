package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.RutinaEntity;
import com.example.harmonyGymBack.service.RutinaServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutinas")
@CrossOrigin(origins = "*")
public class RutinaController {

    @Autowired
    private RutinaServiceImpl rutinaService;

    // ===== ENDPOINTS BÁSICOS =====

    @PostMapping
    public ResponseEntity<?> crearRutina(@RequestBody RutinaEntity rutina) {
        try {
            RutinaEntity rutinaCreada = rutinaService.crearRutina(rutina);
            return ResponseEntity.status(HttpStatus.CREATED).body(rutinaCreada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<RutinaEntity>> obtenerTodasLasRutinas() {
        List<RutinaEntity> rutinas = rutinaService.obtenerTodasLasRutinas();
        return ResponseEntity.ok(rutinas);
    }

    @GetMapping("/{folioRutina}")
    public ResponseEntity<?> obtenerRutinaPorId(@PathVariable String folioRutina) {
        return rutinaService.obtenerRutinaPorId(folioRutina)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{folioRutina}/completa")
    public ResponseEntity<?> obtenerRutinaConEjercicios(@PathVariable String folioRutina) {
        try {
            RutinaEntity rutina = rutinaService.obtenerRutinaConEjercicios(folioRutina);
            return ResponseEntity.ok(rutina);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{folioRutina}")
    public ResponseEntity<?> actualizarRutina(@PathVariable String folioRutina, @RequestBody RutinaEntity rutina) {
        try {
            RutinaEntity rutinaActualizada = rutinaService.actualizarRutina(folioRutina, rutina);
            return ResponseEntity.ok(rutinaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    // ===== ENDPOINTS ESPECÍFICOS PARA CAMBIAR ESTATUS (CORREGIDOS) =====

    @PostMapping("/{folioRutina}/activar")
    public ResponseEntity<?> activarRutina(@PathVariable String folioRutina) {
        try {
            rutinaService.activarRutina(folioRutina);
            return ResponseEntity.ok().body(crearRespuestaExito("Rutina activada correctamente", folioRutina, "Activa"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @PostMapping("/{folioRutina}/desactivar")
    public ResponseEntity<?> desactivarRutina(@PathVariable String folioRutina) {
        try {
            rutinaService.desactivarRutina(folioRutina);
            return ResponseEntity.ok().body(crearRespuestaExito("Rutina desactivada correctamente", folioRutina, "Inactiva"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    // Endpoint genérico para compatibilidad (mejorado)
    @PatchMapping("/{folioRutina}/estatus")
    public ResponseEntity<?> cambiarEstatusRutina(@PathVariable String folioRutina, @RequestBody Map<String, String> request) {
        try {
            String estatus = request.get("estatus");
            if (estatus == null || estatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(crearRespuestaError("El campo 'estatus' es requerido"));
            }

            rutinaService.cambiarEstatusRutina(folioRutina, estatus);
            return ResponseEntity.ok().body(crearRespuestaExito("Estatus actualizado correctamente", folioRutina, estatus));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    // ===== ENDPOINTS PARA INSTRUCTORES =====

    @PostMapping("/instructor/{folioInstructor}")
    public ResponseEntity<?> crearRutinaPorInstructor(@PathVariable String folioInstructor,
                                                      @RequestBody RutinaEntity rutina) {
        try {
            RutinaEntity rutinaCreada = rutinaService.crearRutinaPorInstructor(rutina, folioInstructor);
            return ResponseEntity.status(HttpStatus.CREATED).body(rutinaCreada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping("/instructor/{folioInstructor}")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinasPorInstructor(@PathVariable String folioInstructor) {
        List<RutinaEntity> rutinas = rutinaService.obtenerRutinasPorInstructor(folioInstructor);
        return ResponseEntity.ok(rutinas);
    }

    @GetMapping("/instructor/{folioInstructor}/activas")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinasActivasPorInstructor(@PathVariable String folioInstructor) {
        List<RutinaEntity> rutinas = rutinaService.obtenerRutinasActivasPorInstructor(folioInstructor);
        return ResponseEntity.ok(rutinas);
    }

    // ===== ENDPOINTS DE ASIGNACIÓN =====

    @PostMapping("/{folioRutina}/asignar-cliente/{folioCliente}")
    public ResponseEntity<?> asignarRutinaACliente(@PathVariable String folioRutina,
                                                   @PathVariable String folioCliente) {
        try {
            rutinaService.asignarRutinaACliente(folioRutina, folioCliente);
            return ResponseEntity.ok().body(crearRespuestaExito("Rutina asignada al cliente correctamente", folioRutina, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @PostMapping("/{folioRutina}/asignar-multiples-clientes")
    public ResponseEntity<?> asignarRutinaAMultiplesClientes(@PathVariable String folioRutina,
                                                             @RequestBody List<String> foliosClientes) {
        try {
            RutinaServiceImpl.AsignacionResultado resultado =
                    rutinaService.asignarRutinaAMultiplesClientes(folioRutina, foliosClientes);

            // Crear respuesta detallada
            Map<String, Object> respuesta = Map.of(
                    "mensaje", "Proceso de asignación completado",
                    "exitosos", resultado.getExitosos(),
                    "duplicados", resultado.getDuplicados(),
                    "errores", resultado.getErrores(),
                    "estadisticas", Map.of(
                            "totalProcesados", resultado.getTotalProcesados(),
                            "exitosos", resultado.getTotalExitosos(),
                            "duplicados", resultado.getTotalDuplicados(),
                            "errores", resultado.getTotalErrores()
                    )
            );

            return ResponseEntity.ok().body(respuesta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @DeleteMapping("/{folioRutina}/desasignar-cliente/{folioCliente}")
    public ResponseEntity<?> removerAsignacionRutina(@PathVariable String folioRutina,
                                                     @PathVariable String folioCliente) {
        try {
            rutinaService.removerAsignacionRutina(folioRutina, folioCliente);
            return ResponseEntity.ok().body(crearRespuestaExito("Asignación removida correctamente", folioRutina, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping("/{folioRutina}/clientes")
    public ResponseEntity<?> obtenerClientesDeRutina(@PathVariable String folioRutina) {
        try {
            List<Cliente> clientes = rutinaService.obtenerClientesDeRutina(folioRutina);
            return ResponseEntity.ok(clientes);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{folioRutina}/clientes/activos")
    public ResponseEntity<?> obtenerClientesActivosDeRutina(@PathVariable String folioRutina) {
        try {
            List<Cliente> clientes = rutinaService.obtenerClientesActivosDeRutina(folioRutina);
            return ResponseEntity.ok(clientes);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ===== ENDPOINTS PARA EJERCICIOS EN RUTINAS =====

    @PostMapping("/{folioRutina}/ejercicios")
    public ResponseEntity<?> agregarEjercicioARutina(@PathVariable String folioRutina,
                                                     @RequestBody RutinaEntity.AgregarEjercicioRequest request) {
        try {
            rutinaService.agregarEjercicioARutina(folioRutina, request);
            return ResponseEntity.ok().body(crearRespuestaExito("Ejercicio agregado a la rutina correctamente", folioRutina, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @DeleteMapping("/{folioRutina}/ejercicios/{idEjercicio}")
    public ResponseEntity<?> removerEjercicioDeRutina(@PathVariable String folioRutina,
                                                      @PathVariable String idEjercicio) {
        try {
            rutinaService.removerEjercicioDeRutina(folioRutina, idEjercicio);
            return ResponseEntity.ok().body(crearRespuestaExito("Ejercicio removido de la rutina correctamente", folioRutina, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping("/{folioRutina}/ejercicios")
    public ResponseEntity<?> obtenerEjerciciosDeRutina(@PathVariable String folioRutina) {
        try {
            List<RutinaEntity.EjercicioRutinaDTO> ejercicios = rutinaService.obtenerEjerciciosDeRutina(folioRutina);
            return ResponseEntity.ok(ejercicios);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{folioRutina}/ejercicios/orden")
    public ResponseEntity<?> actualizarOrdenEjercicios(@PathVariable String folioRutina,
                                                       @RequestBody List<String> idsEjerciciosEnOrden) {
        try {
            rutinaService.actualizarOrdenEjercicios(folioRutina, idsEjerciciosEnOrden);
            return ResponseEntity.ok().body(crearRespuestaExito("Orden de ejercicios actualizado correctamente", folioRutina, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @PatchMapping("/{folioRutina}/ejercicios/{idEjercicio}")
    public ResponseEntity<?> actualizarParametrosEjercicio(@PathVariable String folioRutina,
                                                           @PathVariable String idEjercicio,
                                                           @RequestBody Map<String, Object> parametros) {
        try {
            Integer series = parametros.get("series") != null ? Integer.parseInt(parametros.get("series").toString()) : null;
            Integer repeticiones = parametros.get("repeticiones") != null ? Integer.parseInt(parametros.get("repeticiones").toString()) : null;
            Integer descanso = parametros.get("descanso") != null ? Integer.parseInt(parametros.get("descanso").toString()) : null;
            String observaciones = (String) parametros.get("observaciones");

            rutinaService.actualizarParametrosEjercicioEnRutina(folioRutina, idEjercicio, series, repeticiones, descanso, observaciones);
            return ResponseEntity.ok().body(crearRespuestaExito("Parámetros del ejercicio actualizados correctamente", folioRutina, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    // ===== ENDPOINTS PARA EJERCICIOS INDEPENDIENTES =====

    @GetMapping("/ejercicios")
    public ResponseEntity<List<RutinaEntity.EjercicioSimpleDTO>> obtenerTodosLosEjercicios() {
        List<RutinaEntity.EjercicioSimpleDTO> ejercicios = rutinaService.obtenerTodosLosEjercicios();
        return ResponseEntity.ok(ejercicios);
    }

    @GetMapping("/ejercicios/{idEjercicio}")
    public ResponseEntity<?> obtenerEjercicioPorId(@PathVariable String idEjercicio) {
        try {
            RutinaEntity.EjercicioSimpleDTO ejercicio = rutinaService.obtenerEjercicioPorId(idEjercicio);
            return ResponseEntity.ok(ejercicio);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ejercicios/buscar/nombre")
    public ResponseEntity<List<RutinaEntity.EjercicioSimpleDTO>> buscarEjerciciosPorNombre(@RequestParam String nombre) {
        List<RutinaEntity.EjercicioSimpleDTO> ejercicios = rutinaService.buscarEjerciciosPorNombre(nombre);
        return ResponseEntity.ok(ejercicios);
    }

    @GetMapping("/ejercicios/buscar/grupo-muscular")
    public ResponseEntity<List<RutinaEntity.EjercicioSimpleDTO>> buscarEjerciciosPorGrupoMuscular(@RequestParam String grupoMuscular) {
        List<RutinaEntity.EjercicioSimpleDTO> ejercicios = rutinaService.buscarEjerciciosPorGrupoMuscular(grupoMuscular);
        return ResponseEntity.ok(ejercicios);
    }

    @GetMapping("/ejercicios/existe/{idEjercicio}")
    public ResponseEntity<Boolean> existeEjercicio(@PathVariable String idEjercicio) {
        boolean existe = rutinaService.existeEjercicio(idEjercicio);
        return ResponseEntity.ok(existe);
    }

    // ===== ENDPOINTS PARA CLIENTES =====

    @GetMapping("/cliente/{folioCliente}")
    public ResponseEntity<?> obtenerRutinasPorCliente(@PathVariable String folioCliente) {
        try {
            List<RutinaEntity> rutinas = rutinaService.obtenerRutinasPorCliente(folioCliente);
            return ResponseEntity.ok(rutinas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping("/cliente/{folioCliente}/activas")
    public ResponseEntity<?> obtenerRutinasActivasPorCliente(@PathVariable String folioCliente) {
        try {
            List<RutinaEntity> rutinas = rutinaService.obtenerRutinasActivasPorCliente(folioCliente);
            return ResponseEntity.ok(rutinas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    // ===== ENDPOINTS DE BÚSQUEDA =====

    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<RutinaEntity>> buscarRutinasPorNombre(@RequestParam String nombre) {
        List<RutinaEntity> rutinas = rutinaService.buscarRutinasPorNombre(nombre);
        return ResponseEntity.ok(rutinas);
    }

    @GetMapping("/buscar/nivel")
    public ResponseEntity<List<RutinaEntity>> buscarRutinasPorNivel(@RequestParam String nivel) {
        List<RutinaEntity> rutinas = rutinaService.buscarRutinasPorNivel(nivel);
        return ResponseEntity.ok(rutinas);
    }

    @GetMapping("/buscar/objetivo")
    public ResponseEntity<List<RutinaEntity>> buscarRutinasPorObjetivo(@RequestParam String objetivo) {
        List<RutinaEntity> rutinas = rutinaService.buscarRutinasPorObjetivo(objetivo);
        return ResponseEntity.ok(rutinas);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinasActivas() {
        List<RutinaEntity> rutinas = rutinaService.obtenerRutinasActivas();
        return ResponseEntity.ok(rutinas);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinasDisponibles() {
        List<RutinaEntity> rutinas = rutinaService.obtenerRutinasDisponibles();
        return ResponseEntity.ok(rutinas);
    }

    // ===== ENDPOINTS DE ESTADÍSTICAS =====

    @GetMapping("/estadisticas/instructor/{folioInstructor}/total")
    public ResponseEntity<?> contarRutinasPorInstructor(@PathVariable String folioInstructor) {
        try {
            Long total = rutinaService.contarRutinasPorInstructor(folioInstructor);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping("/estadisticas/cliente/{folioCliente}/activas")
    public ResponseEntity<?> contarRutinasActivasPorCliente(@PathVariable String folioCliente) {
        try {
            Long total = rutinaService.contarRutinasActivasPorCliente(folioCliente);
            return ResponseEntity.ok(total);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping("/estadisticas/total-activas")
    public ResponseEntity<Long> obtenerTotalRutinasActivas() {
        Long total = rutinaService.obtenerTotalRutinasActivas();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/cliente/{folioCliente}/tiene-activas")
    public ResponseEntity<?> clienteTieneRutinasActivas(@PathVariable String folioCliente) {
        try {
            boolean tieneActivas = rutinaService.clienteTieneRutinasActivas(folioCliente);
            return ResponseEntity.ok(tieneActivas);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping("/{folioRutina}/asignada-a-cliente/{folioCliente}")
    public ResponseEntity<Boolean> estaRutinaAsignadaACliente(@PathVariable String folioRutina,
                                                              @PathVariable String folioCliente) {
        boolean asignada = rutinaService.estaRutinaAsignadaACliente(folioRutina, folioCliente);
        return ResponseEntity.ok(asignada);
    }

    @GetMapping("/existe/{folioRutina}")
    public ResponseEntity<Boolean> existeRutina(@PathVariable String folioRutina) {
        boolean existe = rutinaService.existeRutina(folioRutina);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/{folioRutina}/tiempo-total")
    public ResponseEntity<?> calcularTiempoTotalRutina(@PathVariable String folioRutina) {
        try {
            Integer tiempoTotal = rutinaService.calcularTiempoTotalRutina(folioRutina);
            return ResponseEntity.ok(tiempoTotal);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @PostMapping("/instructor/{folioInstructor}/desactivar-todas")
    public ResponseEntity<?> desactivarTodasLasRutinasPorInstructor(@PathVariable String folioInstructor) {
        try {
            rutinaService.desactivarTodasLasRutinasPorInstructor(folioInstructor);
            return ResponseEntity.ok().body(crearRespuestaExito("Todas las rutinas del instructor han sido desactivadas", null, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @PostMapping("/instructor/{folioInstructor}/{folioRutina}/desactivar")
    public ResponseEntity<?> desactivarRutinaPorInstructor(@PathVariable String folioInstructor,
                                                           @PathVariable String folioRutina) {
        try {
            rutinaService.desactivarRutinaPorInstructor(folioRutina, folioInstructor);
            return ResponseEntity.ok().body(crearRespuestaExito("Rutina desactivada correctamente", folioRutina, "Inactiva"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    @GetMapping("/{folioRutina}/activa")
    public ResponseEntity<Boolean> isRutinaActiva(@PathVariable String folioRutina) {
        boolean activa = rutinaService.isRutinaActiva(folioRutina);
        return ResponseEntity.ok(activa);
    }

    // Endpoint para desactivar múltiples rutinas
    @PostMapping("/desactivar-multiples")
    public ResponseEntity<?> desactivarMultiplesRutinas(@RequestBody List<String> foliosRutinas) {
        try {
            Map<String, Object> resultado = new HashMap<>();
            List<String> exitosas = new ArrayList<>();
            List<String> errores = new ArrayList<>();

            for (String folioRutina : foliosRutinas) {
                try {
                    rutinaService.desactivarRutina(folioRutina);
                    exitosas.add(folioRutina);
                } catch (RuntimeException e) {
                    errores.add(folioRutina + ": " + e.getMessage());
                }
            }

            resultado.put("exitosas", exitosas);
            resultado.put("errores", errores);
            resultado.put("totalProcesadas", foliosRutinas.size());
            resultado.put("exitosasCount", exitosas.size());
            resultado.put("erroresCount", errores.size());

            return ResponseEntity.ok().body(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(crearRespuestaError(e.getMessage()));
        }
    }

    // ===== MÉTODOS AUXILIARES PARA RESPUESTAS =====

    private Map<String, Object> crearRespuestaExito(String mensaje, String folioRutina, String estatus) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", mensaje);
        respuesta.put("exito", true);
        if (folioRutina != null) {
            respuesta.put("folioRutina", folioRutina);
        }
        if (estatus != null) {
            respuesta.put("estatus", estatus);
        }
        return respuesta;
    }

    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", mensaje);
        respuesta.put("exito", false);
        respuesta.put("error", true);
        return respuesta;
    }
}