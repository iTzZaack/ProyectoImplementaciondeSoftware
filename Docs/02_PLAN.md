# 02 · PLAN — Arquitectura y Diseño

> Cómo se construye lo definido en `01_SPEC.md`. Refleja el estado **real del código** y marca las diferencias con el diagrama del análisis.

## 1. Visión de capas

```
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
```

## 2. Estructura de paquetes

```
src/main/java/org/example
├── Main.java
├── controller/   ProductoController, LoteController, DespachoController, VistaFarmaciaController
├── service/      ProductoService, LoteService, DespachoService
├── impl/         ProductoServiceImpl, LoteServiceImpl, DespachoServiceImpl
├── repository/   ProductoRepository, LoteRepository, ProveedorRepository, MovimientoRepository
└── model/        Producto, Lote, Proveedor, MovimientoInventario
src/main/resources
├── application.properties
└── templates/    index.html, admin.html, farmaceutico.html
```

## 3. Componentes

### 3.1 Controllers REST

| Clase | Método | Endpoint | Servicio llamado | HU |
|---|---|---|---|---|
| `ProductoController` | `registrarProducto` | `POST /api/productos` | `registrarProducto` | HU-01 |
| | `consultarCatalogo` | `GET /api/productos` | `consultarCatalogo` | HU-02 |
| | `consultarStockBajo` | `GET /api/productos/stock-bajo` | `consultarStockBajo` | HU-08 |
| `LoteController` | `registrarIngresoLote` | `POST /api/lotes` | `registrarIngresoLote` | HU-03/04 |
| | `consultarLotesPorVencer` | `GET /api/lotes/por-vencer` | `consultarLotesPorVencer` | HU-07 |
| | `bloquearLoteVencido` | `PUT /api/lotes/{id}/bloquear` | `bloquearLoteVencido` | HU-06 |
| `DespachoController` | `solicitarDespacho` | `POST /api/despachos/solicitar?idProducto&cantidad&usuario` | `solicitarDespacho` | HU-05 |

### 3.2 Controller web (Thymeleaf)

`VistaFarmaciaController`

| Ruta | Acción |
|---|---|
| `GET /` | Pantalla de selección de rol |
| `GET /admin` | Panel admin: productos, proveedores, lotes, movimientos, alertas |
| `GET /farmaceutico` | Panel farmacéutico: stock por producto, lotes vigentes en orden FEFO, alertas |
| `POST /web/productos/guardar` | HU-01 |
| `POST /web/proveedores/guardar` | RF5 |
| `POST /web/lotes/guardar` | HU-03/04 |
| `POST /web/despachos/registrar` | HU-05 |

### 3.3 Services

| Interface | Métodos |
|---|---|
| `ProductoService` | `registrarProducto`, `consultarCatalogo`, `consultarStockBajo` |
| `LoteService` | `registrarIngresoLote`, `consultarLotesPorVencer`, `bloquearLoteVencido` |
| `DespachoService` | `solicitarDespacho`, `registrarSalidaDespacho` |

### 3.4 Dónde vive cada regla

| Regla | Clase.método | Mecanismo |
|---|---|---|
| RN7 / HU-04 | `LoteServiceImpl.registrarIngresoLote` | `if (!venc.isAfter(hoy)) throw` |
| Vencimiento > ingreso | `LoteServiceImpl.registrarIngresoLote` | validación adicional |
| Movimiento de ingreso | `LoteServiceImpl.registrarIngresoLote` | crea `MovimientoInventario` tipo `INGRESO` |
| RN6 por vencer | `LoteServiceImpl.consultarLotesPorVencer` | `findByFechaVencimientoBetween(hoy+1, hoy+30)` |
| RN3 bloqueo | `LoteServiceImpl.bloquearLotesVencidosAutomaticamente` | `@Scheduled(fixedRate = 1 h)` ⚠ requiere `@EnableScheduling` |
| RN4 FEFO | `DespachoServiceImpl.solicitarDespacho` | sort por vencimiento → ingreso → `idLote` |
| RN2 stock | `DespachoServiceImpl.solicitarDespacho` | suma vigente < cantidad → excepción; `@Transactional` |
| RN6 stock bajo | `ProductoServiceImpl.consultarStockBajo` | suma lotes vigentes ≤ `stockMinimo` |
| Duplicados (DFD 1.2) | `ProductoServiceImpl.registrarProducto` | `existsByNombreProductoIgnoreCase` |
| Formato RUC/teléfono/lote | Entities | Bean Validation (`@Pattern`, `@Size`, `@Min`, `@NotBlank`) |

### 3.5 Algoritmo de despacho (FEFO)

