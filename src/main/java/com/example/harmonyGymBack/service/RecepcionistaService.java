package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Recepcionista;
import com.example.harmonyGymBack.repository.RecepcionistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecepcionistaService {

    @Autowired
    private RecepcionistaRepository recepcionistaRepository;

    public List<Recepcionista> getAllRecepcionistas() {
        return recepcionistaRepository.findAll();
    }

    public Optional<Recepcionista> getRecepcionistaById(String idRecepcionista) {
        return recepcionistaRepository.findById(idRecepcionista);
    }

    public Recepcionista createRecepcionista(Recepcionista recepcionista) {
        if (recepcionistaRepository.existsById(recepcionista.getIdRecepcionista())) {
            throw new RuntimeException("El ID de recepcionista ya existe");
        }

        if (recepcionista.getEmail() != null && recepcionistaRepository.findByEmail(recepcionista.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        return recepcionistaRepository.save(recepcionista);
    }

    public Recepcionista updateRecepcionista(String idRecepcionista, Recepcionista recepcionistaDetails) {
        Optional<Recepcionista> optionalRecepcionista = recepcionistaRepository.findById(idRecepcionista);

        if (optionalRecepcionista.isPresent()) {
            Recepcionista recepcionista = optionalRecepcionista.get();

            if (recepcionistaDetails.getEmail() != null &&
                    recepcionistaRepository.existsByEmailAndNotId(recepcionistaDetails.getEmail(), idRecepcionista)) {
                throw new RuntimeException("El email ya está registrado por otro recepcionista");
            }

            recepcionista.setNombre(recepcionistaDetails.getNombre());
            recepcionista.setTelefono(recepcionistaDetails.getTelefono());
            recepcionista.setEmail(recepcionistaDetails.getEmail());
            recepcionista.setFechaContratacion(recepcionistaDetails.getFechaContratacion());

            return recepcionistaRepository.save(recepcionista);
        } else {
            throw new RuntimeException("Recepcionista no encontrado con ID: " + idRecepcionista);
        }
    }

    public void deleteRecepcionista(String idRecepcionista) {
        if (recepcionistaRepository.existsById(idRecepcionista)) {
            recepcionistaRepository.deleteById(idRecepcionista);
        } else {
            throw new RuntimeException("Recepcionista no encontrado con ID: " + idRecepcionista);
        }
    }

    public void darDeBajaRecepcionista(String idRecepcionista) {
        Optional<Recepcionista> optionalRecepcionista = recepcionistaRepository.findById(idRecepcionista);

        if (optionalRecepcionista.isPresent()) {
            recepcionistaRepository.updateEstatus(idRecepcionista, "Inactivo");
        } else {
            throw new RuntimeException("Recepcionista no encontrado con ID: " + idRecepcionista);
        }
    }

    public void activarRecepcionista(String idRecepcionista) {
        Optional<Recepcionista> optionalRecepcionista = recepcionistaRepository.findById(idRecepcionista);

        if (optionalRecepcionista.isPresent()) {
            recepcionistaRepository.updateEstatus(idRecepcionista, "Activo");
        } else {
            throw new RuntimeException("Recepcionista no encontrado con ID: " + idRecepcionista);
        }
    }

    public List<Recepcionista> searchRecepcionistas(String searchTerm) {
        return recepcionistaRepository.findByNombreContainingIgnoreCase(searchTerm);
    }

    public List<Recepcionista> getRecepcionistasByEstatus(String estatus) {
        return recepcionistaRepository.findByEstatus(estatus);
    }
}