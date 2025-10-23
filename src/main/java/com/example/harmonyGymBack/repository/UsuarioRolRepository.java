package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.UsuarioRol;
import com.example.harmonyGymBack.model.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    // Buscar roles por idUsuario
    List<UsuarioRol> findByIdUsuario(String idUsuario);

    // Buscar roles por idUsuario y tipoUsuario
    List<UsuarioRol> findByIdUsuarioAndTipoUsuario(String idUsuario, String tipoUsuario);

    // Verificar si existe asignación de rol
    boolean existsByIdUsuarioAndTipoUsuarioAndIdRol(String idUsuario, String tipoUsuario, String idRol);

    // Obtener rol principal de un usuario
    @Query(value = "SELECT ur.* FROM USUARIO_ROL ur " +
            "WHERE ur.id_usuario = :idUsuario AND ur.tipo_usuario = :tipoUsuario " +
            "LIMIT 1", nativeQuery = true)
    Optional<UsuarioRol> findRolPrincipal(@Param("idUsuario") String idUsuario,
                                          @Param("tipoUsuario") String tipoUsuario);
}