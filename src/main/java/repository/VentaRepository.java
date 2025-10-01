package repository;

import model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // ✅ AGREGAR ESTE MÉTODO QUE FALTA:
    List<Venta> findByVendedor(String vendedor);
}