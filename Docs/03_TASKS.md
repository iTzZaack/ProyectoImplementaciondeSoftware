# 03 · TASKS — Trabajo dividido

> Cada tarea es pequeña, verificable y trazable a la SPEC. Estado según la revisión del código (ver `04_VALIDACION.md`).
> Leyenda: ✅ hecho · 🔧 hecho con ajuste pendiente · ⬜ por hacer

## Fase 0 · Base del proyecto

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-00 | Crear proyecto Maven con Spring Boot 3.4.5 y dependencias (web, thymeleaf, jpa, validation, mysql) | — | ✅ |
| T-01 | Configurar conexión MySQL `farmacia_db` | — | 🔧 contraseña en claro |
| T-02 | Estructura de paquetes `controller/service/impl/repository/model` | Plan §2 | ✅ |
| T-03 | Repositorio Git inicial y `.gitignore` | — | 🔧 faltan commits de los cambios |

## Fase 1 · Modelo (Entities y Repositories)

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-10 | Entity `Producto` con validaciones | DER, RF1 | ✅ |
| T-11 | Entity `Proveedor` (RUC 13 dígitos único, teléfono numérico) | DER, RF5 | ✅ |
| T-12 | Entity `Lote` (FK producto/proveedor, `@PrePersist`) | DER, RN1 | ✅ |
| T-13 | Entity `MovimientoInventario` | DER | ✅ |
| T-14 | Cuatro `Repository` | Plan §3 | ✅ |

## Fase 2 · Producto (Épica 1)

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-20 | `registrarProducto` con validación de duplicados | HU-01, RF1 | ✅ |
| T-21 | `consultarCatalogo` | HU-02 | ✅ |
| T-22 | `consultarStockBajo` (suma de lotes vigentes ≤ mínimo) | HU-08, RF3, RN6 | 🔧 hace `findAll()` de lotes dentro de un bucle por producto |
| T-23 | `ProductoController` REST (3 endpoints) | HU-01/02/08 | ✅ |

## Fase 3 · Lotes y vencimientos (Épica 2)

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-30 | `registrarIngresoLote` con RN7 y movimiento `INGRESO` | HU-03/04, RF7, RN7 | 🔧 sin `@Transactional` |
| T-31 | `consultarLotesPorVencer` (1–30 días) | HU-07, RF4, RN6 | ✅ |
| T-32 | `bloquearLoteVencido` manual | HU-06 | 🔧 no valida que el lote realmente esté vencido |
| T-33 | Job `@Scheduled` de bloqueo automático | RN3 | 🔧 **no corre**: falta `@EnableScheduling` en `Main` |
| T-34 | `LoteController` REST | HU-03/06/07 | ✅ |

## Fase 4 · Despachos (Épica 3)

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-40 | `solicitarDespacho` con FEFO, stock suficiente y `@Transactional` | HU-05, RF2/RF6, RN2/RN4 | ✅ |
| T-41 | Excluir lotes vencidos del despacho | HU-06, RN3 | ✅ |
| T-42 | Un movimiento `SALIDA` por cada lote tocado | RF2 | ✅ |
| T-43 | `DespachoController` REST (`/solicitar`) | HU-05 | ✅ |
| T-44 | Endpoint `/api/despachos/confirmar` del diseño | Plan D1 | ⬜ |
| T-45 | Validar que el producto exista antes de despachar | Plan D7 | ⬜ |

## Fase 5 · Interfaz web

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-50 | `index.html` selector de rol | RNF3 | ✅ |
| T-51 | `admin.html` (formularios producto/proveedor/lote, tablas, alertas) | HU-01/03/07/08 | ✅ |
| T-52 | `farmaceutico.html` (despacho, stock, lotes FEFO, alertas) | HU-02/05, RF6/RF8 | ✅ |
| T-53 | Mensajes de éxito/error con *flash attributes* | RNF3 | ✅ |

## Fase 6 · Pendientes para cumplir el análisis completo

| ID | Prioridad | Tarea | Trazabilidad |
|---|---|---|---|
| T-60 | 🔴 Alta | Agregar `@EnableScheduling` a `Main` | RN3, HU-06 |
| T-61 | 🔴 Alta | Sacar la contraseña de BD de `application.properties` (variable de entorno) y **cambiar la contraseña** | Reglas §6 |
| T-62 | 🔴 Alta | Login con roles `ADMIN` / `FARMACEUTICO` (Spring Security) | RNF4 |
| T-63 | 🟠 Media | `@RestControllerAdvice`: errores de negocio → HTTP 400/409 con mensaje (hoy responden 500) | RNF3 |
| T-64 | 🟠 Media | Crear `ProveedorService`/`ServiceImpl` y quitar repositorios del `VistaFarmaciaController` | Reglas §4, RF5 |
| T-65 | 🟠 Media | `@Transactional` en `registrarIngresoLote` | RNF5 |
| T-66 | 🟠 Media | Reemplazar `findAll()`+filtros en memoria por consultas en repositorio (despacho, stock bajo, vista farmacéutico) | RNF1 |
| T-67 | 🟠 Media | Bloqueo pesimista en el despacho para evitar doble descuento concurrente | RN2, RNF5 |
| T-68 | 🟠 Media | Unificar estados de lote (`ACTIVO/VENCIDO` vs `VIGENTE/POR VENCER`) | Spec §7 |
| T-69 | 🟡 Baja | Mostrar el estado "VENCIDO" correcto en la tabla admin (depende de T-60) | HU-06 |
| T-70 | 🟡 Baja | Usuario del movimiento tomado de la sesión (no `admin_inventario` fijo) | RF2, Spec §7 |
| T-71 | 🟡 Baja | Mensaje amable si el RUC ya existe (hoy sale error de BD) | RF5 |
| T-72 | 🟡 Baja | `GET` de stock por producto/lote/proveedor en la API REST | RF8 |
| T-73 | 🟡 Baja | Quitar `hibernate.dialect` y dejar `target/` e `.idea/workspace.xml` fuera de Git | Reglas §6 |

## Fase 7 · Pruebas y entrega

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-80 | Agregar `spring-boot-starter-test` | — | ⬜ |
| T-81 | Pruebas unitarias de `DespachoServiceImpl` (FEFO, desempate, stock insuficiente, lote vencido) | RN2/3/4 | ⬜ |
| T-82 | Pruebas de `LoteServiceImpl` (RN7, vencimiento ≤ ingreso, 30 días) | RN6/7 | ⬜ |
| T-83 | Pruebas de `ProductoServiceImpl` (duplicado, stock bajo) | RN6 | ⬜ |
| T-84 | Ejecutar el checklist manual de `04_VALIDACION.md` con los datos de ejemplo del PDF | Todas | ⬜ |
| T-85 | Actualizar el PDF de análisis con las diferencias D1–D10 de `02_PLAN.md` | Plan §6 | ⬜ |
| T-86 | Commit final y push | — | ⬜ |

## Orden sugerido

`T-60 → T-61 → T-65 → T-63 → T-62 → T-64 → T-66/67 → T-80…T-84 → T-85 → T-86`

## Reparto sugerido del equipo

| Persona | Tareas |
|---|---|
| Damaris | T-60, T-61, T-62, T-63, T-73, T-86 |
| Isaac | T-64, T-65, T-66, T-67, T-68 |
| Ambos | T-80…T-85 |