package org.example.impl;

import org.example.model.Lote;
import org.example.repository.LoteRepository;
import org.example.service.LoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoteServiceImpl implements LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Override
    public Lote registrarIngresoLote(Lote lote) {
        if (lote.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede registrar un lote vencido (RN-7).");
        }
        lote.setEstado("VIGENTE");
        return loteRepository.save(lote);
    }

    @Override
    public List<Lote> consultarLotesPorVencer() {
        return loteRepository.findByFechaVencimientoBefore(LocalDate.now().plusDays(30));
    }

    @Override
    public void bloquearLoteVencido(Long idLote) {
        Lote lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));
        lote.setEstado("VENCIDO");
        loteRepository.save(lote);
    }
}