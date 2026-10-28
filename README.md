# PayFlow API

[![CI](https://github.com/Mike600Isaac/payflow-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Mike600Isaac/payflow-api/actions/workflows/ci.yml)

A secure **payments REST API** built with **Java 17 and Spring Boot 3** — merchant
accounts, JWT authentication with role-based access control, payment creation and
lookup, and a signed gateway **webhook** that moves a payment to its final status.

Built as a focused, production-shaped backend showcasing the Spring stack. The
backend concepts here (REST design, relational modelling, auth, secure payment
handling, testing, CI/CD, containerisation) are the same ones I apply in Python
(Flask, Django) and PHP (Laravel) — this project demonstrates them in Spring Boot.

## Tech stack

- **Java 17**, **Spring Boot 3.3**
- **Spring Web** (REST), **Spring Data JPA / Hibernate**
- **Spring Security 6** + **JWT** (jjwt), **BCrypt** password hashing
- **PostgreSQL** (production) / **H2** (local & tests)
- **Bean Validation**, centralised error handling (`@RestControllerAdvice`)
- **JUnit 5**, **Mockito**, **MockMvc** integration tests
- **Docker** + **docker-compose**, **GitHub Actions** CI

## Architecture

```
controller  ->  service  ->  repository (Spring Data JPA)  ->  PostgreSQL / H2
     |              |
   DTOs      domain entities (Merchant, Payment)
     |
 Spring Security (JWT filter -> SecurityContext) + BCrypt
```

- `auth/` — register / login, issues JWTs
- `payment/` — create, list, fetch payments; gateway webhook callback
- `security/` — `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`, `AppUserDetailsService`
- `domain/` + `repository/` — JPA entities and Spring Data repositories
- `common/` — typed exceptions + global error handler

## Running locally (H2, no database needed)

Requires JDK 17 and Maven 3.9+.

```bash
mvn spring-boot:run
# API on http://localhost:8080  (H2 console at /h2-console)
```

## Running with Docker + PostgreSQL

```bash
docker compose up --build
# API on http://localhost:8080, PostgreSQL on 5432
```

## API

| Method | Endpoint                 | Auth            | Description                              |
|--------|--------------------------|-----------------|------------------------------------------|
| POST   | `/api/auth/register`     | public          | Create a merchant, returns a JWT         |
| POST   | `/api/auth/login`        | public          | Authenticate, returns a JWT              |
| POST   | `/api/payments`          | Bearer JWT      | Create a payment (status `PENDING`)      |
| GET    | `/api/payments`          | Bearer JWT      | List the merchant's payments             |
| GET    | `/api/payments/{ref}`    | Bearer JWT      | Fetch one of the merchant's payments     |
| POST   | `/api/payments/callback` | `X-Webhook-Secret` | Gateway webhook — set final status    |
| GET    | `/actuator/health`       | public          | Health check                             |

### Example

```bash
# 1) Register (returns a token)
curl -s -X POST localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Acme","email":"acme@shop.com","password":"password123"}'

TOKEN=... # copy the "token" from the response

# 2) Create a payment
curl -s -X POST localhost:8080/api/payments \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"amount":250.00,"currency":"USD","customerEmail":"buyer@mail.com","description":"Order #42"}'

# 3) Gateway webhook marks it SUCCESS (reference from step 2)
curl -s -X POST localhost:8080/api/payments/callback \
  -H 'X-Webhook-Secret: dev-webhook-secret' -H 'Content-Type: application/json' \
  -d '{"reference":"PAY-XXXXXXXXXXXX","status":"SUCCESS"}'
```

## Security notes

- Passwords hashed with **BCrypt**; never stored or logged in plain text.
- Stateless **JWT** auth (HS256); protected routes require `Authorization: Bearer <token>`.
- Merchants can only read **their own** payments.
- The webhook secret is compared in **constant time** (`MessageDigest.isEqual`).
  In production this would be upgraded to an **HMAC signature over the raw body**.
- **No card/PAN data is stored** — only payment metadata, keeping the PCI scope minimal.
- Secrets (`JWT_SECRET`, `WEBHOOK_SECRET`, DB credentials) come from environment variables.

## Coming from Flask / Django / Laravel?

| Concept           | This project (Spring Boot)        |
|-------------------|-----------------------------------|
| Routes/controllers| `@RestController` + `@PostMapping`|
| ORM (SQLAlchemy / Django ORM / Eloquent) | Spring Data JPA repositories |
| Serializers / API Resources | DTO `record`s + Jackson |
| Auth + JWT        | Spring Security + jjwt filter     |
| `.env` / settings | `application.yml` + Spring profiles |
| pytest / PHPUnit  | JUnit 5 + Mockito + MockMvc       |

## Status

Demo / learning project — intentionally focused. Natural next steps: idempotency keys,
HMAC-signed webhooks, pagination, refunds, and Flyway migrations.
