# 04 · VALIDACIÓN — Pruebas y Cumplimiento

> Resultado de revisar el código contra `01_SPEC.md` y `02_PLAN.md`.

## 1. Veredicto

El proyecto está bien encaminado: respeta la arquitectura por capas del análisis, implementa FEFO, bloqueo por vencimiento en despacho, RN7, alertas de 30 días y stock bajo, con trazabilidad por movimientos. Hay 3 problemas de prioridad alta que conviene corregir antes de entregar y varios puntos de mejora.

| Resultado | Cantidad |
|---|---|
| Historias de usuario cumplidas | 8 / 8 (HU-06 con observación) |
| Requisitos funcionales cumplidos | 6 / 8 completos · 2 parciales (RF5, RF8) |
| Reglas de negocio cumplidas | 7 / 7 (RN2 y RN3 con observación) |
| Requisitos no funcionales | RNF4 no implementado · RNF5 parcial · RNF1–3 sin medir |

## 2. Hallazgos

### Alta

| ID | Hallazgo | Dónde | Impacto | Corrección |
|---|---|---|---|---|
| H-01 | El bloqueo automático nunca se ejecuta. Existe @Scheduled pero Main no tiene @EnableScheduling. | Main.java, LoteServiceImpl.bloquearLotesVencidosAutomaticamente | Los lotes vencidos siguen con estado ACTIVO en la tabla del admin (el despacho sí los excluye por fecha, por eso RN3 se cumple en la práctica, pero la pantalla engaña). | Agregar @EnableScheduling sobre Main (ver §5). |
| H-02 | Contraseña de MySQL en texto plano y ya commiteada (root / 160805). | application.properties, historial Git | Cualquiera con acceso al repositorio la ve. | Mover a variable de entorno, cambiar la contraseña y, si el repositorio es público, reescribir el historial. |
| H-03 | No hay autenticación ni roles (RNF4). /admin y /farmaceutico están abiertos; la pantalla de inicio solo es un enlace. | VistaFarmaciaController, index.html | Cualquiera puede registrar lotes o despachar. El campo "usuario" del despacho se escribe a mano. | Implementar control de acceso o Spring Security con roles ADMIN y FARMACEUTICO. |

### Media

| ID | Hallazgo | Dónde | Impacto |
|---|---|---|---|
| H-04 | Los errores de negocio responden HTTP 500 en la API (no hay @ControllerAdvice). | LoteController, DespachoController | Stock insuficiente o lote vencido devuelven error genérico en lugar de código 400/409 con mensaje. |
| H-05 | registrarIngresoLote sin @Transactional: guarda el lote y luego el movimiento por separado. | LoteServiceImpl | Si falla el movimiento, queda un lote sin su registro INGRESO (RNF5). |
| H-06 | Sin bloqueo de concurrencia en el despacho. | DespachoServiceImpl | Dos despachos simultáneos del mismo producto pueden leer el mismo stock y descontarlo dos veces (RN2/RNF5). |
| H-07 | Consultas ineficientes: loteRepository.findAll() filtrado en memoria; en consultarStockBajo se llama dentro del bucle por producto; en /farmaceutico se llama de nuevo por cada producto. | DespachoServiceImpl, ProductoServiceImpl, VistaFarmaciaController | Funciona con pocos datos; con miles de lotes puede romper RNF1 (< 2 s). |
| H-08 | El Controller web inyecta Repositories directamente y no existe ProveedorService. | VistaFarmaciaController | Rompe la regla de capas; RF5 queda parcial (no hay consulta/edición por servicio). |
| H-09 | PUT /api/lotes/{id}/bloquear marca VENCIDO cualquier lote, aunque no esté vencido. | LoteServiceImpl.bloquearLoteVencido | Un lote bueno puede quedar inutilizable por error. Validar fechaVencimiento <= hoy o renombrar a "inhabilitar". |
| H-10 | Despachar con un idProducto inexistente responde "Stock insuficiente. Disponible: 0". | DespachoServiceImpl | Mensaje confuso; falta validar que el producto exista. |
| H-11 | Estados de lote distintos al análisis (ACTIVO/INACTIVO/VENCIDO vs VIGENTE/POR VENCER/VENCIDO); el código acepta ambos al filtrar. | Entities, services, vista | Datos mezclados e inconsistentes. |

### Baja

| ID | Hallazgo |
|---|---|
| H-12 | No hay tests ni spring-boot-starter-test en el pom.xml. |
| H-13 | El usuario del movimiento de ingreso está fijo en "admin_inventario". |
| H-14 | RUC duplicado (restricción unique) produce un mensaje de error técnico de la BD, no uno amable. |
| H-15 | MovimientoRepository.findByLoteId, ProveedorRepository.findByRuc del diseño no existen. |
| H-16 | hibernate.dialect=MySQLDialect es redundante en Hibernate 6. |
| H-17 | El teléfono valida max=10 pero no mínimo; acepta "1". |
| H-18 | Cambios sin commitear en múltiples archivos; el historial Git debe reflejar de mejor manera la evolución del proyecto. |
| H-19 | java.version=24: necesita JDK 24 instalado; Spring Boot 3.4.5 lo soporta. Si el equipo usa otro JDK, bajar a 21. |

