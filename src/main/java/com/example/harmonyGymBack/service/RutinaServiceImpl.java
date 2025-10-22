package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.RutinaEntity;
import com.example.harmonyGymBack.repository.RutinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RutinaServiceImpl {

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private InstructorServiceImpl instructorService;

    @Autowired
    private ClienteServiceImpl clienteService;

    // ===== MÉTODOS BÁSICOS =====
    public List<RutinaEntity> obtenerTodasLasRutinas() {
        return rutinaRepository.findAll();
    }

    public Optional<RutinaEntity> obtenerRutinaPorId(String folioRutina) {
        return rutinaRepository.findById(folioRutina);
    }

    public boolean existeRutina(String folioRutina) {
        return rutinaRepository.existsByFolioRutina(folioRutina);
    }

    @Transactional
    public RutinaEntity crearRutina(RutinaEntity rutina) {
        if (rutinaRepository.existsByFolioRutina(rutina.getFolioRutina())) {
            throw new RuntimeException("Ya existe una rutina con el folio: " + rutina.getFolioRutina());
        }

        // Validar que el instructor existe si se proporciona
        if (rutina.getFolioInstructor() != null &&
                !instructorService.existeInstructor(rutina.getFolioInstructor())) {
            throw new RuntimeException("El instructor no existe: " + rutina.getFolioInstructor());
        }

        return rutinaRepository.save(rutina);
    }

    @Transactional
    public RutinaEntity crearRutinaPorInstructor(RutinaEntity rutina, String folioInstructor) {
        if (!instructorService.existeInstructor(folioInstructor)) {
            throw new RuntimeException("El instructor no existe: " + folioInstructor);
        }
        if (rutinaRepository.existsByFolioRutina(rutina.getFolioRutina())) {
            throw new RuntimeException("Ya existe una rutina con el folio: " + rutina.getFolioRutina());
        }
        rutina.setFolioInstructor(folioInstructor);
        return rutinaRepository.save(rutina);
    }

    // ===== MÉTODOS DE CAMBIO DE ESTATUS (CORREGIDOS) =====

    @Transactional
    public void cambiarEstatusRutina(String folioRutina, String estatus) {
        RutinaEntity rutina = rutinaRepository.findById(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con folio: " + folioRutina));

        // Validar y normalizar el estatus
        String estatusNormalizado = normalizarEstatus(estatus);

        if (!esEstatusValido(estatusNormalizado)) {
            throw new RuntimeException("Estatus no válido: " + estatus + ". Los valores permitidos son: Activa, Inactiva");
        }

        // Verificar si ya está en el estado deseado
        if (estatusNormalizado.equals(rutina.getEstatus())) {
            throw new RuntimeException("La rutina ya está " + estatusNormalizado.toLowerCase());
        }

        rutina.setEstatus(estatusNormalizado);
        rutinaRepository.save(rutina);
    }

    @Transactional
    public void activarRutina(String folioRutina) {
        RutinaEntity rutina = rutinaRepository.findById(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con folio: " + folioRutina));

        if ("Activa".equals(rutina.getEstatus())) {
            throw new RuntimeException("La rutina ya está activa");
        }

        rutinaRepository.activarRutina(folioRutina);
    }

    @Transactional
    public void desactivarRutina(String folioRutina) {
        RutinaEntity rutina = rutinaRepository.findById(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con folio: " + folioRutina));

        if ("Inactiva".equals(rutina.getEstatus())) {
            throw new RuntimeException("La rutina ya está inactiva");
        }

        rutinaRepository.desactivarRutina(folioRutina);
    }

    @Transactional
    public void desactivarRutinaPorInstructor(String folioRutina, String folioInstructor) {
        // Verificar que el instructor existe
        if (!instructorService.existeInstructor(folioInstructor)) {
            throw new RuntimeException("Instructor no encontrado: " + folioInstructor);
        }

        // Verificar que la rutina existe y pertenece al instructor
        RutinaEntity rutina = rutinaRepository.findById(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con folio: " + folioRutina));

        if (!folioInstructor.equals(rutina.getFolioInstructor())) {
            throw new RuntimeException("La rutina no pertenece al instructor especificado");
        }

        if ("Inactiva".equals(rutina.getEstatus())) {
            throw new RuntimeException("La rutina ya está inactiva");
        }

        rutinaRepository.desactivarRutinaPorInstructor(folioRutina, folioInstructor);
    }

    @Transactional
    public void desactivarTodasLasRutinasPorInstructor(String folioInstructor) {
        // Verificar que el instructor existe
        if (!instructorService.existeInstructor(folioInstructor)) {
            throw new RuntimeException("Instructor no encontrado: " + folioInstructor);
        }

        rutinaRepository.desactivarTodasLasRutinasPorInstructor(folioInstructor);
    }

    @Transactional
    public void reactivarRutina(String folioRutina) {
        activarRutina(folioRutina); // Reutilizar el método existente
    }

    // Métodos auxiliares para validación de estatus
    private String normalizarEstatus(String estatus) {
        if (estatus == null) return "Activa";

        // Limpiar y normalizar el string
        estatus = estatus.trim().replace("\"", "").replace("'", "");

        // Mapear valores comunes a los valores de la base de datos
        if (estatus.equalsIgnoreCase("active") || estatus.equalsIgnoreCase("activo")) {
            return "Activa";
        } else if (estatus.equalsIgnoreCase("inactive") || estatus.equalsIgnoreCase("inactivo")) {
            return "Inactiva";
        }

        return estatus;
    }

    private boolean esEstatusValido(String estatus) {
        return "Activa".equals(estatus) || "Inactiva".equals(estatus);
    }

    public boolean isRutinaActiva(String folioRutina) {
        return rutinaRepository.isRutinaActiva(folioRutina);
    }

    // ===== MÉTODOS DE ASIGNACIÓN =====
    @Transactional
    public void asignarRutinaACliente(String folioRutina, String folioCliente) {
        RutinaEntity rutina = rutinaRepository.findById(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con folio: " + folioRutina));

        // Verificar que la rutina tenga un instructor asignado
        if (rutina.getFolioInstructor() == null) {
            throw new RuntimeException("La rutina no tiene un instructor asignado. No se puede asignar a clientes.");
        }

        // Verificar que el instructor existe
        if (!instructorService.existeInstructor(rutina.getFolioInstructor())) {
            throw new RuntimeException("El instructor de esta rutina no existe: " + rutina.getFolioInstructor());
        }

        Cliente cliente = clienteService.getClienteById(folioCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con folio: " + folioCliente));

        if (!"Activo".equals(cliente.getEstatus())) {
            throw new RuntimeException("No se puede asignar rutina a un cliente inactivo");
        }

        // Verificar que no esté ya asignada
        if (rutinaRepository.existsAsignacion(folioRutina, folioCliente)) {
            throw new RuntimeException("El cliente ya tiene asignada esta rutina");
        }

        // Insertar en la tabla asigna
        rutinaRepository.insertAsignacion(
                rutina.getFolioInstructor(),
                folioRutina,
                folioCliente
        );
    }

    @Transactional
    public AsignacionResultado asignarRutinaAMultiplesClientes(String folioRutina, List<String> foliosClientes) {
        RutinaEntity rutina = rutinaRepository.findById(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con folio: " + folioRutina));

        // Verificar que la rutina tenga un instructor asignado
        if (rutina.getFolioInstructor() == null) {
            throw new RuntimeException("La rutina no tiene un instructor asignado. No se puede asignar a clientes.");
        }

        AsignacionResultado resultado = new AsignacionResultado();

        for (String folioCliente : foliosClientes) {
            try {
                Cliente cliente = clienteService.getClienteById(folioCliente)
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + folioCliente));

                if (!"Activo".equals(cliente.getEstatus())) {
                    resultado.agregarError("Cliente inactivo: " + folioCliente);
                    continue;
                }

                if (rutinaRepository.existsAsignacion(folioRutina, folioCliente)) {
                    resultado.agregarDuplicado(folioCliente);
                    continue;
                }

                // Asignar rutina al cliente
                rutinaRepository.insertAsignacion(
                        rutina.getFolioInstructor(),
                        folioRutina,
                        folioCliente
                );
                resultado.agregarExitoso(folioCliente);

            } catch (RuntimeException e) {
                resultado.agregarError("Error con cliente " + folioCliente + ": " + e.getMessage());
            }
        }

        return resultado;
    }

    @Transactional
    public void removerAsignacionRutina(String folioRutina, String folioCliente) {
        // Verificar que la rutina existe
        if (!rutinaRepository.existsById(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
        }

        // Verificar que el cliente existe
        if (!clienteService.getClienteById(folioCliente).isPresent()) {
            throw new RuntimeException("Cliente no encontrado con folio: " + folioCliente);
        }

        // Verificar que la asignación existe
        if (!rutinaRepository.existsAsignacion(folioRutina, folioCliente)) {
            throw new RuntimeException("No se encontró la asignación para eliminar");
        }

        // Eliminar de la tabla asigna
        rutinaRepository.deleteAsignacion(folioRutina, folioCliente);
    }

    // ===== MÉTODOS PARA GESTIONAR EJERCICIOS EN RUTINAS =====

    @Transactional
    public void agregarEjercicioARutina(String folioRutina, RutinaEntity.AgregarEjercicioRequest request) {
        // Verificar que la rutina existe
        RutinaEntity rutina = rutinaRepository.findById(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con folio: " + folioRutina));

        // Verificar que el ejercicio existe y está activo
        if (!rutinaRepository.existeEjercicioActivo(request.getIdEjercicio())) {
            throw new RuntimeException("Ejercicio no encontrado o inactivo con ID: " + request.getIdEjercicio());
        }

        // Verificar que el ejercicio no esté ya en la rutina
        if (rutinaRepository.existeEjercicioEnRutina(folioRutina, request.getIdEjercicio())) {
            throw new RuntimeException("El ejercicio ya está agregado a esta rutina");
        }

        // Determinar el orden si no se proporciona
        Integer orden = request.getOrden();
        if (orden == null) {
            orden = rutinaRepository.findMaxOrdenByRutina(folioRutina) + 1;
        }

        // Insertar ejercicio en la rutina
        rutinaRepository.agregarEjercicioARutina(
                folioRutina,
                request.getIdEjercicio(),
                orden,
                request.getSeriesEjercicio(),
                request.getRepeticionesEjercicio(),
                request.getDescansoEjercicio(),
                request.getObservaciones()
        );

        // Actualizar tiempo estimado de la rutina
        actualizarTiempoEstimadoRutina(folioRutina);
    }

    @Transactional
    public void removerEjercicioDeRutina(String folioRutina, String idEjercicio) {
        // Verificar que la rutina existe
        if (!rutinaRepository.existsById(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
        }

        // Verificar que el ejercicio existe
        if (!rutinaRepository.existeEjercicioActivo(idEjercicio)) {
            throw new RuntimeException("Ejercicio no encontrado con ID: " + idEjercicio);
        }

        // Verificar que el ejercicio está en la rutina
        if (!rutinaRepository.existeEjercicioEnRutina(folioRutina, idEjercicio)) {
            throw new RuntimeException("El ejercicio no está en esta rutina");
        }

        // Eliminar ejercicio de la rutina
        rutinaRepository.eliminarEjercicioDeRutina(folioRutina, idEjercicio);

        // Actualizar tiempo estimado de la rutina
        actualizarTiempoEstimadoRutina(folioRutina);
    }

    @Transactional
    public void actualizarOrdenEjercicios(String folioRutina, List<String> idsEjerciciosEnOrden) {
        // Verificar que la rutina existe
        if (!rutinaRepository.existsById(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
        }

        // Actualizar el orden de cada ejercicio
        for (int i = 0; i < idsEjerciciosEnOrden.size(); i++) {
            String idEjercicio = idsEjerciciosEnOrden.get(i);
            if (!rutinaRepository.existeEjercicioEnRutina(folioRutina, idEjercicio)) {
                throw new RuntimeException("El ejercicio " + idEjercicio + " no está en esta rutina");
            }
            rutinaRepository.actualizarOrdenEjercicio(folioRutina, idEjercicio, i + 1);
        }
    }

    @Transactional
    public void actualizarParametrosEjercicioEnRutina(String folioRutina, String idEjercicio,
                                                      Integer series, Integer repeticiones,
                                                      Integer descanso, String observaciones) {
        // Verificar que la rutina existe
        if (!rutinaRepository.existsById(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
        }

        // Verificar que el ejercicio está en la rutina
        if (!rutinaRepository.existeEjercicioEnRutina(folioRutina, idEjercicio)) {
            throw new RuntimeException("El ejercicio no está en esta rutina");
        }

        // Actualizar parámetros
        rutinaRepository.actualizarParametrosEjercicio(
                folioRutina, idEjercicio, series, repeticiones, descanso, observaciones
        );

        // Actualizar tiempo estimado de la rutina
        actualizarTiempoEstimadoRutina(folioRutina);
    }

    // ===== MÉTODOS DE CONSULTA PARA EJERCICIOS =====

    public List<RutinaEntity.EjercicioRutinaDTO> obtenerEjerciciosDeRutina(String folioRutina) {
        // Verificar que la rutina existe
        if (!rutinaRepository.existsById(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
        }

        List<Object[]> resultados = rutinaRepository.findEjerciciosByRutina(folioRutina);
        List<RutinaEntity.EjercicioRutinaDTO> ejercicios = new ArrayList<>();

        for (Object[] resultado : resultados) {
            RutinaEntity.EjercicioRutinaDTO dto = new RutinaEntity.EjercicioRutinaDTO(
                    (String) resultado[0], // id_ejercicio
                    (String) resultado[1], // nombre
                    resultado[2] != null ? ((Number) resultado[2]).intValue() : null, // tiempo
                    resultado[3] != null ? ((Number) resultado[3]).intValue() : null, // series
                    resultado[4] != null ? ((Number) resultado[4]).intValue() : null, // repeticiones
                    resultado[5] != null ? ((Number) resultado[5]).intValue() : null, // descanso
                    (String) resultado[6], // equipo_necesario
                    (String) resultado[7], // grupo_muscular
                    (String) resultado[8], // instrucciones
                    resultado[9] != null ? ((Number) resultado[9]).intValue() : null, // orden
                    resultado[10] != null ? ((Number) resultado[10]).intValue() : null, // series_ejercicio
                    resultado[11] != null ? ((Number) resultado[11]).intValue() : null, // repeticiones_ejercicio
                    resultado[12] != null ? ((Number) resultado[12]).intValue() : null, // descanso_ejercicio
                    (String) resultado[13]  // observaciones
            );
            ejercicios.add(dto);
        }

        return ejercicios;
    }

    public RutinaEntity obtenerRutinaConEjercicios(String folioRutina) {
        Optional<RutinaEntity> rutinaOpt = rutinaRepository.findById(folioRutina);
        if (rutinaOpt.isPresent()) {
            RutinaEntity rutina = rutinaOpt.get();
            List<RutinaEntity.EjercicioRutinaDTO> ejercicios = obtenerEjerciciosDeRutina(folioRutina);
            rutina.setEjercicios(ejercicios);
            return rutina;
        }
        throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
    }

    public List<RutinaEntity.EjercicioSimpleDTO> obtenerTodosLosEjercicios() {
        List<Object[]> resultados = rutinaRepository.findAllEjerciciosActivos();
        return convertirResultadosAEjerciciosSimples(resultados);
    }

    public List<RutinaEntity.EjercicioSimpleDTO> buscarEjerciciosPorNombre(String nombre) {
        List<Object[]> resultados = rutinaRepository.findEjerciciosByNombre(nombre);
        return convertirResultadosAEjerciciosSimples(resultados);
    }

    public List<RutinaEntity.EjercicioSimpleDTO> buscarEjerciciosPorGrupoMuscular(String grupoMuscular) {
        List<Object[]> resultados = rutinaRepository.findEjerciciosByGrupoMuscular(grupoMuscular);
        return convertirResultadosAEjerciciosSimples(resultados);
    }

    public RutinaEntity.EjercicioSimpleDTO obtenerEjercicioPorId(String idEjercicio) {
        List<Object[]> resultados = rutinaRepository.findEjercicioById(idEjercicio);
        if (resultados.isEmpty()) {
            throw new RuntimeException("Ejercicio no encontrado con ID: " + idEjercicio);
        }
        return convertirResultadoAEjercicioSimple(resultados.get(0));
    }

    public boolean existeEjercicio(String idEjercicio) {
        return rutinaRepository.existeEjercicioActivo(idEjercicio);
    }

    // ===== MÉTODOS DE CÁLCULO =====

    public Integer calcularTiempoTotalRutina(String folioRutina) {
        if (!rutinaRepository.existsById(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
        }

        Integer tiempoTotal = rutinaRepository.calcularTiempoTotalRutina(folioRutina);
        return tiempoTotal != null ? tiempoTotal : 0;
    }

    @Transactional
    public void actualizarTiempoEstimadoRutina(String folioRutina) {
        Integer tiempoTotal = calcularTiempoTotalRutina(folioRutina);

        // Convertir segundos a minutos (redondeando hacia arriba)
        Integer duracionEstimada = (int) Math.ceil(tiempoTotal / 60.0);

        RutinaEntity rutina = rutinaRepository.findById(folioRutina)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada"));

        rutina.setDuracionEstimada(duracionEstimada);
        rutinaRepository.save(rutina);
    }

    // ===== MÉTODOS AUXILIARES =====

    private List<RutinaEntity.EjercicioSimpleDTO> convertirResultadosAEjerciciosSimples(List<Object[]> resultados) {
        List<RutinaEntity.EjercicioSimpleDTO> ejercicios = new ArrayList<>();
        for (Object[] resultado : resultados) {
            ejercicios.add(convertirResultadoAEjercicioSimple(resultado));
        }
        return ejercicios;
    }

    private RutinaEntity.EjercicioSimpleDTO convertirResultadoAEjercicioSimple(Object[] resultado) {
        return new RutinaEntity.EjercicioSimpleDTO(
                (String) resultado[0], // id_ejercicio
                (String) resultado[1], // nombre
                resultado[2] != null ? ((Number) resultado[2]).intValue() : null, // tiempo
                resultado[3] != null ? ((Number) resultado[3]).intValue() : null, // series
                resultado[4] != null ? ((Number) resultado[4]).intValue() : null, // repeticiones
                resultado[5] != null ? ((Number) resultado[5]).intValue() : null, // descanso
                (String) resultado[6], // equipo_necesario
                (String) resultado[7], // grupo_muscular
                (String) resultado[8]  // instrucciones
        );
    }

    // ===== CLASE INTERNA PARA RESULTADOS DE ASIGNACIÓN =====
    public static class AsignacionResultado {
        private List<String> exitosos = new ArrayList<>();
        private List<String> duplicados = new ArrayList<>();
        private List<String> errores = new ArrayList<>();

        public void agregarExitoso(String folioCliente) {
            exitosos.add(folioCliente);
        }

        public void agregarDuplicado(String folioCliente) {
            duplicados.add(folioCliente);
        }

        public void agregarError(String error) {
            errores.add(error);
        }

        // Getters
        public List<String> getExitosos() { return exitosos; }
        public List<String> getDuplicados() { return duplicados; }
        public List<String> getErrores() { return errores; }
        public int getTotalProcesados() { return exitosos.size() + duplicados.size() + errores.size(); }
        public int getTotalExitosos() { return exitosos.size(); }
        public int getTotalDuplicados() { return duplicados.size(); }
        public int getTotalErrores() { return errores.size(); }
    }

    // ===== MÉTODOS DE CONSULTA EXISTENTES =====
    public List<RutinaEntity> obtenerRutinasPorInstructor(String folioInstructor) {
        return rutinaRepository.findByFolioInstructor(folioInstructor);
    }

    public List<RutinaEntity> obtenerRutinasActivasPorInstructor(String folioInstructor) {
        return rutinaRepository.findRutinasActivasPorInstructor(folioInstructor);
    }

    public List<RutinaEntity> obtenerRutinasPorCliente(String folioCliente) {
        if (!clienteService.getClienteById(folioCliente).isPresent()) {
            throw new RuntimeException("Cliente no encontrado con folio: " + folioCliente);
        }
        return rutinaRepository.findRutinasPorCliente(folioCliente);
    }

    public List<RutinaEntity> obtenerRutinasActivasPorCliente(String folioCliente) {
        if (!clienteService.getClienteById(folioCliente).isPresent()) {
            throw new RuntimeException("Cliente no encontrado con folio: " + folioCliente);
        }
        return rutinaRepository.findRutinasActivasPorCliente(folioCliente);
    }

    public List<Cliente> obtenerClientesDeRutina(String folioRutina) {
        if (!rutinaRepository.existsById(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
        }
        return rutinaRepository.findClientesByRutina(folioRutina);
    }

    public List<Cliente> obtenerClientesActivosDeRutina(String folioRutina) {
        if (!rutinaRepository.existsById(folioRutina)) {
            throw new RuntimeException("Rutina no encontrada con folio: " + folioRutina);
        }
        return rutinaRepository.findClientesActivosByRutina(folioRutina);
    }

    public boolean estaRutinaAsignadaACliente(String folioRutina, String folioCliente) {
        return rutinaRepository.existsAsignacion(folioRutina, folioCliente);
    }

    // ===== MÉTODOS RESTANTES =====
    public RutinaEntity actualizarRutina(String folioRutina, RutinaEntity rutinaActualizada) {
        return rutinaRepository.findById(folioRutina)
                .map(rutina -> {
                    rutina.setNombre(rutinaActualizada.getNombre());
                    rutina.setDescripcion(rutinaActualizada.getDescripcion());
                    rutina.setNivel(rutinaActualizada.getNivel());
                    rutina.setObjetivo(rutinaActualizada.getObjetivo());
                    rutina.setDuracionEstimada(rutinaActualizada.getDuracionEstimada());
                    rutina.setEstatus(rutinaActualizada.getEstatus());

                    if (rutinaActualizada.getFolioInstructor() != null) {
                        if (!instructorService.existeInstructor(rutinaActualizada.getFolioInstructor())) {
                            throw new RuntimeException("El instructor no existe: " + rutinaActualizada.getFolioInstructor());
                        }
                        rutina.setFolioInstructor(rutinaActualizada.getFolioInstructor());
                    }

                    return rutinaRepository.save(rutina);
                })
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con folio: " + folioRutina));
    }

    public List<RutinaEntity> buscarRutinasPorNombre(String nombre) {
        return rutinaRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public List<RutinaEntity> buscarRutinasPorNivel(String nivel) {
        return rutinaRepository.findByNivel(nivel);
    }

    public List<RutinaEntity> buscarRutinasPorObjetivo(String objetivo) {
        return rutinaRepository.findByObjetivoContainingIgnoreCase(objetivo);
    }

    public List<RutinaEntity> obtenerRutinasActivas() {
        return rutinaRepository.findRutinasActivas();
    }

    public List<RutinaEntity> obtenerRutinasDisponibles() {
        return rutinaRepository.findRutinasDisponiblesParaAsignar();
    }

    public Long contarRutinasPorInstructor(String folioInstructor) {
        if (!instructorService.existeInstructor(folioInstructor)) {
            throw new RuntimeException("Instructor no encontrado");
        }
        return rutinaRepository.countRutinasPorInstructor(folioInstructor);
    }

    public Long contarRutinasActivasPorCliente(String folioCliente) {
        if (!clienteService.getClienteById(folioCliente).isPresent()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        return rutinaRepository.countRutinasActivasPorCliente(folioCliente);
    }

    public boolean clienteTieneRutinasActivas(String folioCliente) {
        if (!clienteService.getClienteById(folioCliente).isPresent()) {
            throw new RuntimeException("Cliente no encontrado");
        }
        return rutinaRepository.existsRutinasActivasPorCliente(folioCliente);
    }

    public Long obtenerTotalRutinasActivas() {
        return rutinaRepository.countTotalRutinasActivas();
    }
}