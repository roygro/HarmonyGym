package com.example.harmonyGymBack.model;

import java.time.LocalDateTime;
import java.util.Map;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private String idUsuario;
    private String username;
    private String tipoUsuario;
    private String idPersona;
    private String nombreRol;
    private String permisos;
    private String nombreCompleto;
    private LocalDateTime ultimoLogin;

    // Constructores
    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String idUsuario, String username,
                        String tipoUsuario, String idPersona, String nombreRol,
                        String permisos, String nombreCompleto, LocalDateTime ultimoLogin) {
        this.success = success;
        this.message = message;
        this.idUsuario = idUsuario;
        this.username = username;
        this.tipoUsuario = tipoUsuario;
        this.idPersona = idPersona;
        this.nombreRol = nombreRol;
        this.permisos = permisos;
        this.nombreCompleto = nombreCompleto;
        this.ultimoLogin = ultimoLogin;
    }

    // Getters y Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }
}