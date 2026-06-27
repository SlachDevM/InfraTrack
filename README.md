# InfraTrack

Operational asset and field operations management platform for Australian Local Governments.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![React](https://img.shields.io/badge/React-Frontend-61DAFB?logo=react)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-Backend-6DB33F?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql)

---

## Overview

InfraTrack is an enterprise software product for managing public infrastructure operations. The codebase is built on a proven platform foundation providing authentication, user management, notifications, email, and deployment infrastructure.

Business domain features (assets, inspections, work orders, and operational workflows) are implemented incrementally according to the documented use cases.

The backend is designed to support multiple client applications:

- React web application for office-based users
- Future native Android application for field operations
- All clients share the same REST API

---

## Documentation

InfraTrack documentation is organised by project phase in the [`docs/`](docs/) directory.

See [docs/README.md](docs/README.md) for the full structure and reading order.

| Phase | Folder | Description |
|-------|--------|-------------|
| Business Discovery | [`docs/00-business-discovery/`](docs/00-business-discovery/) | Domain model, actors, workflows and business rules |
| Functional Analysis | [`docs/01-functional-analysis/`](docs/01-functional-analysis/) | Use cases |
| System Blueprint | [`docs/02-system-blueprint/`](docs/02-system-blueprint/) | Engineering standards and development workflow |
| Architecture | [`docs/03-architecture/`](docs/03-architecture/) | Detailed architecture (in progress) |
| API | [`docs/04-api/`](docs/04-api/) | API documentation (in progress) |
| Deployment | [`docs/05-deployment/`](docs/05-deployment/) | Deployment and operations (in progress) |

Key entry points:

- [Development Philosophy](docs/00-business-discovery/00-development-philosophy.md)
- [Domain Overview](docs/00-business-discovery/02-domain-overview.md)
- [Functional Use Cases](docs/01-functional-analysis/functional-use-cases.md)
- [System Blueprint](docs/02-system-blueprint/INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md)

---

## What the Platform Provides

The current codebase provides platform infrastructure:

**Backend:**
- Spring Boot configuration (`com.infratrack`)
- JWT authentication
- Spring Security with role-based access control
- User management and lifecycle (invitation, activation, deactivation)
- Email infrastructure (development and production modes)
- Notification infrastructure with Firebase integration
- PostgreSQL configuration
- Docker development and production setup
- Environment variable strategy
- Error handling and logging
- OpenAPI/Swagger documentation

**Frontend:**
- Authentication and protected routing
- User management interface
- Notification center
- Platform shell
- HTTP API client with JWT token management
- Environment-based configuration

**Infrastructure:**
- Development environment (Docker Compose with Mailpit)
- Production environment (Docker Compose with security best practices)
- PostgreSQL database (`infratrack`)
- Environment variable strategy
- Production-grade configuration

---

## What Is Not Yet Implemented

Business domain features are defined in documentation and implemented incrementally:

- Asset registration and lifecycle
- Inspections, work orders, and operational workflows
- Department and reference data management
- Field operations (Android)

See [Functional Use Cases](docs/01-functional-analysis/functional-use-cases.md) for the implementation roadmap.

---

## Quick Start

```bash
git clone <repository-url>
cd InfraTrack
docker compose up --build
```

After starting:

- Frontend: `http://localhost:3000`
- Swagger UI: `http://localhost:4000/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:4000/v3/api-docs`
- Health: `http://localhost:4000/actuator/health`
- Build info: `http://localhost:4000/actuator/info`
- Mailpit (dev email): `http://localhost:8025`

---

## API Developer Guide

InfraTrack exposes a single REST API consumed by the React web app, future Android clients, and third-party integrations.

**Interactive documentation:** Use Swagger UI at `http://localhost:4000/swagger-ui/index.html`. Every public endpoint is annotated with operation summaries, request/response schemas, and common error codes (400, 401, 403, 404, 409). Authenticated endpoints show the **Authorize** button — paste the JWT from login as `Bearer <token>`.

**Authentication:** `POST /api/auth/login` returns a JWT. Send it on subsequent requests:

```
Authorization: Bearer <token>
```

See [Authentication Flow](#authentication-flow) below for activation and lifecycle details.

**Pagination:** Paginated endpoints accept optional `page` (zero-based, default `0`) and `size` (default `20`, maximum `100`) query parameters. Responses use Spring Data `Page` JSON (`content`, `totalElements`, `totalPages`, etc.). Non-paginated list endpoints return a plain JSON array.

**Versioning:** V1 REST paths are stable under `/api/...`. The API description version in OpenAPI is `1.0.1` (release label). Breaking changes require a new major version; additive DTO fields may be introduced without a path change.

**Errors:** Business and validation failures return plain-text bodies with appropriate HTTP status codes. Swagger documents the common responses on each controller via `@StandardApiResponses`.

**Actuator:** Only `health` and `info` are exposed in production. See [Operations & Monitoring](#operations--monitoring).

---

## Operations & Monitoring

InfraTrack exposes lightweight Spring Boot Actuator endpoints for production operability.

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Aggregate health (database, Flyway, document storage) |
| `/actuator/info` | Application name, version, build time, git commit |

**Health contributors:**

- PostgreSQL connectivity (`db`)
- Flyway migration status (`flyway`)
- Operational document storage directory writable (`operationalDocumentStorage`)

Only `health` and `info` are exposed. Sensitive actuator endpoints (`env`, `beans`, `mappings`, `heapdump`, `threaddump`) remain disabled.

**Startup diagnostics:** On application ready, the backend logs once: version, Spring profile, JVM timezone, Flyway schema version, upload storage path, database JDBC URL (no credentials), and port.

**Docker:** Backend and frontend containers include `HEALTHCHECK` instructions. Compose waits for PostgreSQL and backend health before starting dependent services.

**Build metadata:** Maven generates `build-info.properties` and `git.properties` (short commit hash when `.git` is available). Docker builds accept `GIT_COMMIT` as a build argument:

```bash
GIT_COMMIT=$(git rev-parse --short HEAD) docker compose up --build -d
```

---

## Architecture

**Backend:**
```
Controller → Service → Repository
```

**Frontend:**
```
UI → API Client → Backend
```

**Database:**
- PostgreSQL (system of record)
- All clients access the same data through the backend

**Notifications:**
- Database persistence first
- Firebase Cloud Messaging for push delivery
- Backend owns notification lifecycle

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Backend | Java 21, Spring Boot 3.2, Spring Security |
| Frontend | React, Vite |
| Database | PostgreSQL 16 |
| Authentication | JWT (JSON Web Tokens) |
| Notifications | Firebase Cloud Messaging |
| Containerization | Docker Compose |
| Testing | JUnit 5, Mockito |

---

## Authentication Flow

1. User logs in with email and password
2. Backend validates credentials and returns JWT token
3. Frontend stores token in localStorage
4. Subsequent requests include JWT in Authorization header
5. Backend validates token and extracts user identity

**Account Activation:**
- Administrator creates user account
- User receives activation email with secure token
- User clicks activation link and sets password
- Account becomes ACTIVE and user can login

---

## User Roles

InfraTrack uses five business roles aligned with the actor model:

- **ADMINISTRATOR** — Configures the platform and manages user accounts
- **MANAGER** — Makes operational decisions and validates completion reviews
- **OPERATIONAL_COORDINATOR** — Coordinates operational work and assignments
- **FIELD_EMPLOYEE** — Performs inspections and executes internal maintenance
- **CONTRACTOR** — Executes assigned external work

Administrative permissions do not automatically grant operational authority.

---

## Configuration

All configuration is externalized via environment variables. See `backend/src/main/resources/application.properties` for available options.

**Development:**
```bash
docker compose up
```

**Production:**
```bash
cp .env.example .env
# Edit .env with production values
docker compose -f docker-compose.prod.yml up -d
```

---

## Design Philosophy

InfraTrack follows these principles:

- **Simplicity before abstraction** — Explicit code over clever patterns
- **Business before technology** — Backend owns business rules
- **One capability at a time** — Vertical slice development
- **Production-ready** — Proven architecture and deployment strategy
- **Long-term maintainability** — Code understandable years later

See [docs/00-business-discovery/00-development-philosophy.md](docs/00-business-discovery/00-development-philosophy.md) for the complete philosophy.

---

## Testing

Backend services include unit tests:

```bash
cd backend
mvn clean test
```

Frontend unit tests use Vitest and React Testing Library:

```bash
cd frontend
npm ci --legacy-peer-deps
npm test
```

Optional Playwright smoke test (requires browser install):

```bash
cd frontend
npx playwright install --with-deps
npm run test:e2e
```

---

## Production Deployment

Production deployment uses `docker-compose.prod.yml` and `.env.example` as the configuration template. See [docs/README.md](docs/README.md) and [docs/05-deployment/](docs/05-deployment/) for deployment documentation as it is added.

---

## License

InfraTrack is developed as an open-source enterprise software product for Australian Local Government operational asset management.
