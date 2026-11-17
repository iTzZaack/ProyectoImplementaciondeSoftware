# 03 · TASKS — Trabajo dividido

> Cada tarea es pequeña, verificable y trazable a la SPEC. Estado según la revisión del código (ver `04_VALIDACION.md`).
> Leyenda: [hecho] · [ajuste pendiente] · [por hacer]

## Fase 0 · Base del proyecto

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-00 | Crear proyecto Maven con Spring Boot 3.4.5 y dependencias (web, thymeleaf, jpa, validation, mysql) | — | [hecho] |
| T-01 | Configurar conexión MySQL farmacia_db | — | [ajuste pendiente: contraseña en claro] |
| T-02 | Estructura de paquetes controller/service/impl/repository/model | Plan §2 | [hecho] |
| T-03 | Repositorio Git inicial y .gitignore | — | [ajuste pendiente: faltan commits de los cambios] |

## Fase 1 · Modelo (Entities y Repositories)

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-10 | Entity Producto con validaciones | DER, RF1 | [hecho] |
| T-11 | Entity Proveedor (RUC 13 dígitos único, teléfono numérico) | DER, RF5 | [hecho] |
| T-12 | Entity Lote (FK producto/proveedor, @PrePersist) | DER, RN1 | [hecho] |
| T-13 | Entity MovimientoInventario | DER | [hecho] |
| T-14 | Cuatro Repository | Plan §3 | [hecho] |

## Fase 2 · Producto (Épica 1)

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-20 | registrarProducto con validación de duplicados | HU-01, RF1 | [hecho] |
| T-21 | consultarCatalogo | HU-02 | [hecho] |
| T-22 | consultarStockBajo (suma de lotes vigentes <= mínimo) | HU-08, RF3, RN6 | [ajuste pendiente: hace findAll() en bucle] |
| T-23 | ProductoController REST (3 endpoints) | HU-01/02/08 | [hecho] |

## Fase 3 · Lotes y vencimientos (Épica 2)

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-30 | registrarIngresoLote con RN7 y movimiento INGRESO | HU-03/04, RF7, RN7 | [ajuste pendiente: sin @Transactional] |
| T-31 | consultarLotesPorVencer (1–30 días) | HU-07, RF4, RN6 | [hecho] |
| T-32 | bloquearLoteVencido manual | HU-06 | [ajuste pendiente: no valida vencimiento real] |
| T-33 | Job @Scheduled de bloqueo automático | RN3 | [ajuste pendiente: falta @EnableScheduling] |
| T-34 | LoteController REST | HU-03/06/07 | [hecho] |

## Fase 4 · Despachos (Épica 3)

| ID | Tarea                                                           | Trazabilidad | Estado |
|---|-----------------------------------------------------------------|---|---|
| T-40 | solicitarDespacho con Salida, stock suficiente y @Transactional | HU-05, RF2/RF6, RN2/RN4 | [hecho] |
| T-41 | Excluir lotes vencidos del despacho                             | HU-06, RN3 | [hecho] |
| T-42 | Un movimiento SALIDA por cada lote tocado                       | RF2 | [hecho] |
| T-43 | DespachoController REST (/solicitar)                            | HU-05 | [hecho] |
| T-44 | Endpoint /api/despachos/confirmar del diseño                    | Plan D1 | [por hacer] |
| T-45 | Validar que el producto exista antes de despachar               | Plan D7 | [por hacer] |

## Fase 5 · Interfaz web

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-50 | index.html selector de rol | RNF3 | [hecho] |
| T-51 | admin.html (formularios producto/proveedor/lote, tablas, alertas) | HU-01/03/07/08 | [hecho] |
| T-52 | farmaceutico.html (despacho, stock, lotes, alertas) | HU-02/05, RF6/RF8 | [hecho] |
| T-53 | Mensajes de éxito/error con flash attributes | RNF3 | [hecho] |

## Fase 6 · Pendientes para cumplir el análisis completo

| ID | Prioridad | Tarea | Trazabilidad |
|---|---|---|---|
| T-60 | Alta | Agregar @EnableScheduling a Main | RN3, HU-06 |
| T-61 | Alta | Sacar la contraseña de BD de application.properties y cambiarla | Reglas §6 |
| T-62 | Alta | Login con roles ADMIN / FARMACEUTICO (Spring Security) | RNF4 |
| T-63 | Media | @RestControllerAdvice: errores de negocio → HTTP 400/409 | RNF3 |
| T-64 | Media | Crear ProveedorService/ServiceImpl y quitar repos del Controller | Reglas §4, RF5 |
| T-65 | Media | @Transactional en registrarIngresoLote | RNF5 |
| T-66 | Media | Reemplazar findAll()+filtros por consultas en repositorio | RNF1 |
| T-67 | Media | Bloqueo pesimista en el despacho contra concurrencia | RN2, RNF5 |
| T-68 | Media | Unificar estados de lote (ACTIVO/VENCIDO vs VIGENTE/POR VENCER) | Spec §7 |
| T-69 | Baja | Mostrar el estado VENCIDO correcto en la tabla admin | HU-06 |
| T-70 | Baja | Usuario del movimiento tomado de la sesión | RF2, Spec §7 |
| T-71 | Baja | Mensaje amable si el RUC ya existe | RF5 |
| T-72 | Baja | GET de stock por producto/lote/proveedor en la API REST | RF8 |
| T-73 | Baja | Quitar hibernate.dialect y excluir target/ y .idea/ de Git | Reglas §6 |

## Fase 7 · Pruebas y entrega

| ID | Tarea | Trazabilidad | Estado |
|---|---|---|---|
| T-80 | Agregar spring-boot-starter-test | — | [por hacer] |
| T-81 | Pruebas unitarias de DespachoServiceImpl (FEFO, stock, etc.) | RN2/3/4 | [por hacer] |
| T-82 | Pruebas de LoteServiceImpl (RN7, 30 días, etc.) | RN6/7 | [por hacer] |
| T-83 | Pruebas de ProductoServiceImpl (duplicado, stock bajo) | RN6 | [por hacer] |
| T-84 | Ejecutar el checklist manual de 04_VALIDACION.md | Todas | [por hacer] |
| T-85 | Actualizar el PDF de análisis con diferencias D1–D10 de 02_PLAN.md | Plan §6 | [por hacer] |
| T-86 | Commit final y push | — | [por hacer] |

## Orden sugerido

T-60 → T-61 → T-65 → T-63 → T-62 → T-64 → T-66/67 → T-80…T-84 → T-85 → T-86

## Reparto sugerido del equipo

| Persona | Tareas |
|---|---|
| Damaris | T-60, T-61, T-62, T-63, T-73, T-86 |
| Isaac | T-64, T-65, T-66, T-67, T-68 |
| Ambos | T-80…T-85 |