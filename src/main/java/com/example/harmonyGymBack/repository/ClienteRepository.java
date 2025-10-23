package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Cliente;
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

    // Buscar por nombre (like)
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    // Buscar por email
    Optional<Cliente> findByEmail(String email);

    // Buscar por teléfono
    Optional<Cliente> findByTelefono(String telefono);

    // Buscar clientes activos
    List<Cliente> findByEstatus(String estatus);

    // Buscar por estatus ordenados por nombre
    List<Cliente> findByEstatusOrderByNombreAsc(String estatus);

    // Buscar por género
    List<Cliente> findByGenero(String genero);

    // Buscar por estatus y género
    List<Cliente> findByEstatusAndGenero(String estatus, String genero);

    // Cambiar estatus (dar de baja)
    @Modifying
    @Query("UPDATE Cliente c SET c.estatus = :estatus WHERE c.folioCliente = :folioCliente")
    void updateEstatus(@Param("folioCliente") String folioCliente, @Param("estatus") String estatus);

    // Verificar si existe por email excluyendo el actual
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.email = :email AND c.folioCliente != :folioCliente")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("folioCliente") String folioCliente);

    // Verificar si existe por teléfono excluyendo el actual
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.telefono = :telefono AND c.folioCliente != :folioCliente")
    boolean existsByTelefonoAndNotId(@Param("telefono") String telefono, @Param("folioCliente") String folioCliente);

    // Obtener último folio
    @Query(value = "SELECT c.folio_cliente FROM cliente c ORDER BY c.folio_cliente DESC LIMIT 1", nativeQuery = true)
    String findUltimoFolioCliente();

    // Buscar clientes con membresía activa
    @Query(value = """
        SELECT c.* FROM CLIENTE c 
        INNER JOIN CLIENTE_MEMBRESIA cm ON c.Folio_Cliente = cm.Folio_Cliente 
        WHERE cm.Estatus = 'Activa' AND c.Estatus = 'Activo'
        """, nativeQuery = true)
    List<Cliente> findClientesConMembresiaActiva();

    // Contar clientes por estatus
    Long countByEstatus(String estatus);

    // Buscar por rango de fechas de registro
    List<Cliente> findByFechaRegistroBetween(LocalDateTime start, LocalDateTime end);

    // Verificar si existe cliente por folio
    boolean existsByFolioCliente(String folioCliente);
    @Query(value = "SELECT c.* FROM cliente c WHERE c.folio_cliente NOT IN " +
            "(SELECT a.cliente_asignado FROM asigna a WHERE a.folio_rutina = :folioRutina)",
            nativeQuery = true)
    List<Cliente> findClientesNoAsignadosByRutina(@Param("folioRutina") String folioRutina);

}