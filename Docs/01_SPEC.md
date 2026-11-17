# 01 · SPEC — Requisitos, Reglas de Negocio y Criterios



## 1. Problema

La farmacia gestiona inventario, lotes y proveedores con hojas de cálculo y controles manuales. No se conocen las existencias reales en tiempo real, hay pérdidas por medicamentos vencidos (no se aplica salida por caducidad) y se reacciona tarde ante stock bajo.

## 2. Épicas

| Épica | Representa |
|---|---|
| Producto | Catálogo de medicamentos y su stock mínimo. |
| Lotes y vencimientos | Lotes recibidos, fechas clave y alertas. |
| Despachos | Salida de mercancía y selección del lote a entregar. |

## 3. Historias de usuario con criterios de aceptación

| ID | Historia | Épica | Prior. | Criterios de aceptación |
|---|---|---|---|---|
| **HU-01** | Como administrador quiero **registrar nuevos medicamentos** para tener todo actualizado. | Producto | Alta | CA1: se guarda nombre, descripción y stock mínimo. CA2: nombre y stock mínimo son obligatorios; stock mínimo ≥ 0. CA3: no se permite un segundo medicamento con el mismo nombre (sin distinguir mayúsculas). |
| **HU-02** | Como farmacéutico quiero **consultar el stock disponible** para saber si un producto debe despacharse. | Producto | Alta | CA1: se ve el stock total por producto. CA2: se ve el detalle por lote y proveedor. CA3: solo cuenta stock de lotes vigentes. |
| **HU-03** | Como administrador quiero **ingresar un lote** con cantidad, vencimiento y proveedor. | Lotes | Alta | CA1: se guarda número de lote, cantidad, fecha de vencimiento, fecha de ingreso (hoy por defecto), producto y proveedor. CA2: cantidad ≥ 1. CA3: número de lote solo letras, números y guiones. CA4: se genera automáticamente un movimiento `INGRESO`. |
| **HU-04** | Como administrador quiero que el sistema **impida ingresar un lote vencido**. | Lotes | Alta | CA1: vencimiento ≤ hoy → rechazo con mensaje. CA2: vencimiento ≤ fecha de ingreso → rechazo. |
| **HU-05** | Como farmacéutico quiero que el sistema **priorice la salida por vencimiento**. | Despachos | Alta | CA1: se descuenta primero del lote que vence antes. CA2: con igual vencimiento, primero el de ingreso más antiguo. CA3: si un lote no alcanza, se continúa con el siguiente. CA4: cada descuento genera un movimiento `SALIDA`. |
| **HU-06** | Como farmacéutico quiero que se **bloqueen los lotes vencidos**. | Despachos | Media | CA1: un lote con vencimiento ≤ hoy jamás se despacha. CA2: su estado pasa a `VENCIDO`. |
| **HU-07** | Como administrador quiero ver **alertas de lotes a 30 días**. | Lotes | Alta | CA1: se listan lotes vigentes con 1–30 días para vencer y cantidad > 0. |
| **HU-08** | Como administrador quiero **aviso de stock bajo**. | Lotes | Alta | CA1: se avisa si stock vigente total ≤ stock mínimo del producto (incluye productos sin lotes). |

## 4. Requisitos funcionales

| ID | Requisito | HU |
|---|---|---|
| RF1 | Registrar medicamentos y definir stock mínimo individual. | HU-01 |
| RF2 | Registrar salidas/despachos diarios. | HU-05, HU-06 |
| RF3 | Aviso automático de stock bajo. | HU-08 |
| RF4 | Aviso de lotes próximos a vencer. | HU-07 |
| RF5 | Registro de proveedores (RUC, razón social, teléfono, dirección). | HU-03 |
| RF6 | Mostrar primero los lotes que vencen más temprano. | HU-05, HU-06 |
| RF7 | Registrar ingreso de lotes (cantidad, vencimiento, ingreso, proveedor) y rechazar vencidos. | HU-03, HU-04 |
| RF8 | Consultar stock por producto, lote y proveedor. | HU-02 |

## 5. Reglas de negocio

