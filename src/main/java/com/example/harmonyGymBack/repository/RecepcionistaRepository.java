package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Recepcionista;
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

    // Buscar por nombre (like)
    List<Recepcionista> findByNombreContainingIgnoreCase(String nombre);

    // Buscar por email
    Optional<Recepcionista> findByEmail(String email);

    // Buscar por teléfono
    Optional<Recepcionista> findByTelefono(String telefono);

    // Buscar recepcionistas activos
    List<Recepcionista> findByEstatus(String estatus);

    // Buscar por estatus ordenados por nombre
    List<Recepcionista> findByEstatusOrderByNombreAsc(String estatus);

    // Cambiar estatus (dar de baja)
    @Modifying
    @Query("UPDATE Recepcionista r SET r.estatus = :estatus WHERE r.idRecepcionista = :idRecepcionista")
    void updateEstatus(@Param("idRecepcionista") String idRecepcionista, @Param("estatus") String estatus);

    // Verificar si existe por email excluyendo el actual
    @Query("SELECT COUNT(r) > 0 FROM Recepcionista r WHERE r.email = :email AND r.idRecepcionista != :idRecepcionista")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("idRecepcionista") String idRecepcionista);

    // Verificar si existe por teléfono excluyendo el actual
    @Query("SELECT COUNT(r) > 0 FROM Recepcionista r WHERE r.telefono = :telefono AND r.idRecepcionista != :idRecepcionista")
    boolean existsByTelefonoAndNotId(@Param("telefono") String telefono, @Param("idRecepcionista") String idRecepcionista);

    // Obtener último ID
    @Query(value = "SELECT r.id_recepcionista FROM recepcionista r ORDER BY r.id_recepcionista DESC LIMIT 1", nativeQuery = true)
    String findUltimoIdRecepcionista();

    // Contar recepcionistas por estatus
    Long countByEstatus(String estatus);

    // Buscar por rango de fechas de registro
    List<Recepcionista> findByFechaRegistroBetween(LocalDateTime start, LocalDateTime end);

    // Verificar si existe recepcionista por ID
    boolean existsByIdRecepcionista(String idRecepcionista);

    // Buscar recepcionistas contratados en un rango de fechas
    List<Recepcionista> findByFechaContratacionBetween(LocalDate start, LocalDate end);
}