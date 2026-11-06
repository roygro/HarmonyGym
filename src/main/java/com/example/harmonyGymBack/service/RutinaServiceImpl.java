package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.AsignacionRutina;
import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.EjercicioEntity;
import com.example.harmonyGymBack.model.Instructor;
import com.example.harmonyGymBack.model.RutinaEntity;
import com.example.harmonyGymBack.model.enums.EstatusRutina;
import com.example.harmonyGymBack.repository.AsignacionRutinaRepository;
import com.example.harmonyGymBack.repository.ClienteRepository;
import com.example.harmonyGymBack.repository.EjercicioRepository;
import com.example.harmonyGymBack.repository.InstructorRepository;
import com.example.harmonyGymBack.repository.RutinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RutinaServiceImpl {

    private final RutinaRepository rutinaRepository;

    private final AsignacionRutinaRepository asignacionRutinaRepository;

    private final EjercicioRepository ejercicioRepository;

    private final InstructorRepository instructorRepository;

    private final ClienteRepository clienteRepository;

    public RutinaServiceImpl(RutinaRepository rutinaRepository, AsignacionRutinaRepository asignacionRutinaRepository, EjercicioRepository ejercicioRepository, InstructorRepository instructorRepository, ClienteRepository clienteRepository) {
        this.rutinaRepository = rutinaRepository;
        this.asignacionRutinaRepository = asignacionRutinaRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.instructorRepository = instructorRepository;
        this.clienteRepository = clienteRepository;
    }


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

        if (rutina.getEstatus() == null) {
            rutina.setEstatus(EstatusRutina.Activa);
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
            if (rutinaActualizada.getEstatus() != null) {
                rutina.setEstatus(rutinaActualizada.getEstatus());
            }

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
            asignacionRutinaRepository.eliminarTodasAsignacionesDeRutina(folioRutina);
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
        RutinaEntity rutina = rutinaRepository.findByFolioRutina(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + folioRutina));

        Cliente cliente = clienteRepository.findById(folioCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + folioCliente));

        Instructor instructor = instructorRepository.findById(folioInstructor)
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado: " + folioInstructor));

        if (asignacionRutinaRepository.existsByRutina_FolioRutinaAndCliente_FolioCliente(folioRutina, folioCliente)) {
            throw new RuntimeException("La rutina ya está asignada a este cliente");
        }

        AsignacionRutina asignacion = new AsignacionRutina(rutina, cliente, instructor);
        asignacionRutinaRepository.save(asignacion);
    }

    @Transactional
    public void desasignarRutinaDeCliente(String folioRutina, String folioCliente) {
        if (!asignacionRutinaRepository.existsByRutina_FolioRutinaAndCliente_FolioCliente(folioRutina, folioCliente)) {
            throw new RuntimeException("La rutina no está asignada a este cliente");
        }

        asignacionRutinaRepository.desasignarRutinaDeCliente(folioRutina, folioCliente);
    }

    public List<RutinaEntity> getRutinasByCliente(String folioCliente) {
        if (!clienteRepository.existsById(folioCliente)) {
            throw new RuntimeException("Cliente no encontrado: " + folioCliente);
        }

        return asignacionRutinaRepository.findRutinasByCliente(folioCliente);
    }

    public List<Cliente> getClientesByRutina(String folioRutina) {
        if (!rutinaRepository.existsByFolioRutina(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada: " + folioRutina);
        }

        return asignacionRutinaRepository.findClientesByRutina(folioRutina);
    }

    public List<Cliente> getClientesNoAsignadosByRutina(String folioRutina) {
        if (!rutinaRepository.existsByFolioRutina(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada: " + folioRutina);
        }

        return clienteRepository.findClientesNoAsignadosByRutina(folioRutina);
    }

    @Transactional
    public Map<String, Object> asignarRutinaAMultiplesClientes(String folioRutina, List<String> foliosClientes, String folioInstructor) {
        Map<String, Object> resultado = new HashMap<>();
        List<String> asignacionesExitosas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        RutinaEntity rutina = rutinaRepository.findByFolioRutina(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + folioRutina));

        Instructor instructor = instructorRepository.findById(folioInstructor)
                .orElseThrow(() -> new RuntimeException("Instructor no encontrado: " + folioInstructor));

        List<String> clientesYaAsignados = asignacionRutinaRepository.findClientesYaAsignados(folioRutina, foliosClientes);

        for (String folioCliente : foliosClientes) {
            try {
                Optional<Cliente> clienteOpt = clienteRepository.findById(folioCliente);
                if (clienteOpt.isEmpty()) {
                    errores.add("Cliente no encontrado: " + folioCliente);
                    continue;
                }

                if (clientesYaAsignados.contains(folioCliente)) {
                    errores.add("La rutina ya está asignada al cliente: " + folioCliente);
                    continue;
                }

                AsignacionRutina asignacion = new AsignacionRutina(rutina, clienteOpt.get(), instructor);
                asignacionRutinaRepository.save(asignacion);
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
                if (!asignacionRutinaRepository.existsByRutina_FolioRutinaAndCliente_FolioCliente(folioRutina, folioCliente)) {
                    errores.add("La rutina no está asignada al cliente: " + folioCliente);
                    continue;
                }

                asignacionRutinaRepository.desasignarRutinaDeCliente(folioRutina, folioCliente);
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

    @Transactional
    public RutinaEntity cambiarEstatusRutina(String folioRutina, EstatusRutina nuevoEstatus) {
        RutinaEntity rutina = rutinaRepository.findByFolioRutina(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada: " + folioRutina));

        rutina.setEstatus(nuevoEstatus);
        return rutinaRepository.save(rutina);
    }

    @Transactional
    public RutinaEntity activarRutina(String folioRutina) {
        return cambiarEstatusRutina(folioRutina, EstatusRutina.Activa);
    }

    @Transactional
    public RutinaEntity inactivarRutina(String folioRutina) {
        return cambiarEstatusRutina(folioRutina, EstatusRutina.Inactiva);
    }

    public List<RutinaEntity> findByEstatus(EstatusRutina estatus) {
        return rutinaRepository.findByEstatus(estatus);
    }
}
