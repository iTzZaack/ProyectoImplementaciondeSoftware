package org.example.impl;

import org.example.model.Lote;
import org.example.model.MovimientoInventario;
import org.example.repository.LoteRepository;
import org.example.repository.MovimientoRepository;
import org.example.service.LoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;

// ==========================================
// SERVICIO DE LOTES
// ==========================================
@Service
public class LoteServiceImpl implements LoteService {

    // ======================================
    // INYECCIÓN DE DEPENDENCIAS (ENTRADA)
    // ======================================
    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoRepository movimientoInventarioRepository;

    // ======================================
    // REGISTRO DE ENTRADA DE LOTE
    // ======================================
    @Override
    public Lote registrarIngresoLote(Lote lote) {
        // La fecha de ingreso se asigna sola si no viene
        if (lote.getFechaIngreso() == null) {
            lote.setFechaIngreso(LocalDate.now());
        }

        // Validar que el lote no esté vencido (RN-7: fecha menor o igual a hoy no se acepta)
        if (!lote.getFechaVencimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede registrar un lote vencido .");
        }

        // NUEVA VALIDACIÓN: La fecha de vencimiento debe ser posterior a la fecha de ingreso
        if (lote.getFechaIngreso() != null && !lote.getFechaVencimiento().isAfter(lote.getFechaIngreso())) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser posterior a la fecha de ingreso.");
        }

        // Respetar el estado que viene del formulario (ACTIVO / INACTIVO)
        if (lote.getEstado() == null || lote.getEstado().isEmpty()) {
            lote.setEstado("ACTIVO");
        }

        Lote loteGuardado = loteRepository.save(lote);

        // Registrar el movimiento de INGRESO automáticamente
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento("INGRESO");
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setCantidad(loteGuardado.getCantidad());
        movimiento.setUsuario("admin_inventario");
        movimiento.setLote(loteGuardado);
        movimientoInventarioRepository.save(movimiento);

        return loteGuardado;
    }

    // ======================================
    // CONSULTA DE SALIDA / LOTES POR VENCER
    // ======================================
    @Override
    public List<Lote> consultarLotesPorVencer() {
        // RN-6: lotes vigentes (no vencidos) a 30 días o menos de vencer
        LocalDate hoy = LocalDate.now();
        return loteRepository.findByFechaVencimientoBetween(hoy.plusDays(1), hoy.plusDays(30)).stream()
                .filter(l -> ("ACTIVO".equalsIgnoreCase(l.getEstado()) || "VIGENTE".equalsIgnoreCase(l.getEstado()))
                        && l.getCantidad() != null && l.getCantidad() > 0)
                .collect(Collectors.toList());
    }

    // ======================================
    // BLOQUEO AUTOMÁTICO DE SALIDA (VENCIDOS)
    // ======================================
    // RN-3: bloqueo automático de lotes vencidos (se ejecuta al iniciar y cada hora)
    @Scheduled(fixedRate = 3600000)
    public void bloquearLotesVencidosAutomaticamente() {
        List<Lote> vencidos = loteRepository.findByFechaVencimientoBefore(LocalDate.now().plusDays(1));
        for (Lote lote : vencidos) {
            if (!"VENCIDO".equalsIgnoreCase(lote.getEstado())) {
                lote.setEstado("VENCIDO");
                loteRepository.save(lote);
            }
        }
    }

    // ======================================
    // BLOQUEO MANUAL DE SALIDA (LOTE VENCIDO)
    // ======================================
    @Override
    public void bloquearLoteVencido(Long idLote) {
        Lote lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
        lote.setEstado("VENCIDO");
        loteRepository.save(lote);
    }
}