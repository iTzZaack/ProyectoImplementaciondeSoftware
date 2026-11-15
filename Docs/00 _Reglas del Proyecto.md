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

1. **Seguridad del paciente primero:** un lote vencido **nunca** puede ingresar ni despacharse. Sin excepciones y sin clave de supervisor (decisión del cliente).
2. **Stock nunca negativo:** ninguna operación puede dejar un lote con cantidad < 0.
3. **FEFO:** se despacha primero el lote vigente con vencimiento más cercano; si empatan, el que ingresó antes.
4. **Trazabilidad:** todo ingreso y toda salida deja un `MovimientoInventario` con fecha, cantidad, usuario y lote.
5. **Una sola sucursal:** el sistema no maneja multi-sucursal.
6. **Las reglas de negocio viven en el Service**, nunca en el Controller ni en la vista.

## 4. Arquitectura obligatoria (capas)

```
Controller  →  Service (interface)  ←  ServiceImpl  →  Repository (interface)  →  Entity  →  MySQL
```

- El Controller **depende solo de la interface** `*Service`, nunca de `*ServiceImpl`.
- Solo el `ServiceImpl` habla con los `Repository`. **Un Controller no debe inyectar un Repository.**
- Las `Entity` representan el DER; no contienen lógica de negocio pesada.
- Reglas de validación de formato (RUC, teléfono, cantidad mínima) van como anotaciones Bean Validation en la Entity (`@Valid` en el Controller).

## 5. Convenciones de código

| Tema | Regla |
|---|---|
| Idioma | Nombres de clases, métodos y campos en español, igual que el DER (`registrarIngresoLote`, `stockMinimo`). |
| Clases | `PascalCase`; sufijos `Controller`, `Service`, `ServiceImpl`, `Repository`. |
| Métodos | `camelCase` con verbo: `registrar…`, `consultar…`, `bloquear…`, `solicitar…`. |
| Endpoints REST | Prefijo `/api/<recurso>` en plural. Vistas web: `/`, `/admin`, `/farmaceutico`, `/web/...`. |
| Fechas | `LocalDate` para vencimiento/ingreso; `LocalDateTime` para movimientos. Siempre comparar contra `LocalDate.now()`. |
| Estados de lote | Valores permitidos: `ACTIVO`, `INACTIVO`, `VENCIDO`. (Ver `01_SPEC.md` §6: decidir si se unifica con `VIGENTE`/`POR VENCER` del análisis.) |
| Errores | Las reglas violadas lanzan excepción con mensaje claro en español; el usuario nunca debe ver un stack trace. |
| Commits | Mensajes en español, en presente, ligados a HU/RF/RN (ej. `HU-05: ordenar despacho por FEFO`). |

## 6. Restricciones técnicas

- **Base de datos:** MySQL, esquema `farmacia_db`. `ddl-auto=update` solo en desarrollo.
- **Secretos:** la contraseña de la BD **no se versiona**. Usar variables de entorno (`${DB_PASSWORD}`) o un `application-local.properties` ignorado por Git.
- **No versionar:** `target/`, `.idea/workspace.xml`, credenciales.
- **Dependencias:** solo las del `pom.xml` (web, thymeleaf, data-jpa, validation, mysql). Agregar una nueva requiere acuerdo del equipo.
- **Umbral de vencimiento:** fijo en **30 días** en la versión 1 (no configurable).
- **Stock mínimo:** individual por producto.

## 7. Fuera de alcance (v1)

Diagnóstico o prescripción clínica · recomendación o sustitución de medicamentos · facturación electrónica / contabilidad avanzada · gestión de varias sucursales · umbral de alerta configurable.

## 8. Cómo usar la IA en este proyecto

1. Darle siempre estos 5 archivos como contexto: `00` → `04`.
2. Pedir cambios **por tarea** (`T-xx` de `03_TASKS.md`), no "arréglalo todo".
3. Toda respuesta de la IA que toque lógica de negocio debe citar la RN/RF/HU que implementa.
4. Revisar el código generado contra `04_VALIDACION.md` antes de hacer commit.
5. Si una decisión cambia, **se actualiza el `.md` primero** y luego el código.

## 9. Definition of Done (una tarea está terminada cuando…)

- [ ] Cumple los criterios de aceptación de su HU en `01_SPEC.md`.
- [ ] Respeta las capas (sección 4).
- [ ] Tiene prueba (unitaria o manual documentada) en `04_VALIDACION.md`.
- [ ] Compila sin errores y la app arranca.
- [ ] No introduce secretos ni archivos de build al repositorio.
- [ ] Está commiteada con mensaje que cita la HU/RF/RN.