# Sistema de Gestión de Farmacia DAISA

Proyecto desarrollado para la gestión de inventario, control de lotes (Entrada/Salida), alertas de stock bajo y validación de vencimientos.

## Autores
* Damaris Coral
* Isaac Jarrín

## Tecnologías Utilizadas
* **Backend:** Java 17+, Spring Boot (Spring Data JPA)
* **Frontend:** Thymeleaf, HTML5, Bootstrap / CSS
* **Base de Datos:** MySQL

## Instrucciones para Ejecutar el Proyecto
1. Clonar el repositorio.
2. Crear una base de datos en MySQL llamada `farmacia_daisa` (o el nombre que uses en tu `application.properties`).
3. Configurar las credenciales de tu base de datos en el archivo `src/main/resources/application.properties`.
4. Ejecutar la aplicación desde tu entorno de desarrollo (IntelliJ IDEA o Eclipse) corriendo la clase principal de Spring Boot.
5. Abrir el navegador e ingresar a `http://localhost:8080`.