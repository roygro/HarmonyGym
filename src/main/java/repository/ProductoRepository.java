package repository;

import model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar productos con stock disponible
    List<Producto> findByStockGreaterThan(Integer stock);

    // Buscar por código único
    Optional<Producto> findByCodigo(String codigo);

    // Buscar por categoría
    List<Producto> findByCategoria(String categoria);

    // Actualizar stock (para cuando se venda)
    @Modifying
    @Query("UPDATE Producto p SET p.stock = p.stock - :cantidad WHERE p.id = :productoId AND p.stock >= :cantidad")
    int actualizarStock(@Param("productoId") Long productoId, @Param("cantidad") Integer cantidad);
}