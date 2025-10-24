package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Actividad;
import com.example.harmonyGymBack.model.RealizaActividad;
import com.example.harmonyGymBack.repository.ActividadRepository;
import com.example.harmonyGymBack.repository.RealizaActividadRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteActividadService {

    @Autowired
    private RealizaActividadRepository realizaActividadRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ActividadServiceImpl actividadService;

    // ==================== INSCRIPCIÓN A ACTIVIDADES (ACTUALIZADO) ====================

    @Transactional
    public RealizaActividad inscribirClienteEnActividad(String folioCliente, String idActividad) {
        // Verificar si la actividad existe y está activa
        Actividad actividad = actividadService.obtenerActividadPorId(idActividad);

        if (!"Activa".equals(actividad.getEstatus())) {
            throw new RuntimeException("La actividad no está disponible para inscripción");
        }

        // Verificar si la actividad ya pasó
        if (actividad.getFechaActividad().isBefore(LocalDate.now())) {
            throw new RuntimeException("No puedes inscribirte a actividades pasadas");
        }

        // Verificar cupo disponible (usando el cupo actual de la actividad)
        if (actividad.getCupo() <= 0) {
            throw new RuntimeException("No hay cupo disponible para esta actividad. Cupo máximo alcanzado");
        }

        // Verificar si el cliente ya está inscrito
        Optional<RealizaActividad> inscripcionExistente =
                realizaActividadRepository.findInscripcionActiva(folioCliente, idActividad);

        if (inscripcionExistente.isPresent()) {
            throw new RuntimeException("Ya estás inscrito en esta actividad");
        }

        // ✅ ACTUALIZAR EL CUPO EN LA ACTIVIDAD - RESTAR 1
        actividad.setCupo(actividad.getCupo() - 1);
        actividadRepository.save(actividad);

        // Crear nueva inscripción
        RealizaActividad nuevaInscripcion = new RealizaActividad(folioCliente, idActividad);
        nuevaInscripcion.setFechaParticipacion(actividad.getFechaActividad());

        return realizaActividadRepository.save(nuevaInscripcion);
    }

    // ==================== CANCELAR INSCRIPCIÓN (ACTUALIZADO) ====================

    @Transactional
    public RealizaActividad cancelarInscripcion(String folioCliente, String idActividad, String motivo) {
        RealizaActividad inscripcion = realizaActividadRepository
                .findInscripcionActiva(folioCliente, idActividad)
                .orElseThrow(() -> new RuntimeException("No estás inscrito en esta actividad o ya fue cancelada"));

        // Verificar que no sea el mismo día de la actividad
        Actividad actividad = actividadService.obtenerActividadPorId(idActividad);
        if (actividad.getFechaActividad().isEqual(LocalDate.now())) {
            throw new RuntimeException("No puedes cancelar la inscripción el mismo día de la actividad");
        }

        // ✅ ACTUALIZAR EL CUPO EN LA ACTIVIDAD - SUMAR 1
        actividad.setCupo(actividad.getCupo() + 1);
        actividadRepository.save(actividad);

        // Al cancelar, automáticamente se "libera" un cupo
        inscripcion.cancelarInscripcion(motivo);
        return realizaActividadRepository.save(inscripcion);
    }

    // ==================== CONSULTAS PRINCIPALES (ACTUALIZADAS) ====================

    public List<RealizaActividad> obtenerActividadesInscritas(String folioCliente) {
        return realizaActividadRepository.findInscripcionesConActividad(folioCliente);
    }

    public List<RealizaActividad> obtenerHistorialActividades(String folioCliente) {
        return realizaActividadRepository.findHistorialConActividad(folioCliente);
    }

    public List<Actividad> obtenerActividadesDisponibles() {
        List<Actividad> actividadesActivas = actividadService.obtenerActividadesActivas();

        return actividadesActivas.stream()
                .filter(actividad -> actividad.getCupo() > 0) // ✅ Ahora usa el cupo actual de la actividad
                .collect(Collectors.toList());
    }

    public List<ActividadConCupo> obtenerActividadesDisponiblesConCupo() {
        List<Actividad> actividadesActivas = actividadService.obtenerActividadesActivas();

        return actividadesActivas.stream()
                .map(actividad -> {
                    Long inscripcionesActivas = realizaActividadRepository
                            .countInscripcionesActivasByActividad(actividad.getIdActividad());
                    Integer cupoMaximoOriginal = actividad.getCupo() + inscripcionesActivas.intValue();
                    Integer cuposDisponibles = actividad.getCupo(); // ✅ Ahora usa el cupo actual
                    Boolean tieneCupo = cuposDisponibles > 0;

                    return new ActividadConCupo(actividad, cuposDisponibles, tieneCupo,
                            inscripcionesActivas.intValue(), cupoMaximoOriginal);
                })
                .collect(Collectors.toList());
    }

    // ==================== VERIFICACIONES Y CUPOS (ACTUALIZADOS) ====================

    public boolean estaInscritoEnActividad(String folioCliente, String idActividad) {
        Optional<RealizaActividad> inscripcion =
                realizaActividadRepository.findInscripcionActiva(folioCliente, idActividad);
        return inscripcion.isPresent();
    }

    public Integer obtenerCuposDisponibles(String idActividad) {
        Actividad actividad = actividadService.obtenerActividadPorId(idActividad);
        // ✅ Ahora simplemente devuelve el cupo actual de la actividad
        return Math.max(0, actividad.getCupo());
    }

    public Map<String, Object> obtenerInformacionCupo(String idActividad) {
        Actividad actividad = actividadService.obtenerActividadPorId(idActividad);
        Long inscripcionesActivas = realizaActividadRepository
                .countInscripcionesActivasByActividad(idActividad);

        Integer cupoMaximoOriginal = actividad.getCupo() + inscripcionesActivas.intValue();
        Integer cuposDisponibles = actividad.getCupo(); // ✅ Ahora usa el cupo actual

        Map<String, Object> infoCupo = new HashMap<>();
        infoCupo.put("cupoMaximoOriginal", cupoMaximoOriginal);
        infoCupo.put("cupoActual", cuposDisponibles);
        infoCupo.put("inscripcionesActivas", inscripcionesActivas);
        infoCupo.put("cuposDisponibles", cuposDisponibles);
        infoCupo.put("porcentajeOcupacion",
                cupoMaximoOriginal > 0 ?
                        (inscripcionesActivas * 100.0 / cupoMaximoOriginal) : 0.0);

        return infoCupo;
    }

    // ==================== CLASE INTERNA ACTUALIZADA ====================

    public static class ActividadConCupo {
        private Actividad actividad;
        private Integer cuposDisponibles;
        private Boolean tieneCupo;
        private Integer inscripcionesActuales;
        private Integer cupoMaximoOriginal;

        public ActividadConCupo(Actividad actividad, Integer cuposDisponibles, Boolean tieneCupo,
                                Integer inscripcionesActuales, Integer cupoMaximoOriginal) {
            this.actividad = actividad;
            this.cuposDisponibles = cuposDisponibles;
            this.tieneCupo = tieneCupo;
            this.inscripcionesActuales = inscripcionesActuales;
            this.cupoMaximoOriginal = cupoMaximoOriginal;
        }

        // Getters
        public Actividad getActividad() { return actividad; }
        public Integer getCuposDisponibles() { return cuposDisponibles; }
        public Boolean getTieneCupo() { return tieneCupo; }
        public Integer getInscripcionesActuales() { return inscripcionesActuales; }
        public Integer getCupoMaximoOriginal() { return cupoMaximoOriginal; }

        // Setters (necesarios para la serialización JSON)
        public void setActividad(Actividad actividad) { this.actividad = actividad; }
        public void setCuposDisponibles(Integer cuposDisponibles) { this.cuposDisponibles = cuposDisponibles; }
        public void setTieneCupo(Boolean tieneCupo) { this.tieneCupo = tieneCupo; }
        public void setInscripcionesActuales(Integer inscripcionesActuales) { this.inscripcionesActuales = inscripcionesActuales; }
        public void setCupoMaximoOriginal(Integer cupoMaximoOriginal) { this.cupoMaximoOriginal = cupoMaximoOriginal; }
    }
}