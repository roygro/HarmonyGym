package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.ActividadEntity;
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
    private ActividadRepository actividadRepository;

    // ==================== GENERACIÓN AUTOMÁTICA DE ID_ACTIVIDAD ====================

    private String generarIdActividad() {
        try {
            System.out.println("🔍 Buscando último ID de actividad en la base de datos...");

            List<ActividadEntity> todasActividades = actividadRepository.findAll();

            if (todasActividades.isEmpty()) {
                System.out.println("✅ No hay actividades, empezando con ACT001");
                return "ACT001";
            }

            String ultimoId = null;
            int maxNumero = 0;

            for (ActividadEntity actividadEntity : todasActividades) {
                String idActividad = actividadEntity.getIdActividad();
                if (idActividad != null && idActividad.startsWith("ACT")) {
                    try {
                        String numeroStr = idActividad.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                            ultimoId = idActividad;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ ID de actividad con formato inválido: " + idActividad);
                    }
                }
            }

            if (ultimoId == null) {
                System.out.println("✅ No se encontraron IDs válidos, empezando con ACT001");
                return "ACT001";
            }

            int nuevoNumero = maxNumero + 1;
            String nuevoId = String.format("ACT%03d", nuevoNumero);

            System.out.println("📊 Último ID encontrado: " + ultimoId);
            System.out.println("🎯 Nuevo ID generado: " + nuevoId);

            return nuevoId;

        } catch (Exception e) {
            System.err.println("❌ Error crítico al generar ID de actividad: " + e.getMessage());
            e.printStackTrace();

            long totalActividades = actividadRepository.count();
            String idFallback = String.format("ACT%03d", totalActividades + 1);
            System.out.println("🔄 Usando fallback: " + idFallback);
            return idFallback;
        }
    }

    // Crear nueva actividad (MODIFICADO para incluir generación automática de ID)
    public ActividadEntity crearActividad(ActividadEntity actividadEntity) {
        // Generar ID automáticamente
        String nuevoId = generarIdActividad();
        actividadEntity.setIdActividad(nuevoId);

        System.out.println("✅ ID asignado a la nueva actividad: " + nuevoId);

        // Validar que no haya conflicto de horarios
        List<ActividadEntity> conflictos = actividadRepository.findConflictingActivities(
                actividadEntity.getLugar(),
                actividadEntity.getFechaActividad(),
                actividadEntity.getHoraInicio(),
                actividadEntity.getHoraFin()
        );

        if (!conflictos.isEmpty()) {
            throw new RuntimeException("Conflicto de horario en el lugar: " + actividadEntity.getLugar() +
                    " para la fecha: " + actividadEntity.getFechaActividad());
        }

        // Validar que la hora de fin sea después de la hora de inicio
        if (actividadEntity.getHoraFin().isBefore(actividadEntity.getHoraInicio()) ||
                actividadEntity.getHoraFin().equals(actividadEntity.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar que la fecha no sea en el pasado
        if (actividadEntity.getFechaActividad().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de la actividad no puede ser en el pasado");
        }

        // Establecer estatus por defecto si no viene
        if (actividadEntity.getEstatus() == null) {
            actividadEntity.setEstatus("Activa");
        }

        return actividadRepository.save(actividadEntity);
    }

    // Obtener todas las actividades
    public List<ActividadEntity> obtenerTodasActividades() {
        return actividadRepository.findAll();
    }

    // Obtener actividades con filtros
    public List<ActividadEntity> obtenerActividadesFiltradas(String estatus, String lugar) {
        if (estatus != null && lugar != null) {
            return actividadRepository.findByEstatusAndLugarContainingIgnoreCase(estatus, lugar);
        } else if (estatus != null) {
            return actividadRepository.findByEstatus(estatus);
        } else if (lugar != null) {
            return actividadRepository.findByLugarContainingIgnoreCase(lugar);
        } else {
            return actividadRepository.findAll();
        }
    }

    // Obtener actividad por ID
    public ActividadEntity obtenerActividadPorId(String idActividad) {
        Optional<ActividadEntity> actividad = actividadRepository.findById(idActividad);
        return actividad.orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + idActividad));
    }

    // Obtener actividades activas
    public List<ActividadEntity> obtenerActividadesActivas() {
        return actividadRepository.findByEstatusOrderByFechaActividadAscHoraInicioAsc("Activa");
    }

    // Obtener actividades futuras
    public List<ActividadEntity> obtenerActividadesFuturas() {
        return actividadRepository.findByFechaActividadGreaterThanEqualAndEstatusOrderByFechaActividadAscHoraInicioAsc(
                LocalDate.now(), "Activa");
    }

    // Obtener actividades por instructor
    public List<ActividadEntity> obtenerActividadesPorInstructor(String folioInstructor) {
        return actividadRepository.findByFolioInstructorAndEstatus(folioInstructor, "Activa");
    }

    // Obtener actividades por fecha
    public List<ActividadEntity> obtenerActividadesPorFecha(LocalDate fecha) {
        return actividadRepository.findByFechaActividadAndEstatus(fecha, "Activa");
    }

    // Actualizar actividad
    public ActividadEntity actualizarActividad(String idActividad, ActividadEntity actividadEntityActualizada) {
        ActividadEntity actividadEntityExistente = obtenerActividadPorId(idActividad);

        // Actualizar campos (NO permitir cambiar el ID)
        if (actividadEntityActualizada.getNombreActividad() != null) {
            actividadEntityExistente.setNombreActividad(actividadEntityActualizada.getNombreActividad());
        }
        if (actividadEntityActualizada.getFechaActividad() != null) {
            actividadEntityExistente.setFechaActividad(actividadEntityActualizada.getFechaActividad());
        }
        if (actividadEntityActualizada.getHoraInicio() != null) {
            actividadEntityExistente.setHoraInicio(actividadEntityActualizada.getHoraInicio());
        }
        if (actividadEntityActualizada.getHoraFin() != null) {
            actividadEntityExistente.setHoraFin(actividadEntityActualizada.getHoraFin());
        }
        if (actividadEntityActualizada.getDescripcion() != null) {
            actividadEntityExistente.setDescripcion(actividadEntityActualizada.getDescripcion());
        }
        if (actividadEntityActualizada.getCupo() != null) {
            actividadEntityExistente.setCupo(actividadEntityActualizada.getCupo());
        }
        if (actividadEntityActualizada.getLugar() != null) {
            actividadEntityExistente.setLugar(actividadEntityActualizada.getLugar());
        }
        if (actividadEntityActualizada.getImagenUrl() != null) {
            actividadEntityExistente.setImagenUrl(actividadEntityActualizada.getImagenUrl());
        }
        if (actividadEntityActualizada.getFolioInstructor() != null) {
            actividadEntityExistente.setFolioInstructor(actividadEntityActualizada.getFolioInstructor());
        }
        if (actividadEntityActualizada.getEstatus() != null) {
            actividadEntityExistente.setEstatus(actividadEntityActualizada.getEstatus());
        }

        // Validar conflicto de horarios (excluyendo la actividad actual)
        List<ActividadEntity> conflictos = actividadRepository.findConflictingActivitiesExcluding(
                actividadEntityExistente.getLugar(),
                actividadEntityExistente.getFechaActividad(),
                actividadEntityExistente.getHoraInicio(),
                actividadEntityExistente.getHoraFin(),
                idActividad
        );

        if (!conflictos.isEmpty()) {
            throw new RuntimeException("Conflicto de horario en el lugar: " + actividadEntityExistente.getLugar() +
                    " para la fecha: " + actividadEntityExistente.getFechaActividad());
        }

        // Validar que la hora de fin sea después de la hora de inicio
        if (actividadEntityExistente.getHoraFin().isBefore(actividadEntityExistente.getHoraInicio()) ||
                actividadEntityExistente.getHoraFin().equals(actividadEntityExistente.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar que la fecha no sea en el pasado
        if (actividadEntityExistente.getFechaActividad().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de la actividad no puede ser en el pasado");
        }

        return actividadRepository.save(actividadEntityExistente);
    }

    // Cambiar estatus de actividad
    public ActividadEntity cambiarEstatusActividad(String idActividad, String nuevoEstatus) {
        ActividadEntity actividadEntity = obtenerActividadPorId(idActividad);
        actividadEntity.setEstatus(nuevoEstatus);
        return actividadRepository.save(actividadEntity);
    }

    // Desactivar actividad
    public ActividadEntity desactivarActividad(String idActividad) {
        return cambiarEstatusActividad(idActividad, "Inactiva");
    }

    // Activar actividad
    public ActividadEntity activarActividad(String idActividad) {
        return cambiarEstatusActividad(idActividad, "Activa");
    }

    // Eliminar actividad (cambiar estatus a Inactiva)
    public void eliminarActividad(String idActividad) {
        desactivarActividad(idActividad);
    }

    // Buscar actividades por nombre
    public List<ActividadEntity> buscarActividadesPorNombre(String nombre) {
        return actividadRepository.findByNombreActividadContainingIgnoreCase(nombre);
    }

    // Verificar disponibilidad de cupo
    public boolean verificarCupoDisponible(String idActividad) {
        ActividadEntity actividadEntity = obtenerActividadPorId(idActividad);
        return "Activa".equals(actividadEntity.getEstatus());
    }

    // Obtener conteo de actividades activas
    public Long contarActividadesActivas() {
        return actividadRepository.countByEstatus("Activa");
    }

    // Obtener actividades por lugar
    public List<ActividadEntity> obtenerActividadesPorLugar(String lugar) {
        return actividadRepository.findByLugarContainingIgnoreCase(lugar);
    }

    // Verificar si existe una actividad
    public boolean existeActividad(String idActividad) {
        return actividadRepository.existsById(idActividad);
    }

    // Obtener actividades por instructor y fecha
    public List<ActividadEntity> obtenerActividadesPorInstructorYFecha(String folioInstructor, LocalDate fecha) {
        return actividadRepository.findByFolioInstructorAndFechaActividadAndEstatus(folioInstructor, fecha, "Activa");
    }

    // Verificar conflicto de horarios para una nueva actividad
    public boolean tieneConflictoHorario(String lugar, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        List<ActividadEntity> conflictos = actividadRepository.findConflictingActivities(lugar, fecha, horaInicio, horaFin);
        return !conflictos.isEmpty();
    }

    // Verificar conflicto de horarios para actualización (excluyendo una actividad)
    public boolean tieneConflictoHorario(String lugar, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String excludeId) {
        List<ActividadEntity> conflictos = actividadRepository.findConflictingActivitiesExcluding(lugar, fecha, horaInicio, horaFin, excludeId);
        return !conflictos.isEmpty();
    }
}