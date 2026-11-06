package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Membresia;
import com.example.harmonyGymBack.model.TipoMembresia;
import com.example.harmonyGymBack.model.enums.EstatusCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, String> {

    // Buscar membresías por tipo
    List<Membresia> findByTipo(TipoMembresia tipo);

    // Buscar membresías por estatus
    List<Membresia> findByEstatus(EstatusCatalogo estatus);

    // Buscar membresía por tipo y estatus
    Optional<Membresia> findByTipoAndEstatus(TipoMembresia tipo, EstatusCatalogo estatus);

    // Buscar membresías con precio menor o igual
    List<Membresia> findByPrecioLessThanEqual(Double precio);

    // Buscar membresías por duración mínima
    List<Membresia> findByDuracionGreaterThanEqual(Integer duracion);

    // Consulta personalizada para membresías con beneficios específicos
    @Query("SELECT m FROM Membresia m WHERE m.beneficios LIKE %:beneficio% AND m.estatus = com.example.harmonyGymBack.model.enums.EstatusCatalogo.Activa")
    List<Membresia> findByBeneficioContaining(@Param("beneficio") String beneficio);

    long countByEstatus(EstatusCatalogo estatus);
}
