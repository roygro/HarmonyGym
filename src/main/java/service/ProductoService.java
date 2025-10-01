package service;

import model.Producto;
import repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Obtener todos los productos
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    // Obtener productos con stock disponible
    public List<Producto> getProductosConStock() {
        return productoRepository.findByStockGreaterThan(0);
    }

    // Buscar producto por ID
    public Optional<Producto> getProductoById(Long id) {
        return productoRepository.findById(id);
    }

    // Buscar producto por código
    public Optional<Producto> getProductoByCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo);
    }

    // Guardar o actualizar producto
    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    // Actualizar stock (para cuando se venda)
    @Transactional
    public boolean actualizarStock(Long productoId, Integer cantidad) {
        int resultado = productoRepository.actualizarStock(productoId, cantidad);
        return resultado > 0;
    }

    // Validar si hay stock suficiente
    public boolean validarStockSuficiente(Long productoId, Integer cantidadRequerida) {
        Optional<Producto> producto = productoRepository.findById(productoId);
        return producto.isPresent() && producto.get().getStock() >= cantidadRequerida;
    }
}