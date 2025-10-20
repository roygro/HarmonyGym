package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> getClienteById(String folioCliente) {
        return clienteRepository.findById(folioCliente);
    }

    public Cliente createCliente(Cliente cliente) {
        // Validar que el folio no exista
        if (clienteRepository.existsById(cliente.getFolioCliente())) {
            throw new RuntimeException("El folio de cliente ya existe");
        }

        // Validar email único
        if (cliente.getEmail() != null && clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        return clienteRepository.save(cliente);
    }

    public Cliente updateCliente(String folioCliente, Cliente clienteDetails) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(folioCliente);

        if (optionalCliente.isPresent()) {
            Cliente cliente = optionalCliente.get();

            // Validar email único excluyendo el actual
            if (clienteDetails.getEmail() != null &&
                    clienteRepository.existsByEmailAndNotId(clienteDetails.getEmail(), folioCliente)) {
                throw new RuntimeException("El email ya está registrado por otro cliente");
            }

            cliente.setNombre(clienteDetails.getNombre());
            cliente.setTelefono(clienteDetails.getTelefono());
            cliente.setEmail(clienteDetails.getEmail());
            cliente.setFechaNacimiento(clienteDetails.getFechaNacimiento());
            cliente.setGenero(clienteDetails.getGenero());

            return clienteRepository.save(cliente);
        } else {
            throw new RuntimeException("Cliente no encontrado con folio: " + folioCliente);
        }
    }

    public void deleteCliente(String folioCliente) {
        if (clienteRepository.existsById(folioCliente)) {
            clienteRepository.deleteById(folioCliente);
        } else {
            throw new RuntimeException("Cliente no encontrado con folio: " + folioCliente);
        }
    }

    public void darDeBajaCliente(String folioCliente) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(folioCliente);

        if (optionalCliente.isPresent()) {
            clienteRepository.updateEstatus(folioCliente, "Inactivo");
        } else {
            throw new RuntimeException("Cliente no encontrado con folio: " + folioCliente);
        }
    }

    public void activarCliente(String folioCliente) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(folioCliente);

        if (optionalCliente.isPresent()) {
            clienteRepository.updateEstatus(folioCliente, "Activo");
        } else {
            throw new RuntimeException("Cliente no encontrado con folio: " + folioCliente);
        }
    }

    public List<Cliente> searchClientes(String searchTerm) {
        return clienteRepository.findByNombreContainingIgnoreCase(searchTerm);
    }

    public List<Cliente> getClientesByEstatus(String estatus) {
        return clienteRepository.findByEstatus(estatus);
    }
}