## 3. Matriz de cumplimiento

Leyenda: [cumple] · [parcial] · [no cumple] · [sin medir]

### Historias de usuario

| ID | Estado | Evidencia / observación |
|---|---|---|
| HU-01 | [cumple] | ProductoServiceImpl.registrarProducto + validaciones en Producto + duplicados por nombre |
| HU-02 | [cumple] | /farmaceutico muestra stock total por producto y tabla de lotes vigentes con proveedor |
| HU-03 | [cumple] | LoteServiceImpl.registrarIngresoLote + movimiento INGRESO |
| HU-04 | [cumple] | Rechaza vencimiento <= hoy y <= fecha de ingreso |
| HU-05 | [cumple] | Orden vencimiento → ingreso → id; reparte entre lotes |
| HU-06 | [parcial] | El despacho excluye vencidos pero el estado VENCIDO no se actualiza solo (H-01) |
| HU-07 | [cumple] | consultarLotesPorVencer (1–30 días, cantidad > 0) y panel de alertas |
| HU-08 | [cumple] | consultarStockBajo; incluye productos sin lotes (stock 0) |

### Requisitos funcionales

| ID | Estado | Observación |
|---|---|---|
| RF1 | [cumple] | Stock mínimo individual |
| RF2 | [cumple] | Salidas con movimiento por lote |
| RF3 | [cumple] | Panel de stock bajo en admin y farmacéutico |
| RF4 | [cumple] | Panel de lotes por vencer |
| RF5 | [parcial] | Se registra y lista, pero sin ProveedorService ni API (H-08) |
| RF6 | [cumple] | Tabla de lotes vigentes ordenada FEFO |
| RF7 | [cumple] | Ingreso con todos los campos y rechazo de vencidos |
| RF8 | [parcial] | Visible en pantalla; no hay endpoint REST de stock por producto/lote/proveedor |

### Reglas de negocio

| ID | Estado | Observación |
|---|---|---|
| RN1 | [cumple] | @ManyToOne Lote→Producto |
| RN2 | [cumple] | Valida suma antes de descontar, en transacción; sin lock (H-06) |
| RN3 | [cumple] | Filtro por fecha en despacho; job de estado inactivo (H-01) |
| RN4 | [cumple] | Comparator correcto |
| RN5 | [cumple] | Sin entidad sucursal |
| RN6 | [cumple] | <= mínimo y ventana de 30 días |
| RN7 | [cumple] | Rechazo en registrarIngresoLote |

### Requisitos no funcionales

| ID | Estado | Observación |
|---|---|---|
| RNF1 | [sin medir] | Medir con datos reales; ver H-07 |
| RNF2 | [sin medir] | Depende del despliegue |
| RNF3 | [sin medir] | Interfaz simple con mensajes |
| RNF4 | [no cumple] | Sin login (H-03) |
| RNF5 | [parcial] | Despacho transaccional; ingreso no (H-05); sin lock (H-06) |

## 4. Plan de pruebas manuales

| # | Prueba | Pasos | Resultado esperado | Valida |
|---|---|---|---|---|
| P-01 | Registrar producto válido | Admin → nuevo medicamento "Ibuprofeno 400 mg", mínimo 20 | Mensaje de éxito; aparece en catálogo | HU-01 |
| P-02 | Producto duplicado | Repetir "ibuprofeno 400 mg" | Error "Ya existe un medicamento…" | HU-01 |
| P-03 | Stock mínimo negativo | Mínimo = -1 | Error de validación | HU-01 |
| P-04 | Proveedor válido | RUC de 13 dígitos, teléfono válido | Guardado correcto | RF5 |
| P-07 | Ingresar lote válido | Lote con datos correctos y fecha futura | Épico; movimiento INGRESO registrado | HU-03 |
| P-08 | Lote vencido | Vencimiento menor o igual a hoy | Rechazo con mensaje descriptivo | HU-04 |
| P-13 | FEFO simple | Despachar unidades de un producto con múltiples lotes | Se descuentan primero del lote que vence antes | HU-05 |
| P-16 | Stock insuficiente | Despachar más que el stock vigente total | Error de stock insuficiente y reversión transaccional | RN2 |
| P-21 | Stock bajo | Disminuir stock por debajo del mínimo configurado | El producto aparece en la sección de alertas de stock bajo | HU-08 |
| P-25 | Vista farmacéutico | Abrir /farmaceutico | Visualización de lotes vigentes en orden FEFO | HU-02, RF6 |

## 5. Correcciones sugeridas (Código clave)

H-01 — Activar el bloqueo automático en la clase principal:
```java
@SpringBootApplication
@EnableScheduling
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}