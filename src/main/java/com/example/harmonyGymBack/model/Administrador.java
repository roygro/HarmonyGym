package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ADMINISTRADOR")
public class Administrador {
    @Id
    @Column(name = "folio_admin")
    private String folioAdmin;

    @Column(name = "nombrecom", nullable = false)
    private String nombreCom;

    @Column(name = "app")
    private String app;

    @Column(name = "apm")
    private String apm;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    // Constructores
    public Administrador() {
        this.fechaRegistro = LocalDateTime.now();
    }

    public Administrador(String folioAdmin, String nombreCom, String app, String apm) {
        this.folioAdmin = folioAdmin;
        this.nombreCom = nombreCom;
        this.app = app;
        this.apm = apm;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Administrador(String folioAdmin, String nombreCom, String app, String apm, LocalDateTime fechaRegistro) {
        this.folioAdmin = folioAdmin;
        this.nombreCom = nombreCom;
        this.app = app;
        this.apm = apm;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public String getFolioAdmin() { return folioAdmin; }
    public void setFolioAdmin(String folioAdmin) { this.folioAdmin = folioAdmin; }

    public String getNombreCom() { return nombreCom; }
    public void setNombreCom(String nombreCom) { this.nombreCom = nombreCom; }

    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }

    public String getApm() { return apm; }
    public void setApm(String apm) { this.apm = apm; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        return "Administrador{" +
                "folioAdmin='" + folioAdmin + '\'' +
                ", nombreCom='" + nombreCom + '\'' +
                ", app='" + app + '\'' +
                ", apm='" + apm + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}