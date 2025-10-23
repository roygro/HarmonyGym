package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.EjercicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface EjercicioRepository extends JpaRepository<EjercicioEntity, String> {
    Optional<EjercicioEntity> findByIdEjercicio(String idEjercicio);
    List<EjercicioEntity> findByEstatus(String estatus);
    List<EjercicioEntity> findByGrupoMuscular(String grupoMuscular);
    List<EjercicioEntity> findByFolioInstructor(String folioInstructor);

    @Query("SELECT MAX(CAST(SUBSTRING(e.idEjercicio, 4) AS int)) FROM EjercicioEntity e WHERE e.idEjercicio LIKE 'EJE%'")
    Optional<Integer> findMaxFolioNumber();

    boolean existsByIdEjercicio(String idEjercicio);
}