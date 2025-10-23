package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    // Buscar usuario por username
    Optional<Usuario> findByUsername(String username);

    // Verificar si existe un usuario por username
    boolean existsByUsername(String username);

    // Verificar si existe un usuario por idPersona y tipoUsuario
    boolean existsByIdPersonaAndTipoUsuario(String idPersona, String tipoUsuario);

    // Actualizar último login
    @Modifying
    @Query("UPDATE Usuario u SET u.ultimoLogin = :ultimoLogin, u.intentosLogin = 0 WHERE u.username = :username")
    void actualizarUltimoLogin(@Param("username") String username, @Param("ultimoLogin") LocalDateTime ultimoLogin);

    // Incrementar intentos de login fallidos
    @Modifying
    @Query("UPDATE Usuario u SET u.intentosLogin = u.intentosLogin + 1 WHERE u.username = :username")
    void incrementarIntentosLogin(@Param("username") String username);

    // Bloquear usuario por demasiados intentos fallidos
    @Modifying
    @Query("UPDATE Usuario u SET u.estatus = 'Bloqueado', u.fechaBloqueo = :fechaBloqueo WHERE u.username = :username")
    void bloquearUsuario(@Param("username") String username, @Param("fechaBloqueo") LocalDateTime fechaBloqueo);

    // Cambiar contraseña
    @Modifying
    @Query("UPDATE Usuario u SET u.passwordHash = :passwordHash, u.intentosLogin = 0, u.fechaBloqueo = NULL WHERE u.username = :username")
    void cambiarPassword(@Param("username") String username, @Param("passwordHash") String passwordHash);

    // Obtener último ID de usuario
    @Query(value = "SELECT u.id_usuario FROM usuario u ORDER BY u.id_usuario DESC LIMIT 1", nativeQuery = true)
    String findUltimoIdUsuario();

    // Buscar usuario por idPersona
    Optional<Usuario> findByIdPersona(String idPersona);
}