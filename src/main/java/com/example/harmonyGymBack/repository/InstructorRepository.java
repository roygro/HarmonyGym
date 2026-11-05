package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Instructor;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, String> {

    List<Instructor> findByEstatus(EstatusPersona estatus);

    List<Instructor> findByEspecialidadContainingIgnoreCase(String especialidad);

    List<Instructor> findByEstatusOrderByNombreAsc(EstatusPersona estatus);

    List<Instructor> findByEstatusAndEspecialidadContainingIgnoreCase(EstatusPersona estatus, String especialidad);

    boolean existsByFolioInstructor(String folioInstructor);

    List<Instructor> findByNombreContainingIgnoreCase(String nombre);

    Optional<Instructor> findByFolioInstructor(String folioInstructor);

    Optional<Instructor> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Instructor> findByEmailContainingIgnoreCase(String email);

    List<Instructor> findByEstatusAndEmailContainingIgnoreCase(EstatusPersona estatus, String email);

    List<Instructor> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String email);

    @Query(value = """
        SELECT i.*, u.Username, u.Estatus as usuario_estatus
        FROM INSTRUCTOR i
        LEFT JOIN USUARIO u ON i.Folio_Instructor = u.Id_Persona AND u.Tipo_Usuario = 'Instructor'
        WHERE i.Folio_Instructor = :folioInstructor
        """, nativeQuery = true)
    Optional<Object[]> findInstructorWithUsuario(@Param("folioInstructor") String folioInstructor);

    @Query(value = "SELECT i.folio_instructor FROM instructor i ORDER BY i.folio_instructor DESC LIMIT 1", nativeQuery = true)
    String findUltimoFolioInstructor();

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

    @Query("SELECT i.especialidad, COUNT(i) FROM Instructor i WHERE i.estatus = com.example.harmonyGymBack.model.enums.EstatusPersona.Activo GROUP BY i.especialidad")
    List<Object[]> countInstructoresActivosByEspecialidad();

    @Query(value = "SELECT * FROM INSTRUCTOR WHERE Email IS NULL OR Email = ''", nativeQuery = true)
    List<Instructor> findInstructoresSinEmail();

    @Query(value = """
        SELECT COUNT(*) > 0 
        FROM INSTRUCTOR 
        WHERE Email = :email AND Folio_Instructor != :folioInstructor
        """, nativeQuery = true)
    boolean existsByEmailAndFolioInstructorNot(@Param("email") String email,
                                               @Param("folioInstructor") String folioInstructor);

    @Query(value = """
        SELECT Estatus, COUNT(*) as total 
        FROM INSTRUCTOR 
        GROUP BY Estatus
        """, nativeQuery = true)
    List<Object[]> countInstructoresByEstatus();

    @Query(value = """
        SELECT * FROM INSTRUCTOR 
        WHERE Fecha_Contratacion >= CURRENT_DATE - INTERVAL '30 days'
        ORDER BY Fecha_Contratacion DESC
        """, nativeQuery = true)
    List<Instructor> findInstructoresRecientes();

    @Query(value = """
        SELECT * FROM INSTRUCTOR 
        WHERE Fecha_Contratacion BETWEEN CAST(:fechaInicio AS date) AND CAST(:fechaFin AS date)
        ORDER BY Fecha_Contratacion DESC
        """, nativeQuery = true)
    List<Instructor> findInstructoresByRangoFechas(@Param("fechaInicio") String fechaInicio,
                                                   @Param("fechaFin") String fechaFin);

    @Query("SELECT i FROM Instructor i WHERE i.estatus = com.example.harmonyGymBack.model.enums.EstatusPersona.Activo AND i.horaEntrada <= :horaInicio AND i.horaSalida >= :horaFin ORDER BY i.nombre ASC")
    List<Instructor> findInstructoresDisponiblesEnHorario(@Param("horaInicio") java.time.LocalTime horaInicio,
                                                          @Param("horaFin") java.time.LocalTime horaFin);

    @Query("SELECT i FROM Instructor i WHERE i.estatus = com.example.harmonyGymBack.model.enums.EstatusPersona.Activo AND LOWER(i.especialidad) LIKE LOWER(CONCAT('%', :especialidad, '%')) AND i.horaEntrada <= :horaInicio AND i.horaSalida >= :horaFin ORDER BY i.nombre ASC")
    List<Instructor> findInstructoresByEspecialidadYHorario(@Param("especialidad") String especialidad,
                                                            @Param("horaInicio") java.time.LocalTime horaInicio,
                                                            @Param("horaFin") java.time.LocalTime horaFin);

    long countByEstatus(EstatusPersona estatus);
}
