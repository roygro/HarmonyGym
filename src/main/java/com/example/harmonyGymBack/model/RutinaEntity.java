package com.example.harmonyGymBack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "RUTINA")
public class RutinaEntity {

    @Id
    @Column(name = "folio_rutina", length = 50)
    private String folioRutina;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "nivel", length = 20)
    private String nivel;

    @Column(name = "objetivo", length = 100)
    private String objetivo;

    @Column(name = "duracion_estimada")
    private Integer duracionEstimada;

    @Column(name = "estatus", length = 10)
    private String estatus = "Activa";

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Relación con Instructor (creador de la rutina)
    @Column(name = "folio_instructor", length = 50)
    private String folioInstructor;

    // Relación Many-to-Many con Clientes a través de tabla intermedia
    @ManyToMany
    @JoinTable(
            name = "asigna",
            joinColumns = @JoinColumn(name = "folio_rutina"),
            inverseJoinColumns = @JoinColumn(name = "cliente_asignado")
    )
    private Set<Cliente> clientesAsignados = new HashSet<>();

    // Constructores
    public RutinaEntity() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public RutinaEntity(String folioRutina, String nombre, String descripcion, String nivel,
                        String objetivo, Integer duracionEstimada, String folioInstructor) {
        this.folioRutina = folioRutina;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.objetivo = objetivo;
        this.duracionEstimada = duracionEstimada;
        this.folioInstructor = folioInstructor;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public String getFolioRutina() { return folioRutina; }
    public void setFolioRutina(String folioRutina) { this.folioRutina = folioRutina; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public Integer getDuracionEstimada() { return duracionEstimada; }
    public void setDuracionEstimada(Integer duracionEstimada) { this.duracionEstimada = duracionEstimada; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFolioInstructor() { return folioInstructor; }
    public void setFolioInstructor(String folioInstructor) { this.folioInstructor = folioInstructor; }

    public Set<Cliente> getClientesAsignados() { return clientesAsignados; }
    public void setClientesAsignados(Set<Cliente> clientesAsignados) { this.clientesAsignados = clientesAsignados; }

    // Métodos utilitarios para manejar clientes
    public void agregarCliente(Cliente cliente) {
        this.clientesAsignados.add(cliente);
    }

    public void removerCliente(Cliente cliente) {
        this.clientesAsignados.remove(cliente);
    }

    public boolean tieneClienteAsignado(String folioCliente) {
        return this.clientesAsignados.stream()
                .anyMatch(cliente -> cliente.getFolioCliente().equals(folioCliente));
    }
}