package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, String> {

    // Buscar por nombre completo (like)
    List<Administrador> findByNombreComContainingIgnoreCase(String nombreCom);

    // Buscar por apellido paterno
    List<Administrador> findByAppContainingIgnoreCase(String app);

    // Buscar por apellido materno
    List<Administrador> findByApmContainingIgnoreCase(String apm);

    // Buscar por nombre y apellidos
    List<Administrador> findByNombreComContainingIgnoreCaseAndAppContainingIgnoreCaseAndApmContainingIgnoreCase(
            String nombreCom, String app, String apm);

    // Verificar si existe un administrador con el mismo nombre completo excluyendo el actual
    @Query("SELECT COUNT(a) > 0 FROM Administrador a WHERE a.nombreCom = :nombreCom AND a.app = :app AND a.apm = :apm AND a.folioAdmin != :folioAdmin")
    boolean existsByNombreCompletoAndNotId(@Param("nombreCom") String nombreCom,
                                           @Param("app") String app,
                                           @Param("apm") String apm,
                                           @Param("folioAdmin") String folioAdmin);

    // Verificar si existe por nombre completo
    @Query("SELECT COUNT(a) > 0 FROM Administrador a WHERE a.nombreCom = :nombreCom AND a.app = :app AND a.apm = :apm")
    boolean existsByNombreCompleto(@Param("nombreCom") String nombreCom,
                                   @Param("app") String app,
                                   @Param("apm") String apm);

    // Obtener último folio
    @Query(value = "SELECT a.folio_admin FROM administrador a ORDER BY a.folio_admin DESC LIMIT 1", nativeQuery = true)
    String findUltimoFolioAdmin();

    // Buscar por rango de fechas de registro
    List<Administrador> findByFechaRegistroBetween(LocalDateTime start, LocalDateTime end);

    // Verificar si existe administrador por folio
    boolean existsByFolioAdmin(String folioAdmin);

    // Contar total de administradores
    long count();

    // Buscar administradores registrados en los últimos días
    @Query("SELECT a FROM Administrador a WHERE a.fechaRegistro >= :fecha")
    List<Administrador> findAdministradoresRecientes(@Param("fecha") LocalDateTime fecha);

    // Buscar por nombre exacto (para evitar problemas de nomenclatura)
    @Query(value = "SELECT * FROM administrador WHERE nombrecom ILIKE %:nombre%", nativeQuery = true)
    List<Administrador> findByNombreComNative(@Param("nombre") String nombre);
}