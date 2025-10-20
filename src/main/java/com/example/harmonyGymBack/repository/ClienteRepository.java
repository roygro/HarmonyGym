package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // Cambiar estatus (dar de baja)
    @Modifying
    @Query("UPDATE Cliente c SET c.estatus = :estatus WHERE c.folioCliente = :folioCliente")
    void updateEstatus(@Param("folioCliente") String folioCliente, @Param("estatus") String estatus);

    // Verificar si existe por email excluyendo el actual
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.email = :email AND c.folioCliente != :folioCliente")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("folioCliente") String folioCliente);
}