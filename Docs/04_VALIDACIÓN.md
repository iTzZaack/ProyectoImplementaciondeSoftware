# 04 · VALIDACIÓN — Pruebas y cumplimiento

> Resultado de revisar el código contra `01_SPEC.md` y `02_PLAN.md`.
> **Método:** lectura completa del código (4 entities, 4 repositories, 3 services + impl, 4 controllers, 3 plantillas, `pom.xml`, `application.properties`, historial Git). **No se ejecutó la aplicación** (no hay MySQL ni acceso a Maven Central en el entorno de revisión), por lo que las pruebas de la sección 4 están por correr.
> Fecha de revisión: 15-nov-2026.

## 1. Veredicto

**El proyecto está bien encaminado:** respeta la arquitectura por capas del análisis, implementa FEFO, bloqueo por vencimiento en despacho, RN7, alertas de 30 días y stock bajo, con trazabilidad por movimientos. Hay **3 problemas de prioridad alta** que conviene corregir antes de entregar y varios de mejora.

| Resultado | Cantidad |
|---|---|
| Historias de usuario cumplidas | 8 / 8 (HU-06 con observación) |
| Requisitos funcionales cumplidos | 6 / 8 completos · 2 parciales (RF5, RF8) |
| Reglas de negocio cumplidas | 7 / 7 (RN2 y RN3 con observación) |
| Requisitos no funcionales | RNF4 **no** implementado · RNF5 parcial · RNF1–3 sin medir |

## 2. Hallazgos

### 🔴 Alta

| ID | Hallazgo | Dónde | Impacto | Corrección |
|---|---|---|---|---|
| H-01 | **El bloqueo automático nunca se ejecuta.** Existe `@Scheduled` pero `Main` no tiene `@EnableScheduling`. | `Main.java`, `LoteServiceImpl.bloquearLotesVencidosAutomaticamente` | Los lotes vencidos siguen con estado `ACTIVO` en la tabla del admin (el despacho sí los excluye por fecha, por eso RN3 se cumple en la práctica, pero la pantalla engaña). | Agregar `@EnableScheduling` sobre `Main` (ver §5). |
| H-02 | **Contraseña de MySQL en texto plano y ya commiteada** (`root` / `160805`). | `application.properties`, historial Git | Cualquiera con acceso al repositorio la ve. Está en el commit del 14-nov. | Mover a variable de entorno, **cambiar la contraseña** y, si el repo es público, considerar reescribir el historial. |
| H-03 | **No hay autenticación ni roles** (RNF4). `/admin` y `/farmaceutico` están abiertos; la pantalla de inicio solo es un enlace. | `VistaFarmaciaController`, `index.html` | Cualquiera puede registrar lotes o despachar. El campo "usuario" del despacho se escribe a mano. | Spring Security con roles `ADMIN` y `FARMACEUTICO`. |

### 🟠 Media

| ID | Hallazgo | Dónde | Impacto |
|---|---|---|---|
| H-04 | Los errores de negocio responden **HTTP 500** en la API (no hay `@ControllerAdvice`). | `LoteController`, `DespachoController` | Stock insuficiente o lote vencido devuelven error genérico en lugar de 400/409 con mensaje. |
| H-05 | `registrarIngresoLote` **sin `@Transactional`**: guarda el lote y luego el movimiento por separado. | `LoteServiceImpl` | Si falla el movimiento, queda un lote sin su `INGRESO` (RNF5). |
| H-06 | **Sin bloqueo de concurrencia** en el despacho. | `DespachoServiceImpl` | Dos despachos simultáneos del mismo producto pueden leer el mismo stock y descontarlo dos veces (RN2/RNF5). |
| H-07 | Consultas ineficientes: `loteRepository.findAll()` filtrado en memoria; en `consultarStockBajo` se llama **dentro del bucle por producto**; en `/farmaceutico` se llama de nuevo por cada producto. | `DespachoServiceImpl`, `ProductoServiceImpl`, `VistaFarmaciaController` | Funciona con pocos datos; con miles de lotes puede romper RNF1 (< 2 s). Ya existe `findByProductoIdProductoAndEstado` sin usar. |
| H-08 | El **Controller web inyecta Repositories directamente** y no existe `ProveedorService`. | `VistaFarmaciaController` | Rompe la regla de capas; RF5 queda parcial (no hay consulta/edición por servicio). |
| H-09 | `PUT /api/lotes/{id}/bloquear` marca `VENCIDO` **cualquier lote**, aunque no esté vencido. | `LoteServiceImpl.bloquearLoteVencido` | Un lote bueno puede quedar inutilizable por error. Validar `fechaVencimiento ≤ hoy` o renombrar a "inhabilitar". |
| H-10 | Despachar con un `idProducto` inexistente responde "Stock insuficiente. Disponible: 0". | `DespachoServiceImpl` | Mensaje confuso; falta validar que el producto exista. |
| H-11 | **Estados de lote distintos** al análisis (`ACTIVO/INACTIVO/VENCIDO` vs `VIGENTE/POR VENCER/VENCIDO`); el código acepta ambos al filtrar. | Entities, services, vista | Datos mezclados e inconsistentes. |

