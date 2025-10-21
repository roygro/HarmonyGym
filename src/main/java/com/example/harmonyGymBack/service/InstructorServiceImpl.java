package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.InstructorEntity;
import com.example.harmonyGymBack.repository.InstructorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InstructorServiceImpl {

    @Autowired
    private InstructorRepository instructorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== GENERACIÓN AUTOMÁTICA DE FOLIO ====================

    private String generarFolioInstructor() {
        try {
            System.out.println("🔍 Buscando último folio en la base de datos...");

            List<InstructorEntity> todosInstructores = instructorRepository.findAll();

            if (todosInstructores.isEmpty()) {
                System.out.println("✅ No hay instructores, empezando con INS001");
                return "INS001";
            }

            String ultimoFolio = null;
            int maxNumero = 0;

            for (InstructorEntity instructorEntity : todosInstructores) {
                String folio = instructorEntity.getFolioInstructor();
                if (folio != null && folio.startsWith("INS")) {
                    try {
                        String numeroStr = folio.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                            ultimoFolio = folio;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ Folio con formato inválido: " + folio);
                    }
                }
            }

            if (ultimoFolio == null) {
                System.out.println("✅ No se encontraron folios válidos, empezando con INS001");
                return "INS001";
            }

            int nuevoNumero = maxNumero + 1;
            String nuevoFolio = String.format("INS%03d", nuevoNumero);

            System.out.println("📊 Último folio encontrado: " + ultimoFolio);
            System.out.println("🎯 Nuevo folio generado: " + nuevoFolio);

            return nuevoFolio;

        } catch (Exception e) {
            System.err.println("❌ Error crítico al generar folio: " + e.getMessage());
            e.printStackTrace();

            long totalInstructores = instructorRepository.count();
            String folioFallback = String.format("INS%03d", totalInstructores + 1);
            System.out.println("🔄 Usando fallback: " + folioFallback);
            return folioFallback;
        }
    }

    // ==================== CREAR NUEVO INSTRUCTOR ====================

    @Transactional
    public InstructorEntity crearInstructor(String nombre, String app, String apm,
                                            String horaEntrada, String horaSalida,
                                            String especialidad, String fechaContratacion,
                                            String estatus) {
        System.out.println("🚀 Iniciando creación de instructorEntity...");

        // Generar folio
        String folioGenerado = generarFolioInstructor();

        // Crear objeto Instructor
        InstructorEntity instructorEntity = new InstructorEntity();
        instructorEntity.setFolioInstructor(folioGenerado);
        instructorEntity.setNombre(nombre);
        instructorEntity.setApp(app);
        instructorEntity.setApm(apm);
        instructorEntity.setEspecialidad(especialidad);
        instructorEntity.setEstatus(estatus != null ? estatus : "Activo");

        // Convertir y validar horarios
        if (horaEntrada != null && !horaEntrada.trim().isEmpty()) {
            instructorEntity.setHoraEntrada(LocalTime.parse(horaEntrada));
        }
        if (horaSalida != null && !horaSalida.trim().isEmpty()) {
            instructorEntity.setHoraSalida(LocalTime.parse(horaSalida));
        }

        // Validar horarios
        if (instructorEntity.getHoraEntrada() != null && instructorEntity.getHoraSalida() != null) {
            if (instructorEntity.getHoraEntrada().isAfter(instructorEntity.getHoraSalida()) ||
                    instructorEntity.getHoraEntrada().equals(instructorEntity.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Fecha de contratación
        if (fechaContratacion != null && !fechaContratacion.trim().isEmpty()) {
            instructorEntity.setFechaContratacion(LocalDate.parse(fechaContratacion));
        } else {
            instructorEntity.setFechaContratacion(LocalDate.now());
        }

        // Guardar instructorEntity en base de datos
        InstructorEntity instructorEntityGuardado = instructorRepository.save(instructorEntity);
        System.out.println("✅ Instructor creado exitosamente: " + folioGenerado);

        return instructorEntityGuardado;
    }

    // ==================== ACTUALIZAR INSTRUCTOR ====================

    @Transactional
    public InstructorEntity actualizarInstructor(String folioInstructor, String nombre, String app, String apm,
                                                 String horaEntrada, String horaSalida, String especialidad,
                                                 String fechaContratacion, String estatus) {
        System.out.println("✏️ Actualizando instructorEntity: " + folioInstructor);

        InstructorEntity instructorEntityExistente = obtenerInstructorPorId(folioInstructor);

        // Actualizar campos básicos
        if (nombre != null) instructorEntityExistente.setNombre(nombre);
        if (app != null) instructorEntityExistente.setApp(app);
        if (apm != null) instructorEntityExistente.setApm(apm);
        if (especialidad != null) instructorEntityExistente.setEspecialidad(especialidad);
        if (estatus != null) instructorEntityExistente.setEstatus(estatus);

        // Actualizar horarios
        if (horaEntrada != null && !horaEntrada.trim().isEmpty()) {
            instructorEntityExistente.setHoraEntrada(LocalTime.parse(horaEntrada));
        }
        if (horaSalida != null && !horaSalida.trim().isEmpty()) {
            instructorEntityExistente.setHoraSalida(LocalTime.parse(horaSalida));
        }

        // Validar horarios
        if (instructorEntityExistente.getHoraEntrada() != null && instructorEntityExistente.getHoraSalida() != null) {
            if (instructorEntityExistente.getHoraEntrada().isAfter(instructorEntityExistente.getHoraSalida()) ||
                    instructorEntityExistente.getHoraEntrada().equals(instructorEntityExistente.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Actualizar fecha de contratación
        if (fechaContratacion != null && !fechaContratacion.trim().isEmpty()) {
            instructorEntityExistente.setFechaContratacion(LocalDate.parse(fechaContratacion));
        }

        InstructorEntity instructorEntityActualizado = instructorRepository.save(instructorEntityExistente);
        System.out.println("✅ Instructor actualizado: " + folioInstructor);

        return instructorEntityActualizado;
    }

    // ==================== MÉTODOS ORIGINALES (para compatibilidad) ====================

    public InstructorEntity crearInstructor(InstructorEntity instructorEntity) {
        System.out.println("🚀 Iniciando creación de instructorEntity...");

        String folioGenerado = generarFolioInstructor();
        instructorEntity.setFolioInstructor(folioGenerado);

        System.out.println("📝 Folio asignado: " + folioGenerado);

        if (instructorRepository.existsByFolioInstructor(folioGenerado)) {
            throw new RuntimeException("Error: El folio generado " + folioGenerado + " ya existe");
        }

        // Validar horarios
        if (instructorEntity.getHoraEntrada() != null && instructorEntity.getHoraSalida() != null) {
            if (instructorEntity.getHoraEntrada().isAfter(instructorEntity.getHoraSalida()) ||
                    instructorEntity.getHoraEntrada().equals(instructorEntity.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        if (instructorEntity.getFechaContratacion() == null) {
            instructorEntity.setFechaContratacion(LocalDate.now());
        }

        if (instructorEntity.getEstatus() == null) {
            instructorEntity.setEstatus("Activo");
        }

        InstructorEntity instructorEntityGuardado = instructorRepository.save(instructorEntity);
        System.out.println("✅ Instructor guardado exitosamente: " + instructorEntityGuardado.getFolioInstructor());

        return instructorEntityGuardado;
    }

    // ==================== CONSULTAS Y LISTADOS ====================

    public List<InstructorEntity> obtenerTodosLosInstructores() {
        return instructorRepository.findAll();
    }

    public InstructorEntity obtenerInstructorPorId(String folioInstructor) {
        Optional<InstructorEntity> instructorEntity = instructorRepository.findByFolioInstructor(folioInstructor);
        return instructorEntity.orElseThrow(() -> new RuntimeException("Instructor no encontrado con folio: " + folioInstructor));
    }

    public List<InstructorEntity> obtenerInstructoresFiltrados(String estatus, String especialidad) {
        if (estatus != null && especialidad != null) {
            return instructorRepository.findByEstatusAndEspecialidadContainingIgnoreCase(estatus, especialidad);
        } else if (estatus != null) {
            return instructorRepository.findByEstatus(estatus);
        } else if (especialidad != null) {
            return instructorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
        } else {
            return instructorRepository.findAll();
        }
    }

    // ==================== ACTUALIZAR INSTRUCTOR (método original) ====================

    @Transactional
    public InstructorEntity actualizarInstructor(String folioInstructor, InstructorEntity instructorEntityActualizado) {
        System.out.println("✏️ Actualizando instructorEntity: " + folioInstructor);

        InstructorEntity instructorEntityExistente = obtenerInstructorPorId(folioInstructor);

        // Validar horarios
        if (instructorEntityActualizado.getHoraEntrada() != null && instructorEntityActualizado.getHoraSalida() != null) {
            if (instructorEntityActualizado.getHoraEntrada().isAfter(instructorEntityActualizado.getHoraSalida()) ||
                    instructorEntityActualizado.getHoraEntrada().equals(instructorEntityActualizado.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Actualizar campos (NO actualizar folioInstructor)
        if (instructorEntityActualizado.getNombre() != null) {
            instructorEntityExistente.setNombre(instructorEntityActualizado.getNombre());
        }
        if (instructorEntityActualizado.getApp() != null) {
            instructorEntityExistente.setApp(instructorEntityActualizado.getApp());
        }
        if (instructorEntityActualizado.getApm() != null) {
            instructorEntityExistente.setApm(instructorEntityActualizado.getApm());
        }
        if (instructorEntityActualizado.getHoraEntrada() != null) {
            instructorEntityExistente.setHoraEntrada(instructorEntityActualizado.getHoraEntrada());
        }
        if (instructorEntityActualizado.getHoraSalida() != null) {
            instructorEntityExistente.setHoraSalida(instructorEntityActualizado.getHoraSalida());
        }
        if (instructorEntityActualizado.getEspecialidad() != null) {
            instructorEntityExistente.setEspecialidad(instructorEntityActualizado.getEspecialidad());
        }
        if (instructorEntityActualizado.getFechaContratacion() != null) {
            instructorEntityExistente.setFechaContratacion(instructorEntityActualizado.getFechaContratacion());
        }
        if (instructorEntityActualizado.getEstatus() != null) {
            instructorEntityExistente.setEstatus(instructorEntityActualizado.getEstatus());
        }

        InstructorEntity instructorEntityActualizadoDb = instructorRepository.save(instructorEntityExistente);
        System.out.println("✅ Instructor actualizado: " + instructorEntityActualizadoDb.getFolioInstructor());

        return instructorEntityActualizadoDb;
    }

    // ==================== GESTIÓN DE ESTATUS ====================

    @Transactional
    public InstructorEntity cambiarEstatusInstructor(String folioInstructor, String nuevoEstatus) {
        InstructorEntity instructorEntity = obtenerInstructorPorId(folioInstructor);
        instructorEntity.setEstatus(nuevoEstatus);
        return instructorRepository.save(instructorEntity);
    }

    public InstructorEntity desactivarInstructor(String folioInstructor) {
        return cambiarEstatusInstructor(folioInstructor, "Inactivo");
    }

    public InstructorEntity activarInstructor(String folioInstructor) {
        return cambiarEstatusInstructor(folioInstructor, "Activo");
    }

    public void eliminarInstructor(String folioInstructor) {
        desactivarInstructor(folioInstructor);
    }

    // ==================== ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasInstructor(String folioInstructor) {
        // Verificar que el instructorEntity existe
        if (!instructorRepository.existsByFolioInstructor(folioInstructor)) {
            throw new RuntimeException("Instructor no encontrado con folio: " + folioInstructor);
        }

        String query = """
            SELECT 
                COUNT(DISTINCT c.Id_Actividad) as total_actividades,
                COUNT(DISTINCT a.Folio_Rutina) as total_rutinas_asignadas,
                COUNT(DISTINCT ra.Folio_Cliente) as total_clientes_actividades,
                COUNT(DISTINCT rr.Folio_Cliente) as total_clientes_rutinas,
                COALESCE(AVG(ra.Calificacion), 0) as promedio_calificacion_actividades,
                COALESCE(AVG(rr.Calificacion), 0) as promedio_calificacion_rutinas
            FROM INSTRUCTOR i
            LEFT JOIN CREA c ON i.Folio_Instructor = c.Folio_Instructor
            LEFT JOIN ASIGNA a ON i.Folio_Instructor = a.Folio_Instructor
            LEFT JOIN ACTIVIDAD act ON c.Id_Actividad = act.Id_Actividad
            LEFT JOIN REALIZA_ACTIVIDAD ra ON act.Id_Actividad = ra.Id_Actividad
            LEFT JOIN REALIZA_RUTINA rr ON a.Folio_Rutina = rr.Folio_Rutina
            WHERE i.Folio_Instructor = :folioInstructor
            GROUP BY i.Folio_Instructor
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(query)
                .setParameter("folioInstructor", folioInstructor)
                .getResultList();

        Map<String, Object> estadisticas = new HashMap<>();

        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            estadisticas.put("totalActividades", result[0]);
            estadisticas.put("totalRutinasAsignadas", result[1]);
            estadisticas.put("totalClientesActividades", result[2]);
            estadisticas.put("totalClientesRutinas", result[3]);
            estadisticas.put("promedioCalificacionActividades", result[4]);
            estadisticas.put("promedioCalificacionRutinas", result[5]);
        } else {
            // Valores por defecto si no hay estadísticas
            estadisticas.put("totalActividades", 0);
            estadisticas.put("totalRutinasAsignadas", 0);
            estadisticas.put("totalClientesActividades", 0);
            estadisticas.put("totalClientesRutinas", 0);
            estadisticas.put("promedioCalificacionActividades", 0.0);
            estadisticas.put("promedioCalificacionRutinas", 0.0);
        }

        return estadisticas;
    }

    // ==================== CONSULTAS ADICIONALES ====================

    public boolean existeInstructor(String folioInstructor) {
        return instructorRepository.existsByFolioInstructor(folioInstructor);
    }

    public List<InstructorEntity> obtenerInstructoresActivos() {
        return instructorRepository.findByEstatusOrderByNombreAsc("Activo");
    }

    public List<InstructorEntity> buscarInstructoresPorNombre(String nombre) {
        return instructorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Long contarInstructoresActivos() {
        return instructorRepository.findByEstatus("Activo").stream().count();
    }

    public List<InstructorEntity> obtenerInstructoresPorEspecialidad(String especialidad) {
        return instructorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    // ==================== ELIMINAR INSTRUCTOR ====================

    @Transactional
    public void eliminarInstructorCompleto(String folioInstructor) {
        InstructorEntity instructorEntity = obtenerInstructorPorId(folioInstructor);

        // Eliminar de la base de datos
        instructorRepository.delete(instructorEntity);
        System.out.println("✅ Instructor eliminado completamente: " + folioInstructor);
    }
}