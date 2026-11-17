package org.example.service;

import org.example.model.MovimientoInventario;

// ==========================================
// INTERFAZ DE SERVICIO DE DESPACHOS
// ==========================================
public interface DespachoService {

    // ======================================
    // SOLICITUD DE DESPACHO (ENTRADA)
    // ======================================
    void solicitarDespacho(Long idProducto, Integer cantidad, String usuario);

    // ======================================
    // REGISTRO DE SALIDA DE DESPACHO (SALIDA)
    // ======================================
    MovimientoInventario registrarSalidaDespacho(MovimientoInventario movimiento);
}