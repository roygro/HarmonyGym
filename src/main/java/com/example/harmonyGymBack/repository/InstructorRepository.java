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

    // Buscar instructores por estatus
    List<Instructor> findByEstatus(String estatus);

    // Buscar instructores por especialidad (case insensitive)
    List<Instructor> findByEspecialidadContainingIgnoreCase(String especialidad);

    // Buscar instructores activos ordenados por nombre
    List<Instructor> findByEstatusOrderByNombreAsc(String estatus);

    // Buscar por estatus y especialidad
    List<Instructor> findByEstatusAndEspecialidadContainingIgnoreCase(String estatus, String especialidad);

    // Verificar si existe un instructorEntity por folio
    boolean existsByFolioInstructor(String folioInstructor);

    // Buscar por nombre (case insensitive)
    List<Instructor> findByNombreContainingIgnoreCase(String nombre);

    // Buscar por folio
    Optional<Instructor> findByFolioInstructor(String folioInstructor);

    // Obtener instructorEntity con información extendida del usuario
    @Query(value = """
        SELECT i.*, u.Username, u.Estatus as usuario_estatus
        FROM INSTRUCTOR i
        LEFT JOIN USUARIO u ON i.Folio_Instructor = u.Id_Persona AND u.Tipo_Usuario = 'Instructor'
        WHERE i.Folio_Instructor = :folioInstructor
        """, nativeQuery = true)
    Optional<Object[]> findInstructorWithUsuario(@Param("folioInstructor") String folioInstructor);

    @Query(value = "SELECT i.folio_instructor FROM instructorEntity i ORDER BY i.folio_instructor DESC LIMIT 1", nativeQuery = true)
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
}