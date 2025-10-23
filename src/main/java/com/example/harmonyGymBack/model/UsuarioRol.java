package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO_ROL")
@IdClass(UsuarioRolId.class)
public class UsuarioRol {
    @Id
    @Column(name = "id_usuario")
    private String idUsuario;

    @Id
    @Column(name = "tipo_usuario")
    private String tipoUsuario;

    @Id
    @Column(name = "id_rol")
    private String idRol;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "estatus")
    private String estatus = "Activo";

    // Constructores
    public UsuarioRol() {
        this.fechaAsignacion = LocalDateTime.now();
    }

    public UsuarioRol(String idUsuario, String tipoUsuario, String idRol, String estatus) {
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
        this.idRol = idRol;
        this.estatus = estatus;
        this.fechaAsignacion = LocalDateTime.now();
    }

    // Getters y Setters
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public String getIdRol() { return idRol; }
    public void setIdRol(String idRol) { this.idRol = idRol; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }
}