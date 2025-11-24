package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Actividad;
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

            List<Actividad> todasActividades = actividadRepository.findAll();

            if (todasActividades.isEmpty()) {
                System.out.println("✅ No hay actividades, empezando con ACT001");
                return "ACT001";
            }

            String ultimoId = null;
            int maxNumero = 0;

            for (Actividad actividad : todasActividades) {
                String idActividad = actividad.getIdActividad();
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
    public Actividad crearActividad(Actividad actividad) {
        // Generar ID automáticamente
        String nuevoId = generarIdActividad();
        actividad.setIdActividad(nuevoId);

        System.out.println("✅ ID asignado a la nueva actividad: " + nuevoId);
        // ✅ VALIDAR Y ESTABLECER NIVEL POR DEFECTO
        if (actividad.getNivelDificultad() == null) {
            actividad.setNivelDificultad("principiante");
        }
        System.out.println("🎯 Nivel de dificultad asignado: " + actividad.getNivelDificultad());

        // Validar que no haya conflicto de horarios
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

        // Validar que la hora de fin sea después de la hora de inicio
        if (actividad.getHoraFin().isBefore(actividad.getHoraInicio()) ||
                actividad.getHoraFin().equals(actividad.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar que la fecha no sea en el pasado
        if (actividad.getFechaActividad().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de la actividad no puede ser en el pasado");
        }

        // Establecer estatus por defecto si no viene
        if (actividad.getEstatus() == null) {
            actividad.setEstatus("Activa");
        }

        return actividadRepository.save(actividad);
    }

    // Obtener todas las actividades
    public List<Actividad> obtenerTodasActividades() {
        return actividadRepository.findAll();
    }

    // Obtener actividades con filtros
    public List<Actividad> obtenerActividadesFiltradas(String estatus, String lugar) {
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
    public Actividad obtenerActividadPorId(String idActividad) {
        Optional<Actividad> actividad = actividadRepository.findById(idActividad);
        return actividad.orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + idActividad));
    }

    // Obtener actividades activas
    public List<Actividad> obtenerActividadesActivas() {
        return actividadRepository.findByEstatusOrderByFechaActividadAscHoraInicioAsc("Activa");
    }

    // Obtener actividades futuras
    public List<Actividad> obtenerActividadesFuturas() {
        return actividadRepository.findByFechaActividadGreaterThanEqualAndEstatusOrderByFechaActividadAscHoraInicioAsc(
                LocalDate.now(), "Activa");
    }

    // Obtener actividades por instructor
    public List<Actividad> obtenerActividadesPorInstructor(String folioInstructor) {
        return actividadRepository.findByFolioInstructorAndEstatus(folioInstructor, "Activa");
    }

    // Obtener actividades por fecha
    public List<Actividad> obtenerActividadesPorFecha(LocalDate fecha) {
        return actividadRepository.findByFechaActividadAndEstatus(fecha, "Activa");
    }

    // Actualizar actividad
    public Actividad actualizarActividad(String idActividad, Actividad actividadActualizada) {
        Actividad actividadExistente = obtenerActividadPorId(idActividad);

        // Actualizar campos (NO permitir cambiar el ID)
        if (actividadActualizada.getNombreActividad() != null) {
            actividadExistente.setNombreActividad(actividadActualizada.getNombreActividad());
        }
        if (actividadActualizada.getFechaActividad() != null) {
            actividadExistente.setFechaActividad(actividadActualizada.getFechaActividad());
        }
        if (actividadActualizada.getHoraInicio() != null) {
            actividadExistente.setHoraInicio(actividadActualizada.getHoraInicio());
        }
        if (actividadActualizada.getHoraFin() != null) {
            actividadExistente.setHoraFin(actividadActualizada.getHoraFin());
        }
        if (actividadActualizada.getDescripcion() != null) {
            actividadExistente.setDescripcion(actividadActualizada.getDescripcion());
        }
        if (actividadActualizada.getCupo() != null) {
            actividadExistente.setCupo(actividadActualizada.getCupo());
        }
        if (actividadActualizada.getLugar() != null) {
            actividadExistente.setLugar(actividadActualizada.getLugar());
        }
        if (actividadActualizada.getImagenUrl() != null) {
            actividadExistente.setImagenUrl(actividadActualizada.getImagenUrl());
        }
        if (actividadActualizada.getFolioInstructor() != null) {
            actividadExistente.setFolioInstructor(actividadActualizada.getFolioInstructor());
        }
        if (actividadActualizada.getEstatus() != null) {
            actividadExistente.setEstatus(actividadActualizada.getEstatus());
        }

        // ✅ ACTUALIZAR NIVEL DE DIFICULTAD
        if (actividadActualizada.getNivelDificultad() != null) {
            actividadExistente.setNivelDificultad(actividadActualizada.getNivelDificultad());
        }

        // Validar conflicto de horarios (excluyendo la actividad actual)
        List<Actividad> conflictos = actividadRepository.findConflictingActivitiesExcluding(
                actividadExistente.getLugar(),
                actividadExistente.getFechaActividad(),
                actividadExistente.getHoraInicio(),
                actividadExistente.getHoraFin(),
                idActividad
        );

        if (!conflictos.isEmpty()) {
            throw new RuntimeException("Conflicto de horario en el lugar: " + actividadExistente.getLugar() +
                    " para la fecha: " + actividadExistente.getFechaActividad());
        }

        // Validar que la hora de fin sea después de la hora de inicio
        if (actividadExistente.getHoraFin().isBefore(actividadExistente.getHoraInicio()) ||
                actividadExistente.getHoraFin().equals(actividadExistente.getHoraInicio())) {
            throw new RuntimeException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // Validar que la fecha no sea en el pasado
        if (actividadExistente.getFechaActividad().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha de la actividad no puede ser en el pasado");
        }

        return actividadRepository.save(actividadExistente);
    }

    // Cambiar estatus de actividad
    public Actividad cambiarEstatusActividad(String idActividad, String nuevoEstatus) {
        Actividad actividad = obtenerActividadPorId(idActividad);
        actividad.setEstatus(nuevoEstatus);
        return actividadRepository.save(actividad);
    }

    // Desactivar actividad
    public Actividad desactivarActividad(String idActividad) {
        return cambiarEstatusActividad(idActividad, "Inactiva");
    }

    // Activar actividad
    public Actividad activarActividad(String idActividad) {
        return cambiarEstatusActividad(idActividad, "Activa");
    }

    // Eliminar actividad (cambiar estatus a Inactiva)
    public void eliminarActividad(String idActividad) {
        desactivarActividad(idActividad);
    }

    // Buscar actividades por nombre
    public List<Actividad> buscarActividadesPorNombre(String nombre) {
        return actividadRepository.findByNombreActividadContainingIgnoreCase(nombre);
    }

    // Verificar disponibilidad de cupo
    public boolean verificarCupoDisponible(String idActividad) {
        Actividad actividad = obtenerActividadPorId(idActividad);
        return "Activa".equals(actividad.getEstatus());
    }

    // Obtener conteo de actividades activas
    public Long contarActividadesActivas() {
        return actividadRepository.countByEstatus("Activa");
    }

    // Obtener actividades por lugar
    public List<Actividad> obtenerActividadesPorLugar(String lugar) {
        return actividadRepository.findByLugarContainingIgnoreCase(lugar);
    }

    // Verificar si existe una actividad
    public boolean existeActividad(String idActividad) {
        return actividadRepository.existsById(idActividad);
    }

    // Obtener actividades por instructor y fecha
    public List<Actividad> obtenerActividadesPorInstructorYFecha(String folioInstructor, LocalDate fecha) {
        return actividadRepository.findByFolioInstructorAndFechaActividadAndEstatus(folioInstructor, fecha, "Activa");
    }

    // Verificar conflicto de horarios para una nueva actividad
    public boolean tieneConflictoHorario(String lugar, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        List<Actividad> conflictos = actividadRepository.findConflictingActivities(lugar, fecha, horaInicio, horaFin);
        return !conflictos.isEmpty();
    }

    // Verificar conflicto de horarios para actualización (excluyendo una actividad)
    public boolean tieneConflictoHorario(String lugar, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String excludeId) {
        List<Actividad> conflictos = actividadRepository.findConflictingActivitiesExcluding(lugar, fecha, horaInicio, horaFin, excludeId);
        return !conflictos.isEmpty();
    }
}