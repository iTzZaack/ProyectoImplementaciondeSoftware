package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Table(name = "lote")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLote;

    @NotBlank(message = "El número de lote es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9\\-]+$", message = "El número de lote solo permite letras, números y guiones")
    @Column(nullable = false)
    private String numeroLote;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    @Column(nullable = false)
    private Integer cantidad;

    private String estado;

    @NotNull(message = "Debe seleccionar un producto")
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    // --- AUTOMATIZAR FECHA Y ESTADO ANTES DE GUARDAR ---
    @PrePersist
    public void prePersist() {
        if (this.fechaIngreso == null) {
            this.fechaIngreso = LocalDate.now();
        }
        if (this.estado == null) {
            this.estado = "ACTIVO";
        }
    }

    // --- GETTERS Y SETTERS EXPLÍCITOS ---

    public Long getIdLote() {
        return idLote;
    }

    public void setIdLote(Long idLote) {
        this.idLote = idLote;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(String numeroLote) {
        this.numeroLote = numeroLote;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
}