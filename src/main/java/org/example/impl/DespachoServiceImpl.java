package org.example.impl;

import org.example.model.MovimientoInventario;
import org.example.repository.MovimientoRepository;
import org.example.service.DespachoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DespachoServiceImpl implements DespachoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Override
    public void solicitarDespacho(Long idProducto, Integer cantidad, String usuario) {
        // Lógica de despacho aplicando FEFO
    }

    @Override
    public MovimientoInventario registrarSalidaDespacho(MovimientoInventario movimiento) {
        return movimientoRepository.save(movimiento);
    }
}