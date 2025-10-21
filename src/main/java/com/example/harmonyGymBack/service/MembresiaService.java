package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.model.TipoMembresia;
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
                .findByTipoAndEstatus(membresia.getTipo(), "Activa"); // Cambié "ACTIVO" por "Activa"

        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una membresía activa del tipo: " + membresia.getTipo());
        }

        // Generar ID automáticamente si no viene
        if (membresia.getIdMembresia() == null) {
            // Buscar el último ID y generar el siguiente
            List<Membresia> todas = membresiaRepository.findAll();
            String ultimoId = todas.stream()
                    .map(Membresia::getIdMembresia)
                    .filter(id -> id.startsWith("MEM"))
                    .max(String::compareTo)
                    .orElse("MEM000");

            // Generar nuevo ID (MEM004, MEM005, etc.)
            int numero = Integer.parseInt(ultimoId.substring(3)) + 1;
            String nuevoId = "MEM" + String.format("%03d", numero);
            membresia.setIdMembresia(nuevoId);
        }

        // Asegurar que la fecha de creación se establezca
        if (membresia.getFechaCreacion() == null) {
            membresia.setFechaCreacion(java.time.LocalDateTime.now());
        }

        return membresiaRepository.save(membresia);
    }

    // Obtener todas las membresías
    public List<Membresia> obtenerTodasLasMembresias() {
        return membresiaRepository.findAll();
    }

    // Obtener membresía por ID - CAMBIADO A String
    public Membresia obtenerMembresiaPorId(String id) {
        return membresiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membresía no encontrada con ID: " + id));
    }

    // Obtener membresías activas
    public List<Membresia> obtenerMembresiasActivas() {
        return membresiaRepository.findByEstatus("Activa"); // Cambié "ACTIVO" por "Activa"
    }

    // Obtener membresías por tipo
    public List<Membresia> obtenerMembresiasPorTipo(TipoMembresia tipo) {
        return membresiaRepository.findByTipo(tipo);
    }

    // Obtener membresías por rango de precio
    public List<Membresia> obtenerMembresiasPorPrecioMaximo(Double precioMaximo) {
        return membresiaRepository.findByPrecioLessThanEqual(precioMaximo);
    }

    // Obtener membresías por duración mínima
    public List<Membresia> obtenerMembresiasPorDuracionMinima(Integer duracionMinima) {
        return membresiaRepository.findByDuracionGreaterThanEqual(duracionMinima);
    }

    // Actualizar membresía - CAMBIADO A String
    public Membresia actualizarMembresia(String id, Membresia membresiaActualizada) {
        Membresia membresiaExistente = obtenerMembresiaPorId(id);

        // Actualizar solo los campos permitidos (no el ID)
        membresiaExistente.setTipo(membresiaActualizada.getTipo());
        membresiaExistente.setPrecio(membresiaActualizada.getPrecio());
        membresiaExistente.setDuracion(membresiaActualizada.getDuracion());
        membresiaExistente.setDescripcion(membresiaActualizada.getDescripcion());
        membresiaExistente.setBeneficios(membresiaActualizada.getBeneficios());
        membresiaExistente.setEstatus(membresiaActualizada.getEstatus());

        return membresiaRepository.save(membresiaExistente);
    }

    // Desactivar membresía (eliminación lógica) - CAMBIADO A String
    public Membresia desactivarMembresia(String id) {
        Membresia membresia = obtenerMembresiaPorId(id);
        membresia.setEstatus("Inactiva"); // Cambié "INACTIVO" por "Inactiva"
        return membresiaRepository.save(membresia);
    }

    // Activar membresía - CAMBIADO A String
    public Membresia activarMembresia(String id) {
        Membresia membresia = obtenerMembresiaPorId(id);
        membresia.setEstatus("Activa"); // Cambié "ACTIVO" por "Activa"
        return membresiaRepository.save(membresia);
    }

    // Buscar membresías por beneficio
    public List<Membresia> buscarMembresiasPorBeneficio(String beneficio) {
        return membresiaRepository.findByBeneficioContaining(beneficio);
    }

    // Obtener estadísticas básicas
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