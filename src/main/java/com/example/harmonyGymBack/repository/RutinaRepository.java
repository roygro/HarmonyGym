package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.RutinaEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RutinaRepository extends JpaRepository<RutinaEntity, String> {
    Optional<RutinaEntity> findByFolioRutina(String folioRutina);

    List<RutinaEntity> findByFolioInstructor(String folioInstructor);

    List<RutinaEntity> findByEstatus(String estatus);

    @Query("SELECT MAX(CAST(SUBSTRING(r.folioRutina, 4) AS int)) FROM RutinaEntity r WHERE r.folioRutina LIKE 'RUT%'")
    Optional<Integer> findMaxFolioNumber();

    boolean existsByFolioRutina(String folioRutina);

    // NUEVO: Cargar rutina con instructor
    @Query("SELECT r FROM RutinaEntity r LEFT JOIN FETCH r.instructor WHERE r.folioRutina = :folioRutina")
    Optional<RutinaEntity> findByFolioRutinaWithInstructor(@Param("folioRutina") String folioRutina);

    // NUEVO: Cargar todas las rutinas con instructores
    @Query("SELECT r FROM RutinaEntity r LEFT JOIN FETCH r.instructor")
    List<RutinaEntity> findAllWithInstructor();

    // NUEVO: Cargar rutinas por instructor con datos del instructor
    @Query("SELECT r FROM RutinaEntity r LEFT JOIN FETCH r.instructor WHERE r.folioInstructor = :folioInstructor")
    List<RutinaEntity> findByFolioInstructorWithInstructor(@Param("folioInstructor") String folioInstructor);

    // Métodos para asignación de rutinas a clientes (usando consultas nativas)
    @Query(value = "SELECT r.* FROM rutina r INNER JOIN asigna a ON r.folio_rutina = a.folio_rutina WHERE a.cliente_asignado = :folioCliente", nativeQuery = true)
    List<RutinaEntity> findRutinasByCliente(@Param("folioCliente") String folioCliente);

    @Query(value = "SELECT c.* FROM cliente c INNER JOIN asigna a ON c.folio_cliente = a.cliente_asignado WHERE a.folio_rutina = :folioRutina", nativeQuery = true)
    List<Cliente> findClientesByRutina(@Param("folioRutina") String folioRutina);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO asigna (folio_instructor, folio_rutina, cliente_asignado, fecha_asignacion) VALUES (:folioInstructor, :folioRutina, :folioCliente, CURRENT_TIMESTAMP)", nativeQuery = true)
    void asignarRutinaACliente(@Param("folioInstructor") String folioInstructor,
                               @Param("folioRutina") String folioRutina,
                               @Param("folioCliente") String folioCliente);

    // NUEVO: Método para asignar a múltiples clientes (individual)
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO asigna (folio_instructor, folio_rutina, cliente_asignado, fecha_asignacion) VALUES (:folioInstructor, :folioRutina, :cliente, CURRENT_TIMESTAMP)", nativeQuery = true)
    void asignarRutinaAClienteIndividual(@Param("folioInstructor") String folioInstructor,
                                         @Param("folioRutina") String folioRutina,
                                         @Param("cliente") String cliente);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asigna WHERE folio_rutina = :folioRutina AND cliente_asignado = :folioCliente", nativeQuery = true)
    void desasignarRutinaDeCliente(@Param("folioRutina") String folioRutina,
                                   @Param("folioCliente") String folioCliente);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asigna WHERE folio_rutina = :folioRutina", nativeQuery = true)
    void eliminarTodasAsignacionesDeRutina(@Param("folioRutina") String folioRutina);

    @Query(value = "SELECT COUNT(*) > 0 FROM asigna WHERE folio_rutina = :folioRutina AND cliente_asignado = :folioCliente", nativeQuery = true)
    boolean existsAsignacion(@Param("folioRutina") String folioRutina,
                             @Param("folioCliente") String folioCliente);

    // NUEVO: Verificar asignaciones existentes para múltiples clientes
    @Query(value = "SELECT cliente_asignado FROM asigna WHERE folio_rutina = :folioRutina AND cliente_asignado IN :foliosClientes", nativeQuery = true)
    List<String> findClientesYaAsignados(@Param("folioRutina") String folioRutina,
                                         @Param("foliosClientes") List<String> foliosClientes);


}