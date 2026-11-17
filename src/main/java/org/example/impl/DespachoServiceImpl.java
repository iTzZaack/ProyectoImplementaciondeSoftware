package org.example.impl;

import org.example.model.Lote;
import org.example.model.MovimientoInventario;
import org.example.repository.LoteRepository;
import org.example.repository.MovimientoRepository;
import org.example.service.DespachoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// ==========================================
// SERVICIO DE DESPACHOS
// ==========================================
@Service
public class DespachoServiceImpl implements DespachoService {

    // ======================================
    // INYECCIÓN DE DEPENDENCIAS (ENTRADA)
    // ======================================
    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    // ======================================
    // PROCESAMIENTO Y SOLICITUD DE SALIDA
    // ======================================
    @Override
    @Transactional
    public void solicitarDespacho(Long idProducto, Integer cantidad, String usuario) {
        // ==================================
        // VALIDACIÓN DE ENTRADA
        // ==================================
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a despachar debe ser mayor a cero.");
        }

        // ==================================
        // BÚSQUEDA DE ENTRADA (LOTES VIGENTES)
        // ==================================
        List<Lote> lotesVigentes = loteRepository.findAll().stream()
                .filter(l -> l.getProducto() != null
                        && l.getProducto().getIdProducto().equals(idProducto)
                        && ("ACTIVO".equalsIgnoreCase(l.getEstado()) || "VIGENTE".equalsIgnoreCase(l.getEstado()))
                        && l.getFechaVencimiento().isAfter(LocalDate.now()))
                .collect(Collectors.toList());

        // ==================================
        // ORDENAMIENTO INTERNO DE LOTES
        // ==================================
        lotesVigentes.sort(Comparator.comparing(Lote::getFechaVencimiento)
                .thenComparing(l -> l.getFechaIngreso() != null ? l.getFechaIngreso() : LocalDate.MIN)
                .thenComparing(Lote::getIdLote));

        // ==================================
        // VALIDACIÓN DE STOCK TOTAL (ENTRADA)
        // ==================================
        int stockTotalDisponible = lotesVigentes.stream().mapToInt(Lote::getCantidad).sum();
        if (stockTotalDisponible < cantidad) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + stockTotalDisponible + ", Solicitado: " + cantidad);
        }

        // ==================================
        // PROCESAMIENTO DE SALIDA Y DESCUENTO
        // ==================================
        int cantidadRestante = cantidad;
        for (Lote lote : lotesVigentes) {
            if (cantidadRestante <= 0) break;

            int cantidadADescontar = Math.min(cantidadRestante, lote.getCantidad());

            // Actualizar stock del lote
            lote.setCantidad(lote.getCantidad() - cantidadADescontar);
            loteRepository.save(lote);

            // Registrar automáticamente el movimiento de SALIDA para la tabla
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setTipoMovimiento("SALIDA");
            movimiento.setFechaMovimiento(LocalDateTime.now());
            movimiento.setCantidad(cantidadADescontar);
            movimiento.setUsuario(usuario != null && !usuario.isEmpty() ? usuario : "farmaceutico");
            movimiento.setLote(lote);
            movimientoRepository.save(movimiento);

            cantidadRestante -= cantidadADescontar;
        }
    }

    // ======================================
    // REGISTRO DE SALIDA DE DESPACHO
    // ======================================
    @Override
    public MovimientoInventario registrarSalidaDespacho(MovimientoInventario movimiento) {
        return movimientoRepository.save(movimiento);
    }
}