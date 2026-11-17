package org.example.repository;

import org.example.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

// ==========================================
// REPOSITORIO DE LOTES
// ==========================================
@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    // ======================================
    // CONSULTA DE ENTRADA / VENCIMIENTO ANTERIOR
    // ======================================
    List<Lote> findByFechaVencimientoBefore(LocalDate fecha);

    // ======================================
    // CONSULTA DE ENTRADA / RANGO DE VENCIMIENTO
    // ======================================
    List<Lote> findByFechaVencimientoBetween(LocalDate desde, LocalDate hasta);

    // ======================================
    // CONSULTA DE ENTRADA / PRODUCTO Y ESTADO
    // ======================================
    List<Lote> findByProductoIdProductoAndEstado(Long idProducto, String estado);

    // ======================================
    // CONSULTA DE SALIDA / ORDEN DESCENDENTE
    // ======================================
    // muestra en la tabla como primero el que se acaba de registrar
    List<Lote> findAllByOrderByIdLoteDesc();
}