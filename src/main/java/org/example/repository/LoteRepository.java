package org.example.repository;

import org.example.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    List<Lote> findByFechaVencimientoBefore(LocalDate fecha);
    List<Lote> findByProductoIdProductoAndEstado(Long idProducto, String estado);
}