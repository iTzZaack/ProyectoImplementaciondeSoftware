package org.example.service;

import org.example.model.Producto;
import java.util.List;

public interface ProductoService {
    Producto registrarProducto(Producto producto);
    List<Producto> consultarCatalogo();
    List<Producto> consultarStockBajo();
}