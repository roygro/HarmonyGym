package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Recepcionista;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecepcionistaRepository extends JpaRepository<Recepcionista, String> {

    List<Recepcionista> findByNombreContainingIgnoreCase(String nombre);

    Optional<Recepcionista> findByEmail(String email);

    Optional<Recepcionista> findByTelefono(String telefono);

    List<Recepcionista> findByEstatus(EstatusPersona estatus);

    List<Recepcionista> findByEstatusOrderByNombreAsc(EstatusPersona estatus);

    @Modifying
    @Query("UPDATE Recepcionista r SET r.estatus = :estatus WHERE r.idRecepcionista = :idRecepcionista")
    void updateEstatus(@Param("idRecepcionista") String idRecepcionista, @Param("estatus") EstatusPersona estatus);

    @Query("SELECT COUNT(r) > 0 FROM Recepcionista r WHERE r.email = :email AND r.idRecepcionista != :idRecepcionista")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("idRecepcionista") String idRecepcionista);

    @Query("SELECT COUNT(r) > 0 FROM Recepcionista r WHERE r.telefono = :telefono AND r.idRecepcionista != :idRecepcionista")
    boolean existsByTelefonoAndNotId(@Param("telefono") String telefono, @Param("idRecepcionista") String idRecepcionista);

    @Query(value = "SELECT r.id_recepcionista FROM recepcionista r ORDER BY r.id_recepcionista DESC LIMIT 1", nativeQuery = true)
    String findUltimoIdRecepcionista();

    Long countByEstatus(EstatusPersona estatus);

    List<Recepcionista> findByFechaRegistroBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByIdRecepcionista(String idRecepcionista);

    List<Recepcionista> findByFechaContratacionBetween(LocalDate start, LocalDate end);
}
