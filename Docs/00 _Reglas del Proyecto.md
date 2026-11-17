# 00 · Reglas del Proyecto — Sistema de Farmacia

> Principios y restricciones que **no cambian** durante el desarrollo. Cualquier persona que trabaje en el proyecto debe leer este archivo primero.

| Dato | Valor |
|---|---|
| Proyecto | Sistema de Gestión de Farmacia (inventario, lotes y vencimientos) |
| Institución | Instituto Superior Tecnológico CENESTUR |
| Equipo | Damaris Coral · Isaac Jarrín |
| Stack | Java 17 · Spring Boot · Spring Data JPA/Hibernate · Thymeleaf · MySQL · Maven |
| Paquete base | `org.example` |
| Repositorio | Git (rama `main`) |

## 1. Propósito

Reemplazar las hojas de cálculo y controles manuales de la farmacia por un sistema que muestre las existencias reales, **despache primero los lotes que vencen antes** y avise cuando haya **stock bajo** o **lotes próximos a vencer**.

## 2. Actores

| Actor | Qué hace |
|---|---|
| Administrador de inventario | Registra productos, proveedores y lotes; consulta alertas y parámetros. |
| Farmacéutico / personal de despacho | Consulta stock y registra despachos (salidas) aplicando la lógica. |

## 3. Principios no negociables

1. **Seguridad del paciente primero:** un lote vencido **nunca** puede ingresar ni despacharse (RN-3, RN-7). Sin excepciones.
2. **Stock nunca negativo:** ninguna operación de despacho puede dejar la cantidad de un lote por debajo de 0 (RN-2).
3. **Entrada/Salida:** se despacha primero el lote vigente con la fecha de vencimiento más cercana; si coinciden, se aplica desempate por fecha de ingreso (RN-4).
4. **Trazabilidad:** todo ingreso y toda salida genera obligatoriamente un registro histórico de movimiento con fecha, cantidad, usuario y lote asociado.
5. **Una sola sucursal:** el sistema opera exclusivamente para una única farmacia/sucursal local (RN-5).
6. **Separación de responsabilidades:** las reglas de negocio residen estrictamente en la capa de servicios (`ServiceImpl`), nunca en los controladores ni en las vistas HTML.

## 4. Arquitectura obligatoria (Capas)

El sistema se estructura respetando estrictamente el patrón por capas de Spring Boot:
* **Capa de Presentación (Controller):** Recibe las peticiones HTTP y coordina el flujo redirigiendo a las vistas Thymeleaf o devolviendo respuestas.
* **Capa de Negocio (Service / ServiceImpl):** Contiene la lógica de negocio pura y aplica las validaciones de las reglas (RN1 a RN7).
* **Capa de Acceso a Datos (Repository):** Interfaces que extienden de `JpaRepository` para la persistencia y consultas personalizadas con Spring Data.
* **Capa de Dominio (Entity):** Clases de entidad que representan el modelo relacional mapeado con la base de datos MySQL (Producto, Lote, Proveedor, Movimiento).