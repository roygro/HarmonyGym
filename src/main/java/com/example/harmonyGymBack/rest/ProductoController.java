package com.example.harmonyGymBack.controller;

import com.example.harmonyGymBack.model.Producto;
import com.example.harmonyGymBack.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // ===== ENDPOINTS BÁSICOS =====

    // GET - Obtener todos los productos
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodosLosProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener producto por código
    @GetMapping("/{codigo}")
    public ResponseEntity<?> obtenerProductoPorCodigo(@PathVariable String codigo) {
        try {
            Producto producto = productoService.obtenerProductoPorCodigo(codigo);
            return ResponseEntity.ok(producto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al obtener el producto: " + e.getMessage());
        }
    }

    // POST - Crear nuevo producto
    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) {
        try {
            // Validaciones básicas
            if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El código del producto es requerido");
            }
            if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del producto es requerido");
            }
            if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
                return ResponseEntity.badRequest().body("El precio debe ser mayor a 0");
            }

            Producto nuevoProducto = productoService.crearProducto(producto);
            return ResponseEntity.ok(nuevoProducto);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al crear el producto: " + e.getMessage());
        }
    }

    // PUT - Actualizar producto
    @PutMapping("/{codigo}")
    public ResponseEntity<?> actualizarProducto(@PathVariable String codigo, @RequestBody Producto producto) {
        try {
            // Validaciones básicas
            if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del producto es requerido");
            }
            if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
                return ResponseEntity.badRequest().body("El precio debe ser mayor a 0");
            }

            Producto productoActualizado = productoService.actualizarProducto(codigo, producto);
            return ResponseEntity.ok(productoActualizado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar el producto: " + e.getMessage());
        }
    }

    // DELETE - Eliminar producto
    @DeleteMapping("/{codigo}")
    public ResponseEntity<?> eliminarProducto(@PathVariable String codigo) {
        try {
            productoService.eliminarProducto(codigo);
            return ResponseEntity.ok().body("Producto eliminado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al eliminar el producto: " + e.getMessage());
        }
    }

    // ===== ENDPOINTS DE CONSULTA =====

    // GET - Obtener productos por categoría
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> obtenerProductosPorCategoria(@PathVariable String categoria) {
        try {
            List<Producto> productos = productoService.obtenerProductosPorCategoria(categoria);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener productos por estatus
    @GetMapping("/estatus/{estatus}")
    public ResponseEntity<List<Producto>> obtenerProductosPorEstatus(@PathVariable String estatus) {
        try {
            List<Producto> productos = productoService.obtenerProductosPorEstatus(estatus);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener productos activos
    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> obtenerProductosActivos() {
        try {
            List<Producto> productos = productoService.obtenerProductosActivos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Buscar productos por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarProductosPorNombre(@RequestParam String nombre) {
        try {
            List<Producto> productos = productoService.buscarProductosPorNombre(nombre);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener productos con stock bajo
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> obtenerProductosConStockBajo(@RequestParam(defaultValue = "10") Integer stockMinimo) {
        try {
            List<Producto> productos = productoService.obtenerProductosConStockBajo(stockMinimo);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener productos por rango de precios
    @GetMapping("/rango-precios")
    public ResponseEntity<List<Producto>> obtenerProductosPorRangoPrecios(
            @RequestParam Double precioMin,
            @RequestParam Double precioMax) {
        try {
            List<Producto> productos = productoService.obtenerProductosPorRangoPrecios(precioMin, precioMax);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET - Obtener todas las categorías
    @GetMapping("/categorias")
    public ResponseEntity<List<String>> obtenerTodasLasCategorias() {
        try {
            List<String> categorias = productoService.obtenerTodasLasCategorias();
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== ENDPOINTS DE GESTIÓN =====

    // PATCH - Actualizar stock
    @PatchMapping("/{codigo}/stock")
    public ResponseEntity<?> actualizarStock(@PathVariable String codigo, @RequestParam Integer stock) {
        try {
            Producto producto = productoService.actualizarStock(codigo, stock);
            return ResponseEntity.ok(producto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar stock: " + e.getMessage());
        }
    }

    // PATCH - Cambiar estatus
    @PatchMapping("/{codigo}/estatus")
    public ResponseEntity<?> cambiarEstatus(@PathVariable String codigo, @RequestParam String estatus) {
        try {
            Producto producto = productoService.cambiarEstatus(codigo, estatus);
            return ResponseEntity.ok(producto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al cambiar estatus: " + e.getMessage());
        }
    }
}