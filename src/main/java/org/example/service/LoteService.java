package org.example.service;

import org.example.model.Lote;
import java.util.List;

// ==========================================
// INTERFAZ DE SERVICIO DE LOTES
// ==========================================
public interface LoteService {

    // ======================================
    // REGISTRO DE ENTRADA DE LOTE (ENTRADA)
    // ======================================
    Lote registrarIngresoLote(Lote lote);

    // ======================================
    // CONSULTA DE SALIDA / LOTES POR VENCER (SALIDA)
    // ======================================
    List<Lote> consultarLotesPorVencer();

    // ======================================
    // BLOQUEO DE SALIDA / LOTE VENCIDO (SALIDA)
    // ======================================
    void bloquearLoteVencido(Long idLote);
}