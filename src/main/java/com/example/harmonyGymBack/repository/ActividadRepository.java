package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.ActividadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<ActividadEntity, String> {

    // Buscar actividades por nombre
    List<ActividadEntity> findByNombreActividadContainingIgnoreCase(String nombre);

    // Buscar actividades por estatus
    List<ActividadEntity> findByEstatus(String estatus);

    // Buscar actividades por lugar
    List<ActividadEntity> findByLugarContainingIgnoreCase(String lugar);

    // Buscar actividades activas ordenadas por fecha y hora
    List<ActividadEntity> findByEstatusOrderByFechaActividadAscHoraInicioAsc(String estatus);

    // Buscar actividades por instructor
    List<ActividadEntity> findByFolioInstructorAndEstatus(String folioInstructor, String estatus);

    // Buscar actividades por fecha
    List<ActividadEntity> findByFechaActividadAndEstatus(LocalDate fecha, String estatus);

    // Buscar actividades por instructor y fecha
    List<ActividadEntity> findByFolioInstructorAndFechaActividadAndEstatus(String folioInstructor, LocalDate fecha, String estatus);

    // Verificar conflicto de horarios considerando fecha
    @Query("SELECT a FROM Actividad a WHERE a.lugar = :lugar AND a.fechaActividad = :fecha AND a.estatus = 'Activa' " +
            "AND ((a.horaInicio BETWEEN :horaInicio AND :horaFin) OR " +
            "(a.horaFin BETWEEN :horaInicio AND :horaFin) OR " +
            "(:horaInicio BETWEEN a.horaInicio AND a.horaFin))")
    List<ActividadEntity> findConflictingActivities(@Param("lugar") String lugar,
                                                    @Param("fecha") LocalDate fecha,
                                                    @Param("horaInicio") LocalTime horaInicio,
                                                    @Param("horaFin") LocalTime horaFin);

    // Verificar conflicto de horarios excluyendo una actividad específica
    @Query("SELECT a FROM Actividad a WHERE a.lugar = :lugar AND a.fechaActividad = :fecha AND a.estatus = 'Activa' " +
            "AND a.idActividad != :excludeId " +
            "AND ((a.horaInicio BETWEEN :horaInicio AND :horaFin) OR " +
            "(a.horaFin BETWEEN :horaInicio AND :horaFin) OR " +
            "(:horaInicio BETWEEN a.horaInicio AND a.horaFin))")
    List<ActividadEntity> findConflictingActivitiesExcluding(@Param("lugar") String lugar,
                                                             @Param("fecha") LocalDate fecha,
                                                             @Param("horaInicio") LocalTime horaInicio,
                                                             @Param("horaFin") LocalTime horaFin,
                                                             @Param("excludeId") String excludeId);

    // Contar actividades activas
    Long countByEstatus(String estatus);
    // Buscar por estatus y lugar (case insensitive)
    List<ActividadEntity> findByEstatusAndLugarContainingIgnoreCase(String estatus, String lugar);

    // Verificar existencia
    boolean existsById(String idActividad);

    // Obtener actividades futuras
    List<ActividadEntity> findByFechaActividadGreaterThanEqualAndEstatusOrderByFechaActividadAscHoraInicioAsc(LocalDate fecha, String estatus);
}