```
solicitarDespacho(idProducto, cantidad, usuario)
 1. cantidad > 0, si no → error
 2. lotes = lotes del producto con estado ACTIVO/VIGENTE y vencimiento > hoy
 3. ordenar lotes por (fechaVencimiento, fechaIngreso, idLote)
 4. si suma(lotes.cantidad) < cantidad → error "Stock insuficiente"
 5. por cada lote en orden, mientras falte cantidad:
        tomar = min(restante, lote.cantidad)
        lote.cantidad -= tomar ; guardar
        registrar Movimiento SALIDA (tomar, usuario, lote)
        restante -= tomar
 (todo dentro de una transacción)
```

## 4. Modelo de datos

```
Producto 1 ───< Lote >─── 1 Proveedor
                  │ 1
                  ▼ N
          MovimientoInventario
```

| Tabla | Columnas |
|---|---|
| `producto` | `id_producto` PK · `nombre_producto` NN · `descripcion` TEXT · `stock_minimo` NN |
| `proveedor` | `id_proveedor` PK · `ruc` NN único len 13 · `razon_social` NN · `telefono` NN len 10 · `direccion` NN |
| `lote` | `id_lote` PK · `numero_lote` NN · `fecha_ingreso` NN · `fecha_vencimiento` NN · `cantidad` NN · `estado` · `id_producto` FK NN · `id_proveedor` FK |
| `movimiento_inventario` | `id_movimiento` PK · `tipo_movimiento` · `fecha_movimiento` · `cantidad` · `usuario` · `id_lote` FK |

## 5. Configuración

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/farmacia_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> El archivo actual tiene la contraseña escrita en claro; debe pasar a variable de entorno (ver `04_VALIDACION.md`, H-02).
> `hibernate.dialect=MySQLDialect` es redundante en Hibernate 6 y se puede eliminar.

## 6. Diferencias entre el diseño del PDF y el código

| # | Diseño (PDF) | Código actual | Decisión sugerida |
|---|---|---|---|
| D1 | `DespachoController.registrarSalidaDespacho` → `POST /api/despachos/confirmar` | No existe el endpoint (el método existe solo en el service) | Implementarlo o quitarlo del diseño |
| D2 | `LoteController` → `PUT /api/{id}/lotes/bloqueado` | `PUT /api/lotes/{id}/bloquear` | Actualizar el PDF (la ruta del código es más correcta) |
| D3 | `ProveedorService`/`ProveedorController` (RF5) | El proveedor se guarda directo con `ProveedorRepository` desde `VistaFarmaciaController` | Crear `ProveedorService` + `ProveedorServiceImpl` |
| D4 | `ProductoRepository.findByNombreProducto/findByStockMinimo` | `existsByNombreProductoIgnoreCase` | Actualizar PDF |
| D5 | `ProveedorRepository.findByRuc`, `MovimientoRepository.findByLoteId` | No existen | Agregar cuando se use (validar RUC duplicado con mensaje amable) |
| D6 | `LoteServiceImpl` con `productoRepository`, `proveedorRepository`, `movimientoRepository` | Solo `loteRepository` y `movimientoRepository` | Agregar al validar producto/proveedor, o actualizar PDF |
| D7 | `DespachoServiceImpl` con `productoRepository` | No lo usa | Validar que el producto exista (hoy un id inexistente da "Stock insuficiente") |
| D8 | Entidad "Movimiento" | `MovimientoInventario` | Unificar nombre en el PDF |
| D9 | Estados `VIGENTE`/`POR VENCER`/`VENCIDO` | `ACTIVO`/`INACTIVO`/`VENCIDO` (y se aceptan ambos al filtrar) | Elegir uno |
| D10 | RNF4 login por rol | Sin autenticación | Agregar Spring Security |

## 7. Decisiones de diseño pendientes

1. **Autenticación (RNF4):** Spring Security con dos roles `ADMIN` y `FARMACEUTICO`; el `usuario` del movimiento sale del usuario autenticado (hoy se escribe a mano / `admin_inventario` fijo).
2. **Manejo de errores:** `@RestControllerAdvice` que traduzca `IllegalArgumentException` → 400 y reglas de negocio → 409/422 con JSON.
3. **Consultas eficientes:** reemplazar `findAll()` + filtros en memoria por consultas en el repositorio (`findByProductoIdProductoAndEstado…`, o `@Query` con suma agregada).
4. **Concurrencia:** bloqueo pesimista (`@Lock(PESSIMISTIC_WRITE)`) en la consulta de lotes del despacho.
5. **Tests:** agregar `spring-boot-starter-test` y pruebas de las 7 RN.