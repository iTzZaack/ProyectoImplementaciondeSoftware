# 02 · PLAN — Arquitectura y Diseño

> Cómo se construye lo definido en `01_SPEC.md`. Refleja el estado real del código y marca las coherencias con la arquitectura del sistema.

## 1. Visión de capas

```text
 Navegador / Cliente HTTP
        │
 ┌──────▼────────────────────────────────────────────────────────┐
 │ CONTROLLER   ProductoController · LoteController ·            │
 │              DespachoController   (REST  /api/**)             │
 │              VistaFarmaciaController (Thymeleaf  /, /admin…)  │
 ├───────────────────────────────────────────────────────────────┤
 │ SERVICE (interfaces)  ProductoService · LoteService ·         │
 │                       DespachoService                         │
 ├───────────────────────────────────────────────────────────────┤
 │ IMPL (reglas de negocio)  ProductoServiceImpl · LoteServiceImpl│
 │                           · DespachoServiceImpl               │
 ├───────────────────────────────────────────────────────────────┤
 │ REPOSITORY (Spring Data JPA)  Producto · Lote · Proveedor ·   │
 │                               Movimiento                      │
 ├───────────────────────────────────────────────────────────────┤
 │ ENTITY  Producto · Lote · Proveedor · MovimientoInventario    │
 └──────────────────────────┬────────────────────────────────────┘
                            ▼
                  MySQL  ·  farmacia_db