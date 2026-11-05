package com.example.harmonyGymBack.security;

import java.io.Serializable;

public class UsuarioPrincipal implements Serializable {
    private final String username;
    private final String tipoUsuario;
    private final String rol;
    private final String idPersona;

    public UsuarioPrincipal(String username, String tipoUsuario, String rol, String idPersona) {
        this.username = username;
        this.tipoUsuario = tipoUsuario;
        this.rol = rol;
        this.idPersona = idPersona;
    }

    public String getUsername() { return username; }
    public String getTipoUsuario() { return tipoUsuario; }
    public String getRol() { return rol; }
    public String getIdPersona() { return idPersona; }
}
