package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Administrador;
import com.example.harmonyGymBack.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    public List<Administrador> getAllAdministradores() {
        return administradorRepository.findAll();
    }

    public Optional<Administrador> getAdministradorById(String folioAdmin) {
        return administradorRepository.findById(folioAdmin);
    }

    public Administrador createAdministrador(Administrador administrador) {
        // Validar que el folio no exista
        if (administradorRepository.existsById(administrador.getFolioAdmin())) {
            throw new RuntimeException("El folio de administrador ya existe");
        }

        // Validar que no exista un administrador con el mismo nombre completo
        if (administradorRepository.existsByNombreCompletoAndNotId(
                administrador.getNombreCom(), administrador.getApp(),
                administrador.getApm(), "")) {
            throw new RuntimeException("Ya existe un administrador con el mismo nombre completo");
        }

        return administradorRepository.save(administrador);
    }

    public Administrador updateAdministrador(String folioAdmin, Administrador administradorDetails) {
        Optional<Administrador> optionalAdministrador = administradorRepository.findById(folioAdmin);

        if (optionalAdministrador.isPresent()) {
            Administrador administrador = optionalAdministrador.get();

            // Validar que no exista otro administrador con el mismo nombre completo
            if (administradorRepository.existsByNombreCompletoAndNotId(
                    administradorDetails.getNombreCom(), administradorDetails.getApp(),
                    administradorDetails.getApm(), folioAdmin)) {
                throw new RuntimeException("Ya existe otro administrador con el mismo nombre completo");
            }

            administrador.setNombreCom(administradorDetails.getNombreCom());
            administrador.setApp(administradorDetails.getApp());
            administrador.setApm(administradorDetails.getApm());

            return administradorRepository.save(administrador);
        } else {
            throw new RuntimeException("Administrador no encontrado con folio: " + folioAdmin);
        }
    }

    public void deleteAdministrador(String folioAdmin) {
        if (administradorRepository.existsById(folioAdmin)) {
            administradorRepository.deleteById(folioAdmin);
        } else {
            throw new RuntimeException("Administrador no encontrado con folio: " + folioAdmin);
        }
    }

    public List<Administrador> searchAdministradores(String searchTerm) {
        return administradorRepository.findByNombreComContainingIgnoreCase(searchTerm);
    }

    public List<Administrador> searchByApp(String app) {
        return administradorRepository.findByAppContainingIgnoreCase(app);
    }

    public List<Administrador> searchByApm(String apm) {
        return administradorRepository.findByApmContainingIgnoreCase(apm);
    }
}