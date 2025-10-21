package com.example.harmonyGymBack.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ADMINISTRADOR")
public class Administrador {
    @Id
    @Column(name = "folio_admin")
    @JsonProperty("folioAdmin")
    private String folioAdmin;

    @Column(name = "nombrecom")
    @JsonProperty("nombreCom")
    private String nombreCom;

    @Column(name = "app")
    @JsonProperty("app")
    private String app;

    @Column(name = "apm")
    @JsonProperty("apm")
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
}