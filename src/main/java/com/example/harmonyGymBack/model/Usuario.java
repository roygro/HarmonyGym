package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    @Column(name = "id_usuario")
    private String idUsuario;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "passwordhash", nullable = false)
    private String passwordHash;

    @Column(name = "tipo_usuario", nullable = false)
    private String tipoUsuario;

    @Column(name = "id_persona", nullable = false)
    private String idPersona;

    @Column(name = "estatus")
    private String estatus = "Activo";

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "intentos_login")
    private Integer intentosLogin = 0;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    // Constructores
    public Usuario() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Usuario(String idUsuario, String username, String passwordHash,
                   String tipoUsuario, String idPersona) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.passwordHash = passwordHash;
        this.tipoUsuario = tipoUsuario;
        this.idPersona = idPersona;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getIntentosLogin() { return intentosLogin; }
    public void setIntentosLogin(Integer intentosLogin) { this.intentosLogin = intentosLogin; }

    public LocalDateTime getFechaBloqueo() { return fechaBloqueo; }
    public void setFechaBloqueo(LocalDateTime fechaBloqueo) { this.fechaBloqueo = fechaBloqueo; }
}