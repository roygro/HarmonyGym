package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, String> {

    // Buscar por nombre (like)
    List<Administrador> findByNombreComContainingIgnoreCase(String nombreCom);

    // Buscar por apellido paterno
    List<Administrador> findByAppContainingIgnoreCase(String app);

    // Buscar por apellido materno
    List<Administrador> findByApmContainingIgnoreCase(String apm);

    // Verificar si existe un administrador con el mismo nombre completo excluyendo el actual
    @Query("SELECT COUNT(a) > 0 FROM Administrador a WHERE a.nombreCom = :nombreCom AND a.app = :app AND a.apm = :apm AND a.folioAdmin != :folioAdmin")
    boolean existsByNombreCompletoAndNotId(@Param("nombreCom") String nombreCom,
                                           @Param("app") String app,
                                           @Param("apm") String apm,
                                           @Param("folioAdmin") String folioAdmin);
}