package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Actividad;
import com.example.harmonyGymBack.model.enums.EstatusCatalogo;
import com.example.harmonyGymBack.repository.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ActividadServiceImpl {

    @Autowired
    private com.example.harmonyGymBack.repository.RealizaActividadRepository realizaActividadRepository;

    private final ActividadRepository actividadRepository;

    public ActividadServiceImpl(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }


    private String generarIdActividad() {
        List<Actividad> todasActividades = actividadRepository.findAll();

        if (todasActividades.isEmpty()) {
            return "ACT001";
        }

        int maxNumero = 0;
        for (Actividad actividad : todasActividades) {
            String idActividad = actividad.getIdActividad();
            if (idActividad != null && idActividad.startsWith("ACT")) {
                try {
                    String numeroStr = idActividad.substring(3);
                    int numero = Integer.parseInt(numeroStr);
                    if (numero > maxNumero) {
                        maxNumero = numero;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        int nuevoNumero = maxNumero + 1;
        return String.format("ACT%03d", nuevoNumero);
    }

    public Actividad crearActividad(Actividad actividad) {
        String nuevoId = generarIdActividad();
        actividad.setIdActividad(nuevoId);

        if (actividad.getNivelDificultad() == null) {
            actividad.setNivelDificultad("principiante");
        }

        List<Actividad> conflictos = actividadRepository.findConflictingActivities(
                actividad.getLugar(),
                actividad.getFechaActividad(),
                actividad.getHoraInicio(),
                actividad.getHoraFin()
        );

        if (!conflictos.isEmpty()) {
            throw new RuntimeException("Conflicto de horario en el lugar: " + actividad.getLugar() +
                    " para la fecha: " + actividad.getFechaActividad());
        }

        if (actividad.getHoraFin().isBefore(actividad.getHoraInicio()) ||
                actividad.getHoraFin().equals(actividad.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        if (actividad.getFechaActividad().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de la actividad no puede ser en el pasado");
        }

        if (actividad.getEstatus() == null) {
            actividad.setEstatus(EstatusCatalogo.Activa);
        }

        return actividadRepository.save(actividad);
    }

    public List<Actividad> obtenerTodasActividades() {
        return actividadRepository.findAll();
    }

    public List<Actividad> obtenerActividadesFiltradas(String estatus, String lugar) {
        if (estatus != null && lugar != null) {
            return actividadRepository.findByEstatusAndLugarContainingIgnoreCase(EstatusCatalogo.fromString(estatus), lugar);
        } else if (estatus != null) {
            return actividadRepository.findByEstatus(EstatusCatalogo.fromString(estatus));
        } else if (lugar != null) {
            return actividadRepository.findByLugarContainingIgnoreCase(lugar);
        } else {
            return actividadRepository.findAll();
        }
    }

    public Actividad obtenerActividadPorId(String idActividad) {
        Optional<Actividad> actividad = actividadRepository.findById(idActividad);
        return actividad.orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + idActividad));
    }

    public List<Actividad> obtenerActividadesActivas() {
        return actividadRepository.findByEstatusOrderByFechaActividadAscHoraInicioAsc(EstatusCatalogo.Activa);
    }

    public List<Actividad> obtenerActividadesFuturas() {
        return actividadRepository.findByFechaActividadGreaterThanEqualAndEstatusOrderByFechaActividadAscHoraInicioAsc(
                LocalDate.now(), EstatusCatalogo.Activa);
    }

    public List<Actividad> obtenerActividadesPorInstructor(String folioInstructor) {
        return actividadRepository.findByFolioInstructorAndEstatus(folioInstructor, EstatusCatalogo.Activa);
    }

    public List<Actividad> obtenerActividadesPorFecha(LocalDate fecha) {
        return actividadRepository.findByFechaActividadAndEstatus(fecha, EstatusCatalogo.Activa);
    }

    public Actividad actualizarActividad(String idActividad, Actividad actividadActualizada) {
        Actividad actividadExistente = obtenerActividadPorId(idActividad);

        List<Actividad> conflictos = actividadRepository.findConflictingActivitiesExcluding(
                actividadActualizada.getLugar(),
                actividadActualizada.getFechaActividad(),
                actividadActualizada.getHoraInicio(),
                actividadActualizada.getHoraFin(),
                idActividad
        );

        if (!conflictos.isEmpty()) {
            throw new RuntimeException("Conflicto de horario en el lugar: " + actividadActualizada.getLugar() +
                    " para la fecha: " + actividadActualizada.getFechaActividad());
        }

        actividadExistente.setNombreActividad(actividadActualizada.getNombreActividad());
        actividadExistente.setFechaActividad(actividadActualizada.getFechaActividad());
        actividadExistente.setHoraInicio(actividadActualizada.getHoraInicio());
        actividadExistente.setHoraFin(actividadActualizada.getHoraFin());
        actividadExistente.setDescripcion(actividadActualizada.getDescripcion());
        actividadExistente.setCupo(actividadActualizada.getCupo());
        actividadExistente.setCupoMaximo(actividadActualizada.getCupoMaximo());
        actividadExistente.setLugar(actividadActualizada.getLugar());
        actividadExistente.setImagenUrl(actividadActualizada.getImagenUrl());
        actividadExistente.setFolioInstructor(actividadActualizada.getFolioInstructor());
        actividadExistente.setNivelDificultad(actividadActualizada.getNivelDificultad());

        if (actividadActualizada.getEstatus() != null) {
            actividadExistente.setEstatus(actividadActualizada.getEstatus());
        }

        if (actividadExistente.getHoraFin().isBefore(actividadExistente.getHoraInicio()) ||
                actividadExistente.getHoraFin().equals(actividadExistente.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        if (actividadExistente.getFechaActividad().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de la actividad no puede ser en el pasado");
        }

        return actividadRepository.save(actividadExistente);
    }

    @org.springframework.transaction.annotation.Transactional
    public Actividad cambiarEstatusActividad(String idActividad, EstatusCatalogo nuevoEstatus) {
        Actividad actividad = obtenerActividadPorId(idActividad);
        actividad.setEstatus(nuevoEstatus);
        Actividad guardada = actividadRepository.save(actividad);

        if (EstatusCatalogo.Inactiva.equals(nuevoEstatus)) {
            List<com.example.harmonyGymBack.model.RealizaActividad> inscripciones = 
                realizaActividadRepository.findByIdActividadAndEstatus(idActividad, com.example.harmonyGymBack.model.enums.EstatusInscripcion.Inscrito);
            for (com.example.harmonyGymBack.model.RealizaActividad ra : inscripciones) {
                ra.cancelarInscripcion("Clase cancelada por el gimnasio");
                realizaActividadRepository.save(ra);
            }
        }
        return guardada;
    }

    public Actividad desactivarActividad(String idActividad) {
        return cambiarEstatusActividad(idActividad, EstatusCatalogo.Inactiva);
    }

    public Actividad activarActividad(String idActividad) {
        return cambiarEstatusActividad(idActividad, EstatusCatalogo.Activa);
    }

    public void eliminarActividad(String idActividad) {
        desactivarActividad(idActividad);
    }

    public List<Actividad> buscarActividadesPorNombre(String nombre) {
        return actividadRepository.findByNombreActividadContainingIgnoreCase(nombre);
    }

    public boolean verificarCupoDisponible(String idActividad) {
        Actividad actividad = obtenerActividadPorId(idActividad);
        return EstatusCatalogo.Activa.equals(actividad.getEstatus());
    }

    public Long contarActividadesActivas() {
        return actividadRepository.countByEstatus(EstatusCatalogo.Activa);
    }

    public List<Actividad> obtenerActividadesPorLugar(String lugar) {
        return actividadRepository.findByLugarContainingIgnoreCase(lugar);
    }

    public boolean existeActividad(String idActividad) {
        return actividadRepository.existsById(idActividad);
    }

    public List<Actividad> obtenerActividadesPorInstructorYFecha(String folioInstructor, LocalDate fecha) {
        return actividadRepository.findByFolioInstructorAndFechaActividadAndEstatus(folioInstructor, fecha, EstatusCatalogo.Activa);
    }

    public boolean tieneConflictoHorario(String lugar, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        List<Actividad> conflictos = actividadRepository.findConflictingActivities(lugar, fecha, horaInicio, horaFin);
        return !conflictos.isEmpty();
    }

    public boolean tieneConflictoHorario(String lugar, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String excludeId) {
        List<Actividad> conflictos = actividadRepository.findConflictingActivitiesExcluding(lugar, fecha, horaInicio, horaFin, excludeId);
        return !conflictos.isEmpty();
    }
}