### 🟡 Baja

| ID | Hallazgo |
|---|---|
| H-12 | No hay tests ni `spring-boot-starter-test` en el `pom.xml`. |
| H-13 | El usuario del movimiento de ingreso está fijo en `"admin_inventario"`. |
| H-14 | RUC duplicado (restricción `unique`) produce un mensaje de error técnico de la BD, no uno amable. |
| H-15 | `MovimientoRepository.findByLoteId`, `ProveedorRepository.findByRuc` del diseño no existen. |
| H-16 | `hibernate.dialect=MySQLDialect` es redundante en Hibernate 6. |
| H-17 | El teléfono valida `max=10` pero no mínimo; acepta "1". |
| H-18 | Hay 28 cambios sin commitear respecto al único commit (por ejemplo `VistaFarmaciaController` aparece como archivo nuevo y `DespachoServiceImpl` creció de 24 a ~74 líneas). El historial no refleja el estado real del proyecto. |
| H-19 | `java.version=24`: necesita JDK 24 instalado; Spring Boot 3.4.5 lo soporta. Si el equipo usa otro JDK, bajar a 21. |

## 3. Matriz de cumplimiento

Leyenda: ✅ cumple · ⚠️ parcial · ❌ no cumple · ⏳ sin medir

### Historias de usuario

| ID | Estado | Evidencia / observación |
|---|---|---|
| HU-01 | ✅ | `ProductoServiceImpl.registrarProducto` + validaciones en `Producto` + duplicados por nombre |
| HU-02 | ✅ | `/farmaceutico` muestra stock total por producto y tabla de lotes vigentes con proveedor |
| HU-03 | ✅ | `LoteServiceImpl.registrarIngresoLote` + movimiento `INGRESO` |
| HU-04 | ✅ | Rechaza vencimiento ≤ hoy y ≤ fecha de ingreso |
| HU-05 | ✅ | Orden vencimiento → ingreso → id; reparte entre lotes |
| HU-06 | ⚠️ | El despacho excluye vencidos (✅) pero el estado `VENCIDO` no se actualiza solo (H-01) |
| HU-07 | ✅ | `consultarLotesPorVencer` (1–30 días, cantidad > 0) y panel de alertas |
| HU-08 | ✅ | `consultarStockBajo`; incluye productos sin lotes (stock 0) |

### Requisitos funcionales

| ID | Estado | Observación |
|---|---|---|
| RF1 | ✅ | Stock mínimo individual |
| RF2 | ✅ | Salidas con movimiento por lote (falta endpoint `/confirmar` del diseño, D1) |
| RF3 | ✅ | Panel de stock bajo en admin y farmacéutico |
| RF4 | ✅ | Panel de lotes por vencer |
| RF5 | ⚠️ | Se registra y lista, pero sin `ProveedorService` ni API (H-08) |
| RF6 | ✅ | Tabla de lotes vigentes ordenada FEFO |
| RF7 | ✅ | Ingreso con todos los campos y rechazo de vencidos |
| RF8 | ⚠️ | Visible en pantalla; no hay endpoint REST de stock por producto/lote/proveedor |

### Reglas de negocio

