package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.AsignacionRutina;
import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.RutinaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionRutinaRepository extends JpaRepository<AsignacionRutina, Long> {

    @Query("SELECT a.rutina FROM AsignacionRutina a WHERE a.cliente.folioCliente = :folioCliente")
    List<RutinaEntity> findRutinasByCliente(@Param("folioCliente") String folioCliente);

    @Query("SELECT a.cliente FROM AsignacionRutina a WHERE a.rutina.folioRutina = :folioRutina")
    List<Cliente> findClientesByRutina(@Param("folioRutina") String folioRutina);

    Optional<AsignacionRutina> findByRutina_FolioRutinaAndCliente_FolioCliente(String folioRutina, String folioCliente);

    boolean existsByRutina_FolioRutinaAndCliente_FolioCliente(String folioRutina, String folioCliente);

    @Query("SELECT a.cliente.folioCliente FROM AsignacionRutina a WHERE a.rutina.folioRutina = :folioRutina AND a.cliente.folioCliente IN :foliosClientes")
    List<String> findClientesYaAsignados(@Param("folioRutina") String folioRutina, @Param("foliosClientes") List<String> foliosClientes);

    @Modifying
    @Query("DELETE FROM AsignacionRutina a WHERE a.rutina.folioRutina = :folioRutina AND a.cliente.folioCliente = :folioCliente")
    void desasignarRutinaDeCliente(@Param("folioRutina") String folioRutina, @Param("folioCliente") String folioCliente);

    @Modifying
    @Query("DELETE FROM AsignacionRutina a WHERE a.rutina.folioRutina = :folioRutina")
    void eliminarTodasAsignacionesDeRutina(@Param("folioRutina") String folioRutina);
}
