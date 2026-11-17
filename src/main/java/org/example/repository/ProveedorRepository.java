package org.example.repository;

import org.example.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// ==========================================
// REPOSITORIO DE PROVEEDORES
// ==========================================
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    // ======================================
    // CONSULTA DE SALIDA / ORDEN DESCENDENTE
    // ======================================
    // muestra en la tabla como primero el que se acaba de registrar
    List<Proveedor> findAllByOrderByIdProveedorDesc();
}