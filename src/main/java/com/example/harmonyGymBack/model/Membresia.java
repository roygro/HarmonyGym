package com.example.harmonyGymBack.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "membresias")
public class Membresia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    private TipoMembresia tipo;

    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private Double precio;
    private String estado;

    // Constructor por defecto, constructor con parámetros,
    // getters y setters
}