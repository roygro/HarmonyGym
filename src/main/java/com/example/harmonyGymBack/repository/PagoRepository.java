package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    // Encontrar pagos por cliente
    List<Pago> findByFolioCliente(String folioCliente);

    // Encontrar pagos por recepcionista
    List<Pago> findByIdRecepcionista(String idRecepcionista);

    // Encontrar pagos por rango de fechas
    List<Pago> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Encontrar pagos por tipo de producto/membresía
    List<Pago> findByCodigoProducto(String codigoProducto);

    // Obtener historial de pagos ordenado por fecha
    List<Pago> findAllByOrderByFechaVentaDesc();

    // Obtener total de ventas del día - CORREGIDO
    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pago p WHERE CAST(p.fechaVenta AS localdate) = CURRENT_DATE")
    Double findTotalVentasHoy();

    // Obtener cantidad de ventas del día - CORREGIDO
    @Query("SELECT COUNT(p) FROM Pago p WHERE CAST(p.fechaVenta AS localdate) = CURRENT_DATE")
    Long countVentasHoy();

    // Método alternativo: Obtener pagos por fecha específica
    @Query("SELECT p FROM Pago p WHERE CAST(p.fechaVenta AS localdate) = :fecha")
    List<Pago> findByFechaVentaDate(@Param("fecha") LocalDate fecha);
}