package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Producto;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    Optional<Producto> findByCodigo(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.codigo = :codigo")
    Optional<Producto> findByCodigoWithLock(@Param("codigo") String codigo);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByEstatus(EstatusPersona estatus);

    List<Producto> findByEstatusOrderByNombreAsc(EstatusPersona estatus);

    @Query("SELECT p FROM Producto p WHERE p.stock < :stockMinimo AND p.estatus = com.example.harmonyGymBack.model.enums.EstatusPersona.Activo")
    List<Producto> findProductosConStockBajo(@Param("stockMinimo") Integer stockMinimo);

    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax AND p.estatus = com.example.harmonyGymBack.model.enums.EstatusPersona.Activo")
    List<Producto> findByPrecioBetween(@Param("precioMin") Double precioMin,
                                       @Param("precioMax") Double precioMax);

    @Query("SELECT DISTINCT p.categoria FROM Producto p WHERE p.categoria IS NOT NULL")
    List<String> findDistinctCategorias();

    boolean existsByCodigo(String codigo);

    List<Producto> findAllByOrderByFechaRegistroDesc();

    long countByEstatus(EstatusPersona estatus);
}
