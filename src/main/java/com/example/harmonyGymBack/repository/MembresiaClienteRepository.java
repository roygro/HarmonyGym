package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.MembresiaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembresiaClienteRepository extends JpaRepository<MembresiaCliente, Long> {

    // Buscar membresías por cliente
    List<MembresiaCliente> findByClienteFolioCliente(String folioCliente);

    // Buscar membresía activa de un cliente
    Optional<MembresiaCliente> findByClienteFolioClienteAndEstatus(String folioCliente, String estatus);

    // Buscar todas las membresías por estatus
    List<MembresiaCliente> findByEstatus(String estatus);

    // Contar membresías por estatus (MÉTODO NUEVO AGREGADO)
    long countByEstatus(String estatus);

    // Buscar membresías que expiran en una fecha específica
    List<MembresiaCliente> findByFechaFin(LocalDate fechaFin);

    // Buscar membresías que expiran pronto (en los próximos X días)
    @Query("SELECT mc FROM MembresiaCliente mc WHERE mc.fechaFin BETWEEN :hoy AND :fechaLimite AND mc.estatus = 'Activa'")
    List<MembresiaCliente> findMembresiasPorExpirar(@Param("hoy") LocalDate hoy, @Param("fechaLimite") LocalDate fechaLimite);

    // Buscar membresías expiradas
    @Query("SELECT mc FROM MembresiaCliente mc WHERE mc.fechaFin < :hoy AND mc.estatus = 'Activa'")
    List<MembresiaCliente> findMembresiasExpiradas(@Param("hoy") LocalDate hoy);

    // Actualizar estatus de membresías expiradas
    @Modifying
    @Query("UPDATE MembresiaCliente mc SET mc.estatus = 'Expirada', mc.fechaActualizacion = CURRENT_TIMESTAMP WHERE mc.fechaFin < :hoy AND mc.estatus = 'Activa'")
    int actualizarMembresiasExpiradas(@Param("hoy") LocalDate hoy);

    // Contar membresías activas por cliente
    Long countByClienteFolioClienteAndEstatus(String folioCliente, String estatus);

    // Verificar si un cliente tiene membresía activa
    boolean existsByClienteFolioClienteAndEstatus(String folioCliente, String estatus);

    // Obtener historial de membresías ordenado por fecha de inicio
    List<MembresiaCliente> findByClienteFolioClienteOrderByFechaInicioDesc(String folioCliente);

    // Buscar membresías por tipo de membresía
    @Query("SELECT mc FROM MembresiaCliente mc WHERE mc.membresia.tipo = :tipo AND mc.estatus = 'Activa'")
    List<MembresiaCliente> findByMembresiaTipoAndActiva(@Param("tipo") com.example.harmonyGymBack.model.TipoMembresia tipo);


    // Verificar si existe otra membresía activa excluyendo una específica
    @Query("SELECT COUNT(mc) > 0 FROM MembresiaCliente mc WHERE mc.cliente.folioCliente = :folioCliente AND mc.estatus = :estatus AND mc.idMembresiaCliente != :excludeId")
    boolean existsByClienteFolioClienteAndEstatusAndIdMembresiaClienteNot(
            @Param("folioCliente") String folioCliente,
            @Param("estatus") String estatus,
            @Param("excludeId") Long excludeId);


}