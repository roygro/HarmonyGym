package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {

    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByTelefono(String telefono);

    List<Cliente> findByEstatus(EstatusPersona estatus);

    List<Cliente> findByEstatusOrderByNombreAsc(EstatusPersona estatus);

    List<Cliente> findByGenero(String genero);

    List<Cliente> findByEstatusAndGenero(EstatusPersona estatus, String genero);

    @Modifying
    @Query("UPDATE Cliente c SET c.estatus = :estatus WHERE c.folioCliente = :folioCliente")
    void updateEstatus(@Param("folioCliente") String folioCliente, @Param("estatus") EstatusPersona estatus);

    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.email = :email AND c.folioCliente != :folioCliente")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("folioCliente") String folioCliente);

    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.telefono = :telefono AND c.folioCliente != :folioCliente")
    boolean existsByTelefonoAndNotId(@Param("telefono") String telefono, @Param("folioCliente") String folioCliente);

    @Query(value = "SELECT c.folio_cliente FROM cliente c ORDER BY c.folio_cliente DESC LIMIT 1", nativeQuery = true)
    String findUltimoFolioCliente();

    @Query("SELECT DISTINCT c FROM Cliente c, MembresiaCliente cm WHERE cm.cliente = c AND cm.estatus = com.example.harmonyGymBack.model.enums.EstatusMembresiaCliente.Activa AND c.estatus = com.example.harmonyGymBack.model.enums.EstatusPersona.Activo")
    List<Cliente> findClientesConMembresiaActiva();

    Long countByEstatus(EstatusPersona estatus);

    List<Cliente> findByFechaRegistroBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByFolioCliente(String folioCliente);

    @Query("SELECT c FROM Cliente c WHERE c.folioCliente NOT IN (SELECT a.cliente.folioCliente FROM AsignacionRutina a WHERE a.rutina.folioRutina = :folioRutina)")
    List<Cliente> findClientesNoAsignadosByRutina(@Param("folioRutina") String folioRutina);
}
