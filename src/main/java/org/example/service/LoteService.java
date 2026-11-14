package org.example.service;

import org.example.model.Lote;
import java.util.List;

public interface LoteService {
    Lote registrarIngresoLote(Lote lote);
    List<Lote> consultarLotesPorVencer();
    void bloquearLoteVencido(Long idLote);
}