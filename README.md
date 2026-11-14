# CRUD de usuarios — Spring Boot

API REST de ejemplo con un CRUD de usuarios, autenticación JWT, PostgreSQL, migraciones con Flyway,
borrado lógico, paginación y filtros.

## Stack

- Java 21 · Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security + OAuth2 Resource Server)
- PostgreSQL 17 · Flyway
- MapStruct + Lombok
- springdoc-openapi (Swagger UI)
- Testcontainers para las pruebas

## Requisitos

- JDK 21
- Docker (para la base de datos en desarrollo y para las pruebas)

## Ejecutar

```bash
ADMIN_PASSWORD=una-clave-segura ./mvnw spring-boot:run
```

Al arrancar con `spring-boot:run`, Spring Boot levanta automáticamente el PostgreSQL de `compose.yaml`
y Flyway aplica las migraciones de `src/main/resources/db/migration`.

- Swagger UI: http://localhost:8080/swagger-ui.html (botón **Authorize** para pegar el token)

### Variables de entorno

| Variable         | Por defecto                              | Descripción |
|------------------|------------------------------------------|-------------|
| `DB_URL`         | `jdbc:postgresql://localhost:5432/crud`  | URL JDBC (ignorada si se usa `compose.yaml`) |
| `DB_USERNAME`    | `crud`                                   | Usuario de la BD |
| `DB_PASSWORD`    | `crud`                                   | Contraseña de la BD |
| `JWT_SECRET`     | *(vacía → clave aleatoria al arrancar)*  | Clave HMAC en Base64, mínimo 32 bytes: `openssl rand -base64 32`. **Obligatoria en producción.** |
| `JWT_EXPIRATION` | `1h`                                     | Duración del token |
| `ADMIN_EMAIL`    | `admin@example.com`                      | Email del administrador inicial |
| `ADMIN_PASSWORD` | *(vacía → no se crea admin)*             | Si se define, se crea el admin al arrancar (si no existe) |

## Pruebas

```bash
./mvnw test
```

Las pruebas usan un PostgreSQL real en Docker (Testcontainers), con las mismas migraciones de Flyway.

## Autenticación

```bash
# 1. Registrarse (rol USER)
curl -X POST localhost:8080/v1/auth/register -H 'Content-Type: application/json' \
  -d '{"name":"Ana","lastName":"Pérez","email":"ana@example.com","password":"ana-pass-123"}'

# 2. Login → {"accessToken":"...","tokenType":"Bearer","expiresIn":3600}
TOKEN=$(curl -s -X POST localhost:8080/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ana@example.com","password":"ana-pass-123"}' | jq -r .accessToken)

# 3. Usar el token
curl localhost:8080/v1/auth/me -H "Authorization: Bearer $TOKEN"
```

El token incluye `sub` (id del usuario), `email` y `roles`.

## Endpoints

| Método | Ruta                 | Acceso                    | Descripción |
|--------|----------------------|---------------------------|-------------|
| POST   | `/v1/auth/register`  | Público                   | Registro con rol USER → 201 |
| POST   | `/v1/auth/login`     | Público                   | Devuelve el token JWT → 200 / 401 |
| GET    | `/v1/auth/me`        | Autenticado               | Usuario actual |
| GET    | `/v1/users`          | Autenticado               | Lista paginada. Filtros `name`, `lastName`, `email`; `page`, `size`, `sort` |
| GET    | `/v1/users/{id}`     | Autenticado               | Obtiene un usuario → 200 / 404 |
| POST   | `/v1/users`          | ADMIN                     | Crea un usuario (puede indicar `role`) → 201 + `Location` |
| PATCH  | `/v1/users/{id}`     | ADMIN o el propio usuario | Actualiza solo los campos enviados → 200 / 404 / 409 |
| DELETE | `/v1/users/{id}`     | ADMIN                     | Borrado lógico → 204 / 404 |

Sin token → 401; sin permisos → 403. Los errores se devuelven como `application/problem+json` (RFC 9457).

## Migraciones (Flyway)

Los cambios de esquema se hacen **solo** con nuevos scripts en `src/main/resources/db/migration`,
con el nombre `V<n>__descripcion.sql` (por ejemplo `V3__add_address_to_users.sql`).
Nunca se modifica una migración ya aplicada. Hibernate está en modo `validate`: si una entidad
no coincide con el esquema, la aplicación no arranca.
