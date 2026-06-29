# InfraTrack

Operational asset and field operations management platform for Australian Local Governments.

**Version 1.0.1**

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![React](https://img.shields.io/badge/React-Frontend-61DAFB?logo=react)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-Backend-6DB33F?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql)

---

## Overview

InfraTrack is an enterprise software product for managing public infrastructure operations for Australian Local Governments. Version 1.0.1 delivers the complete V1 operational workflow from asset registration through inspections, issues, operational decisions, work orders, maintenance, completion reviews, operational documents, notifications and delegated authority.

**V2 Phase A+B** adds the **Domain Engine**: structured inspection intelligence (Decision Engine) and preventive maintenance orchestration (Preventive Maintenance Engine), both following a **human-in-the-loop** principle — the system proposes; the Manager decides.

The backend is the single source of truth for all business rules. Clients consume one REST API:

- **React web application** — office-based operational users (included)
- **Native Android application** — field operations (planned post-V1; same REST API)

---

## V1.0.1 Capabilities

| Use Case | Capability |
|----------|------------|
| UC-001 | Asset registration |
| UC-002 | Asset lifecycle status |
| UC-003 | Inspection assignment |
| UC-004 | Inspection completion |
| UC-005 | Issue recording |
| UC-006 | Business trigger recording |
| UC-007 | Operational decisions and work order creation |
| UC-008 | Work order assignment; delegated authority |
| UC-009 | Maintenance activity completion |
| UC-010 | Completion review |
| UC-011 | Asset operational history |
| UC-012 | Operational document upload and download |
| UC-013 | In-app notifications |

**Platform:** JWT authentication, user lifecycle (invitation, activation, deactivation), role-based access control, email (dev Mailpit / prod SMTP), Firebase push notifications, PostgreSQL, Flyway migrations, OpenAPI/Swagger, Spring Actuator health and info.

**Reference data:** Departments, asset categories, user directories by role.

---

## V2 Domain Engine (Phase A+B)

| Engine | Capabilities |
|--------|----------------|
| **Decision Engine** (Phase A) | Inspection Templates, Questions, Answers, Decision Rules, Rule Evaluation Reports, Suggested Actions, Decision Assistant |
| **Preventive Maintenance Engine** (Phase B) | Plans, Trigger Evaluation, Execution Candidates, Preventive Decision Assistant, Execution Reports, Controlled Scheduler |

**Human-in-the-loop:** Rules produce suggestions, not automatic Issues. The scheduler generates candidates, not automatic Inspections.

| Document | Description |
|----------|-------------|
| [Domain Engine](docs/07-business-architecture/domain-engine.md) | Authoritative V2 business architecture |
| [V2 Phase A+B Release Notes](docs/06-release-notes/v2-phase-a-b.md) | Milestone summary and validation checklist |
| [V2 API endpoint groups](docs/04-api/v2-domain-engine-api.md) | Major V2 REST paths |
| [BDR-001 Human-in-the-loop](docs/03-architecture/bdr-001-human-in-the-loop-decision-engine.md) | Why rules suggest, not execute |
| [BDR-002 Preventive candidates](docs/03-architecture/bdr-002-preventive-candidates-before-automation.md) | Why scheduler generates candidates only |
| [ADR-003 V2 workflow](docs/03-architecture/adr-003-v2-domain-driven-workflow.md) | How V2 business domains interact |
| [Business Glossary](docs/01-business-architecture/glossary.md) | Stakeholder terminology |
| [V2 Roadmap](docs/06-release-notes/v2-roadmap.md) | Phase evolution |

---

## Roadmap (Post V2 Phase A+B)

- Native Android field application (same REST API)
- METER and EVENT preventive trigger evaluation
- Optional automation of accepted suggestions (A3.6)
- Preventive and decision-engine KPI dashboards
- Expanded pagination on remaining list endpoints

See [Functional Use Cases](docs/01-business-architecture/functional-use-cases.md) for the authoritative V1 business scope, [Domain Engine](docs/07-business-architecture/domain-engine.md) for V2, and [V2 Roadmap](docs/06-release-notes/v2-roadmap.md) for planned phases.

---

## Documentation

InfraTrack documentation is organised by project phase in the [`docs/`](docs/) directory.

See [docs/README.md](docs/README.md) for the full structure and reading order.

| Phase | Folder | Description |
|-------|--------|-------------|
| Business Discovery | [`docs/00-business-discovery/`](docs/00-business-discovery/) | Domain model, actors, workflows and business rules |
| Business Architecture | [`docs/01-business-architecture/`](docs/01-business-architecture/) | Business architecture, use cases, and [glossary](docs/01-business-architecture/glossary.md) |
| System Blueprint | [`docs/02-system-blueprint/`](docs/02-system-blueprint/) | Engineering standards and development workflow |
| Architecture | [`docs/03-architecture/`](docs/03-architecture/) | ADRs, BDRs, and architecture decisions |
| API | [`docs/04-api/`](docs/04-api/) | OpenAPI reference and V2 endpoint groups |
| Deployment | [`docs/05-deployment/`](docs/05-deployment/) | Deployment, secrets, and security hardening |
| Release Notes | [`docs/06-release-notes/`](docs/06-release-notes/) | Sprint and milestone release notes |
| V2 Business Architecture | [`docs/07-business-architecture/`](docs/07-business-architecture/) | Domain Engine, Decision Engine, Preventive Maintenance Engine |

Key entry points:

- [Development Philosophy](docs/00-business-discovery/00-development-philosophy.md)
- [Domain Overview](docs/00-business-discovery/02-domain-overview.md)
- [Functional Use Cases](docs/01-business-architecture/functional-use-cases.md)
- [System Blueprint](docs/02-system-blueprint/INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md)
- [Domain Engine (V2)](docs/07-business-architecture/domain-engine.md)
- [Business Glossary](docs/01-business-architecture/glossary.md)
- [V2 Phase A+B Release Notes](docs/06-release-notes/v2-phase-a-b.md)
- [V2 Roadmap](docs/06-release-notes/v2-roadmap.md)

---

## Quick Start

### Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Docker & Docker Compose | Recent | Recommended for full stack |
| Java | 21 | Backend development and tests |
| Maven | 3.9+ | Backend build |
| Node.js | 22 LTS | Frontend development and tests |
| npm | 10+ | Frontend package manager |
| Git | 2.x | Clone and version control |

Docker Desktop (or Docker Engine + Compose plugin) is the fastest way to run InfraTrack locally. For backend-only or frontend-only work, install Java/Maven or Node separately.

### Docker Compose (recommended)

```bash
git clone <repository-url>
cd InfraTrack
docker compose up --build -d
```

After starting:

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:4000 |
| Swagger UI (dev only) | http://localhost:4000/swagger-ui/index.html |
| OpenAPI JSON (dev only) | http://localhost:4000/v3/api-docs |
| Actuator health | http://localhost:4000/actuator/health |
| Actuator info | http://localhost:4000/actuator/info |
| Mailpit (dev email UI) | http://localhost:8025 |

Default bootstrap administrator (development only): `admin@infratrack.local` / `change-me`

### Backend commands

```bash
cd backend
mvn spring-boot:run                    # Run locally (requires PostgreSQL)
mvn clean test                         # Unit and integration tests
mvn clean package -DskipTests          # Production JAR
mvn org.owasp:dependency-check-maven:check   # Dependency vulnerability scan
```

Integration tests use Testcontainers and require Docker.

### Frontend commands

```bash
cd frontend
npm ci --legacy-peer-deps              # Install dependencies
npm run dev                            # Vite dev server
npm test -- --run                      # Vitest unit tests
npm run build                          # Production build
npm run test:e2e                       # Playwright E2E (requires browser install)
```

### Environment variables

Configuration is externalized via environment variables. Templates:

- [`.env.example`](.env.example) — production deployment
- [`backend/src/main/resources/application.properties`](backend/src/main/resources/application.properties) — defaults and property keys

Key variables:

| Variable | Required (prod) | Purpose |
|----------|-----------------|---------|
| `JWT_SECRET` | Yes | JWT signing key |
| `SPRING_DATASOURCE_*` | Yes | PostgreSQL connection |
| `FRONTEND_ORIGIN` | Yes | CORS allowed origin |
| `SPRING_MAIL_*` | Yes | SMTP for activation emails |
| `ACTIVATION_LINK_BASE_URL` | Yes | Account activation links |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | No | FCM push (optional) |
| `SPRING_PROFILES_ACTIVE` | Yes | `dev` locally, `prod` in production |
| `PREVENTIVE_SCHEDULER_ENABLED` | No | Default `false`; scheduled candidate generation |
| `PREVENTIVE_SCHEDULER_CRON` | No | Cron for preventive scheduler (default `0 0 6 * * *`) |

See [Deployment documentation](docs/05-deployment/README.md) for the full list.

### Secrets policy

- **Never commit** real credentials, `.env` files, or Firebase service account JSON.
- Use [`backend/firebase-service-account.example.json`](backend/firebase-service-account.example.json) as a structural reference only.
- Store production secrets outside Git (environment variables, secret manager, or host-mounted files).
- Optional local FCM: copy [`docker-compose.firebase.example.yml`](docker-compose.firebase.example.yml) to `docker-compose.override.yml`.

Full guide: [docs/05-deployment/secrets.md](docs/05-deployment/secrets.md)

### Release validation

Before tagging or deploying a release:

1. `cd backend && mvn clean test && mvn clean package -DskipTests`
2. `cd frontend && npm ci --legacy-peer-deps && npm test -- --run && npm run build`
3. `docker compose up --build -d` — verify health at `/actuator/health`
4. Confirm Swagger works in dev and is disabled with `prod` profile
5. Confirm login rate limit returns HTTP 429 with `Retry-After`

Full checklist: [docs/05-deployment/production-checklist.md](docs/05-deployment/production-checklist.md)

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

**Versioning:** V1 REST paths are stable under `/api/...`. The API description version in OpenAPI is `1.0.1`. Breaking changes require a new major version; additive DTO fields may be introduced without a path change.

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
- Flyway versioned migrations
- All clients access the same data through the backend

**Notifications:**

- Database persistence first
- Firebase Cloud Messaging for push delivery
- Backend owns notification lifecycle

**Security:**

- JWT authentication on all `/api/**` endpoints except auth and actuator
- Role-based authorization enforced in backend services
- CORS configured globally in `SecurityConfig` (not per-controller)
- Login rate limiting on `POST /api/auth/login` (10 attempts per minute per IP and email)
- Swagger UI and OpenAPI docs disabled in production (`prod` profile)
- Firebase credentials optional at runtime (see [Secrets management](docs/05-deployment/secrets.md))

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Backend | Java 21, Spring Boot 3.2, Spring Security |
| Frontend | React 19, Vite |
| Database | PostgreSQL 16 |
| Authentication | JWT (JSON Web Tokens) |
| Notifications | Firebase Cloud Messaging |
| Containerization | Docker Compose |
| Testing | JUnit 5, Mockito, Vitest, Playwright |

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

All configuration is externalized via environment variables. See `backend/src/main/resources/application.properties` and `.env.example` for available options.

**Firebase credentials** are not committed to Git. FCM is optional — when `FIREBASE_SERVICE_ACCOUNT_PATH` is unset, push notifications are skipped and all other features work normally. See [Secrets management](docs/05-deployment/secrets.md) for local and production setup.

**Production security:** Swagger is disabled in the `prod` profile. Login attempts are rate limited. See [Security hardening](docs/05-deployment/security.md).

**Development:**

```bash
docker compose up --build
```

Optional FCM for local testing: copy `docker-compose.firebase.example.yml` to `docker-compose.override.yml` and place credentials in `backend/firebase-service-account.local.json` (gitignored).

**Production:**

```bash
cp .env.example .env
# Edit .env with production values
docker compose -f docker-compose.prod.yml up -d
```

---

## Testing

**Backend** (JUnit 5, Mockito, Testcontainers integration smoke):

```bash
cd backend
mvn clean test
mvn clean package -DskipTests
```

**Backend code coverage** (JaCoCo, informational — no build failure on thresholds):

After `mvn clean test`, reports are generated at:

- HTML: `backend/target/site/jacoco/index.html`
- XML: `backend/target/site/jacoco/jacoco.xml`

Open the HTML report in a browser to inspect package and class coverage locally.

On every push to `main`, GitHub Actions publishes:

- **jacoco-html-report** — downloadable HTML report (Actions → workflow run → Artifacts)
- **jacoco-xml-report** — machine-readable XML for tooling
- **Job summary** — instruction, branch, and line coverage percentages

Coverage is measured but not enforced in Sprint 0. A future quality gate of **80% minimum instruction coverage** is planned but not enabled.

**Frontend** (Vitest, React Testing Library):

```bash
cd frontend
npm ci --legacy-peer-deps
npm test
npm run build
```

**E2E smoke** (Playwright — requires browser install):

```bash
cd frontend
npx playwright install --with-deps
npm run test:e2e
```

Integration tests require Docker for Testcontainers; they are skipped when Docker is unavailable.

---

## Production Deployment

Production deployment uses `docker-compose.prod.yml` and `.env.example` as the configuration template. The frontend is served by nginx; the backend runs as a non-root container with health checks on both services.

See [docs/README.md](docs/README.md) and [docs/05-deployment/README.md](docs/05-deployment/README.md) for deployment guides, backup/restore, troubleshooting, and secrets documentation.

---

## Version History

| Version | Summary |
|---------|---------|
| **V2 Phase A+B** | Domain Engine — Decision Engine, Preventive Maintenance Engine, Controlled Scheduler (human-in-the-loop) |
| **1.0.1** | V1 release freeze — complete operational workflow (UC-001–UC-013), OpenAPI documentation, observability, security hardening, frontend test suite |
| 1.0.0 | Initial React frontend release label (superseded by unified 1.0.1 versioning) |

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

## License

InfraTrack is developed as an open-source enterprise software product for Australian Local Government operational asset management.
