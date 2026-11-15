package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.Producto;
import org.example.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<Producto> registrarProducto(@Valid @RequestBody Producto producto) {
        // Al agregar @Valid, Spring valida automáticamente las restricciones de Producto.java
        return ResponseEntity.ok(productoService.registrarProducto(producto));
    }

    @GetMapping
    public ResponseEntity<List<Producto>> consultarCatalogo() {
        return ResponseEntity.ok(productoService.consultarCatalogo());
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> consultarStockBajo() {
        return ResponseEntity.ok(productoService.consultarStockBajo());
    }
}