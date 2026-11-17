package org.example.repository;

import org.example.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// ==========================================
// REPOSITORIO DE PRODUCTOS
// ==========================================
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // ======================================
    // CONSULTA DE ENTRADA / VERIFICAR DUPLICADOS
    // ======================================
    boolean existsByNombreProductoIgnoreCase(String nombreProducto);

    // ======================================
    // CONSULTA DE SALIDA / ORDEN DESCENDENTE
    // ======================================
    // muestra en la tabla como primero el que se acaba de registrar
    List<Producto> findAllByOrderByIdProductoDesc();
}