package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, String> {

    // Buscar rol por nombre
    Optional<Rol> findByNombreRol(String nombreRol);

    // Buscar roles por tipo de usuario
    @Query(value = "SELECT r.* FROM ROL r WHERE r.id_rol IN (" +
            "SELECT ur.id_rol FROM USUARIO_ROL ur WHERE ur.tipo_usuario = :tipoUsuario)",
            nativeQuery = true)
    List<Rol> findRolesByTipoUsuario(@Param("tipoUsuario") String tipoUsuario);

    // Obtener rol por defecto para un tipo de usuario
    @Query(value = "SELECT r.* FROM ROL r " +
            "WHERE r.id_rol = (" +
            "  SELECT ur.id_rol FROM USUARIO_ROL ur " +
            "  WHERE ur.tipo_usuario = :tipoUsuario " +
            "  LIMIT 1" +
            ")", nativeQuery = true)
    Optional<Rol> findRolPorDefecto(@Param("tipoUsuario") String tipoUsuario);
}