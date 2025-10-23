package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.EjercicioEntity;
import com.example.harmonyGymBack.model.RutinaEntity;
import com.example.harmonyGymBack.repository.ClienteRepository;
import com.example.harmonyGymBack.repository.EjercicioRepository;
import com.example.harmonyGymBack.repository.InstructorRepository;
import com.example.harmonyGymBack.repository.RutinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RutinaServiceImpl {

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private EjercicioRepository ejercicioRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // ==================== MÉTODOS EXISTENTES ====================

    public List<RutinaEntity> findAll() {
        return rutinaRepository.findAll();
    }

    public Optional<RutinaEntity> findByFolioRutina(String folioRutina) {
        return rutinaRepository.findByFolioRutina(folioRutina);
    }

    public List<RutinaEntity> findByInstructor(String folioInstructor) {
        return rutinaRepository.findByFolioInstructor(folioInstructor);
    }

    @Transactional
    public RutinaEntity createRutina(RutinaEntity rutina) {
        if (!instructorRepository.existsById(rutina.getFolioInstructor())) {
            throw new RuntimeException("El instructor no existe: " + rutina.getFolioInstructor());
        }

        if (rutina.getFolioRutina() == null || rutina.getFolioRutina().isEmpty()) {
            String folio = generarFolioRutina();
            rutina.setFolioRutina(folio);
        }

        if (rutina.getEjercicios() != null) {
            for (int i = 0; i < rutina.getEjercicios().size(); i++) {
                rutina.getEjercicios().get(i).setOrden(i + 1);
            }
        }

        calcularDuracionRutina(rutina);
        return rutinaRepository.save(rutina);
    }

    @Transactional
    public RutinaEntity updateRutina(String folioRutina, RutinaEntity rutinaActualizada) {
        Optional<RutinaEntity> rutinaExistente = rutinaRepository.findByFolioRutina(folioRutina);

        if (rutinaExistente.isPresent()) {
            RutinaEntity rutina = rutinaExistente.get();

            rutina.setNombre(rutinaActualizada.getNombre());
            rutina.setDescripcion(rutinaActualizada.getDescripcion());
            rutina.setNivel(rutinaActualizada.getNivel());
            rutina.setObjetivo(rutinaActualizada.getObjetivo());
            rutina.setEstatus(rutinaActualizada.getEstatus());

            if (rutinaActualizada.getEjercicios() != null) {
                for (int i = 0; i < rutinaActualizada.getEjercicios().size(); i++) {
                    rutinaActualizada.getEjercicios().get(i).setOrden(i + 1);
                }
            }
            rutina.setEjercicios(rutinaActualizada.getEjercicios());

            calcularDuracionRutina(rutina);
            return rutinaRepository.save(rutina);
        }

        return null;
    }

    @Transactional
    public boolean deleteRutina(String folioRutina) {
        Optional<RutinaEntity> rutina = rutinaRepository.findByFolioRutina(folioRutina);

        if (rutina.isPresent()) {
            rutinaRepository.eliminarTodasAsignacionesDeRutina(folioRutina);
            rutinaRepository.delete(rutina.get());
            return true;
        }

        return false;
    }

    @Transactional
    public RutinaEntity agregarEjercicio(String folioRutina, RutinaEntity.EjercicioRutina ejercicioRutina) {
        Optional<RutinaEntity> rutinaOpt = rutinaRepository.findByFolioRutina(folioRutina);

        if (rutinaOpt.isPresent()) {
            RutinaEntity rutina = rutinaOpt.get();
            int siguienteOrden = rutina.getEjercicios().size() + 1;
            ejercicioRutina.setOrden(siguienteOrden);

            rutina.getEjercicios().add(ejercicioRutina);
            calcularDuracionRutina(rutina);
            return rutinaRepository.save(rutina);
        }

        return null;
    }


    @Transactional
    public RutinaEntity eliminarEjercicio(String folioRutina, String idEjercicio) {
        Optional<RutinaEntity> rutinaOpt = rutinaRepository.findByFolioRutina(folioRutina);

        if (rutinaOpt.isPresent()) {
            RutinaEntity rutina = rutinaOpt.get();
            rutina.getEjercicios().removeIf(e -> e.getIdEjercicio().equals(idEjercicio));

            for (int i = 0; i < rutina.getEjercicios().size(); i++) {
                rutina.getEjercicios().get(i).setOrden(i + 1);
            }

            calcularDuracionRutina(rutina);
            return rutinaRepository.save(rutina);
        }

        return null;
    }

    // ==================== MÉTODOS PARA ASIGNACIÓN DE RUTINAS A CLIENTES ====================

    @Transactional
    public void asignarRutinaACliente(String folioRutina, String folioCliente, String folioInstructor) {
        if (!rutinaRepository.existsByFolioRutina(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada: " + folioRutina);
        }

        if (!clienteRepository.existsById(folioCliente)) {
            throw new RuntimeException("Cliente no encontrado: " + folioCliente);
        }

        if (!instructorRepository.existsById(folioInstructor)) {
            throw new RuntimeException("Instructor no encontrado: " + folioInstructor);
        }

        if (rutinaRepository.existsAsignacion(folioRutina, folioCliente)) {
            throw new RuntimeException("La rutina ya está asignada a este cliente");
        }

        rutinaRepository.asignarRutinaACliente(folioInstructor, folioRutina, folioCliente);
    }

    @Transactional
    public void desasignarRutinaDeCliente(String folioRutina, String folioCliente) {
        if (!rutinaRepository.existsAsignacion(folioRutina, folioCliente)) {
            throw new RuntimeException("La rutina no está asignada a este cliente");
        }

        rutinaRepository.desasignarRutinaDeCliente(folioRutina, folioCliente);
    }

    public List<RutinaEntity> getRutinasByCliente(String folioCliente) {
        if (!clienteRepository.existsById(folioCliente)) {
            throw new RuntimeException("Cliente no encontrado: " + folioCliente);
        }

        return rutinaRepository.findRutinasByCliente(folioCliente);
    }

    public List<Cliente> getClientesByRutina(String folioRutina) {
        if (!rutinaRepository.existsByFolioRutina(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada: " + folioRutina);
        }

        return rutinaRepository.findClientesByRutina(folioRutina);
    }

    // ==================== MÉTODOS NUEVOS PARA CLIENTES NO ASIGNADOS ====================

    /**
     * Obtiene los clientes que NO están asignados a una rutina específica
     */
    public List<Cliente> getClientesNoAsignadosByRutina(String folioRutina) {
        if (!rutinaRepository.existsByFolioRutina(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada: " + folioRutina);
        }

        try {
            // Intentar usar la query nativa optimizada
            return clienteRepository.findClientesNoAsignadosByRutina(folioRutina);
        } catch (Exception e) {
            // Fallback al método manual si la query nativa falla
            System.err.println("Error en query nativa, usando método manual: " + e.getMessage());
            return getClientesNoAsignadosManual(folioRutina);
        }
    }

    /**
     * Método manual para obtener clientes no asignados (respaldo)
     */
    private List<Cliente> getClientesNoAsignadosManual(String folioRutina) {
        List<Cliente> todosLosClientes = clienteRepository.findAll();
        List<Cliente> clientesAsignados = getClientesByRutina(folioRutina);

        Set<String> foliosAsignados = new HashSet<>();
        for (Cliente cliente : clientesAsignados) {
            foliosAsignados.add(cliente.getFolioCliente());
        }

        List<Cliente> clientesNoAsignados = new ArrayList<>();
        for (Cliente cliente : todosLosClientes) {
            if (!foliosAsignados.contains(cliente.getFolioCliente())) {
                clientesNoAsignados.add(cliente);
            }
        }

        return clientesNoAsignados;
    }

    // ==================== MÉTODOS NUEVOS PARA ASIGNACIÓN MÚLTIPLE ====================

    @Transactional
    public Map<String, Object> asignarRutinaAMultiplesClientes(String folioRutina, List<String> foliosClientes, String folioInstructor) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> asignacionesExitosas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        if (!rutinaRepository.existsByFolioRutina(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada: " + folioRutina);
        }

        if (!instructorRepository.existsById(folioInstructor)) {
            throw new RuntimeException("Instructor no encontrado: " + folioInstructor);
        }

        List<String> clientesYaAsignados = rutinaRepository.findClientesYaAsignados(folioRutina, foliosClientes);

        for (String folioCliente : foliosClientes) {
            try {
                if (!clienteRepository.existsById(folioCliente)) {
                    errores.add("Cliente no encontrado: " + folioCliente);
                    continue;
                }

                if (clientesYaAsignados.contains(folioCliente)) {
                    errores.add("La rutina ya está asignada al cliente: " + folioCliente);
                    continue;
                }

                rutinaRepository.asignarRutinaAClienteIndividual(folioInstructor, folioRutina, folioCliente);
                asignacionesExitosas.add(folioCliente);

            } catch (Exception e) {
                errores.add("Error asignando al cliente " + folioCliente + ": " + e.getMessage());
            }
        }

        resultado.put("asignacionesExitosas", asignacionesExitosas);
        resultado.put("errores", errores);
        resultado.put("totalExitosas", asignacionesExitosas.size());
        resultado.put("totalErrores", errores.size());

        return resultado;
    }

    @Transactional
    public Map<String, Object> desasignarRutinaDeMultiplesClientes(String folioRutina, List<String> foliosClientes) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> desasignacionesExitosas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (String folioCliente : foliosClientes) {
            try {
                if (!rutinaRepository.existsAsignacion(folioRutina, folioCliente)) {
                    errores.add("La rutina no está asignada al cliente: " + folioCliente);
                    continue;
                }

                rutinaRepository.desasignarRutinaDeCliente(folioRutina, folioCliente);
                desasignacionesExitosas.add(folioCliente);

            } catch (Exception e) {
                errores.add("Error desasignando al cliente " + folioCliente + ": " + e.getMessage());
            }
        }

        resultado.put("desasignacionesExitosas", desasignacionesExitosas);
        resultado.put("errores", errores);
        resultado.put("totalExitosas", desasignacionesExitosas.size());
        resultado.put("totalErrores", errores.size());

        return resultado;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private String generarFolioRutina() {
        Optional<Integer> maxFolio = rutinaRepository.findMaxFolioNumber();
        int nextNumber = maxFolio.map(n -> n + 1).orElse(1);
        return String.format("RUT%03d", nextNumber);
    }

    private void calcularDuracionRutina(RutinaEntity rutina) {
        if (rutina.getEjercicios() == null || rutina.getEjercicios().isEmpty()) {
            rutina.setDuracionEstimada(0);
            return;
        }

        int duracionTotal = 0;
        for (RutinaEntity.EjercicioRutina ejercicioRutina : rutina.getEjercicios()) {
            Optional<EjercicioEntity> ejercicioOpt = ejercicioRepository.findByIdEjercicio(ejercicioRutina.getIdEjercicio());

            if (ejercicioOpt.isPresent()) {
                EjercicioEntity ejercicio = ejercicioOpt.get();
                Integer tiempoEjercicio = ejercicio.getTiempo() != null ? ejercicio.getTiempo() : ejercicio.getDuracionEstimada();

                if (tiempoEjercicio != null && ejercicioRutina.getSeriesEjercicio() != null) {
                    int duracionEjercicio = tiempoEjercicio * ejercicioRutina.getSeriesEjercicio();
                    int descansoTotal = (ejercicioRutina.getDescansoEjercicio() != null ? ejercicioRutina.getDescansoEjercicio() : 0) * ejercicioRutina.getSeriesEjercicio();
                    duracionTotal += duracionEjercicio + descansoTotal;
                }
            }
        }

        rutina.setDuracionEstimada(duracionTotal);
    }

    // ==================== MÉTODOS PARA CAMBIAR ESTATUS ====================

    @Transactional
    public RutinaEntity cambiarEstatusRutina(String folioRutina, String nuevoEstatus) {
        Optional<RutinaEntity> rutinaOpt = rutinaRepository.findByFolioRutina(folioRutina);

        if (rutinaOpt.isPresent()) {
            RutinaEntity rutina = rutinaOpt.get();

            // Validar que el nuevo estatus sea válido
            if (!"Activa".equals(nuevoEstatus) && !"Inactiva".equals(nuevoEstatus)) {
                throw new RuntimeException("Estatus no válido. Debe ser 'Activa' o 'Inactiva'");
            }

            rutina.setEstatus(nuevoEstatus);
            return rutinaRepository.save(rutina);
        }

        throw new RuntimeException("Rutina no encontrada: " + folioRutina);
    }

    @Transactional
    public RutinaEntity activarRutina(String folioRutina) {
        return cambiarEstatusRutina(folioRutina, "Activa");
    }

    @Transactional
    public RutinaEntity inactivarRutina(String folioRutina) {
        return cambiarEstatusRutina(folioRutina, "Inactiva");
    }

    // Método para obtener rutinas por estatus
    public List<RutinaEntity> findByEstatus(String estatus) {
        if (!"Activa".equals(estatus) && !"Inactiva".equals(estatus)) {
            throw new RuntimeException("Estatus no válido. Debe ser 'Activa' o 'Inactiva'");
        }
        return rutinaRepository.findByEstatus(estatus);
    }
}