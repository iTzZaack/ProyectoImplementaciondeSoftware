package org.example.service;

import org.example.model.MovimientoInventario;

public interface DespachoService {
    void solicitarDespacho(Long idProducto, Integer cantidad, String usuario);
    MovimientoInventario registrarSalidaDespacho(MovimientoInventario movimiento);
}