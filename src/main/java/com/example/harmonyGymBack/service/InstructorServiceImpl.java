package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Instructor;
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

            List<Instructor> todosInstructores = instructorRepository.findAll();

            if (todosInstructores.isEmpty()) {
                System.out.println("✅ No hay instructores, empezando con INS001");
                return "INS001";
            }

            String ultimoFolio = null;
            int maxNumero = 0;

            for (Instructor instructor : todosInstructores) {
                String folio = instructor.getFolioInstructor();
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
    public Instructor crearInstructor(String nombre, String app, String apm,
                                      String horaEntrada, String horaSalida,
                                      String especialidad, String fechaContratacion,
                                      String estatus) {
        System.out.println("🚀 Iniciando creación de instructorEntity...");

        // Generar folio
        String folioGenerado = generarFolioInstructor();

        // Crear objeto Instructor
        Instructor instructor = new Instructor();
        instructor.setFolioInstructor(folioGenerado);
        instructor.setNombre(nombre);
        instructor.setApp(app);
        instructor.setApm(apm);
        instructor.setEspecialidad(especialidad);
        instructor.setEstatus(estatus != null ? estatus : "Activo");

        // Convertir y validar horarios
        if (horaEntrada != null && !horaEntrada.trim().isEmpty()) {
            instructor.setHoraEntrada(LocalTime.parse(horaEntrada));
        }
        if (horaSalida != null && !horaSalida.trim().isEmpty()) {
            instructor.setHoraSalida(LocalTime.parse(horaSalida));
        }

        // Validar horarios
        if (instructor.getHoraEntrada() != null && instructor.getHoraSalida() != null) {
            if (instructor.getHoraEntrada().isAfter(instructor.getHoraSalida()) ||
                    instructor.getHoraEntrada().equals(instructor.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Fecha de contratación
        if (fechaContratacion != null && !fechaContratacion.trim().isEmpty()) {
            instructor.setFechaContratacion(LocalDate.parse(fechaContratacion));
        } else {
            instructor.setFechaContratacion(LocalDate.now());
        }

        // Guardar instructorEntity en base de datos
        Instructor instructorGuardado = instructorRepository.save(instructor);
        System.out.println("✅ Instructor creado exitosamente: " + folioGenerado);

        return instructorGuardado;
    }

    // ==================== ACTUALIZAR INSTRUCTOR ====================

    @Transactional
    public Instructor actualizarInstructor(String folioInstructor, String nombre, String app, String apm,
                                           String horaEntrada, String horaSalida, String especialidad,
                                           String fechaContratacion, String estatus) {
        System.out.println("✏️ Actualizando instructorEntity: " + folioInstructor);

        Instructor instructorExistente = obtenerInstructorPorId(folioInstructor);

        // Actualizar campos básicos
        if (nombre != null) instructorExistente.setNombre(nombre);
        if (app != null) instructorExistente.setApp(app);
        if (apm != null) instructorExistente.setApm(apm);
        if (especialidad != null) instructorExistente.setEspecialidad(especialidad);
        if (estatus != null) instructorExistente.setEstatus(estatus);

        // Actualizar horarios
        if (horaEntrada != null && !horaEntrada.trim().isEmpty()) {
            instructorExistente.setHoraEntrada(LocalTime.parse(horaEntrada));
        }
        if (horaSalida != null && !horaSalida.trim().isEmpty()) {
            instructorExistente.setHoraSalida(LocalTime.parse(horaSalida));
        }

        // Validar horarios
        if (instructorExistente.getHoraEntrada() != null && instructorExistente.getHoraSalida() != null) {
            if (instructorExistente.getHoraEntrada().isAfter(instructorExistente.getHoraSalida()) ||
                    instructorExistente.getHoraEntrada().equals(instructorExistente.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Actualizar fecha de contratación
        if (fechaContratacion != null && !fechaContratacion.trim().isEmpty()) {
            instructorExistente.setFechaContratacion(LocalDate.parse(fechaContratacion));
        }

        Instructor instructorActualizado = instructorRepository.save(instructorExistente);
        System.out.println("✅ Instructor actualizado: " + folioInstructor);

        return instructorActualizado;
    }

    // ==================== MÉTODOS ORIGINALES (para compatibilidad) ====================

    public Instructor crearInstructor(Instructor instructor) {
        System.out.println("🚀 Iniciando creación de instructorEntity...");

        String folioGenerado = generarFolioInstructor();
        instructor.setFolioInstructor(folioGenerado);

        System.out.println("📝 Folio asignado: " + folioGenerado);

        if (instructorRepository.existsByFolioInstructor(folioGenerado)) {
            throw new RuntimeException("Error: El folio generado " + folioGenerado + " ya existe");
        }

        // Validar horarios
        if (instructor.getHoraEntrada() != null && instructor.getHoraSalida() != null) {
            if (instructor.getHoraEntrada().isAfter(instructor.getHoraSalida()) ||
                    instructor.getHoraEntrada().equals(instructor.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        if (instructor.getFechaContratacion() == null) {
            instructor.setFechaContratacion(LocalDate.now());
        }

        if (instructor.getEstatus() == null) {
            instructor.setEstatus("Activo");
        }

        Instructor instructorGuardado = instructorRepository.save(instructor);
        System.out.println("✅ Instructor guardado exitosamente: " + instructorGuardado.getFolioInstructor());

        return instructorGuardado;
    }

    // ==================== CONSULTAS Y LISTADOS ====================

    public List<Instructor> obtenerTodosLosInstructores() {
        return instructorRepository.findAll();
    }

    public Instructor obtenerInstructorPorId(String folioInstructor) {
        Optional<Instructor> instructorEntity = instructorRepository.findByFolioInstructor(folioInstructor);
        return instructorEntity.orElseThrow(() -> new RuntimeException("Instructor no encontrado con folio: " + folioInstructor));
    }

    public List<Instructor> obtenerInstructoresFiltrados(String estatus, String especialidad) {
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
    public Instructor actualizarInstructor(String folioInstructor, Instructor instructorActualizado) {
        System.out.println("✏️ Actualizando instructorEntity: " + folioInstructor);

        Instructor instructorExistente = obtenerInstructorPorId(folioInstructor);

        // Validar horarios
        if (instructorActualizado.getHoraEntrada() != null && instructorActualizado.getHoraSalida() != null) {
            if (instructorActualizado.getHoraEntrada().isAfter(instructorActualizado.getHoraSalida()) ||
                    instructorActualizado.getHoraEntrada().equals(instructorActualizado.getHoraSalida())) {
                throw new RuntimeException("La hora de entrada debe ser anterior a la hora de salida");
            }
        }

        // Actualizar campos (NO actualizar folioInstructor)
        if (instructorActualizado.getNombre() != null) {
            instructorExistente.setNombre(instructorActualizado.getNombre());
        }
        if (instructorActualizado.getApp() != null) {
            instructorExistente.setApp(instructorActualizado.getApp());
        }
        if (instructorActualizado.getApm() != null) {
            instructorExistente.setApm(instructorActualizado.getApm());
        }
        if (instructorActualizado.getHoraEntrada() != null) {
            instructorExistente.setHoraEntrada(instructorActualizado.getHoraEntrada());
        }
        if (instructorActualizado.getHoraSalida() != null) {
            instructorExistente.setHoraSalida(instructorActualizado.getHoraSalida());
        }
        if (instructorActualizado.getEspecialidad() != null) {
            instructorExistente.setEspecialidad(instructorActualizado.getEspecialidad());
        }
        if (instructorActualizado.getFechaContratacion() != null) {
            instructorExistente.setFechaContratacion(instructorActualizado.getFechaContratacion());
        }
        if (instructorActualizado.getEstatus() != null) {
            instructorExistente.setEstatus(instructorActualizado.getEstatus());
        }

        Instructor instructorActualizadoDb = instructorRepository.save(instructorExistente);
        System.out.println("✅ Instructor actualizado: " + instructorActualizadoDb.getFolioInstructor());

        return instructorActualizadoDb;
    }

    // ==================== GESTIÓN DE ESTATUS ====================

    @Transactional
    public Instructor cambiarEstatusInstructor(String folioInstructor, String nuevoEstatus) {
        Instructor instructor = obtenerInstructorPorId(folioInstructor);
        instructor.setEstatus(nuevoEstatus);
        return instructorRepository.save(instructor);
    }

    public Instructor desactivarInstructor(String folioInstructor) {
        return cambiarEstatusInstructor(folioInstructor, "Inactivo");
    }

    public Instructor activarInstructor(String folioInstructor) {
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

    public List<Instructor> obtenerInstructoresActivos() {
        return instructorRepository.findByEstatusOrderByNombreAsc("Activo");
    }

    public List<Instructor> buscarInstructoresPorNombre(String nombre) {
        return instructorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Long contarInstructoresActivos() {
        return instructorRepository.findByEstatus("Activo").stream().count();
    }

    public List<Instructor> obtenerInstructoresPorEspecialidad(String especialidad) {
        return instructorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    // ==================== ELIMINAR INSTRUCTOR ====================

    @Transactional
    public void eliminarInstructorCompleto(String folioInstructor) {
        Instructor instructor = obtenerInstructorPorId(folioInstructor);

        // Eliminar de la base de datos
        instructorRepository.delete(instructor);
        System.out.println("✅ Instructor eliminado completamente: " + folioInstructor);
    }
}