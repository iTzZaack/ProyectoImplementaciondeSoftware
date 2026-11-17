package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.Producto;
import org.example.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// ==========================================
// CONTROLADOR DE PRODUCTOS
// ==========================================
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    // ======================================
    // INYECCIÓN DE DEPENDENCIAS (ENTRADA)
    // ======================================
    @Autowired
    private ProductoService productoService;

    // ======================================
    // REGISTRO DE ENTRADA DE PRODUCTO
    // ======================================
    @PostMapping
    public ResponseEntity<Producto> registrarProducto(@Valid @RequestBody Producto producto) {
        // Al agregar @Valid, Spring valida automáticamente las restricciones de Producto.java
        return ResponseEntity.ok(productoService.registrarProducto(producto));
    }

    // ======================================
    // CONSULTA DE SALIDA / CATÁLOGO
    // ======================================
    @GetMapping
    public ResponseEntity<List<Producto>> consultarCatalogo() {
        return ResponseEntity.ok(productoService.consultarCatalogo());
    }

    // ======================================
    // CONSULTA DE SALIDA / STOCK BAJO
    // ======================================
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> consultarStockBajo() {
        return ResponseEntity.ok(productoService.consultarStockBajo());
    }
}