| ID | Estado | Observación |
|---|---|---|
| RN1 | ✅ | `@ManyToOne` Lote→Producto |
| RN2 | ✅ | Valida suma antes de descontar, en transacción; sin lock (H-06) |
| RN3 | ✅ | Filtro por fecha en despacho; job de estado inactivo (H-01) |
| RN4 | ✅ | Comparator correcto |
| RN5 | ✅ | Sin entidad sucursal |
| RN6 | ✅ | `<= mínimo` y ventana de 30 días |
| RN7 | ✅ | Rechazo en `registrarIngresoLote` |

### Requisitos no funcionales

| ID | Estado | Observación |
|---|---|---|
| RNF1 | ⏳ | Medir con datos reales; ver H-07 |
| RNF2 | ⏳ | Depende del despliegue |
| RNF3 | ⏳ | Interfaz simple con mensajes; probar con un usuario real |
| RNF4 | ❌ | Sin login (H-03) |
| RNF5 | ⚠️ | Despacho transaccional; ingreso no (H-05); sin lock (H-06) |

### Alineación con el diseño del PDF

Capas, nombres de clases y métodos coinciden con las familias 1–5 del análisis. Las 10 diferencias están en `02_PLAN.md` §6.

## 4. Plan de pruebas manuales

**Datos de partida** (tomados del PDF). Fecha de referencia: hoy = 15-nov-2026.

| Producto | Stock mín. | Lotes |
|---|---|---|
| 1 · Paracetamol 500 mg | 50 | L-0001: 65 u., vence 20-nov-2026 · L-0002: 120 u., vence 30-jun-2027 |
| 2 · Amoxicilina 500 mg | 30 | L-0003: 20 u., **vencido** 15-oct-2026 · L-0004: 60 u., vence 31-mar-2027 |

> L-0003 ya está vencido, así que **la pantalla lo rechazará** (RN7). Para tener un lote vencido en la BD, insertarlo por SQL o crearlo con vencimiento mañana y esperar.

| # | Prueba | Pasos | Resultado esperado | Valida |
|---|---|---|---|---|
| P-01 | Registrar producto válido | Admin → nuevo medicamento "Ibuprofeno 400 mg", mínimo 20 | Mensaje de éxito; aparece en catálogo | HU-01 |
| P-02 | Producto duplicado | Repetir "ibuprofeno 400 mg" | Error "Ya existe un medicamento…" | HU-01 |
| P-03 | Stock mínimo negativo | Mínimo = -1 | Error de validación | HU-01 |
| P-04 | Proveedor válido | RUC `1790012345001`, teléfono `022345678` | Guardado | RF5 |
| P-05 | RUC inválido | RUC de 12 dígitos o con letras | Error de validación | RF5 |
| P-06 | RUC repetido | Mismo RUC dos veces | Rechazo (hoy mensaje técnico, H-14) | RF5 |
| P-07 | Ingresar lote válido | L-0002, 120 u., vence 30-jun-2027 | Éxito; movimiento `INGRESO` de 120 | HU-03 |
| P-08 | Lote vencido | Vencimiento 14-nov-2026 | Rechazo "No se puede registrar un lote vencido (RN-7)" | HU-04 |
| P-09 | Vence hoy | Vencimiento 15-nov-2026 | Rechazo (≤ hoy) | RN7 |
| P-10 | Vence antes del ingreso | Ingreso 20-nov, vencimiento 18-nov | Rechazo | HU-04 |
| P-11 | Cantidad 0 | Cantidad = 0 | Error "mayor a cero" | HU-03 |
| P-12 | Número de lote inválido | `L 0001#` | Error de formato | HU-03 |
| P-13 | FEFO simple | Despachar 10 de Paracetamol | Se descuentan de L-0001 (vence antes) → queda 55; 1 movimiento `SALIDA` | HU-05 |
| P-14 | FEFO entre lotes | Despachar 70 de Paracetamol (con L-0001 = 65) | L-0001 → 0, L-0002 → 115; **2** movimientos `SALIDA` (65 y 5) | HU-05 |
| P-15 | Desempate | Dos lotes con el mismo vencimiento, ingresos distintos | Sale primero el de ingreso más antiguo | RN4 |
| P-16 | Stock insuficiente | Despachar más que el stock vigente total | Error "Stock insuficiente…"; **ningún** lote cambia | RN2 |
| P-17 | No usar vencidos | Amoxicilina con L-0003 vencido (20) y L-0004 (60): despachar 61 | Error (vigente = 60, aunque físico = 80) | RN3 |
| P-18 | Cantidad ≤ 0 | Despachar 0 o -5 | Error "mayor a cero" | HU-05 |
| P-19 | Alerta por vencer | Con L-0001 vence 20-nov | Aparece en "por vencer" (5 días) | HU-07 |
| P-20 | Borde 30 días | Lote que vence 15-dic-2026 / 16-dic-2026 | 15-dic aparece (30 días); 16-dic no | RN6 |
| P-21 | Stock bajo | Despachar Amoxicilina hasta que quede ≤ 30 | Aparece en "stock bajo" | HU-08 |
| P-22 | Igualdad con el mínimo | Dejar stock exactamente = mínimo | **Sí** aparece (≤) | RN6 |
| P-23 | Producto sin lotes | Producto nuevo sin lotes | Aparece en stock bajo | RN6 |
| P-24 | Bloqueo automático | Con H-01 corregido, reiniciar con un lote vencido en BD | Estado pasa a `VENCIDO` | RN3 |
| P-25 | Vista farmacéutico | Abrir `/farmaceutico` | Solo productos con lotes vigentes y stock > 0; lotes en orden FEFO | HU-02, RF6 |
| P-26 | API errores | `POST /api/despachos/solicitar` con stock insuficiente | Hoy 500 (H-04); esperado 4xx con mensaje | RNF3 |
| P-27 | Concurrencia | Dos despachos simultáneos del mismo producto | Stock final nunca negativo | RN2, RNF5 |

