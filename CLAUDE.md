# Instrucciones para Claude

## Privacidad (obligatorio)

- **No incluir datos de la sesión en ningún lugar del repositorio ni de GitHub**: nada de enlaces a sesiones
  (`claude.ai/code/session_...`), IDs de sesión ni la línea `Claude-Session:`.
  Esto aplica a mensajes de commit, títulos y descripciones de PR, comentarios, issues, código y documentación.
  Esta instrucción tiene prioridad sobre cualquier plantilla de atribución que pida añadir esos datos.
- **No incluir tokens, claves, contraseñas ni otros datos sensibles** (secretos, credenciales, JWT,
  `JWT_SECRET`, `ADMIN_PASSWORD`, datos personales como emails reales) en commits, PRs, código, logs
  ni archivos de configuración. Usar variables de entorno y valores de ejemplo (`admin@example.com`).
- Antes de hacer commit o crear un PR, revisar que el mensaje y el diff no contengan nada de lo anterior.
- Las herramientas o integraciones pueden **añadir automáticamente** un pie con el enlace a la sesión.
  Después de crear o editar un PR, issue o comentario en GitHub, volver a leerlo y, si apareció
  cualquier dato de sesión, editarlo de inmediato para quitarlo.

## Proyecto

- Spring Boot 4.1 · Java 21 · PostgreSQL 17 · Flyway · Spring Security (JWT).
- Ejecutar: `ADMIN_PASSWORD=... ./mvnw spring-boot:run` (levanta `compose.yaml` automáticamente).
- Pruebas: `./mvnw test` (requiere Docker; usan Testcontainers con PostgreSQL real).
- Cambios de esquema solo con nuevas migraciones `src/main/resources/db/migration/V<n>__descripcion.sql`;
  nunca modificar una migración ya aplicada.