| ID | Regla | Dónde se implementa |
|---|---|---|
| RN1 | Un producto puede tener múltiples lotes con sus fechas y cantidades. | `Lote.producto` (`@ManyToOne`) |
| RN2 | Ninguna operación puede dejar stock negativo. | `DespachoServiceImpl.solicitarDespacho` |
| RN3 | Lote vencido se bloquea automáticamente, **sin excepción**. | `DespachoServiceImpl` (filtro) + `LoteServiceImpl.bloquearLotesVencidosAutomaticamente` |
| RN4 | Orden de salida: vencimiento más cercano; empate → fecha de ingreso (luego `idLote`). | `DespachoServiceImpl` (comparator) |
| RN5 | Inventario de una sola sucursal. | Modelo sin entidad "sucursal" |
| RN6 | Stock bajo: total vigente ≤ mínimo. Por vencer: faltan **30 días o menos**. | `ProductoServiceImpl.consultarStockBajo`, `LoteServiceImpl.consultarLotesPorVencer` |
| RN7 | Prohibido registrar lotes con vencimiento ≤ fecha actual. | `LoteServiceImpl.registrarIngresoLote` |

### Decisiones del cliente

| Duda | Decisión |
|---|---|
| Umbral de alerta | Fijo, 30 días. |
| Stock bajo | Mínimo individual por producto. |
| Despachar lote vencido | Bloqueado sin excepción (sin clave de supervisor). |
| Desempate de vencimiento | Sale primero el que ingresó antes. |
| Sucursales | Solo una. |

## 6. Requisitos no funcionales

| ID | Requisito | Medida |
|---|---|---|
| RNF1 Rendimiento | Consultas de stock, orden de salida y alertas | < 2 segundos |
| RNF2 Disponibilidad | Sistema operativo en el horario de atención | 99 % |
| RNF3 Usabilidad | Un usuario aprende a despachar siguiendo el orden sugerido | < 1 hora de capacitación |
| RNF4 Seguridad y acceso | Acceso por credenciales según rol (Administrador / Farmacéutico) | Login + permisos por rol |
| RNF5 Integridad de datos | Cantidades consistentes y nunca < 0 | Transacciones atómicas |

## 7. Modelo de datos (DER)

| Entidad | PK | Atributos |
|---|---|---|
| Producto | `id_producto` | nombre_producto, descripcion, stock_minimo |
| Lote | `id_lote` | numero_lote, fecha_ingreso, fecha_vencimiento, cantidad, estado, `id_producto` (FK), `id_proveedor` (FK) |
| Proveedor | `id_proveedor` | ruc (13 dígitos, único), razon_social, telefono (≤ 10 dígitos), direccion |
| MovimientoInventario | `id_movimiento` | tipo_movimiento (`INGRESO`/`SALIDA`), fecha_movimiento, cantidad, usuario, `id_lote` (FK) |

*Cardinalidades:* Producto 1—N Lote · Proveedor 1—N Lote · Lote 1—N Movimiento.

> **Nota técnica:** Se unifican los estados de lote utilizando `ACTIVO / INACTIVO / VENCIDO` en código, calculando "por vencer" de manera dinámica.

## 8. Procesos (DFD)

| Código | Proceso | HU / RF / RN |
|---|---|---|
| P1.0 | Gestionar catálogo de productos y proveedores | HU-01, HU-02, RF1, RF5, RF8 |
| P2.0 | Gestionar ingreso de lotes | HU-03, HU-04, RF7, RN7 |
| P3.0 | Gestionar stock y consultas | HU-02, RF8 |
| P4.0 | Gestionar salidas | HU-05, HU-06, RF2, RF6, RN2–RN4 |
| P5.0 | Controlar alertas preventivas | HU-07, HU-08, RF3, RF4, RN6 |

*Almacenes:* **D1** Productos y Lotes · **D2** Proveedores · **D3** Movimientos de inventario.

## 9. Fuera de alcance

Diagnóstico y prescripción clínica · sustitución de medicamentos · facturación electrónica y contabilidad avanzada · soporte multi-sucursal · umbral de alerta configurable dinámicamente.