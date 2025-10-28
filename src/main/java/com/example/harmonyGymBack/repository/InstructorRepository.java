package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, String> {

    // ==================== MÉTODOS DE CONSULTA BÁSICOS ====================

    // Buscar instructores por estatus
    List<Instructor> findByEstatus(String estatus);

    // Buscar instructores por especialidad (case insensitive)
    List<Instructor> findByEspecialidadContainingIgnoreCase(String especialidad);

    // Buscar instructores activos ordenados por nombre
    List<Instructor> findByEstatusOrderByNombreAsc(String estatus);

    // Buscar por estatus y especialidad
    List<Instructor> findByEstatusAndEspecialidadContainingIgnoreCase(String estatus, String especialidad);

    // Verificar si existe un instructor por folio
    boolean existsByFolioInstructor(String folioInstructor);

    // Buscar por nombre (case insensitive)
    List<Instructor> findByNombreContainingIgnoreCase(String nombre);

    // Buscar por folio
    Optional<Instructor> findByFolioInstructor(String folioInstructor);

    // ==================== MÉTODOS DE EMAIL (NUEVOS) ====================

    // Buscar instructor por email exacto
    Optional<Instructor> findByEmail(String email);

    // Verificar si existe un instructor por email
    boolean existsByEmail(String email);

    // Buscar instructores por email (case insensitive, contiene)
    List<Instructor> findByEmailContainingIgnoreCase(String email);

    // Buscar por estatus y email (case insensitive)
    List<Instructor> findByEstatusAndEmailContainingIgnoreCase(String estatus, String email);

    // Buscar por nombre o email (case insensitive)
    List<Instructor> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String email);

    // ==================== CONSULTAS NATIVAS PERSONALIZADAS ====================

    // Obtener instructor con información extendida del usuario
    @Query(value = """
        SELECT i.*, u.Username, u.Estatus as usuario_estatus
        FROM INSTRUCTOR i
        LEFT JOIN USUARIO u ON i.Folio_Instructor = u.Id_Persona AND u.Tipo_Usuario = 'Instructor'
        WHERE i.Folio_Instructor = :folioInstructor
        """, nativeQuery = true)
    Optional<Object[]> findInstructorWithUsuario(@Param("folioInstructor") String folioInstructor);

    // Obtener el último folio de instructor
    @Query(value = "SELECT i.folio_instructor FROM instructor i ORDER BY i.folio_instructor DESC LIMIT 1", nativeQuery = true)
    String findUltimoFolioInstructor();

    // Obtener todos los instructores con información de usuario
    @Query(value = """
        SELECT i.*, u.Username, u.Estatus as usuario_estatus
        FROM INSTRUCTOR i
        LEFT JOIN USUARIO u ON i.Folio_Instructor = u.Id_Persona AND u.Tipo_Usuario = 'Instructor'
        WHERE (:estatus IS NULL OR i.Estatus = :estatus)
          AND (:especialidad IS NULL OR i.Especialidad ILIKE %:especialidad%)
        ORDER BY i.Fecha_Contratacion DESC
        """, nativeQuery = true)
    List<Object[]> findAllInstructoresWithUsuario(@Param("estatus") String estatus,
                                                  @Param("especialidad") String especialidad);

    // ==================== CONSULTAS NATIVAS CON EMAIL (NUEVAS) ====================

    // Obtener instructores con filtros incluyendo email
    @Query(value = """
        SELECT i.*, u.Username, u.Estatus as usuario_estatus
        FROM INSTRUCTOR i
        LEFT JOIN USUARIO u ON i.Folio_Instructor = u.Id_Persona AND u.Tipo_Usuario = 'Instructor'
        WHERE (:estatus IS NULL OR i.Estatus = :estatus)
          AND (:especialidad IS NULL OR i.Especialidad ILIKE %:especialidad%)
          AND (:email IS NULL OR i.Email ILIKE %:email%)
        ORDER BY i.Fecha_Contratacion DESC
        """, nativeQuery = true)
    List<Object[]> findAllInstructoresWithUsuarioFiltros(@Param("estatus") String estatus,
                                                         @Param("especialidad") String especialidad,
                                                         @Param("email") String email);

    // Buscar instructores por múltiples criterios
    @Query(value = """
        SELECT * FROM INSTRUCTOR i
        WHERE (:nombre IS NULL OR i.Nombre ILIKE %:nombre%)
          AND (:email IS NULL OR i.Email ILIKE %:email%)
          AND (:especialidad IS NULL OR i.Especialidad ILIKE %:especialidad%)
          AND (:estatus IS NULL OR i.Estatus = :estatus)
        ORDER BY i.Nombre ASC
        """, nativeQuery = true)
    List<Instructor> findInstructoresByMultipleCriterios(@Param("nombre") String nombre,
                                                         @Param("email") String email,
                                                         @Param("especialidad") String especialidad,
                                                         @Param("estatus") String estatus);

    // Contar instructores activos por especialidad
    @Query(value = """
        SELECT i.Especialidad, COUNT(*) as total
        FROM INSTRUCTOR i
        WHERE i.Estatus = 'Activo'
        GROUP BY i.Especialidad
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> countInstructoresActivosByEspecialidad();

    // Obtener instructores con email nulo o vacío (para migración)
    @Query(value = "SELECT * FROM INSTRUCTOR WHERE Email IS NULL OR Email = ''", nativeQuery = true)
    List<Instructor> findInstructoresSinEmail();

    // Verificar si existe email excluyendo un folio específico (para actualizaciones)
    @Query(value = """
        SELECT COUNT(*) > 0 
        FROM INSTRUCTOR 
        WHERE Email = :email AND Folio_Instructor != :folioInstructor
        """, nativeQuery = true)
    boolean existsByEmailAndFolioInstructorNot(@Param("email") String email,
                                               @Param("folioInstructor") String folioInstructor);

    // ==================== CONSULTAS DE ESTADÍSTICAS ====================

    // Contar total de instructores por estatus
    @Query(value = """
        SELECT Estatus, COUNT(*) as total 
        FROM INSTRUCTOR 
        GROUP BY Estatus
        """, nativeQuery = true)
    List<Object[]> countInstructoresByEstatus();

    // Obtener instructores recién contratados (últimos 30 días)
    @Query(value = """
        SELECT * FROM INSTRUCTOR 
        WHERE Fecha_Contratacion >= CURRENT_DATE - INTERVAL '30 days'
        ORDER BY Fecha_Contratacion DESC
        """, nativeQuery = true)
    List<Instructor> findInstructoresRecientes();

    // Buscar instructores por rango de fechas de contratación
    @Query(value = """
        SELECT * FROM INSTRUCTOR 
        WHERE Fecha_Contratacion BETWEEN :fechaInicio AND :fechaFin
        ORDER BY Fecha_Contratacion DESC
        """, nativeQuery = true)
    List<Instructor> findInstructoresByRangoFechas(@Param("fechaInicio") String fechaInicio,
                                                   @Param("fechaFin") String fechaFin);

    // ==================== CONSULTAS DE HORARIOS ====================

    // Buscar instructores activos con horario disponible en un rango específico
    @Query(value = """
        SELECT * FROM INSTRUCTOR 
        WHERE Estatus = 'Activo'
          AND Hora_Entrada <= :horaInicio
          AND Hora_Salida >= :horaFin
        ORDER BY Nombre ASC
        """, nativeQuery = true)
    List<Instructor> findInstructoresDisponiblesEnHorario(@Param("horaInicio") String horaInicio,
                                                          @Param("horaFin") String horaFin);

    // Obtener instructores por especialidad y horario disponible
    @Query(value = """
        SELECT * FROM INSTRUCTOR 
        WHERE Estatus = 'Activo'
          AND Especialidad ILIKE %:especialidad%
          AND Hora_Entrada <= :horaInicio
          AND Hora_Salida >= :horaFin
        ORDER BY Nombre ASC
        """, nativeQuery = true)
    List<Instructor> findInstructoresByEspecialidadYHorario(@Param("especialidad") String especialidad,
                                                            @Param("horaInicio") String horaInicio,
                                                            @Param("horaFin") String horaFin);
}