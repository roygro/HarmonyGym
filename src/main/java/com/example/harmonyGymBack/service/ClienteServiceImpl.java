package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ClienteRepository clienteRepository;

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

    // ==================== GENERACIÓN AUTOMÁTICA DE FOLIO ====================

    private String generarFolioCliente() {
        try {
            System.out.println("🔍 Buscando último folio de cliente...");

            List<Cliente> todosClientes = clienteRepository.findAll();

            if (todosClientes.isEmpty()) {
                System.out.println("✅ No hay clientes, empezando con CLI001");
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
                        System.err.println("⚠ Folio con formato inválido: " + folio);
                    }
                }
            }

            if (ultimoFolio == null) {
                System.out.println("✅ No se encontraron folios válidos, empezando con CLI001");
                return "CLI001";
            }

            int nuevoNumero = maxNumero + 1;
            String nuevoFolio = String.format("CLI%03d", nuevoNumero);

            System.out.println("📊 Último folio encontrado: " + ultimoFolio);
            System.out.println("🎯 Nuevo folio generado: " + nuevoFolio);

            return nuevoFolio;

        } catch (Exception e) {
            System.err.println("❌ Error crítico al generar folio: " + e.getMessage());
            e.printStackTrace();

            long totalClientes = clienteRepository.count();
            String folioFallback = String.format("CLI%03d", totalClientes + 1);
            System.out.println("🔄 Usando fallback: " + folioFallback);
            return folioFallback;
        }
    }

    // ==================== CREAR NUEVO CLIENTE CON FOTO ====================

    @Transactional
    public Cliente crearClienteConFoto(String nombre, String telefono, String email,
                                       String fechaNacimiento, String genero, String estatus,
                                       MultipartFile foto) throws IOException {
        System.out.println("🚀 Iniciando creación de cliente con foto...");

        validarArchivo(foto);

        String folioGenerado = generarFolioCliente();

        Cliente cliente = new Cliente();
        cliente.setFolioCliente(folioGenerado);
        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        cliente.setEmail(email);
        cliente.setGenero(genero);
        cliente.setEstatus(estatus != null ? estatus : "Activo");
        cliente.setFechaRegistro(LocalDateTime.now());

        if (fechaNacimiento != null && !fechaNacimiento.trim().isEmpty()) {
            cliente.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
        }

        if (foto != null && !foto.isEmpty()) {
            String nombreArchivoFoto = guardarArchivo(foto, folioGenerado);
            cliente.setNombreArchivoFoto(nombreArchivoFoto);
            System.out.println("📸 Foto guardada: " + nombreArchivoFoto);
        }

        // Validar email único
        if (cliente.getEmail() != null && clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado por otro cliente");
        }

        // Validar teléfono único
        if (cliente.getTelefono() != null && clienteRepository.findByTelefono(cliente.getTelefono()).isPresent()) {
            throw new RuntimeException("El teléfono ya está registrado por otro cliente");
        }

        Cliente clienteGuardado = clienteRepository.save(cliente);
        System.out.println("✅ Cliente creado exitosamente: " + folioGenerado);

        return clienteGuardado;
    }

    // ==================== ACTUALIZAR CLIENTE CON FOTO ====================

    @Transactional
    public Cliente actualizarClienteConFoto(String folioCliente, String nombre, String telefono,
                                            String email, String fechaNacimiento, String genero,
                                            String estatus, MultipartFile foto,
                                            boolean eliminarFoto) throws IOException {
        System.out.println("✏ Actualizando cliente: " + folioCliente);

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

        // Manejo de la foto
        if (eliminarFoto) {
            if (clienteExistente.getNombreArchivoFoto() != null) {
                eliminarArchivo(clienteExistente.getNombreArchivoFoto());
                clienteExistente.setNombreArchivoFoto(null);
                System.out.println("🗑 Foto eliminada");
            }
        } else if (foto != null && !foto.isEmpty()) {
            if (clienteExistente.getNombreArchivoFoto() != null) {
                eliminarArchivo(clienteExistente.getNombreArchivoFoto());
            }
            String nombreArchivoFoto = guardarArchivo(foto, folioCliente);
            clienteExistente.setNombreArchivoFoto(nombreArchivoFoto);
            System.out.println("📸 Foto actualizada: " + nombreArchivoFoto);
        }

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        System.out.println("✅ Cliente actualizado: " + folioCliente);

        return clienteActualizado;
    }

    // ==================== MÉTODOS ORIGINALES ====================

    public Cliente crearCliente(Cliente cliente) {
        System.out.println("🚀 Iniciando creación de cliente...");

        String folioGenerado = generarFolioCliente();
        cliente.setFolioCliente(folioGenerado);

        System.out.println("📝 Folio asignado: " + folioGenerado);

        if (cliente.getFechaRegistro() == null) {
            cliente.setFechaRegistro(LocalDateTime.now());
        }

        if (cliente.getEstatus() == null) {
            cliente.setEstatus("Activo");
        }

        // Validaciones de unicidad
        if (cliente.getEmail() != null && clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (cliente.getTelefono() != null && clienteRepository.findByTelefono(cliente.getTelefono()).isPresent()) {
            throw new RuntimeException("El teléfono ya está registrado");
        }

        Cliente clienteGuardado = clienteRepository.save(cliente);
        System.out.println("✅ Cliente guardado exitosamente: " + clienteGuardado.getFolioCliente());

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
            return clienteRepository.findByEstatusAndGenero(estatus, genero);
        } else if (estatus != null) {
            return clienteRepository.findByEstatus(estatus);
        } else if (genero != null) {
            return clienteRepository.findByGenero(genero);
        } else {
            return clienteRepository.findAll();
        }
    }

    @Transactional
    public Cliente actualizarCliente(String folioCliente, Cliente clienteActualizado) {
        System.out.println("✏ Actualizando cliente: " + folioCliente);

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
        System.out.println("✅ Cliente actualizado: " + clienteActualizadoDb.getFolioCliente());

        return clienteActualizadoDb;
    }

    // ==================== GESTIÓN DE ESTATUS ====================

    @Transactional
    public Cliente cambiarEstatusCliente(String folioCliente, String nuevoEstatus) {
        Cliente cliente = obtenerClientePorId(folioCliente);
        cliente.setEstatus(nuevoEstatus);
        return clienteRepository.save(cliente);
    }

    public Cliente desactivarCliente(String folioCliente) {
        return cambiarEstatusCliente(folioCliente, "Inactivo");
    }

    public Cliente activarCliente(String folioCliente) {
        return cambiarEstatusCliente(folioCliente, "Activo");
    }

    public void eliminarCliente(String folioCliente) {
        desactivarCliente(folioCliente);
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
        return clienteRepository.findByEstatusOrderByNombreAsc("Activo");
    }

    public List<Cliente> buscarClientesPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Long contarClientesActivos() {
        return clienteRepository.countByEstatus("Activo");
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
        System.out.println("✅ Cliente eliminado completamente: " + folioCliente);
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
        estadisticas.put("clientesActivos", clienteRepository.countByEstatus("Activo"));
        estadisticas.put("clientesInactivos", clienteRepository.countByEstatus("Inactivo"));
        estadisticas.put("clientesConMembresiaActiva", clienteRepository.findClientesConMembresiaActiva().size());

        return estadisticas;
    }
}