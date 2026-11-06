package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Actividad;
import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.MembresiaCliente;
import com.example.harmonyGymBack.model.RealizaActividad;
import com.example.harmonyGymBack.model.enums.EstatusCatalogo;
import com.example.harmonyGymBack.model.enums.EstatusInscripcion;
import com.example.harmonyGymBack.model.enums.EstatusMembresiaCliente;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import com.example.harmonyGymBack.repository.ActividadRepository;
import com.example.harmonyGymBack.repository.ClienteRepository;
import com.example.harmonyGymBack.repository.MembresiaClienteRepository;
import com.example.harmonyGymBack.repository.RealizaActividadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClienteActividadService {

    private final RealizaActividadRepository realizaActividadRepository;

    private final ActividadRepository actividadRepository;

    private final ClienteRepository clienteRepository;

    private final MembresiaClienteRepository membresiaClienteRepository;

    private final ActividadServiceImpl actividadService;

    public ClienteActividadService(RealizaActividadRepository realizaActividadRepository, ActividadRepository actividadRepository, ClienteRepository clienteRepository, MembresiaClienteRepository membresiaClienteRepository, ActividadServiceImpl actividadService) {
        this.realizaActividadRepository = realizaActividadRepository;
        this.actividadRepository = actividadRepository;
        this.clienteRepository = clienteRepository;
        this.membresiaClienteRepository = membresiaClienteRepository;
        this.actividadService = actividadService;
    }


    // ==================== INSCRIPCIÓN A ACTIVIDADES ====================

    @Transactional
    public RealizaActividad inscribirClienteEnActividad(String folioCliente, String idActividad) {
        // Bloquear pesimistamente la fila de la actividad para evitar sobrecupo en concurrencia
        Actividad actividad = actividadRepository.findByIdWithLock(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + idActividad));

        // 1. Validar que el cliente exista y esté activo
        Cliente cliente = clienteRepository.findById(folioCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con folio: " + folioCliente));

        if (!EstatusPersona.Activo.equals(cliente.getEstatus())) {
            throw new RuntimeException("El cliente no está activo");
        }

        // 2. Validar que el cliente tenga membresía activa y vigente
        Optional<MembresiaCliente> membresiaOpt = membresiaClienteRepository
                .findByClienteFolioClienteAndEstatus(folioCliente, EstatusMembresiaCliente.Activa);

        if (membresiaOpt.isEmpty() || !membresiaOpt.get().isVigente()) {
            throw new RuntimeException("El cliente no cuenta con una membresía activa y vigente");
        }

        // 3. Validar que la actividad esté activa
        if (!EstatusCatalogo.Activa.equals(actividad.getEstatus())) {
            throw new RuntimeException("La actividad no está disponible para inscripción");
        }

        // 4. Validar que sea fecha y hora futura
        LocalDateTime inicioActividad = LocalDateTime.of(actividad.getFechaActividad(), actividad.getHoraInicio());
        if (inicioActividad.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No puedes inscribirte a actividades pasadas o en curso");
        }

        // 5. Validar cupo disponible con cupoMaximo
        long inscripcionesActivas = realizaActividadRepository.countInscripcionesActivasByActividad(idActividad);
        int cupoMaximo = actividad.getCupoMaximo();
        if (inscripcionesActivas >= cupoMaximo) {
            throw new RuntimeException("Sin cupo disponible para esta actividad. Cupo máximo alcanzado");
        }

        // 6. Validar que el cliente no esté ya inscrito a esta actividad
        Optional<RealizaActividad> inscripcionExistente =
                realizaActividadRepository.findInscripcionActiva(folioCliente, idActividad);

        if (inscripcionExistente.isPresent()) {
            throw new RuntimeException("Ya estás inscrito en esta actividad");
        }

        // 7. Validar que el cliente no esté inscrito a otra clase en el mismo horario (traslape)
        List<RealizaActividad> inscripcionesCliente = realizaActividadRepository.findInscripcionesConActividad(folioCliente);
        for (RealizaActividad ra : inscripcionesCliente) {
            Actividad otra = ra.getActividad();
            if (otra != null && otra.getFechaActividad().isEqual(actividad.getFechaActividad())) {
                boolean traslape = actividad.getHoraInicio().isBefore(otra.getHoraFin()) &&
                                   actividad.getHoraFin().isAfter(otra.getHoraInicio());
                if (traslape) {
                    throw new RuntimeException("Conflicto de horario: ya estás inscrito en la actividad '" +
                            otra.getNombreActividad() + "' en ese horario");
                }
            }
        }

        // 8. Validar que el instructor no tenga conflicto de horario
        List<Actividad> conflictosInstructor = actividadRepository.findConflictingInstructorActivitiesExcluding(
                actividad.getFolioInstructor(), actividad.getFechaActividad(),
                actividad.getHoraInicio(), actividad.getHoraFin(), actividad.getIdActividad()
        );
        if (!conflictosInstructor.isEmpty()) {
            throw new RuntimeException("El instructor asignado tiene otra actividad programada en ese horario");
        }

        // 9. Validar que el lugar no esté ocupado
        if (actividad.getLugar() != null && !actividad.getLugar().trim().isEmpty()) {
            List<Actividad> conflictosLugar = actividadRepository.findConflictingActivitiesExcluding(
                    actividad.getLugar(), actividad.getFechaActividad(),
                    actividad.getHoraInicio(), actividad.getHoraFin(), actividad.getIdActividad()
            );
            if (!conflictosLugar.isEmpty()) {
                throw new RuntimeException("El lugar '" + actividad.getLugar() + "' ya está ocupado en ese horario");
            }
        }

        // Actualizar cupo en actividad
        actividad.setCupo((int) Math.max(0, cupoMaximo - (inscripcionesActivas + 1)));
        actividadRepository.save(actividad);

        // Crear y guardar la inscripción
        RealizaActividad nuevaInscripcion = new RealizaActividad(folioCliente, idActividad);
        nuevaInscripcion.setFechaParticipacion(actividad.getFechaActividad());
        nuevaInscripcion.setEstatus(EstatusInscripcion.Inscrito);

        return realizaActividadRepository.save(nuevaInscripcion);
    }

    // ==================== CANCELAR INSCRIPCIÓN ====================

    @Transactional
    public RealizaActividad cancelarInscripcion(String folioCliente, String idActividad, String motivo) {
        RealizaActividad inscripcion = realizaActividadRepository
                .findInscripcionActiva(folioCliente, idActividad)
                .orElseThrow(() -> new RuntimeException("No estás inscrito en esta actividad o ya fue cancelada"));

        Actividad actividad = actividadRepository.findByIdWithLock(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        if (actividad.getFechaActividad().isEqual(LocalDate.now())) {
            throw new RuntimeException("No puedes cancelar la inscripción el mismo día de la actividad");
        }

        // Actualizar el cupo
        long inscripcionesActivas = realizaActividadRepository.countInscripcionesActivasByActividad(idActividad);
        actividad.setCupo((int) Math.min(actividad.getCupoMaximo(), actividad.getCupoMaximo() - (inscripcionesActivas - 1)));
        actividadRepository.save(actividad);

        inscripcion.cancelarInscripcion(motivo);
        return realizaActividadRepository.save(inscripcion);
    }

    // ==================== ASISTENCIA DEL INSTRUCTOR ====================

    @Transactional
    public List<RealizaActividad> registrarAsistencia(String idActividad, String folioInstructor, List<Map<String, Object>> asistencias) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + idActividad));

        if (!actividad.getFolioInstructor().equalsIgnoreCase(folioInstructor)) {
            throw new RuntimeException("Solo el instructor asignado (" + actividad.getFolioInstructor() + ") puede registrar asistencia en esta actividad");
        }

        List<RealizaActividad> resultados = new ArrayList<>();

        for (Map<String, Object> item : asistencias) {
            String folioCliente = (String) item.get("folioCliente");
            Boolean asistio = (Boolean) item.get("asistio");

            Optional<RealizaActividad> opt = realizaActividadRepository.findInscripcionActiva(folioCliente, idActividad);
            if (opt.isPresent()) {
                RealizaActividad ra = opt.get();
                ra.setAsistio(Boolean.TRUE.equals(asistio));
                if (Boolean.TRUE.equals(asistio)) {
                    ra.setEstatus(EstatusInscripcion.Completado);
                }
                resultados.add(realizaActividadRepository.save(ra));
            }
        }

        return resultados;
    }

    // ==================== CALIFICAR ACTIVIDAD ====================

    @Transactional
    public RealizaActividad calificarActividad(String folioCliente, String idActividad, Integer calificacion, String comentario) {
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new RuntimeException("La calificación debe ser un número entero entre 1 y 5");
        }

        List<RealizaActividad> inscripciones = realizaActividadRepository.findByFolioCliente(folioCliente);

        RealizaActividad inscripcion = inscripciones.stream()
                .filter(ra -> ra.getIdActividad().equals(idActividad))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No tienes una inscripción para esta actividad"));

        if (!Boolean.TRUE.equals(inscripcion.getAsistio())) {
            throw new RuntimeException("Solo puedes calificar actividades a las que hayas asistido");
        }

        inscripcion.setCalificacion(calificacion);
        inscripcion.setComentarios(comentario);

        return realizaActividadRepository.save(inscripcion);
    }

    // ==================== CONSULTAS ====================

    public List<RealizaActividad> obtenerActividadesInscritas(String folioCliente) {
        return realizaActividadRepository.findInscripcionesConActividad(folioCliente);
    }

    public List<RealizaActividad> obtenerHistorialActividades(String folioCliente) {
        return realizaActividadRepository.findHistorialConActividad(folioCliente);
    }

    public List<Actividad> obtenerActividadesDisponibles() {
        List<Actividad> actividadesActivas = actividadService.obtenerActividadesActivas();
        return actividadesActivas.stream()
                .filter(actividad -> {
                    long ocupados = realizaActividadRepository.countInscripcionesActivasByActividad(actividad.getIdActividad());
                    return ocupados < actividad.getCupoMaximo();
                })
                .collect(Collectors.toList());
    }

    public List<ActividadConCupo> obtenerActividadesDisponiblesConCupo() {
        List<Actividad> actividadesActivas = actividadService.obtenerActividadesActivas();

        return actividadesActivas.stream()
                .map(actividad -> {
                    Long inscripcionesActivas = realizaActividadRepository
                            .countInscripcionesActivasByActividad(actividad.getIdActividad());
                    Integer cupoMax = actividad.getCupoMaximo();
                    Integer cuposDisponibles = Math.max(0, (int) (cupoMax - inscripcionesActivas));
                    Boolean tieneCupo = cuposDisponibles > 0;

                    return new ActividadConCupo(actividad, cuposDisponibles, tieneCupo,
                            inscripcionesActivas.intValue(), cupoMax);
                })
                .collect(Collectors.toList());
    }

    public boolean estaInscritoEnActividad(String folioCliente, String idActividad) {
        return realizaActividadRepository.findInscripcionActiva(folioCliente, idActividad).isPresent();
    }

    public Integer obtenerCuposDisponibles(String idActividad) {
        Actividad actividad = actividadService.obtenerActividadPorId(idActividad);
        long inscripcionesActivas = realizaActividadRepository.countInscripcionesActivasByActividad(idActividad);
        return Math.max(0, (int) (actividad.getCupoMaximo() - inscripcionesActivas));
    }

    public Map<String, Object> obtenerInformacionCupo(String idActividad) {
        Actividad actividad = actividadService.obtenerActividadPorId(idActividad);
        Long inscripcionesActivas = realizaActividadRepository
                .countInscripcionesActivasByActividad(idActividad);

        Integer cupoMaximo = actividad.getCupoMaximo();
        Integer cuposDisponibles = Math.max(0, (int) (cupoMaximo - inscripcionesActivas));

        Map<String, Object> infoCupo = new HashMap<>();
        infoCupo.put("cupoMaximoOriginal", cupoMaximo);
        infoCupo.put("cupoActual", cuposDisponibles);
        infoCupo.put("inscripcionesActivas", inscripcionesActivas);
        infoCupo.put("cuposDisponibles", cuposDisponibles);
        infoCupo.put("porcentajeOcupacion",
                cupoMaximo > 0 ? (inscripcionesActivas * 100.0 / cupoMaximo) : 0.0);

        return infoCupo;
    }

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

        public Actividad getActividad() { return actividad; }
        public Integer getCuposDisponibles() { return cuposDisponibles; }
        public Boolean getTieneCupo() { return tieneCupo; }
        public Integer getInscripcionesActuales() { return inscripcionesActuales; }
        public Integer getCupoMaximoOriginal() { return cupoMaximoOriginal; }

        public void setActividad(Actividad actividad) { this.actividad = actividad; }
        public void setCuposDisponibles(Integer cuposDisponibles) { this.cuposDisponibles = cuposDisponibles; }
        public void setTieneCupo(Boolean tieneCupo) { this.tieneCupo = tieneCupo; }
        public void setInscripcionesActuales(Integer inscripcionesActuales) { this.inscripcionesActuales = inscripcionesActuales; }
        public void setCupoMaximoOriginal(Integer cupoMaximoOriginal) { this.cupoMaximoOriginal = cupoMaximoOriginal; }
    }
}
