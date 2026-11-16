# 00 · Reglas del Proyecto — Sistema de Farmacia

> Principios y restricciones que **no cambian** durante el desarrollo. Cualquier persona (o IA) que trabaje en el proyecto debe leer este archivo primero.

| Dato | Valor |
|---|---|
| Proyecto | Sistema de Gestión de Farmacia (inventario, lotes y vencimientos) |
| Institución | Instituto Superior Tecnológico CENESTUR |
| Equipo | Damaris Coral · Isaac Jarrín |
| Stack | Java 24 · Spring Boot 3.4.5 · Spring Data JPA/Hibernate · Thymeleaf · MySQL · Maven |
| Paquete base | `org.example` |
| Repositorio | Git (rama `main`) |

## 1. Propósito

Reemplazar las hojas de cálculo y controles manuales de la farmacia por un sistema que muestre las existencias reales, **despache primero los lotes que vencen antes (FEFO)** y avise cuando haya **stock bajo** o **lotes próximos a vencer**.

## 2. Actores

| Actor | Qué hace |
|---|---|
| Administrador de inventario | Registra productos, proveedores y lotes; consulta alertas. |
| Farmacéutico / personal de despacho | Consulta stock y registra despachos (salidas). |

## 3. Principios no negociables

1. **Seguridad del paciente primero:** un lote vencido **nunca** puede ingresar ni despacharse. Sin excepciones y sin clave de supervisor.
2. **Stock nunca negativo:** ninguna operación puede dejar un lote con cantidad menor a 0.
3. **FEFO:** se despacha primero el lote vigente con vencimiento más cercano; si empatan, el que ingresó antes.
4. **Trazabilidad:** todo ingreso y toda salida deja un registro de movimiento con fecha, cantidad, usuario y lote.
5. **Una sola sucursal:** el sistema no maneja multi-sucursal.
6. **Las reglas de negocio viven en el Service**, nunca en el Controller ni en la vista.

## 4. Arquitectura obligatoria (capas)