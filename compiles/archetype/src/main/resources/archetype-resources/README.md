

# Arquetipo Spring Boot

Arquetipo base para la creación de proyectos **API REST** monolíticos con **Spring Boot**, orientado a proyectos que puedan evolucionar hasta ambientes productivos.

Este proyecto sirve como base para construir nuevos desarrollos con una estructura estandarizada, configuración inicial, autenticación JWT, manejo centralizado de errores, auditoría, migraciones con Flyway y soporte para **PostgreSQL** y **MySQL**.

---

## Objetivo

Estandarizar la creación de proyectos backend con una base técnica reutilizable que permita:

- acelerar el inicio de nuevos desarrollos
- mantener una arquitectura homogénea
- reducir código repetido
- contar con una base segura y extensible
- facilitar la evolución del proyecto hacia producción

---

## Tecnologías principales

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- Flyway
- PostgreSQL
- MySQL
- Springdoc OpenAPI / Swagger
- Spring Boot Actuator
- Caffeine
- Bucket4j
- Lombok
- Maven

---

## Estructura estándar del proyecto

```text
src/main/java/com/tuempresa/proyecto
├── BaseProjectApplication.java
├── config
├── domain
│   ├── entity
│   └── repository
├── exception
├── services
├── validation
└── web
    ├── controller
    └── model
```

### Responsabilidad por capa

#### `config`
Contiene configuraciones generales del proyecto, por ejemplo:
- seguridad
- OpenAPI / Swagger
- propiedades globales
- configuración de rutas protegidas

#### `domain/entity`
Contiene las entidades JPA del proyecto.

#### `domain/repository`
Contiene los repositorios base y concretos para acceso a datos.

#### `exception`
Contendrá el manejo centralizado de errores:
- `MainExceptionHandler`
- estructura de respuesta de error
- excepciones personalizadas

#### `services`
Contiene la lógica de negocio del sistema.

#### `web/controller`
Contiene los endpoints expuestos por la API.

#### `web/model`
Contiene los modelos de entrada y salida de la API.

---

## Flujo arquitectónico

```text
controller → service → repository → domain
```

---

## Características base incluidas

- autenticación JWT
- login base configurable
- soporte para login por `email` o `username`
- rate limit para login
- protección básica contra fuerza bruta
- auditoría con `created_at`, `updated_at` y `deleted_at`
- soft delete configurable
- manejo centralizado de errores
- documentación Swagger/OpenAPI
- health check con Actuator
- migraciones con Flyway
- soporte para PostgreSQL y MySQL

---

## Configuración por perfiles

El proyecto usa tres archivos principales de configuración:

- `application.yml`
- `application-dev.yml`
- `application-prod.yml`

### `application.yml`
Configuración común para todos los entornos.

### `application-dev.yml`
Configuración de desarrollo o QA.

### `application-prod.yml`
Configuración de producción.

---

## Propiedades importantes

### Seguridad

```yaml
app:
  security:
    login-enabled: true
    login-identifier: email
```

#### `login-enabled`
- `true`: habilita el login base del arquetipo
- `false`: desactiva el login base y deja las rutas abiertas

#### `login-identifier`
- `email`: el login se resolverá por correo electrónico
- `username`: el login intentará usar username

Si se configura `username`, la resolución del usuario puede hacer fallback a `email` según la lógica implementada.

### Base de datos

```yaml
app:
  database:
    soft-delete: true
```

#### `soft-delete`
- `true`: habilita el borrado lógico
- `false`: permite eliminación física si la implementación lo contempla

---

## Seguridad de rutas

La seguridad de rutas **no se define en el archivo yml**, sino en una clase Java específica para mantener control explícito desde código.

### Clase de configuración

- `RouteAuthorizationInitializer`

### Ejemplo

```java
return new RouteAuthorizationConfig()
        .withToken("/users/**")
        .withToken("/orders/**")
        .withoutToken("/prices/**")
        .withoutToken("/public/**");
```

### Regla de funcionamiento

- Las rutas definidas con `withToken(...)` requieren JWT
- Las rutas definidas con `withoutToken(...)` no requieren JWT
- Si `login-enabled=false`, todas las rutas se permiten
- Si no se define ninguna ruta, por defecto todas quedan públicas

---

## Auditoría

Las entidades auditables extienden de `AuditableEntity`.

Campos incluidos:
- `created_at`
- `updated_at`
- `deleted_at`

### Regla importante
La tabla `users` se considera siempre auditable dentro del arquetipo.

---

## Migraciones Flyway

Las migraciones se separan por motor de base de datos:

```text
src/main/resources/db/migration/postgresql
src/main/resources/db/migration/mysql
```

### Recomendación
- usar scripts específicos por motor
- no mezclar scripts de PostgreSQL y MySQL en la misma carpeta

### Ejemplos de nombres válidos
- `V1__create_users_table.sql`
- `V2__alter_users_add_name.sql`

---

## Manejo de errores

El proyecto incluye una estructura estándar de error similar a esta:

```json
{
  "timestamp": "2026-03-13T20:00:00Z",
  "message": "Validation incorrect",
  "errors": {
    "email": "must be a well-formed email address",
    "name": "must not be blank"
  }
}
```

### Status code manejados explícitamente

#### 4xx
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 405 Method Not Allowed
- 409 Conflict
- 415 Unsupported Media Type
- 429 Too Many Requests

#### 5xx
- 500 Internal Server Error
- 503 Service Unavailable
- 504 Gateway Timeout

---

## Validaciones

Las validaciones se definen en los `model` usando anotaciones como:

- `@NotBlank`
- `@NotNull`
- `@Email`
- `@Size`

Y se aplican en controllers con `@Valid`.

---

## Conversión entre modelos y entidades

Se utiliza un `GenericConverter` para mapear propiedades compatibles entre modelos y entidades por nombre usando getters y setters.

Este enfoque funciona bien cuando:
- los nombres de propiedades coinciden
- los tipos son compatibles
- no se requiere lógica compleja de transformación

Si una conversión requiere reglas especiales, se recomienda crear un converter concreto.

---

## Arranque del proyecto

### Compilar

```bash
mvn clean compile
```

### Ejecutar

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Endpoints base esperados

### Registro

```http
POST /auth/register
```

### Login

```http
POST /auth/login
```

### Health check

```http
GET /actuator/health
```

### Swagger

```http
GET /swagger-ui.html
```

---

## Consideraciones de seguridad

- el login base puede activarse o desactivarse
- el uso de `login-enabled=false` no se recomienda para producción
- las contraseñas deben almacenarse cifradas con `PasswordEncoder`
- el rate limit del login ayuda a mitigar fuerza bruta
- la defensa contra SQL Injection debe basarse en JPA/Hibernate y consultas parametrizadas
- las entradas deben validarse desde `model` y controlarse desde la capa web y service

---

## Autor

Base inicial del arquetipo para estandarizar futuros proyectos Spring Boot API REST.