package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.RealizaActividad;
import com.example.harmonyGymBack.model.enums.EstatusInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RealizaActividadRepository extends JpaRepository<RealizaActividad, Long> {

    @Query("SELECT ra FROM RealizaActividad ra JOIN FETCH ra.actividad WHERE ra.folioCliente = :folioCliente AND ra.estatus = com.example.harmonyGymBack.model.enums.EstatusInscripcion.Inscrito")
    List<RealizaActividad> findInscripcionesConActividad(@Param("folioCliente") String folioCliente);

    @Query("SELECT ra FROM RealizaActividad ra JOIN FETCH ra.actividad WHERE ra.folioCliente = :folioCliente")
    List<RealizaActividad> findHistorialConActividad(@Param("folioCliente") String folioCliente);

    List<RealizaActividad> findByFolioCliente(String folioCliente);

    List<RealizaActividad> findByIdActividad(String idActividad);

    @Query("SELECT ra FROM RealizaActividad ra WHERE ra.folioCliente = :folioCliente AND ra.idActividad = :idActividad AND ra.estatus = com.example.harmonyGymBack.model.enums.EstatusInscripcion.Inscrito")
    Optional<RealizaActividad> findInscripcionActiva(@Param("folioCliente") String folioCliente, @Param("idActividad") String idActividad);

    @Query("SELECT COUNT(ra) FROM RealizaActividad ra WHERE ra.idActividad = :idActividad AND ra.estatus = com.example.harmonyGymBack.model.enums.EstatusInscripcion.Inscrito")
    Long countInscripcionesActivasByActividad(@Param("idActividad") String idActividad);

    List<RealizaActividad> findByFolioClienteAndEstatus(String folioCliente, EstatusInscripcion estatus);

    List<RealizaActividad> findByFechaParticipacion(LocalDate fechaParticipacion);

    List<RealizaActividad> findByFechaParticipacionBetween(LocalDate start, LocalDate end);

    List<RealizaActividad> findByIdActividadAndEstatus(String idActividad, EstatusInscripcion estatus);

    boolean existsByFolioClienteAndIdActividad(String folioCliente, String idActividad);

    long countByEstatus(EstatusInscripcion estatus);
}
