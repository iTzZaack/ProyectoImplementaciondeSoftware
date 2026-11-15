package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.Lote;
import org.example.service.LoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    @Autowired
    private LoteService loteService;

    @PostMapping
    public ResponseEntity<Lote> registrarIngresoLote(@Valid @RequestBody Lote lote) {
        // Al usar @Valid, se ejecutan automáticamente las restricciones de Lote.java
        return ResponseEntity.ok(loteService.registrarIngresoLote(lote));
    }

    @GetMapping("/por-vencer")
    public ResponseEntity<List<Lote>> consultarLotesPorVencer() {
        return ResponseEntity.ok(loteService.consultarLotesPorVencer());
    }

    @PutMapping("/{id}/bloquear")
    public ResponseEntity<Void> bloquearLoteVencido(@PathVariable Long id) {
        loteService.bloquearLoteVencido(id);
        return ResponseEntity.ok().build();
    }
}