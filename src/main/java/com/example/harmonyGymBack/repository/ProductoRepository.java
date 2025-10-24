package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    // Encontrar producto por código
    Optional<Producto> findByCodigo(String codigo);

    // Encontrar productos por nombre (búsqueda parcial)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Encontrar productos por categoría
    List<Producto> findByCategoria(String categoria);

    // Encontrar productos por estatus
    List<Producto> findByEstatus(String estatus);

    // Encontrar productos activos
    List<Producto> findByEstatusOrderByNombreAsc(String estatus);

    // Encontrar productos con stock bajo
    @Query("SELECT p FROM Producto p WHERE p.stock < :stockMinimo AND p.estatus = 'Activo'")
    List<Producto> findProductosConStockBajo(@Param("stockMinimo") Integer stockMinimo);

    // Encontrar productos por rango de precios
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax AND p.estatus = 'Activo'")
    List<Producto> findByPrecioBetween(@Param("precioMin") Double precioMin,
                                       @Param("precioMax") Double precioMax);

    // Obtener todas las categorías distintas
    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL")
    List<String> findDistinctCategorias();

    // Verificar si existe un producto con el mismo código
    boolean existsByCodigo(String codigo);

    // Obtener productos ordenados por fecha de registro (más recientes primero)
    List<Producto> findAllByOrderByFechaRegistroDesc();
}