package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {
    List<Membresia> findByClienteId(Long clienteId);
    List<Membresia> findByEstado(String estado);
}
