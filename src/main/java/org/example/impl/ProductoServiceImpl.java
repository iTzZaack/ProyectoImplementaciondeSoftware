package org.example.impl;

import org.example.model.Producto;
import org.example.repository.LoteRepository;
import org.example.repository.ProductoRepository;
import org.example.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Override
    public Producto registrarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public List<Producto> consultarCatalogo() {
        return productoRepository.findAll();
    }

    @Override
    public List<Producto> consultarStockBajo() {
        return productoRepository.findAll();
    }
}