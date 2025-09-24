# Kanban API – Spring Boot back-end

Aplikacija Kanban servis s REST i GraphQL API-jem, WebSocket notifikacijama i JWT autentikacijom, dockerizirano okruženje (app + DB).

---

## Tehnologije

- **Java 17**
- **Spring Boot** (REST, GraphQL, WebSocket, Security)
- **Hibernate / JPA**
- **PostgreSQL** (Docker)
- **Flyway** (migracije baze)
- **Maven** (build tool)
- **JUnit 5 / Mockito** (unit i integracijski testovi)
- **Docker & Docker Compose** (aplikacija + baza)
- **JWT** (autentikacija)

---

## Upute za pokretanje

### Preduvjeti

- Docker i Docker Compose

### Pokretanje

1. **Build Docker image:**

```bash
docker build -t kanban-api .
```

2. **Docker Compose:**

```bash
docker-compose up -d
```

### Pristup API-ju

- REST: http://localhost:8080/api/tasks (zaštićeno)
- GraphQL: http://localhost:8080/graphql
- WebSocket: ws://localhost:8080/ws

### Autentikacija

- http://localhost:8080/api/auth/login i user "user1", pw "user1"
- http://localhost:8080/api/auth/register i novi podaci

### Testovi

Testove možete pokrenuti iz IntelliJ IDEA:
- Desni klik na test → Run