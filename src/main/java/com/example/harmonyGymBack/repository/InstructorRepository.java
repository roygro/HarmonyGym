package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, String> {

    List<Instructor> findByNombreContainingIgnoreCase(String nombre);

    List<Instructor> findByEspecialidadContainingIgnoreCase(String especialidad);

    List<Instructor> findByEstatus(String estatus);

    @Modifying
    @Query("UPDATE Instructor i SET i.estatus = :estatus WHERE i.folioInstructor = :folioInstructor")
    void updateEstatus(@Param("folioInstructor") String folioInstructor, @Param("estatus") String estatus);

    @Query("SELECT COUNT(i) > 0 FROM Instructor i WHERE i.nombre = :nombre AND i.app = :app AND i.apm = :apm AND i.folioInstructor != :folioInstructor")
    boolean existsByNombreCompletoAndNotId(@Param("nombre") String nombre, @Param("app") String app,
                                           @Param("apm") String apm, @Param("folioInstructor") String folioInstructor);
}