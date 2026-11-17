package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.Lote;
import org.example.model.Producto;
import org.example.model.Proveedor;
import org.example.repository.LoteRepository;
import org.example.repository.MovimientoRepository;
import org.example.repository.ProductoRepository;
import org.example.repository.ProveedorRepository;
import org.example.service.DespachoService;
import org.example.service.LoteService;
import org.example.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ==========================================
// CONTROLADOR DE VISTAS DE FARMACIA
// ==========================================
@Controller
public class VistaFarmaciaController {

    // ======================================
    // INYECCIÓN DE DEPENDENCIAS (ENTRADA)
    // ======================================
    @Autowired
    private ProductoService productoService;

    @Autowired
    private LoteService loteService;

    @Autowired
    private DespachoService despachoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    // ==========================================
    // RUTA PRINCIPAL (SELECTOR DE ROLES) (SALIDA)
    // ==========================================
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ==========================================
    // PANEL DE ADMINISTRACIÓN (VISTA Y SALIDA)
    // ==========================================
    @GetMapping("/admin")
    public String panelAdmin(Model model) {
        if (!model.containsAttribute("nuevoProducto")) {
            model.addAttribute("nuevoProducto", new Producto());
        }
        if (!model.containsAttribute("nuevoProveedor")) {
            model.addAttribute("nuevoProveedor", new Proveedor());
        }
        if (!model.containsAttribute("nuevoLote")) {
            model.addAttribute("nuevoLote", new Lote());
        }

        // Listas ordenadas de forma descendente (para admin)
        model.addAttribute("productos", productoRepository.findAllByOrderByIdProductoDesc());
        model.addAttribute("proveedores", proveedorRepository.findAllByOrderByIdProveedorDesc());
        model.addAttribute("lotes", loteRepository.findAllByOrderByIdLoteDesc());
        model.addAttribute("movimientos", movimientoRepository.findAllByOrderByIdMovimientoDesc());

        // Alertas del sistema
        model.addAttribute("stockBajo", productoService.consultarStockBajo());
        model.addAttribute("lotesPorVencer", loteService.consultarLotesPorVencer());

        return "admin";
    }

    // ==========================================
    // PANEL DE FARMACÉUTICO (SALIDA)
    // ==========================================
    @GetMapping("/farmaceutico")
    public String panelFarmaceutico(Model model) {
        // FILTRAR: Mostrar únicamente productos con stock útil y lotes vigentes
        List<Producto> productosConStock = productoService.consultarCatalogo().stream()
                .filter(p -> loteRepository.findAll().stream()
                        .anyMatch(l -> l.getProducto() != null
                                && l.getProducto().getIdProducto().equals(p.getIdProducto())
                                && ("ACTIVO".equalsIgnoreCase(l.getEstado()) || "VIGENTE".equalsIgnoreCase(l.getEstado()))
                                && l.getFechaVencimiento().isAfter(LocalDate.now())
                                && l.getCantidad() > 0))
                .collect(Collectors.toList());

        // Algoritmo salida estricto: Ordenado por fecha de vencimiento más cercana
        List<Lote> lotesVigentes = loteRepository.findAll().stream()
                .filter(l -> ("ACTIVO".equalsIgnoreCase(l.getEstado()) || "VIGENTE".equalsIgnoreCase(l.getEstado()))
                        && l.getFechaVencimiento().isAfter(LocalDate.now())
                        && l.getCantidad() > 0)
                .sorted(Comparator.comparing(Lote::getFechaVencimiento)
                        .thenComparing(l -> l.getFechaIngreso() != null ? l.getFechaIngreso() : LocalDate.MIN))
                .collect(Collectors.toList());

        // Cálculo de stock disponible real por producto
        Map<Long, Integer> stockPorProducto = new HashMap<>();
        for (Lote l : lotesVigentes) {
            if (l.getProducto() != null) {
                stockPorProducto.merge(l.getProducto().getIdProducto(), l.getCantidad(), Integer::sum);
            }
        }

        model.addAttribute("productos", productosConStock);
        model.addAttribute("lotesVigentes", lotesVigentes);
        model.addAttribute("stockPorProducto", stockPorProducto);
        model.addAttribute("lotes", loteRepository.findAll());
        model.addAttribute("stockBajo", productoService.consultarStockBajo());
        model.addAttribute("lotesPorVencer", loteService.consultarLotesPorVencer());
        return "farmaceutico";
    }

    // ==========================================
    // REGISTRO DE NUEVOS MEDICAMENTOS (ENTRADA)
    // ==========================================
    @PostMapping("/web/productos/guardar")
    public String guardarProducto(@Valid @ModelAttribute("nuevoProducto") Producto producto,
                                  BindingResult result,
                                  RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("org.springframework.validation.BindingResult.nuevoProducto", result);
            flash.addFlashAttribute("nuevoProducto", producto);
            flash.addFlashAttribute("error", "Error de validación en el producto. Revise los campos.");
            return "redirect:/admin";
        }
        try {
            productoService.registrarProducto(producto);
            flash.addFlashAttribute("exito", "Medicamento registrado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al registrar producto: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // ==========================================
    // REGISTRO DE PROVEEDORES (ENTRADA)
    // ==========================================
    @PostMapping("/web/proveedores/guardar")
    public String guardarProveedor(@Valid @ModelAttribute("nuevoProveedor") Proveedor proveedor,
                                   BindingResult result,
                                   RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("org.springframework.validation.BindingResult.nuevoProveedor", result);
            flash.addFlashAttribute("nuevoProveedor", proveedor);
            flash.addFlashAttribute("error", "Error de validación en el proveedor. Revise el RUC (13 dígitos) y el teléfono.");
            return "redirect:/admin";
        }
        try {
            proveedorRepository.save(proveedor);
            flash.addFlashAttribute("exito", "Proveedor registrado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al registrar proveedor: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // ==========================================
    // INGRESO DE LOTES Y VALIDACIÓN DE VENCIMIENTO (ENTRADA)
    // ==========================================
    @PostMapping("/web/lotes/guardar")
    public String guardarLote(@Valid @ModelAttribute("nuevoLote") Lote lote,
                              BindingResult result,
                              RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("org.springframework.validation.BindingResult.nuevoLote", result);
            flash.addFlashAttribute("nuevoLote", lote);
            flash.addFlashAttribute("error", "Error de validación en el lote. Verifique las fechas y la cantidad.");
            return "redirect:/admin";
        }
        try {
            if (lote.getProveedor() != null && lote.getProveedor().getIdProveedor() == null) {
                lote.setProveedor(null);
            }

            if (lote.getEstado() == null || lote.getEstado().isEmpty()) {
                lote.setEstado("ACTIVO");
            }

            loteService.registrarIngresoLote(lote);
            flash.addFlashAttribute("exito", "Lote ingresado al inventario correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Rechazado: " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // ==========================================
    // PROCESAMIENTO DE DESPACHOS (SALIDA)
    // ==========================================
    @PostMapping("/web/despachos/registrar")
    public String registrarDespacho(@RequestParam Long idProducto,
                                    @RequestParam Integer cantidad,
                                    @RequestParam String usuario,
                                    RedirectAttributes flash) {
        try {
            despachoService.solicitarDespacho(idProducto, cantidad, usuario);
            flash.addFlashAttribute("exito", "Despacho realizado con éxito.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo despachar: " + e.getMessage());
        }
        return "redirect:/farmaceutico";
    }
}