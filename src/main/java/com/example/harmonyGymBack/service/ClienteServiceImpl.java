package com.example.harmonyGymBack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import com.example.harmonyGymBack.model.enums.EstatusUsuario;
import com.example.harmonyGymBack.model.Usuario;
import com.example.harmonyGymBack.model.AuthResponse;
import com.example.harmonyGymBack.model.RegisterRequest;
import com.example.harmonyGymBack.repository.ClienteRepository;
import com.example.harmonyGymBack.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClienteServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(ClienteServiceImpl.class);


    private final UsuarioRepository usuarioRepository;

    private final AuthService authService;

    private final ClienteRepository clienteRepository;

    private final EmailService emailService;

    public ClienteServiceImpl(UsuarioRepository usuarioRepository, AuthService authService, ClienteRepository clienteRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.clienteRepository = clienteRepository;
        this.emailService = emailService;
    }


    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.upload.dir:uploads/clientes}")
    private String uploadDir;

    // ==================== MÉTODOS PARA MANEJO DE ARCHIVOS ====================

    private String guardarArchivo(MultipartFile archivo, String folioCliente) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String extension = obtenerExtensionArchivo(archivo.getOriginalFilename());
        String nombreArchivo = folioCliente + "_" + UUID.randomUUID().toString() + "." + extension;

        Path filePath = uploadPath.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return nombreArchivo;
    }

    private void eliminarArchivo(String nombreArchivo) throws IOException {
        if (nombreArchivo != null && !nombreArchivo.trim().isEmpty()) {
            Path filePath = Paths.get(uploadDir).resolve(nombreArchivo);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
    }

    private String obtenerExtensionArchivo(String nombreArchivo) {
        if (nombreArchivo == null) {
            return "jpg";
        }
        int lastDotIndex = nombreArchivo.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return nombreArchivo.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg";
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return;
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen válida");
        }

        long maxSize = 5 * 1024 * 1024;
        if (archivo.getSize() > maxSize) {
            throw new RuntimeException("La imagen no debe superar los 5MB");
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo != null) {
            String extension = obtenerExtensionArchivo(nombreArchivo);
            if (!extension.matches("(jpg|jpeg|png|gif|webp)")) {
                throw new RuntimeException("Solo se permiten imágenes JPG, PNG, GIF o WebP");
            }
        }
    }

    // ==================== MÉTODOS PARA CREACIÓN DE USUARIO AUTOMÁTICO ====================

    private void crearUsuarioParaCliente(Cliente cliente) {
        try {
            log.info("👤 Creando usuario automático para cliente: " + cliente.getFolioCliente());

            // Verificar si ya existe un usuario para este cliente
            Optional<Usuario> usuarioExistente = authService.obtenerUsuarioPorIdPersona(cliente.getFolioCliente());
            if (usuarioExistente.isPresent()) {
                log.info("ℹ️ Ya existe un usuario para este cliente: " + cliente.getFolioCliente());
                return;
            }

            // Generar username único basado en el email o teléfono
            String username = generarUsernameParaCliente(cliente);

            // Generar password temporal
            String passwordTemporal = generarPasswordTemporal();

            // Crear request de registro
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername(username);
            registerRequest.setPassword(passwordTemporal);
            registerRequest.setTipoUsuario("Cliente");
            registerRequest.setIdPersona(cliente.getFolioCliente());

            // Registrar el usuario
            AuthResponse authResponse = authService.registrarUsuario(registerRequest);

            if (authResponse.isSuccess()) {
                log.info("✅ Usuario creado exitosamente:");
                log.info("   📧 Username: " + username);
                log.info("   🔑 Password temporal: " + passwordTemporal);
                log.info("   👤 ID Usuario: " + authResponse.getIdUsuario());

                // ✅ ENVIAR CREDENCIALES POR EMAIL
                if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
                    try {
                        emailService.enviarCredencialesCliente(
                                cliente.getEmail(),
                                cliente.getNombre(),
                                username,
                                passwordTemporal
                        );
                        log.info("✅ Credenciales enviadas por email a: " + cliente.getEmail());
                    } catch (Exception e) {
                        log.error("⚠️ No se pudieron enviar las credenciales por email: " + e.getMessage());
                        // No revertimos la creación por fallo en el email
                    }
                } else {
                    log.info("⚠️ Cliente no tiene email, no se enviaron credenciales");
                }

            } else {
                throw new RuntimeException("Error al crear usuario: " + authResponse.getMessage());
            }

        } catch (Exception e) {
            log.error("❌ Error al crear usuario automático: " + e.getMessage());
            throw new RuntimeException("No se pudo crear el usuario automáticamente: " + e.getMessage());
        }
    }

    private String generarUsernameParaCliente(Cliente cliente) {
        String usernameBase = "";

        // Prioridad 1: Usar el email como base
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
            usernameBase = cliente.getEmail().split("@")[0];
        }
        // Prioridad 2: Usar el nombre + apellido
        else if (cliente.getNombre() != null && !cliente.getNombre().trim().isEmpty()) {
            String[] nombrePartes = cliente.getNombre().split("\\s+");
            if (nombrePartes.length >= 2) {
                // Usar primera letra del nombre + apellido completo
                usernameBase = nombrePartes[0].toLowerCase().charAt(0) +
                        nombrePartes[nombrePartes.length - 1].toLowerCase();
            } else {
                // Si solo tiene un nombre, usarlo completo
                usernameBase = cliente.getNombre().toLowerCase();
            }
            // Limpiar caracteres especiales
            usernameBase = usernameBase.replaceAll("[^a-zA-Z0-9]", "");
        }
        // Prioridad 3: Usar el folio del cliente
        else {
            usernameBase = "user" + cliente.getFolioCliente().toLowerCase();
        }

        // Verificar disponibilidad y generar username único
        return generarUsernameUnico(usernameBase);
    }

    private String generarUsernameUnico(String base) {
        String username = base;
        int counter = 1;

        // Verificar disponibilidad usando el AuthService
        Map<String, Object> disponibilidad = authService.verificarDisponibilidadUsername(username);
        boolean disponible = (Boolean) disponibilidad.get("disponible");

        while (!disponible) {
            username = base + counter;
            disponibilidad = authService.verificarDisponibilidadUsername(username);
            disponible = (Boolean) disponibilidad.get("disponible");
            counter++;

            // Prevenir loop infinito
            if (counter > 100) {
                throw new RuntimeException("No se pudo generar un username único después de 100 intentos");
            }
        }

        return username;
    }

    private String generarPasswordTemporal() {
        // Generar una contraseña temporal de 8 caracteres
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();

        // Asegurar que tenga al menos una mayúscula, una minúscula y un número
        password.append(caracteres.charAt((int) (Math.random() * 26))); // Mayúscula
        password.append(caracteres.charAt(26 + (int) (Math.random() * 26))); // Minúscula
        password.append(caracteres.charAt(52 + (int) (Math.random() * 10))); // Número

        // Completar los 8 caracteres
        for (int i = 3; i < 8; i++) {
            int index = (int) (Math.random() * caracteres.length());
            password.append(caracteres.charAt(index));
        }

        // Mezclar los caracteres
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    private void sincronizarEstatusUsuario(String folioCliente, EstatusPersona estatusCliente) {
        try {
            Optional<Usuario> usuarioOpt = authService.obtenerUsuarioPorIdPersona(folioCliente);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                EstatusUsuario estatusUsuario = EstatusPersona.Activo.equals(estatusCliente) ? EstatusUsuario.Activo : EstatusUsuario.Inactivo;

                if (!estatusUsuario.equals(usuario.getEstatus())) {
                    usuario.setEstatus(estatusUsuario);
                    // Si se activa el cliente, resetear intentos de login
                    if (EstatusUsuario.Activo.equals(estatusUsuario)) {
                        usuario.setIntentosLogin(0);
                        usuario.setFechaBloqueo(null);
                    }

                    // Guardar cambios en el usuario
                    usuarioRepository.save(usuario);

                    log.info("✅ Estatus de usuario actualizado: " + usuario.getUsername() + " -> " + estatusUsuario);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error al sincronizar estatus de usuario: " + e.getMessage());
            throw new RuntimeException("Error al sincronizar estatus de usuario");
        }
    }

    // ==================== GENERACIÓN AUTOMÁTICA DE FOLIO ====================

    private String generarFolioCliente() {
        try {
            log.info("🔍 Buscando último folio de cliente...");

            List<Cliente> todosClientes = clienteRepository.findAll();

            if (todosClientes.isEmpty()) {
                log.info("✅ No hay clientes, empezando con CLI001");
                return "CLI001";
            }

            String ultimoFolio = null;
            int maxNumero = 0;

            for (Cliente cliente : todosClientes) {
                String folio = cliente.getFolioCliente();
                if (folio != null && folio.startsWith("CLI")) {
                    try {
                        String numeroStr = folio.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                            ultimoFolio = folio;
                        }
                    } catch (NumberFormatException e) {
                        log.error("⚠ Folio con formato inválido: " + folio);
                    }
                }
            }

            if (ultimoFolio == null) {
                log.info("✅ No se encontraron folios válidos, empezando con CLI001");
                return "CLI001";
            }

            int nuevoNumero = maxNumero + 1;
            String nuevoFolio = String.format("CLI%03d", nuevoNumero);

            log.info("📊 Último folio encontrado: " + ultimoFolio);
            log.info("🎯 Nuevo folio generado: " + nuevoFolio);

            return nuevoFolio;

        } catch (Exception e) {
            log.error("❌ Error crítico al generar folio: " + e.getMessage());
            log.error("Error: ", e);

            long totalClientes = clienteRepository.count();
            String folioFallback = String.format("CLI%03d", totalClientes + 1);
            log.info("🔄 Usando fallback: " + folioFallback);
            return folioFallback;
        }
    }

    // ==================== CREAR NUEVO CLIENTE CON FOTO ====================

    @Transactional
    public Cliente crearClienteConFoto(String nombre, String telefono, String email,
                                       String fechaNacimiento, String genero, String estatus,
                                       MultipartFile foto) throws IOException {
        log.info("🚀 Iniciando creación de cliente con foto...");

        // ✅ VALIDAR QUE EL EMAIL ES OBLIGATORIO
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("El email es obligatorio para crear las credenciales de acceso");
        }

        validarArchivo(foto);

        String folioGenerado = generarFolioCliente();

        Cliente cliente = new Cliente();
        cliente.setFolioCliente(folioGenerado);
        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        cliente.setEmail(email); // Email es obligatorio
        cliente.setGenero(genero);
        cliente.setEstatus(estatus != null ? EstatusPersona.fromString(estatus) : EstatusPersona.Activo);
        cliente.setFechaRegistro(LocalDateTime.now());

        if (fechaNacimiento != null && !fechaNacimiento.trim().isEmpty()) {
            cliente.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }

        if (foto != null && !foto.isEmpty()) {
            String nombreArchivoFoto = guardarArchivo(foto, folioGenerado);
            cliente.setNombreArchivoFoto(nombreArchivoFoto);
            log.info("📸 Foto guardada: " + nombreArchivoFoto);
        }

        // Validar email único
        if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado por otro cliente");
        }

        // Validar teléfono único
        if (cliente.getTelefono() != null && clienteRepository.findByTelefono(cliente.getTelefono()).isPresent()) {
            throw new RuntimeException("El teléfono ya está registrado por otro cliente");
        }

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("✅ Cliente creado exitosamente: " + folioGenerado);

        // ✅ Crear usuario automáticamente para el cliente
        try {
            crearUsuarioParaCliente(clienteGuardado);
            log.info("✅ Usuario creado automáticamente para el cliente: " + folioGenerado);
        } catch (Exception e) {
            log.error("⚠ Advertencia: No se pudo crear el usuario para el cliente: " + e.getMessage());
            // No lanzamos excepción para no revertir la creación del cliente
        }

        return clienteGuardado;
    }

    @Transactional
    public Cliente actualizarClienteConFoto(String folioCliente, String nombre, String telefono,
                                            String email, String fechaNacimiento, String genero,
                                            String estatus, MultipartFile foto,
                                            boolean eliminarFoto) throws IOException {
        // Llamar al método sobrecargado con nombreArchivoFoto como null
        return actualizarClienteConFoto(folioCliente, nombre, telefono, email, fechaNacimiento,
                genero, estatus, foto, eliminarFoto, null);
    }
    // ==================== MÉTODOS ORIGINALES ====================

    public Cliente crearCliente(Cliente cliente) {
        log.info("🚀 Iniciando creación de cliente...");

        // ✅ VALIDAR QUE EL EMAIL ES OBLIGATORIO
        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            throw new RuntimeException("El email es obligatorio para crear las credenciales de acceso");
        }

        String folioGenerado = generarFolioCliente();
        cliente.setFolioCliente(folioGenerado);

        log.info("📝 Folio asignado: " + folioGenerado);

        if (cliente.getFechaRegistro() == null) {
            cliente.setFechaRegistro(LocalDateTime.now());
        }

        if (cliente.getEstatus() == null) {
            cliente.setEstatus(EstatusPersona.Activo);
        }

        // Validaciones de unicidad
        if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (cliente.getTelefono() != null && clienteRepository.findByTelefono(cliente.getTelefono()).isPresent()) {
            throw new RuntimeException("El teléfono ya está registrado");
        }

        Cliente clienteGuardado = clienteRepository.save(cliente);
        log.info("✅ Cliente guardado exitosamente: " + clienteGuardado.getFolioCliente());

        // ✅ Crear usuario automáticamente
        try {
            crearUsuarioParaCliente(clienteGuardado);
            log.info("✅ Usuario creado automáticamente para el cliente: " + folioGenerado);
        } catch (Exception e) {
            log.error("⚠ Advertencia: No se pudo crear el usuario para el cliente: " + e.getMessage());
        }

        return clienteGuardado;
    }

    // ==================== CONSULTAS Y LISTADOS ====================

    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    public Cliente obtenerClientePorId(String folioCliente) {
        Optional<Cliente> cliente = clienteRepository.findById(folioCliente);
        return cliente.orElseThrow(() -> new RuntimeException("Cliente no encontrado con folio: " + folioCliente));
    }

    public List<Cliente> obtenerClientesFiltrados(String estatus, String genero) {
        if (estatus != null && genero != null) {
            return clienteRepository.findByEstatusAndGenero(EstatusPersona.fromString(estatus), genero);
        } else if (estatus != null) {
            return clienteRepository.findByEstatus(EstatusPersona.fromString(estatus));
        } else if (genero != null) {
            return clienteRepository.findByGenero(genero);
        } else {
            return clienteRepository.findAll();
        }
    }

    @Transactional
    public Cliente actualizarCliente(String folioCliente, Cliente clienteActualizado) {
        log.info("✏ Actualizando cliente: " + folioCliente);

        Cliente clienteExistente = obtenerClientePorId(folioCliente);

        if (clienteActualizado.getNombre() != null) {
            clienteExistente.setNombre(clienteActualizado.getNombre());
        }
        if (clienteActualizado.getTelefono() != null) {
            clienteExistente.setTelefono(clienteActualizado.getTelefono());
        }
        if (clienteActualizado.getEmail() != null) {
            clienteExistente.setEmail(clienteActualizado.getEmail());
        }
        if (clienteActualizado.getFechaNacimiento() != null) {
            clienteExistente.setFechaNacimiento(clienteActualizado.getFechaNacimiento());
        }
        if (clienteActualizado.getGenero() != null) {
            clienteExistente.setGenero(clienteActualizado.getGenero());
        }
        if (clienteActualizado.getEstatus() != null) {
            clienteExistente.setEstatus(clienteActualizado.getEstatus());
        }
        if (clienteActualizado.getNombreArchivoFoto() != null) {
            clienteExistente.setNombreArchivoFoto(clienteActualizado.getNombreArchivoFoto());
        }

        // Validaciones de unicidad
        if (clienteActualizado.getEmail() != null &&
                clienteRepository.existsByEmailAndNotId(clienteActualizado.getEmail(), folioCliente)) {
            throw new RuntimeException("El email ya está registrado por otro cliente");
        }

        if (clienteActualizado.getTelefono() != null &&
                clienteRepository.existsByTelefonoAndNotId(clienteActualizado.getTelefono(), folioCliente)) {
            throw new RuntimeException("El teléfono ya está registrado por otro cliente");
        }

        Cliente clienteActualizadoDb = clienteRepository.save(clienteExistente);
        log.info("✅ Cliente actualizado: " + clienteActualizadoDb.getFolioCliente());

        return clienteActualizadoDb;
    }

    // ==================== GESTIÓN DE ESTATUS ====================

    @Transactional
    public Cliente cambiarEstatusCliente(String folioCliente, EstatusPersona nuevoEstatus) {
        Cliente cliente = obtenerClientePorId(folioCliente);
        cliente.setEstatus(nuevoEstatus);
        Cliente clienteActualizado = clienteRepository.save(cliente);
        try {
            sincronizarEstatusUsuario(folioCliente, nuevoEstatus);
        } catch (Exception e) {
            log.error("Advertencia: " + e.getMessage());
        }
        return clienteActualizado;
    }
    public Cliente cambiarEstatusCliente(String folioCliente, String nuevoEstatusStr) {
        return cambiarEstatusCliente(folioCliente, EstatusPersona.fromString(nuevoEstatusStr));
    }
    public Cliente desactivarCliente(String folioCliente) {
        return cambiarEstatusCliente(folioCliente, EstatusPersona.Inactivo);
    }

    public Cliente activarCliente(String folioCliente) {
        return cambiarEstatusCliente(folioCliente, EstatusPersona.Activo);
    }

    public void eliminarCliente(String folioCliente) {
        // Primero desactivar el cliente
        Cliente clienteDesactivado = cambiarEstatusCliente(folioCliente, EstatusPersona.Inactivo);

        // Luego desactivar el usuario asociado
        try {
            sincronizarEstatusUsuario(folioCliente, EstatusPersona.Inactivo);
            log.info("✅ Cliente y usuario desactivados: " + folioCliente);
        } catch (Exception e) {
            log.error("⚠ Cliente desactivado pero no se pudo desactivar el usuario: " + e.getMessage());
        }
    }

    // ==================== ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasCliente(String folioCliente) {
        if (!clienteRepository.existsById(folioCliente)) {
            throw new RuntimeException("Cliente no encontrado con folio: " + folioCliente);
        }

        String query = """
            SELECT 
                COUNT(DISTINCT cm.Id_Membresia) as total_membresias,
                COUNT(DISTINCT ra.Id_Actividad) as total_actividades,
                COUNT(DISTINCT rr.Folio_Rutina) as total_rutinas,
                COALESCE(AVG(ra.Calificacion), 0) as promedio_calificacion_actividades,
                COALESCE(AVG(rr.Calificacion), 0) as promedio_calificacion_rutinas,
                (SELECT COUNT(*) FROM VENDE v WHERE v.Folio_Cliente = :folioCliente) as total_compras,
                (SELECT COALESCE(SUM(v.Total), 0) FROM VENDE v WHERE v.Folio_Cliente = :folioCliente) as total_gastado
            FROM CLIENTE c
            LEFT JOIN CLIENTE_MEMBRESIA cm ON c.Folio_Cliente = cm.Folio_Cliente
            LEFT JOIN REALIZA_ACTIVIDAD ra ON c.Folio_Cliente = ra.Folio_Cliente
            LEFT JOIN REALIZA_RUTINA rr ON c.Folio_Cliente = rr.Folio_Cliente
            WHERE c.Folio_Cliente = :folioCliente
            GROUP BY c.Folio_Cliente
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(query)
                .setParameter("folioCliente", folioCliente)
                .getResultList();

        Map<String, Object> estadisticas = new HashMap<>();

        if (!results.isEmpty()) {
            Object[] result = results.get(0);
            estadisticas.put("totalMembresias", result[0]);
            estadisticas.put("totalActividades", result[1]);
            estadisticas.put("totalRutinas", result[2]);
            estadisticas.put("promedioCalificacionActividades", result[3]);
            estadisticas.put("promedioCalificacionRutinas", result[4]);
            estadisticas.put("totalCompras", result[5]);
            estadisticas.put("totalGastado", result[6]);
        } else {
            estadisticas.put("totalMembresias", 0);
            estadisticas.put("totalActividades", 0);
            estadisticas.put("totalRutinas", 0);
            estadisticas.put("promedioCalificacionActividades", 0.0);
            estadisticas.put("promedioCalificacionRutinas", 0.0);
            estadisticas.put("totalCompras", 0);
            estadisticas.put("totalGastado", 0.0);
        }

        return estadisticas;
    }

    // ==================== CONSULTAS ADICIONALES ====================

    public boolean existeCliente(String folioCliente) {
        return clienteRepository.existsById(folioCliente);
    }

    public List<Cliente> obtenerClientesActivos() {
        return clienteRepository.findByEstatusOrderByNombreAsc(EstatusPersona.Activo);
    }

    public List<Cliente> buscarClientesPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Long contarClientesActivos() {
        return clienteRepository.countByEstatus(EstatusPersona.Activo);
    }

    public List<Cliente> obtenerClientesConMembresiaActiva() {
        return clienteRepository.findClientesConMembresiaActiva();
    }

    // ==================== ELIMINAR CLIENTE (con foto) ====================

    @Transactional
    public void eliminarClienteCompleto(String folioCliente) throws IOException {
        Cliente cliente = obtenerClientePorId(folioCliente);

        if (cliente.getNombreArchivoFoto() != null) {
            eliminarArchivo(cliente.getNombreArchivoFoto());
        }

        clienteRepository.delete(cliente);
        log.info("✅ Cliente eliminado completamente: " + folioCliente);
    }

    // ==================== OBTENER FOTO ====================

    public byte[] obtenerFotoCliente(String folioCliente) throws IOException {
        Cliente cliente = obtenerClientePorId(folioCliente);

        if (cliente.getNombreArchivoFoto() == null) {
            return null;
        }

        Path filePath = Paths.get(uploadDir).resolve(cliente.getNombreArchivoFoto());
        if (!Files.exists(filePath)) {
            return null;
        }

        return Files.readAllBytes(filePath);
    }

    // ==================== DASHBOARD ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        estadisticas.put("totalClientes", clienteRepository.count());
        estadisticas.put("clientesActivos", clienteRepository.countByEstatus(EstatusPersona.Activo));
        estadisticas.put("clientesInactivos", clienteRepository.countByEstatus(EstatusPersona.Inactivo));
        estadisticas.put("clientesConMembresiaActiva", clienteRepository.findClientesConMembresiaActiva().size());

        return estadisticas;
    }

    // ==================== MÉTODOS PARA MANEJAR URL DE FOTO ====================

    /**
     * Actualizar solo la URL de la foto del cliente
     */
    @Transactional
    public Cliente actualizarUrlFoto(String folioCliente, String urlFoto) {
        log.info("📸 Actualizando URL de foto para: " + folioCliente);

        Cliente clienteExistente = obtenerClientePorId(folioCliente);

        // Validar que sea una URL válida
        if (urlFoto != null && !urlFoto.trim().isEmpty()) {
            try {
                // Validar formato de URL
                if (!esUrlValida(urlFoto)) {
                    throw new RuntimeException("La URL proporcionada no tiene un formato válido");
                }

                clienteExistente.setNombreArchivoFoto(urlFoto.trim());
                log.info("✅ URL de foto validada y guardada: " +
                        (urlFoto.length() > 100 ? urlFoto.substring(0, 100) + "..." : urlFoto));
            } catch (Exception e) {
                throw new RuntimeException("Error al validar URL: " + e.getMessage());
            }
        } else {
            // Si la URL está vacía, eliminar la foto
            clienteExistente.setNombreArchivoFoto(null);
            log.info("🗑 Foto eliminada (URL vacía)");
        }

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        log.info("✅ URL de foto actualizada para: " + folioCliente);

        return clienteActualizado;
    }

    /**
     * Método auxiliar para validar URLs
     */
    private boolean esUrlValida(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        String urlTrimmed = url.trim();

        // Verificar que comience con http:// o https://
        if (!urlTrimmed.startsWith("http://") && !urlTrimmed.startsWith("https://")) {
            return false;
        }

        // Verificar longitud razonable (máximo 1000 caracteres)
        if (urlTrimmed.length() > 1000) {
            throw new RuntimeException("La URL es demasiado larga (máximo 1000 caracteres)");
        }

        // Verificar formato básico de URL
        try {
            new java.net.URL(urlTrimmed);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Actualizar cliente con URL de foto (sobrecarga del método existente)
     */
    @Transactional
    public Cliente actualizarClienteConFoto(String folioCliente, String nombre, String telefono,
                                            String email, String fechaNacimiento, String genero,
                                            String estatus, MultipartFile foto,
                                            boolean eliminarFoto, String nombreArchivoFoto) throws IOException {
        log.info("✏ Actualizando cliente con URL de foto: " + folioCliente);

        Cliente clienteExistente = obtenerClientePorId(folioCliente);

        validarArchivo(foto);

        if (nombre != null) clienteExistente.setNombre(nombre);
        if (telefono != null) clienteExistente.setTelefono(telefono);
        if (email != null) clienteExistente.setEmail(email);
        if (genero != null) clienteExistente.setGenero(genero);
        if (estatus != null) clienteExistente.setEstatus(estatus);

        if (fechaNacimiento != null && !fechaNacimiento.trim().isEmpty()) {
            clienteExistente.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }

        // Validar email único excluyendo el actual
        if (email != null && clienteRepository.existsByEmailAndNotId(email, folioCliente)) {
            throw new RuntimeException("El email ya está registrado por otro cliente");
        }

        // Validar teléfono único excluyendo el actual
        if (telefono != null && clienteRepository.existsByTelefonoAndNotId(telefono, folioCliente)) {
            throw new RuntimeException("El teléfono ya está registrado por otro cliente");
        }

        // Manejo de la foto - PRIORIDAD PARA URL
        if (nombreArchivoFoto != null && !nombreArchivoFoto.trim().isEmpty()) {
            // Si se envía una URL, validarla y usarla directamente
            if (esUrlValida(nombreArchivoFoto)) {
                clienteExistente.setNombreArchivoFoto(nombreArchivoFoto.trim());
                log.info("📸 URL de foto actualizada: " +
                        (nombreArchivoFoto.length() > 100 ? nombreArchivoFoto.substring(0, 100) + "..." : nombreArchivoFoto));
            } else {
                throw new RuntimeException("La URL de foto proporcionada no es válida");
            }
        } else if (eliminarFoto) {
            if (clienteExistente.getNombreArchivoFoto() != null) {
                // Solo eliminar archivo físico si es un archivo local, no una URL
                if (!clienteExistente.getNombreArchivoFoto().startsWith("http")) {
                    eliminarArchivo(clienteExistente.getNombreArchivoFoto());
                }
                clienteExistente.setNombreArchivoFoto(null);
                log.info("🗑 Foto eliminada");
            }
        } else if (foto != null && !foto.isEmpty()) {
            // Si se sube un archivo, eliminar la anterior (si es local) y guardar la nueva
            if (clienteExistente.getNombreArchivoFoto() != null &&
                    !clienteExistente.getNombreArchivoFoto().startsWith("http")) {
                eliminarArchivo(clienteExistente.getNombreArchivoFoto());
            }
            String nombreArchivoFotoGuardado = guardarArchivo(foto, folioCliente);
            clienteExistente.setNombreArchivoFoto(nombreArchivoFotoGuardado);
            log.info("📸 Foto actualizada: " + nombreArchivoFotoGuardado);
        }

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        log.info("✅ Cliente actualizado: " + folioCliente);

        return clienteActualizado;
    }
}