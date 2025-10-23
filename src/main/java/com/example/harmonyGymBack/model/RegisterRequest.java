package com.example.harmonyGymBack.model;

public class RegisterRequest {
    private String username;
    private String password;
    private String tipoUsuario;
    private String idPersona;

    // Constructores
    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String tipoUsuario, String idPersona) {
        this.username = username;
        this.password = password;
        this.tipoUsuario = tipoUsuario;
        this.idPersona = idPersona;
    }

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }
}