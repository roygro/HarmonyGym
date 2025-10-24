package com.example.harmonyGymBack.service;

import com.example.harmonyGymBack.model.Producto;
import com.example.harmonyGymBack.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // ===== CRUD BÁSICO =====

    // Obtener todos los productos
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAllByOrderByFechaRegistroDesc();
    }

    // Obtener producto por código
    public Producto obtenerProductoPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con código: " + codigo));
    }

    // Crear nuevo producto
    public Producto crearProducto(Producto producto) {
        // Validar que no exista un producto con el mismo código
        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new RuntimeException("Ya existe un producto con el código: " + producto.getCodigo());
        }

        // Validaciones adicionales
        if (producto.getPrecio() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }

        if (producto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        return productoRepository.save(producto);
    }

    // Actualizar producto
    public Producto actualizarProducto(String codigo, Producto productoActualizado) {
        Producto productoExistente = obtenerProductoPorCodigo(codigo);

        // Actualizar campos
        productoExistente.setNombre(productoActualizado.getNombre());
        productoExistente.setPrecio(productoActualizado.getPrecio());
        productoExistente.setStock(productoActualizado.getStock());
        productoExistente.setCategoria(productoActualizado.getCategoria());
        productoExistente.setDescripcion(productoActualizado.getDescripcion());
        productoExistente.setEstatus(productoActualizado.getEstatus());

        return productoRepository.save(productoExistente);
    }

    // Eliminar producto
    public void eliminarProducto(String codigo) {
        Producto producto = obtenerProductoPorCodigo(codigo);
        productoRepository.delete(producto);
    }

    // ===== MÉTODOS DE CONSULTA =====

    // Obtener productos por categoría
    public List<Producto> obtenerProductosPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    // Obtener productos por estatus
    public List<Producto> obtenerProductosPorEstatus(String estatus) {
        return productoRepository.findByEstatus(estatus);
    }

    // Obtener productos activos
    public List<Producto> obtenerProductosActivos() {
        return productoRepository.findByEstatusOrderByNombreAsc("Activo");
    }

    // Buscar productos por nombre
    public List<Producto> buscarProductosPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // Obtener productos con stock bajo
    public List<Producto> obtenerProductosConStockBajo(Integer stockMinimo) {
        return productoRepository.findProductosConStockBajo(stockMinimo);
    }

    // Obtener productos por rango de precios
    public List<Producto> obtenerProductosPorRangoPrecios(Double precioMin, Double precioMax) {
        return productoRepository.findByPrecioBetween(precioMin, precioMax);
    }

    // Obtener todas las categorías
    public List<String> obtenerTodasLasCategorias() {
        return productoRepository.findDistinctCategorias();
    }

    // ===== MÉTODOS DE GESTIÓN =====

    // Actualizar stock
    public Producto actualizarStock(String codigo, Integer nuevoStock) {
        if (nuevoStock < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        Producto producto = obtenerProductoPorCodigo(codigo);
        producto.setStock(nuevoStock);
        return productoRepository.save(producto);
    }

    // Ajustar stock (incrementar/decrementar)
    public Producto ajustarStock(String codigo, Integer cantidad) {
        Producto producto = obtenerProductoPorCodigo(codigo);
        int nuevoStock = producto.getStock() + cantidad;

        if (nuevoStock < 0) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " + producto.getStock());
        }

        producto.setStock(nuevoStock);
        return productoRepository.save(producto);
    }

    // Cambiar estatus
    public Producto cambiarEstatus(String codigo, String nuevoEstatus) {
        Producto producto = obtenerProductoPorCodigo(codigo);
        producto.setEstatus(nuevoEstatus);
        return productoRepository.save(producto);
    }

    // Verificar existencia
    public boolean existeProducto(String codigo) {
        return productoRepository.existsByCodigo(codigo);
    }
}