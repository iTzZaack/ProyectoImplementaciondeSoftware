package org.example.controller;

import org.example.service.DespachoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/despachos")
public class DespachoController {

    @Autowired
    private DespachoService despachoService;

    @PostMapping("/solicitar")
    public ResponseEntity<String> solicitarDespacho(@RequestParam Long idProducto, @RequestParam Integer cantidad, @RequestParam String usuario) {
        despachoService.solicitarDespacho(idProducto, cantidad, usuario);
        return ResponseEntity.ok("Despacho procesado con éxito.");
    }
}