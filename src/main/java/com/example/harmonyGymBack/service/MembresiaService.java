package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.repository.MembresiaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class MembresiaService {

    @Autowired
    private MembresiaRepository membresiaRepository;

    public Membresia crearMembresia(Membresia membresia) {
        // Lógica de validación y creación
        return membresiaRepository.save(membresia);
    }

    public List<Membresia> obtenerMembresiasPorCliente(Long clienteId) {
        return membresiaRepository.findByClienteId(clienteId);
    }


}