package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// ==========================================
// ENTIDAD PROVEEDOR
// ==========================================
@Entity
@Table(name = "proveedor")
public class Proveedor {

    // ======================================
    // IDENTIFICADOR DE ENTRADA
    // ======================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProveedor;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 13, max = 13, message = "El RUC debe tener exactamente 13 dígitos")
    @Pattern(regexp = "^[0-9]+$", message = "El RUC solo debe contener números")
    @Column(nullable = false, length = 13, unique = true)
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Column(nullable = false)
    private String razonSocial;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 10, message = "El teléfono no puede tener más de 10 dígitos")
    @Pattern(regexp = "^[0-9]+$", message = "El teléfono solo debe contener números")
    @Column(nullable = false, length = 10)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false)
    private String direccion;

    // ==========================================
    // GETTERS Y SETTERS EXPLÍCITOS (SALIDA)
    // ==========================================
    public Long getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}