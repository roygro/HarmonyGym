package com.example.harmonyGymBack.rest;

import com.example.harmonyGymBack.model.ActividadEntity;
import com.example.harmonyGymBack.service.ActividadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actividades")
@CrossOrigin(origins = "*")
public class ActividadController {

    @Autowired
    private ActividadServiceImpl actividadService;

    // GUARDAR - Crear nueva actividad
    @PostMapping("/guardar")
    public ResponseEntity<?> guardarActividad(@RequestBody ActividadEntity actividadEntity) {
        try {
            ActividadEntity nuevaActividadEntity = actividadService.crearActividad(actividadEntity);
            return new ResponseEntity<>(nuevaActividadEntity, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // LISTAR - Obtener todas las actividades
    @GetMapping("/listar")
    public ResponseEntity<List<ActividadEntity>> listarActividades() {
        try {
            List<ActividadEntity> actividades = actividadService.obtenerTodasActividades();
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // LISTAR FILTRADAS - Obtener actividades con filtros
    @GetMapping("/listar-filtradas")
    public ResponseEntity<List<ActividadEntity>> listarActividadesFiltradas(
            @RequestParam(required = false) String estatus,
            @RequestParam(required = false) String lugar) {
        try {
            List<ActividadEntity> actividades = actividadService.obtenerActividadesFiltradas(estatus, lugar);
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // LISTAR ACTIVAS - Obtener actividades activas
    @GetMapping("/listar-activas")
    public ResponseEntity<List<ActividadEntity>> listarActividadesActivas() {
        try {
            List<ActividadEntity> actividades = actividadService.obtenerActividadesActivas();
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // LISTAR FUTURAS - Obtener actividades futuras
    @GetMapping("/listar-futuras")
    public ResponseEntity<List<ActividadEntity>> listarActividadesFuturas() {
        try {
            List<ActividadEntity> actividades = actividadService.obtenerActividadesFuturas();
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // BUSCAR POR ID - Obtener actividad por ID
    @GetMapping("/buscar/{id}")
    public ResponseEntity<?> buscarActividadPorId(@PathVariable String id) {
        try {
            ActividadEntity actividadEntity = actividadService.obtenerActividadPorId(id);
            return new ResponseEntity<>(actividadEntity, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // EDITAR - Actualizar actividad
    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editarActividad(@PathVariable String id, @RequestBody ActividadEntity actividadEntity) {
        try {
            ActividadEntity actividadEntityActualizada = actividadService.actualizarActividad(id, actividadEntity);
            return new ResponseEntity<>(actividadEntityActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CAMBIAR ESTATUS - Cambiar estatus de actividad
    @PutMapping("/cambiar-estatus/{id}")
    public ResponseEntity<?> cambiarEstatusActividad(
            @PathVariable String id,
            @RequestParam String estatus) {
        try {
            ActividadEntity actividadEntity = actividadService.cambiarEstatusActividad(id, estatus);
            return new ResponseEntity<>(Map.of(
                    "message", "Estatus de actividad actualizado exitosamente",
                    "data", actividadEntity
            ), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ACTIVAR - Activar actividad
    @PutMapping("/activar/{id}")
    public ResponseEntity<?> activarActividad(@PathVariable String id) {
        try {
            ActividadEntity actividadEntity = actividadService.activarActividad(id);
            return new ResponseEntity<>(Map.of(
                    "message", "Actividad activada exitosamente",
                    "data", actividadEntity
            ), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DESACTIVAR - Desactivar actividad
    @PutMapping("/desactivar/{id}")
    public ResponseEntity<?> desactivarActividad(@PathVariable String id) {
        try {
            ActividadEntity actividadEntity = actividadService.desactivarActividad(id);
            return new ResponseEntity<>(Map.of(
                    "message", "Actividad desactivada exitosamente",
                    "data", actividadEntity
            ), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ELIMINAR - Cambiar estatus a inactiva
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarActividad(@PathVariable String id) {
        try {
            actividadService.eliminarActividad(id);
            return new ResponseEntity<>("Actividad eliminada exitosamente", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // BUSCAR POR NOMBRE - Buscar actividades por nombre
    @GetMapping("/buscar-por-nombre")
    public ResponseEntity<List<ActividadEntity>> buscarActividadesPorNombre(@RequestParam String nombre) {
        try {
            List<ActividadEntity> actividades = actividadService.buscarActividadesPorNombre(nombre);
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // BUSCAR POR INSTRUCTOR - Obtener actividades por instructor
    @GetMapping("/buscar-por-instructor/{folioInstructor}")
    public ResponseEntity<List<ActividadEntity>> buscarActividadesPorInstructor(@PathVariable String folioInstructor) {
        try {
            List<ActividadEntity> actividades = actividadService.obtenerActividadesPorInstructor(folioInstructor);
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // BUSCAR POR FECHA - Obtener actividades por fecha
    @GetMapping("/buscar-por-fecha")
    public ResponseEntity<List<ActividadEntity>> buscarActividadesPorFecha(@RequestParam String fecha) {
        try {
            LocalDate fechaActividad = LocalDate.parse(fecha);
            List<ActividadEntity> actividades = actividadService.obtenerActividadesPorFecha(fechaActividad);
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // BUSCAR POR LUGAR - Obtener actividades por lugar
    @GetMapping("/buscar-por-lugar")
    public ResponseEntity<List<ActividadEntity>> buscarActividadesPorLugar(@RequestParam String lugar) {
        try {
            List<ActividadEntity> actividades = actividadService.obtenerActividadesPorLugar(lugar);
            return new ResponseEntity<>(actividades, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // VERIFICAR CUPO - Verificar cupo disponible
    @GetMapping("/verificar-cupo/{id}")
    public ResponseEntity<Boolean> verificarCupoDisponible(@PathVariable String id) {
        try {
            boolean cupoDisponible = actividadService.verificarCupoDisponible(id);
            return new ResponseEntity<>(cupoDisponible, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // VERIFICAR CONFLICTO - Verificar conflicto de horarios
    @GetMapping("/verificar-conflicto")
    public ResponseEntity<Boolean> verificarConflictoHorario(
            @RequestParam String lugar,
            @RequestParam String fecha,
            @RequestParam String horaInicio,
            @RequestParam String horaFin) {
        try {
            LocalDate fechaActividad = LocalDate.parse(fecha);
            LocalTime inicio = LocalTime.parse(horaInicio);
            LocalTime fin = LocalTime.parse(horaFin);

            boolean tieneConflicto = actividadService.tieneConflictoHorario(lugar, fechaActividad, inicio, fin);
            return new ResponseEntity<>(tieneConflicto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CONTAR ACTIVAS - Obtener conteo de actividades activas
    @GetMapping("/contar-activas")
    public ResponseEntity<Long> contarActividadesActivas() {
        try {
            Long conteo = actividadService.contarActividadesActivas();
            return new ResponseEntity<>(conteo, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // VERIFICAR EXISTENCIA - Verificar si existe una actividad
    @GetMapping("/verificar-existencia/{id}")
    public ResponseEntity<Boolean> verificarExistenciaActividad(@PathVariable String id) {
        try {
            boolean existe = actividadService.existeActividad(id);
            return new ResponseEntity<>(existe, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}