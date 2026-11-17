package org.example.impl;

import org.example.model.Lote;
import org.example.model.Producto;
import org.example.repository.LoteRepository;
import org.example.repository.ProductoRepository;
import org.example.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

// ==========================================
// SERVICIO DE PRODUCTOS
// ==========================================
@Service
public class ProductoServiceImpl implements ProductoService {

    // ======================================
    // INYECCIÓN DE DEPENDENCIAS (ENTRADA)
    // ======================================
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    // ======================================
    // REGISTRO DE ENTRADA DE PRODUCTO
    // ======================================
    @Override
    public Producto registrarProducto(Producto producto) {
        // Validar duplicados (DFD 1.2)
        if (producto.getIdProducto() == null
                && productoRepository.existsByNombreProductoIgnoreCase(producto.getNombreProducto().trim())) {
            throw new IllegalArgumentException("Ya existe un medicamento registrado con ese nombre.");
        }
        return productoRepository.save(producto);
    }

    // ======================================
    // CONSULTA DE SALIDA / CATÁLOGO
    // ======================================
    @Override
    public List<Producto> consultarCatalogo() {
        return productoRepository.findAll();
    }

    // ======================================
    // CONSULTA DE SALIDA / STOCK BAJO
    // ======================================
    @Override
    public List<Producto> consultarStockBajo() {
        List<Producto> todosLosProductos = productoRepository.findAll();
        List<Producto> stockBajoList = new ArrayList<>();

        for (Producto producto : todosLosProductos) {
            // Buscamos todos los lotes asociados a este producto
            List<Lote> lotes = loteRepository.findAll(); // O el método que tengas para filtrar por producto

            int stockTotal = 0;
            for (Lote lote : lotes) {
                // Verificamos que el lote pertenezca al producto y esté vigente/activo
                if (lote.getProducto() != null && lote.getProducto().getIdProducto().equals(producto.getIdProducto())) {
                    if (("VIGENTE".equalsIgnoreCase(lote.getEstado()) || "ACTIVO".equalsIgnoreCase(lote.getEstado()))
                            && lote.getFechaVencimiento().isAfter(java.time.LocalDate.now())) {
                        stockTotal += lote.getCantidad();
                    }
                }
            }

            // Si el stock total es menor o igual al mínimo definido, entra en stock bajo (RN6)
            if (stockTotal <= producto.getStockMinimo()) {
                stockBajoList.add(producto);
            }
        }

        return stockBajoList;
    }
}