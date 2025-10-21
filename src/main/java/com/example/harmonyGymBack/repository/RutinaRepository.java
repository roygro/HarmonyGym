package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.RutinaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutinaRepository extends JpaRepository<RutinaEntity, String> {

    // ===== CONSULTAS BÁSICAS =====
    List<RutinaEntity> findByNombreContainingIgnoreCase(String nombre);
    List<RutinaEntity> findByNivel(String nivel);
    List<RutinaEntity> findByEstatus(String estatus);
    List<RutinaEntity> findByObjetivoContainingIgnoreCase(String objetivo);
    boolean existsByFolioRutina(String folioRutina);

    // ===== CONSULTAS POR INSTRUCTOR =====
    List<RutinaEntity> findByFolioInstructor(String folioInstructor);

    @Query("SELECT r FROM RutinaEntity r WHERE r.folioInstructor = :instructor AND r.estatus = 'Activa'")
    List<RutinaEntity> findRutinasActivasPorInstructor(@Param("instructor") String folioInstructor);

    @Query("SELECT COUNT(r) FROM RutinaEntity r WHERE r.folioInstructor = :instructor")
    Long countRutinasPorInstructor(@Param("instructor") String folioInstructor);

    // ===== CONSULTAS POR CLIENTE (usando la relación Many-to-Many) =====

    @Query("SELECT r FROM RutinaEntity r JOIN r.clientesAsignados c WHERE c.folioCliente = :cliente")
    List<RutinaEntity> findRutinasPorCliente(@Param("cliente") String folioCliente);

    @Query("SELECT r FROM RutinaEntity r JOIN r.clientesAsignados c WHERE c.folioCliente = :cliente AND r.estatus = 'Activa'")
    List<RutinaEntity> findRutinasActivasPorCliente(@Param("cliente") String folioCliente);

    @Query("SELECT COUNT(r) > 0 FROM RutinaEntity r JOIN r.clientesAsignados c WHERE c.folioCliente = :cliente AND r.estatus = 'Activa'")
    boolean existsRutinasActivasPorCliente(@Param("cliente") String folioCliente);

    @Query("SELECT COUNT(r) FROM RutinaEntity r JOIN r.clientesAsignados c WHERE c.folioCliente = :cliente AND r.estatus = 'Activa'")
    Long countRutinasActivasPorCliente(@Param("cliente") String folioCliente);

    // ===== CONSULTAS DE ASIGNACIÓN =====

    @Query("SELECT COUNT(r) > 0 FROM RutinaEntity r JOIN r.clientesAsignados c WHERE r.folioRutina = :rutina AND c.folioCliente = :cliente")
    boolean existsAsignacion(@Param("rutina") String folioRutina, @Param("cliente") String folioCliente);

    // ===== CONSULTAS COMBINADAS =====

    @Query("SELECT r FROM RutinaEntity r WHERE r.folioInstructor = :instructor AND r.estatus = 'Activa' AND r NOT IN " +
            "(SELECT r2 FROM RutinaEntity r2 JOIN r2.clientesAsignados c WHERE c.folioCliente = :cliente)")
    List<RutinaEntity> findRutinasDisponiblesParaCliente(@Param("instructor") String folioInstructor,
                                                         @Param("cliente") String folioCliente);

    // ===== CONSULTAS DE ESTADÍSTICAS =====

    @Query("SELECT r.folioInstructor, COUNT(r) FROM RutinaEntity r GROUP BY r.folioInstructor")
    List<Object[]> countRutinasPorInstructor();

    @Query("SELECT r.folioInstructor, COUNT(DISTINCT c.folioCliente) FROM RutinaEntity r JOIN r.clientesAsignados c GROUP BY r.folioInstructor")
    List<Object[]> countClientesAtendidosPorInstructor();
}