package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.DetalleVenta;
import com.example.harmonyGymBack.repository.DetalleVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetalleVentaService {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    public DetalleVenta saveDetalleVenta(DetalleVenta detalleVenta) {
        return detalleVentaRepository.save(detalleVenta);
    }

    // ✅ AGREGAR ESTOS MÉTODOS CORREGIDOS:
    public List<DetalleVenta> getAllDetallesVenta() {
        return detalleVentaRepository.findAll();
    }

    public Optional<DetalleVenta> getDetalleVentaById(Long id) {
        return detalleVentaRepository.findById(id);
    }
}