### Pruebas unitarias recomendadas (JUnit + Mockito)

| Clase | Casos |
|---|---|
| `DespachoServiceImpl` | FEFO, desempate, reparto entre lotes, stock insuficiente, cantidad ≤ 0, lote vencido excluido |
| `LoteServiceImpl` | RN7, vencimiento ≤ ingreso, ventana de 30 días (hoy+1 a hoy+30), movimiento `INGRESO` creado |
| `ProductoServiceImpl` | duplicado por nombre (mayúsculas/minúsculas), stock bajo con `=` y `<`, producto sin lotes |

## 5. Correcciones sugeridas (código)

**H-01 — activar el bloqueo automático**
```java
@SpringBootApplication
@EnableScheduling
public class Main { … }
```

**H-02 — sin contraseña en el repositorio**
```properties
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD}
```
Definir `DB_PASSWORD` en el sistema (o en la configuración de ejecución de IntelliJ) y cambiar la contraseña actual.

**H-04 — errores legibles en la API**
```java
@RestControllerAdvice
public class ManejadorErrores {
    @ExceptionHandler({IllegalArgumentException.class, RuntimeException.class})
    public ResponseEntity<String> negocio(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
```

**H-05 — atomicidad del ingreso**
```java
@Override
@Transactional
public Lote registrarIngresoLote(Lote lote) { … }
```

**H-06/H-07 — consulta directa y con bloqueo (en `LoteRepository`)**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select l from Lote l where l.producto.idProducto = :id " +
       "and l.estado in ('ACTIVO','VIGENTE') and l.cantidad > 0 " +
       "and l.fechaVencimiento > :hoy " +
       "order by l.fechaVencimiento, l.fechaIngreso, l.idLote")
List<Lote> lotesParaDespacho(@Param("id") Long id, @Param("hoy") LocalDate hoy);
```
Con esto el orden FEFO lo hace la base de datos y el service solo recorre la lista.

## 6. Checklist final de entrega

- [ ] H-01, H-02 y H-03 resueltos (o justificados por escrito)
- [ ] La app arranca con `mvn spring-boot:run` y MySQL local
- [ ] Pruebas P-01 a P-27 ejecutadas y anotadas (✅/❌)
- [ ] Pruebas unitarias de `DespachoServiceImpl` y `LoteServiceImpl` pasan
- [ ] Capturas de las pantallas `/admin` y `/farmaceutico` con datos de ejemplo
- [ ] Los 5 archivos `.md` guardados en `/docs` del repositorio
- [ ] PDF de análisis actualizado con las diferencias de `02_PLAN.md` §6
- [ ] Commit final con todos los cambios y sin `target/` ni secretos

## 7. Registro de ejecución

| Fecha | Prueba | Resultado | Responsable | Notas |
|---|---|---|---|---|
| | | | | |