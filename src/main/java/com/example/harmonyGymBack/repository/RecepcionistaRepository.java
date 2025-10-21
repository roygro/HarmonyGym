package com.example.harmonyGymBack.repository;

import com.example.harmonyGymBack.model.Recepcionista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecepcionistaRepository extends JpaRepository<Recepcionista, String> {

    List<Recepcionista> findByNombreContainingIgnoreCase(String nombre);

    Optional<Recepcionista> findByEmail(String email);

    List<Recepcionista> findByEstatus(String estatus);

    @Modifying
    @Query("UPDATE Recepcionista r SET r.estatus = :estatus WHERE r.idRecepcionista = :idRecepcionista")
    void updateEstatus(@Param("idRecepcionista") String idRecepcionista, @Param("estatus") String estatus);

    @Query("SELECT COUNT(r) > 0 FROM Recepcionista r WHERE r.email = :email AND r.idRecepcionista != :idRecepcionista")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("idRecepcionista") String idRecepcionista);
}