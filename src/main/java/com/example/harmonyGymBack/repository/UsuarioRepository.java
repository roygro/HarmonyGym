package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Usuario;
import com.example.harmonyGymBack.model.enums.EstatusUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByIdPersonaAndTipoUsuario(String idPersona, String tipoUsuario);

    @Modifying
    @Query("UPDATE Usuario u SET u.ultimoLogin = :ultimoLogin, u.intentosLogin = 0 WHERE u.username = :username")
    void actualizarUltimoLogin(@Param("username") String username, @Param("ultimoLogin") LocalDateTime ultimoLogin);

    @Modifying
    @Query("UPDATE Usuario u SET u.intentosLogin = u.intentosLogin + 1 WHERE u.username = :username")
    void incrementarIntentosLogin(@Param("username") String username);

    @Modifying
    @Query("UPDATE Usuario u SET u.estatus = com.example.harmonyGymBack.model.enums.EstatusUsuario.Bloqueado, u.fechaBloqueo = :fechaBloqueo WHERE u.username = :username")
    void bloquearUsuario(@Param("username") String username, @Param("fechaBloqueo") LocalDateTime fechaBloqueo);

    @Modifying
    @Query("UPDATE Usuario u SET u.passwordHash = :passwordHash, u.intentosLogin = 0, u.fechaBloqueo = NULL WHERE u.username = :username")
    void cambiarPassword(@Param("username") String username, @Param("passwordHash") String passwordHash);

    @Query(value = "SELECT u.id_usuario FROM usuario u ORDER BY u.id_usuario DESC LIMIT 1", nativeQuery = true)
    String findUltimoIdUsuario();

    Optional<Usuario> findByIdPersona(String idPersona);

    List<Usuario> findByEstatus(EstatusUsuario estatus);

    long countByEstatus(EstatusUsuario estatus);

    long countByTipoUsuarioAndEstatus(String tipoUsuario, EstatusUsuario estatus);
}
