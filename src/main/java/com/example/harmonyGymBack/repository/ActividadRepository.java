package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Actividad;
import com.example.harmonyGymBack.model.enums.EstatusCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, String> {

    List<Actividad> findByNombreActividadContainingIgnoreCase(String nombre);

    List<Actividad> findByEstatus(EstatusCatalogo estatus);

    List<Actividad> findByLugarContainingIgnoreCase(String lugar);

    List<Actividad> findByEstatusOrderByFechaActividadAscHoraInicioAsc(EstatusCatalogo estatus);

    List<Actividad> findByFolioInstructorAndEstatus(String folioInstructor, EstatusCatalogo estatus);

    List<Actividad> findByFechaActividadAndEstatus(LocalDate fecha, EstatusCatalogo estatus);

    List<Actividad> findByFolioInstructorAndFechaActividadAndEstatus(String folioInstructor, LocalDate fecha, EstatusCatalogo estatus);

    @Query("SELECT a FROM Actividad a WHERE a.lugar = :lugar AND a.fechaActividad = :fecha AND a.estatus = com.example.harmonyGymBack.model.enums.EstatusCatalogo.Activa " +
            "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
    List<Actividad> findConflictingActivities(@Param("lugar") String lugar,
                                              @Param("fecha") LocalDate fecha,
                                              @Param("horaInicio") LocalTime horaInicio,
                                              @Param("horaFin") LocalTime horaFin);

    @Query("SELECT a FROM Actividad a WHERE a.lugar = :lugar AND a.fechaActividad = :fecha AND a.estatus = com.example.harmonyGymBack.model.enums.EstatusCatalogo.Activa " +
            "AND a.idActividad != :excludeId " +
            "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
    List<Actividad> findConflictingActivitiesExcluding(@Param("lugar") String lugar,
                                                       @Param("fecha") LocalDate fecha,
                                                       @Param("horaInicio") LocalTime horaInicio,
                                                       @Param("horaFin") LocalTime horaFin,
                                                       @Param("excludeId") String excludeId);

    @Query("SELECT a FROM Actividad a WHERE a.folioInstructor = :folioInstructor AND a.fechaActividad = :fecha AND a.estatus = com.example.harmonyGymBack.model.enums.EstatusCatalogo.Activa " +
            "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
    List<Actividad> findConflictingInstructorActivities(@Param("folioInstructor") String folioInstructor,
                                                         @Param("fecha") LocalDate fecha,
                                                         @Param("horaInicio") LocalTime horaInicio,
                                                         @Param("horaFin") LocalTime horaFin);

    @Query("SELECT a FROM Actividad a WHERE a.folioInstructor = :folioInstructor AND a.fechaActividad = :fecha AND a.estatus = com.example.harmonyGymBack.model.enums.EstatusCatalogo.Activa " +
            "AND a.idActividad != :excludeId " +
            "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio))")
    List<Actividad> findConflictingInstructorActivitiesExcluding(@Param("folioInstructor") String folioInstructor,
                                                                 @Param("fecha") LocalDate fecha,
                                                                 @Param("horaInicio") LocalTime horaInicio,
                                                                 @Param("horaFin") LocalTime horaFin,
                                                                 @Param("excludeId") String excludeId);

    Long countByEstatus(EstatusCatalogo estatus);

    List<Actividad> findByEstatusAndLugarContainingIgnoreCase(EstatusCatalogo estatus, String lugar);

    boolean existsById(String idActividad);

    List<Actividad> findByFechaActividadGreaterThanEqualAndEstatusOrderByFechaActividadAscHoraInicioAsc(LocalDate fecha, EstatusCatalogo estatus);
}
