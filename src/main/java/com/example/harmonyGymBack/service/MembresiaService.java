package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.model.TipoMembresia;
import com.example.harmonyGymBack.model.enums.EstatusCatalogo;
import com.example.harmonyGymBack.repository.MembresiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class MembresiaService {

    @Autowired
    private MembresiaRepository membresiaRepository;

    // Crear nueva membresía
    public Membresia crearMembresia(Membresia membresia) {
        // Validar que no exista una membresía del mismo tipo activa
        Optional<Membresia> existente = membresiaRepository
                .findByTipoAndEstatus(membresia.getTipo(), EstatusCatalogo.Activa);

        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una membresía activa del tipo: " + membresia.getTipo());
        }

        // Siempre generar el folio al crear, ignorando cualquier id que venga del cliente
        String nuevoId = generarSiguienteFolio();
        membresia.setIdMembresia(nuevoId);

        if (membresia.getEstatus() == null) {
            membresia.setEstatus(EstatusCatalogo.Activa);
        }

        if (membresia.getFechaCreacion() == null) {
            membresia.setFechaCreacion(java.time.LocalDateTime.now());
        }

        return membresiaRepository.save(membresia);
    }

    public synchronized String generarSiguienteFolio() {
        List<Membresia> todas = membresiaRepository.findAll();
        int maxNumero = 0;
        for (Membresia m : todas) {
            String mid = m.getIdMembresia();
            if (mid != null && mid.startsWith("MEM")) {
                try {
                    int num = Integer.parseInt(mid.substring(3).trim());
                    if (num > maxNumero) {
                        maxNumero = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        int numero = maxNumero + 1;
        return String.format("MEM%03d", numero);
    }

    public List<Membresia> obtenerTodasLasMembresias() {
        return membresiaRepository.findAll();
    }

    public Membresia obtenerMembresiaPorId(String id) {
        return membresiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada con ID: " + id));
    }

    public List<Membresia> obtenerMembresiasActivas() {
        return membresiaRepository.findByEstatus(EstatusCatalogo.Activa);
    }

    public List<Membresia> obtenerMembresiasPorTipo(TipoMembresia tipo) {
        return membresiaRepository.findByTipo(tipo);
    }

    public List<Membresia> obtenerMembresiasPorPrecioMaximo(Double precioMaximo) {
        return membresiaRepository.findByPrecioLessThanEqual(precioMaximo);
    }

    public List<Membresia> obtenerMembresiasPorDuracionMinima(Integer duracionMinima) {
        return membresiaRepository.findByDuracionGreaterThanEqual(duracionMinima);
    }

    public Membresia actualizarMembresia(String id, Membresia membresiaActualizada) {
        Membresia membresiaExistente = obtenerMembresiaPorId(id);

        membresiaExistente.setTipo(membresiaActualizada.getTipo());
        membresiaExistente.setPrecio(membresiaActualizada.getPrecio());
        membresiaExistente.setDuracion(membresiaActualizada.getDuracion());
        membresiaExistente.setDescripcion(membresiaActualizada.getDescripcion());
        membresiaExistente.setBeneficios(membresiaActualizada.getBeneficios());
        if (membresiaActualizada.getEstatus() != null) {
            membresiaExistente.setEstatus(membresiaActualizada.getEstatus());
        }

        return membresiaRepository.save(membresiaExistente);
    }

    public Membresia desactivarMembresia(String id) {
        Membresia membresia = obtenerMembresiaPorId(id);
        membresia.setEstatus(EstatusCatalogo.Inactiva);
        return membresiaRepository.save(membresia);
    }

    public Membresia activarMembresia(String id) {
        Membresia membresia = obtenerMembresiaPorId(id);
        membresia.setEstatus(EstatusCatalogo.Activa);
        return membresiaRepository.save(membresia);
    }

    public List<Membresia> buscarMembresiasPorBeneficio(String beneficio) {
        return membresiaRepository.findByBeneficioContaining(beneficio);
    }

    public Map<String, Object> obtenerEstadisticas() {
        List<Membresia> activas = obtenerMembresiasActivas();
        long totalMembresias = membresiaRepository.count();
        long membresiasActivas = activas.size();
        long membresiasInactivas = totalMembresias - membresiasActivas;

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalMembresias", totalMembresias);
        estadisticas.put("membresiasActivas", membresiasActivas);
        estadisticas.put("membresiasInactivas", membresiasInactivas);

        return estadisticas;
    }
}
