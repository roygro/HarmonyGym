package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Administrador;
import com.example.harmonyGymBack.model.enums.EstatusPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, String> {

    List<Administrador> findByNombreComContainingIgnoreCase(String nombreCom);

    List<Administrador> findByAppContainingIgnoreCase(String app);

    List<Administrador> findByApmContainingIgnoreCase(String apm);

    List<Administrador> findByNombreComContainingIgnoreCaseAndAppContainingIgnoreCaseAndApmContainingIgnoreCase(
            String nombreCom, String app, String apm);

    @Query("SELECT COUNT(a) > 0 FROM Administrador a WHERE a.nombreCom = :nombreCom AND a.app = :app AND a.apm = :apm AND a.folioAdmin != :folioAdmin")
    boolean existsByNombreCompletoAndNotId(@Param("nombreCom") String nombreCom,
                                           @Param("app") String app,
                                           @Param("apm") String apm,
                                           @Param("folioAdmin") String folioAdmin);

    @Query("SELECT COUNT(a) > 0 FROM Administrador a WHERE a.nombreCom = :nombreCom AND a.app = :app AND a.apm = :apm")
    boolean existsByNombreCompleto(@Param("nombreCom") String nombreCom,
                                   @Param("app") String app,
                                   @Param("apm") String apm);

    @Query(value = "SELECT a.folio_admin FROM administrador a ORDER BY a.folio_admin DESC LIMIT 1", nativeQuery = true)
    String findUltimoFolioAdmin();

    List<Administrador> findByFechaRegistroBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByFolioAdmin(String folioAdmin);

    long count();

    List<Administrador> findByEstatus(EstatusPersona estatus);

    long countByEstatus(EstatusPersona estatus);

    @Query("SELECT a FROM Administrador a WHERE a.fechaRegistro >= :fecha")
    List<Administrador> findAdministradoresRecientes(@Param("fecha") LocalDateTime fecha);

    @Query(value = "SELECT * FROM administrador WHERE nombrecom ILIKE %:nombre%", nativeQuery = true)
    List<Administrador> findByNombreComNative(@Param("nombre") String nombre);
}
