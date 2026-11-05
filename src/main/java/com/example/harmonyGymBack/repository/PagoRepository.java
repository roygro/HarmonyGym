package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Pago;
import com.example.harmonyGymBack.model.enums.EstatusPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    @Query("SELECT p FROM Pago p WHERE p.cliente.folioCliente = :folioCliente ORDER BY p.fechaVenta DESC")
    List<Pago> findByFolioCliente(@Param("folioCliente") String folioCliente);

    @Query("SELECT p FROM Pago p WHERE p.recepcionista.idRecepcionista = :idRecepcionista ORDER BY p.fechaVenta DESC")
    List<Pago> findByIdRecepcionista(@Param("idRecepcionista") String idRecepcionista);

    List<Pago> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT p FROM Pago p WHERE p.producto.codigo = :codigoProducto")
    List<Pago> findByCodigoProducto(@Param("codigoProducto") String codigoProducto);

    List<Pago> findAllByOrderByFechaVentaDesc();

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pago p WHERE CAST(p.fechaVenta AS localdate) = CURRENT_DATE AND p.estatusPago = com.example.harmonyGymBack.model.enums.EstatusPago.PAGADO")
    Double findTotalVentasHoy();

    @Query("SELECT COUNT(p) FROM Pago p WHERE CAST(p.fechaVenta AS localdate) = CURRENT_DATE AND p.estatusPago = com.example.harmonyGymBack.model.enums.EstatusPago.PAGADO")
    Long countVentasHoy();

    @Query("SELECT p FROM Pago p WHERE CAST(p.fechaVenta AS localdate) = :fecha")
    List<Pago> findByFechaVentaDate(@Param("fecha") LocalDate fecha);

    Optional<Pago> findByReferenciaExterna(String referenciaExterna);

    List<Pago> findByEstatusPago(EstatusPago estatusPago);
}
