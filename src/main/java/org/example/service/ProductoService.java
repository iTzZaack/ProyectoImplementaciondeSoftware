package org.example.service;

import org.example.model.Producto;
import java.util.List;

// ==========================================
// INTERFAZ DE SERVICIO DE PRODUCTOS
// ==========================================
public interface ProductoService {

    // ======================================
    // REGISTRO DE ENTRADA DE PRODUCTO (ENTRADA)
    // ======================================
    Producto registrarProducto(Producto producto);

    // ======================================
    // CONSULTA DE SALIDA / CATÁLOGO (SALIDA)
    // ======================================
    List<Producto> consultarCatalogo();

    // ======================================
    // CONSULTA DE SALIDA / STOCK BAJO (SALIDA)
    // ======================================
    List<Producto> consultarStockBajo();
}