package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Cliente;
import com.example.harmonyGymBack.model.RutinaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RutinaRepository extends JpaRepository<RutinaEntity, String> {

    // Consultas básicas
    List<RutinaEntity> findByNombreContainingIgnoreCase(String nombre);
    List<RutinaEntity> findByNivel(String nivel);
    List<RutinaEntity> findByEstatus(String estatus);
    List<RutinaEntity> findByObjetivoContainingIgnoreCase(String objetivo);
    boolean existsByFolioRutina(String folioRutina);

    // Consultas por instructor
    List<RutinaEntity> findByFolioInstructor(String folioInstructor);

    @Query("SELECT r FROM RutinaEntity r WHERE r.folioInstructor = :instructor AND r.estatus = 'Activa'")
    List<RutinaEntity> findRutinasActivasPorInstructor(@Param("instructor") String folioInstructor);

    @Query("SELECT COUNT(r) FROM RutinaEntity r WHERE r.folioInstructor = :instructor")
    Long countRutinasPorInstructor(@Param("instructor") String folioInstructor);

    // ===== CONSULTAS NATIVAS PARA LA TABLA ASIGNA =====

    // Insertar asignación
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO asigna (folio_instructor, folio_rutina, cliente_asignado, fecha_asignacion) " +
            "VALUES (:folioInstructor, :folioRutina, :clienteAsignado, CURRENT_TIMESTAMP)",
            nativeQuery = true)
    void insertAsignacion(@Param("folioInstructor") String folioInstructor,
                          @Param("folioRutina") String folioRutina,
                          @Param("clienteAsignado") String clienteAsignado);

    // Eliminar asignación
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM asigna WHERE folio_rutina = :folioRutina AND cliente_asignado = :clienteAsignado",
            nativeQuery = true)
    void deleteAsignacion(@Param("folioRutina") String folioRutina,
                          @Param("clienteAsignado") String clienteAsignado);

    // Verificar si existe asignación
    @Query(value = "SELECT COUNT(*) > 0 FROM asigna WHERE folio_rutina = :folioRutina AND cliente_asignado = :clienteAsignado",
            nativeQuery = true)
    boolean existsAsignacion(@Param("folioRutina") String folioRutina,
                             @Param("clienteAsignado") String clienteAsignado);

    // Obtener rutinas por cliente
    @Query(value = "SELECT r.* FROM rutina r " +
            "INNER JOIN asigna a ON r.folio_rutina = a.folio_rutina " +
            "WHERE a.cliente_asignado = :cliente",
            nativeQuery = true)
    List<RutinaEntity> findRutinasPorCliente(@Param("cliente") String folioCliente);

    // Obtener rutinas activas por cliente
    @Query(value = "SELECT r.* FROM rutina r " +
            "INNER JOIN asigna a ON r.folio_rutina = a.folio_rutina " +
            "WHERE a.cliente_asignado = :cliente AND r.estatus = 'Activa'",
            nativeQuery = true)
    List<RutinaEntity> findRutinasActivasPorCliente(@Param("cliente") String folioCliente);

    // Obtener clientes de una rutina
    @Query(value = "SELECT c.* FROM cliente c " +
            "INNER JOIN asigna a ON c.folio_cliente = a.cliente_asignado " +
            "WHERE a.folio_rutina = :folioRutina",
            nativeQuery = true)
    List<Cliente> findClientesByRutina(@Param("folioRutina") String folioRutina);

    // Obtener clientes activos de una rutina
    @Query(value = "SELECT c.* FROM cliente c " +
            "INNER JOIN asigna a ON c.folio_cliente = a.cliente_asignado " +
            "WHERE a.folio_rutina = :folioRutina AND c.estatus = 'Activo'",
            nativeQuery = true)
    List<Cliente> findClientesActivosByRutina(@Param("folioRutina") String folioRutina);

    // Contar rutinas activas por cliente
    @Query(value = "SELECT COUNT(*) FROM rutina r " +
            "INNER JOIN asigna a ON r.folio_rutina = a.folio_rutina " +
            "WHERE a.cliente_asignado = :cliente AND r.estatus = 'Activa'",
            nativeQuery = true)
    Long countRutinasActivasPorCliente(@Param("cliente") String folioCliente);

    // Verificar si cliente tiene rutinas activas
    @Query(value = "SELECT COUNT(*) > 0 FROM rutina r " +
            "INNER JOIN asigna a ON r.folio_rutina = a.folio_rutina " +
            "WHERE a.cliente_asignado = :cliente AND r.estatus = 'Activa'",
            nativeQuery = true)
    boolean existsRutinasActivasPorCliente(@Param("cliente") String folioCliente);

    // Obtener rutinas disponibles para asignar (sin clientes asignados)
    @Query(value = "SELECT r.* FROM rutina r " +
            "WHERE r.estatus = 'Activa' AND r.folio_rutina NOT IN " +
            "(SELECT a.folio_rutina FROM asigna a)",
            nativeQuery = true)
    List<RutinaEntity> findRutinasDisponiblesParaAsignar();

    // Consultas generales
    @Query("SELECT r FROM RutinaEntity r WHERE r.estatus = 'Activa'")
    List<RutinaEntity> findRutinasActivas();

    @Query("SELECT r FROM RutinaEntity r WHERE r.folioInstructor IS NULL AND r.estatus = 'Activa'")
    List<RutinaEntity> findRutinasSinInstructor();

    // Estadísticas
    @Query("SELECT COUNT(r) FROM RutinaEntity r WHERE r.estatus = 'Activa'")
    Long countTotalRutinasActivas();

    // ===== CONSULTAS PARA EJERCICIOS EN RUTINAS =====

    // Agregar ejercicio a rutina
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO contiene_rutina (folio_rutina, id_ejercicio, orden, series_ejercicio, repeticiones_ejercicio, descanso_ejercicio, observaciones) " +
            "VALUES (:folioRutina, :idEjercicio, :orden, :seriesEjercicio, :repeticionesEjercicio, :descansoEjercicio, :observaciones)",
            nativeQuery = true)
    void agregarEjercicioARutina(@Param("folioRutina") String folioRutina,
                                 @Param("idEjercicio") String idEjercicio,
                                 @Param("orden") Integer orden,
                                 @Param("seriesEjercicio") Integer seriesEjercicio,
                                 @Param("repeticionesEjercicio") Integer repeticionesEjercicio,
                                 @Param("descansoEjercicio") Integer descansoEjercicio,
                                 @Param("observaciones") String observaciones);

    // Eliminar ejercicio de rutina
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM contiene_rutina WHERE folio_rutina = :folioRutina AND id_ejercicio = :idEjercicio",
            nativeQuery = true)
    void eliminarEjercicioDeRutina(@Param("folioRutina") String folioRutina,
                                   @Param("idEjercicio") String idEjercicio);

    // Verificar si ejercicio está en rutina
    @Query(value = "SELECT COUNT(*) > 0 FROM contiene_rutina WHERE folio_rutina = :folioRutina AND id_ejercicio = :idEjercicio",
            nativeQuery = true)
    boolean existeEjercicioEnRutina(@Param("folioRutina") String folioRutina,
                                    @Param("idEjercicio") String idEjercicio);

    // Obtener ejercicios de una rutina con detalles
    @Query(value = "SELECT e.id_ejercicio, e.nombre, e.tiempo, e.series, e.repeticiones, e.descanso, " +
            "e.equipo_necesario, e.grupo_muscular, e.instrucciones, " +
            "cr.orden, cr.series_ejercicio, cr.repeticiones_ejercicio, cr.descanso_ejercicio, cr.observaciones " +
            "FROM ejercicio e " +
            "INNER JOIN contiene_rutina cr ON e.id_ejercicio = cr.id_ejercicio " +
            "WHERE cr.folio_rutina = :folioRutina AND e.estatus = 'Activo' " +
            "ORDER BY cr.orden",
            nativeQuery = true)
    List<Object[]> findEjerciciosByRutina(@Param("folioRutina") String folioRutina);

    // Obtener máximo orden en rutina
    @Query(value = "SELECT COALESCE(MAX(orden), 0) FROM contiene_rutina WHERE folio_rutina = :folioRutina",
            nativeQuery = true)
    Integer findMaxOrdenByRutina(@Param("folioRutina") String folioRutina);

    // Actualizar orden de ejercicios
    @Modifying
    @Transactional
    @Query(value = "UPDATE contiene_rutina SET orden = :nuevoOrden WHERE folio_rutina = :folioRutina AND id_ejercicio = :idEjercicio",
            nativeQuery = true)
    void actualizarOrdenEjercicio(@Param("folioRutina") String folioRutina,
                                  @Param("idEjercicio") String idEjercicio,
                                  @Param("nuevoOrden") Integer nuevoOrden);

    // Calcular tiempo total estimado de rutina
    @Query(value = "SELECT SUM(" +
            "CASE " +
            "WHEN e.tiempo IS NOT NULL THEN e.tiempo " +
            "WHEN cr.series_ejercicio IS NOT NULL AND cr.repeticiones_ejercicio IS NOT NULL AND cr.descanso_ejercicio IS NOT NULL " +
            "THEN (cr.series_ejercicio * cr.repeticiones_ejercicio * 4) + ((cr.series_ejercicio - 1) * cr.descanso_ejercicio) " +
            "WHEN e.series IS NOT NULL AND e.repeticiones IS NOT NULL AND e.descanso IS NOT NULL " +
            "THEN (e.series * e.repeticiones * 4) + ((e.series - 1) * e.descanso) " +
            "ELSE 0 " +
            "END) " +
            "FROM contiene_rutina cr " +
            "INNER JOIN ejercicio e ON cr.id_ejercicio = e.id_ejercicio " +
            "WHERE cr.folio_rutina = :folioRutina AND e.estatus = 'Activo'",
            nativeQuery = true)
    Integer calcularTiempoTotalRutina(@Param("folioRutina") String folioRutina);

    // Actualizar parámetros de ejercicio en rutina
    @Modifying
    @Transactional
    @Query(value = "UPDATE contiene_rutina SET " +
            "series_ejercicio = COALESCE(:seriesEjercicio, series_ejercicio), " +
            "repeticiones_ejercicio = COALESCE(:repeticionesEjercicio, repeticionesEjercicio), " +
            "descanso_ejercicio = COALESCE(:descansoEjercicio, descansoEjercicio), " +
            "observaciones = COALESCE(:observaciones, observaciones) " +
            "WHERE folio_rutina = :folioRutina AND id_ejercicio = :idEjercicio",
            nativeQuery = true)
    void actualizarParametrosEjercicio(@Param("folioRutina") String folioRutina,
                                       @Param("idEjercicio") String idEjercicio,
                                       @Param("seriesEjercicio") Integer seriesEjercicio,
                                       @Param("repeticionesEjercicio") Integer repeticionesEjercicio,
                                       @Param("descansoEjercicio") Integer descansoEjercicio,
                                       @Param("observaciones") String observaciones);

    // ===== CONSULTAS PARA EJERCICIOS INDEPENDIENTES =====

    // Verificar si ejercicio existe
    @Query(value = "SELECT COUNT(*) > 0 FROM ejercicio WHERE id_ejercicio = :idEjercicio AND estatus = 'Activo'",
            nativeQuery = true)
    boolean existeEjercicioActivo(@Param("idEjercicio") String idEjercicio);

    // Obtener ejercicio por ID
    @Query(value = "SELECT * FROM ejercicio WHERE id_ejercicio = :idEjercicio",
            nativeQuery = true)
    List<Object[]> findEjercicioById(@Param("idEjercicio") String idEjercicio);

    // Buscar ejercicios por nombre
    @Query(value = "SELECT * FROM ejercicio WHERE nombre ILIKE CONCAT('%', :nombre, '%') AND estatus = 'Activo'",
            nativeQuery = true)
    List<Object[]> findEjerciciosByNombre(@Param("nombre") String nombre);

    // Buscar ejercicios por grupo muscular
    @Query(value = "SELECT * FROM ejercicio WHERE grupo_muscular ILIKE CONCAT('%', :grupoMuscular, '%') AND estatus = 'Activo'",
            nativeQuery = true)
    List<Object[]> findEjerciciosByGrupoMuscular(@Param("grupoMuscular") String grupoMuscular);

    // Obtener todos los ejercicios activos
    @Query(value = "SELECT * FROM ejercicio WHERE estatus = 'Activo' ORDER BY nombre",
            nativeQuery = true)
    List<Object[]> findAllEjerciciosActivos();

    // ===== MÉTODOS PARA CAMBIAR ESTATUS (CORREGIDOS) =====

    @Modifying
    @Transactional
    @Query("UPDATE RutinaEntity r SET r.estatus = :estatus WHERE r.folioRutina = :folioRutina")
    void actualizarEstatusRutina(@Param("folioRutina") String folioRutina,
                                 @Param("estatus") String estatus);

    @Modifying
    @Transactional
    @Query("UPDATE RutinaEntity r SET r.estatus = 'Activa' WHERE r.folioRutina = :folioRutina")
    void activarRutina(@Param("folioRutina") String folioRutina);

    @Modifying
    @Transactional
    @Query("UPDATE RutinaEntity r SET r.estatus = 'Inactiva' WHERE r.folioRutina = :folioRutina")
    void desactivarRutina(@Param("folioRutina") String folioRutina);

    @Modifying
    @Transactional
    @Query("UPDATE RutinaEntity r SET r.estatus = 'Inactiva' WHERE r.folioRutina = :folioRutina AND r.folioInstructor = :folioInstructor")
    void desactivarRutinaPorInstructor(@Param("folioRutina") String folioRutina,
                                       @Param("folioInstructor") String folioInstructor);

    @Modifying
    @Transactional
    @Query("UPDATE RutinaEntity r SET r.estatus = 'Inactiva' WHERE r.folioInstructor = :folioInstructor")
    void desactivarTodasLasRutinasPorInstructor(@Param("folioInstructor") String folioInstructor);

    // Consulta para verificar si una rutina está activa
    @Query("SELECT COUNT(r) > 0 FROM RutinaEntity r WHERE r.folioRutina = :folioRutina AND r.estatus = 'Activa'")
    boolean isRutinaActiva(@Param("folioRutina") String folioRutina);
}