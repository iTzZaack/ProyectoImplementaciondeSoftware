package org.example.repository;

import org.example.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// ==========================================
// REPOSITORIO DE MOVIMIENTOS DE INVENTARIO
// ==========================================
@Repository
public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Long> {

    // ======================================
    // CONSULTA DE SALIDA / ORDEN DESCENDENTE
    // ======================================
    // muestra en la tabla como primero el que se acaba de registrar
    List<MovimientoInventario> findAllByOrderByIdMovimientoDesc();
}