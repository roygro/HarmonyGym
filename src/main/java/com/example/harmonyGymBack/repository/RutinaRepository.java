package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.RutinaEntity;
import com.example.harmonyGymBack.model.enums.EstatusRutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutinaRepository extends JpaRepository<RutinaEntity, String> {
    Optional<RutinaEntity> findByFolioRutina(String folioRutina);

    List<RutinaEntity> findByFolioInstructor(String folioInstructor);

    List<RutinaEntity> findByEstatus(EstatusRutina estatus);

    @Query("SELECT MAX(CAST(SUBSTRING(r.folioRutina, 4) AS int)) FROM RutinaEntity r WHERE r.folioRutina LIKE 'RUT%'")
    Optional<Integer> findMaxFolioNumber();

    boolean existsByFolioRutina(String folioRutina);

    @Query("SELECT r FROM RutinaEntity r LEFT JOIN FETCH r.instructor WHERE r.folioRutina = :folioRutina")
    Optional<RutinaEntity> findByFolioRutinaWithInstructor(@Param("folioRutina") String folioRutina);

    @Query("SELECT r FROM RutinaEntity r LEFT JOIN FETCH r.instructor")
    List<RutinaEntity> findAllWithInstructor();

    @Query("SELECT r FROM RutinaEntity r LEFT JOIN FETCH r.instructor WHERE r.folioInstructor = :folioInstructor")
    List<RutinaEntity> findByFolioInstructorWithInstructor(@Param("folioInstructor") String folioInstructor);

    @Query("SELECT a.rutina FROM AsignacionRutina a WHERE a.cliente.folioCliente = :folioCliente")
    List<RutinaEntity> findRutinasByCliente(@Param("folioCliente") String folioCliente);

    @Query("SELECT a.cliente FROM AsignacionRutina a WHERE a.rutina.folioRutina = :folioRutina")
    List<Cliente> findClientesByRutina(@Param("folioRutina") String folioRutina);

    @Query("SELECT a.cliente.folioCliente FROM AsignacionRutina a WHERE a.rutina.folioRutina = :folioRutina AND a.cliente.folioCliente IN :foliosClientes")
    List<String> findClientesYaAsignados(@Param("folioRutina") String folioRutina, @Param("foliosClientes") List<String> foliosClientes);

    @Query("SELECT COUNT(a) > 0 FROM AsignacionRutina a WHERE a.rutina.folioRutina = :folioRutina AND a.cliente.folioCliente = :folioCliente")
    boolean existsAsignacion(@Param("folioRutina") String folioRutina, @Param("folioCliente") String folioCliente);

    long countByEstatus(EstatusRutina estatus);
}
