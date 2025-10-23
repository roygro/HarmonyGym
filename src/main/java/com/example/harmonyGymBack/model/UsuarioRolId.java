package com.example.harmonyGymBack.model;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioRolId implements Serializable {
    private String idUsuario;
    private String tipoUsuario;
    private String idRol;

    // Constructores
    public UsuarioRolId() {}

    public UsuarioRolId(String idUsuario, String tipoUsuario, String idRol) {
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
        this.idRol = idRol;
    }

    // Getters y Setters
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public String getIdRol() { return idRol; }
    public void setIdRol(String idRol) { this.idRol = idRol; }

    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioRolId that = (UsuarioRolId) o;
        return Objects.equals(idUsuario, that.idUsuario) &&
                Objects.equals(tipoUsuario, that.tipoUsuario) &&
                Objects.equals(idRol, that.idRol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, tipoUsuario, idRol);
    }
}