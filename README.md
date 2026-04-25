# Family League

A Spring Boot backend for a family-and-friends sports prediction platform. Users predict league standings and match outcomes; points are calculated server-side.

## Tech Stack

- Java 21 / Spring Boot 4.x
- PostgreSQL
- Flyway migrations
- JWT authentication (stateless)
- Swagger UI at `/swagger-ui.html`

## Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- A running PostgreSQL instance

## Setup

### 1. Create a `.env` file

Copy the template below into a `.env` file at the project root (it is git-ignored):

```env
DB_URL=jdbc:postgresql://localhost:5432/family_league
DB_USER=your_db_user
DB_PASS=your_db_password

JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRY_MS=3600000

MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
ADMIN_EMAIL=admin@family-league.local
```

### 2. Build

```bash
./mvnw clean package -DskipTests
```

## Running

Source the `.env` file and start the app with the `local` profile:

```bash
set -a && source .env && set +a && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Or run the packaged jar:

```bash
set -a && source .env && set +a
java -Dspring.profiles.active=local -jar target/family-league-0.0.1-SNAPSHOT.jar
```

The app starts on `http://localhost:8080`.

## API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`

OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

```bash
# All tests (integration tests spin up a PostgreSQL Testcontainer)
./mvnw test
```

## Key Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/register` | Public | Register a new user |
| POST | `/api/v1/auth/login` | Public | Login, receive JWT |
| GET | `/api/v1/seasons/{id}/leaderboard` | User | Season leaderboard |
| POST | `/api/v1/matches/{id}/predictions` | User | Submit match prediction |
| POST | `/api/v1/admin/matches/{id}/result` | Admin | Publish match result |
| POST | `/api/v1/admin/notifications/bulk` | Admin | Send bulk email |

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | — | JDBC URL for PostgreSQL |
| `DB_USER` | — | Database username |
| `DB_PASS` | — | Database password |
| `JWT_SECRET` | — | HS256 signing secret (min 256 bits) |
| `JWT_EXPIRY_MS` | `3600000` | Token expiry in milliseconds |
| `MAIL_HOST` | `localhost` | SMTP host |
| `MAIL_PORT` | `1025` | SMTP port |
| `MAIL_USERNAME` | _(empty)_ | SMTP username |
| `MAIL_PASSWORD` | _(empty)_ | SMTP password |
| `ADMIN_EMAIL` | `admin@family-league.local` | Admin notification recipient |
