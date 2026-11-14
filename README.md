# CRUD de usuarios — Spring Boot

API REST de ejemplo con un CRUD de usuarios, borrado lógico, paginación y filtros.

## Stack

- Java 21
- Spring Boot 4.1 (Web MVC, Data JPA, Validation)
- H2 en memoria
- MapStruct + Lombok
- springdoc-openapi (Swagger UI)

## Ejecutar

```bash
./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Consola H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:clever`, usuario `sa`, sin contraseña)

## Pruebas

```bash
./mvnw test
```

## Endpoints

| Método | Ruta              | Descripción                                   | Respuesta |
|--------|-------------------|-----------------------------------------------|-----------|
| POST   | `/v1/users`       | Crea un usuario                               | 201 + `Location` |
| GET    | `/v1/users/{id}`  | Obtiene un usuario                            | 200 / 404 |
| PATCH  | `/v1/users/{id}`  | Actualiza solo los campos enviados            | 200 / 404 / 409 |
| DELETE | `/v1/users/{id}`  | Borrado lógico                                | 204 / 404 |
| GET    | `/v1/users`       | Lista paginada. Filtros: `name`, `lastName`, `email` (contiene, sin distinguir mayúsculas). Paginación: `page`, `size`, `sort` | 200 |

Ejemplo:

```bash
curl -X POST localhost:8080/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ana","lastName":"Pérez","email":"ana@example.com","cellphone":"+57 300 123 4567","birthdate":"1990-05-20"}'

curl 'localhost:8080/v1/users?email=example.com&page=0&size=10&sort=lastName,asc'
```

Los errores se devuelven como `application/problem+json` (RFC 9457); los de validación incluyen
un objeto `errors` con el mensaje de cada campo.
