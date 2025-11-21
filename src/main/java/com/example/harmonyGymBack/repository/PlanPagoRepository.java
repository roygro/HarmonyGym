package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.PlanPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanPagoRepository extends JpaRepository<PlanPago, Long> {
    List<PlanPago> findByEstatus(String estatus);
    Optional<PlanPago> findByNombre(String nombre);

    @Query("SELECT p FROM PlanPago p WHERE p.factorDescuento < 1.0 AND p.estatus = 'Activo'")
    List<PlanPago> findPlanesConDescuento();
}