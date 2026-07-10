# InfraTrack

Operational asset and field operations management platform for Australian Local Governments.

## Current Platform Baseline

| Item | Value |
|------|-------|
| **Backend platform** | V2.6.x |
| **React web** | V2.6.x |
| **Android application** | **v1.3.0** |
| **Maven/npm artifact** | `2.0.1` per [ADR-004](docs/03-architecture/adr-004-platform-versioning-strategy.md) |
| **Documentation status** | Living Documentation |
| **Platform status** | Internally validated |
| **Active development** | **M6.6** — see [Product Roadmap](docs/06-release-notes/v2-roadmap.md) |

**Versioning:** **Platform versions** (V2.x) describe delivered platform capabilities. The **Android application** uses an independent release cycle (currently **v1.3.0**). These numbering schemes are not interchangeable.

Delivered platform capability history: [Platform Version History](docs/06-release-notes/platform-version-history.md). This documentation describes **platform capability V2.6.x** (Work Order Offline backend validated through M6.5).

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![React](https://img.shields.io/badge/React-Frontend-61DAFB?logo=react)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-Backend-6DB33F?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql)

---

## Overview

InfraTrack is an enterprise software product for managing public infrastructure operations for Australian Local Governments.

**[Product Vision](docs/00-product-vision.md)** — why the platform exists, its principles, evolution, and long-term direction.

**[Business Capability Map](docs/01-business-architecture/business-capability-map.md)** — what the platform can do today at a business level.

**[API Consumer Guide](docs/04-api/api-consumer-guide.md)** — how client applications should consume the platform API.

| Version | Capability |
|---------|------------|
| **1.0.0** | Core CMMS — complete V1 operational workflow |
| **1.0.1** | Platform hardening |
| **2.0.0** | Inspection Intelligence & Preventive Maintenance (Domain Engine) |
| **2.0.1** | Security & quality hardening + V2 validation baseline |
| **2.1.0** | Operations Intelligence & dashboard (validated) |
| **2.2.x** | Mobile API & CSV reporting foundations (validated) |
| **2.3.x** | Policy Engine & extended reporting (CSV/XLSX/PDF) (validated) |
| **2.4.x** | Mobile asset context, QR foundation, Spring Boot 4, security hardening (validated — partial Android/offline scope remains) |
| **2.5.x** | Mobile offline sync backend — protocol, inspection delta, conflicts (validated — partial) |
| **2.6.x** | Work Order Offline — mobile sync extensions through M6.5 (in progress — **current baseline**) |

See [Platform Version History](docs/06-release-notes/platform-version-history.md) and [Product Roadmap](docs/06-release-notes/v2-roadmap.md) for delivery status.

**Version 2.0.0** adds the **Domain Engine**: structured inspection intelligence (Decision Engine) and preventive maintenance orchestration (Preventive Maintenance Engine), both following a **human-in-the-loop** principle — the system proposes; the Manager decides.

The backend is the single source of truth for all business rules. Clients consume one REST API:

- **React web application** — office-based operational users (included)
- **Native Android application** — field operations (**v1.3.0**; consumes Mobile API and platform V2.6.x sync backend)

---

## V1 Capabilities

Complete V1 operational workflow (UC-001 through UC-013): assets, inspections, issues, decisions, work orders, maintenance, completion reviews, operational documents, notifications, and reference data.

See [Functional Use Cases](docs/01-business-architecture/functional-use-cases.md) for the authoritative catalogue.

---

## V2 Domain Engine & Validated Foundations

| Area | Summary |
|------|---------|
| **Decision Engine** v1.0 | Templates, rules, suggested actions, Decision Assistant |
| **Preventive Engine** v1.0 | Plans, candidates, scheduler, execution reports |
| **Policy Engine** (BDR-004) | Inspection Visibility, Notification, Dashboard, Reporting, Approval policies |
| **Operations Intelligence** | KPIs, trends, dashboard, personalisation ([2.1.0](docs/06-release-notes/platform-version-history.md#version-210)) |
| **Mobile API** | Compact `/api/mobile/*` foundation + M4 asset context ([2.4.x](docs/06-release-notes/v2.4.md)) |
| **Reporting** | CSV/XLSX/PDF exports with export menu and security hardening ([reporting-api](docs/04-api/reporting-api.md)) |
| **QR Navigation** | Asset business codes, QR generation, mobile lookup ([mobile-api](docs/04-api/mobile-api.md)) |
| **Document Management** | Operational documents on web and mobile asset context |
| **Security Hardening** | CSP, JWT account-status cache, export guards ([security](docs/05-deployment/security.md)) |

| Document | Description |
|----------|-------------|
| [Platform Version History](docs/06-release-notes/platform-version-history.md) | Product versions (authoritative) |
| [Domain Engine](docs/07-business-architecture/domain-engine.md) | V2 business architecture |
| [Business Capability Map](docs/01-business-architecture/business-capability-map.md) | What the platform can do today |
| [Product Roadmap](docs/06-release-notes/v2-roadmap.md) | Planned versions |
| [V2.4 milestone notes](docs/06-release-notes/v2.4.md) | V2.4 DOC-1 consolidation (historical) |
| [ADR Index](docs/03-architecture/ADR-INDEX.md) | Architecture decisions |
| [BDR-004](docs/03-architecture/bdr-004-configurable-organizational-policies.md) | Configurable organizational policies principle |
| [BDR-005](docs/03-architecture/bdr-005-offline-synchronization-architecture.md) | Offline & synchronization architecture (M5) |
| [BDR-006 — Conflict Resolution Strategy](docs/03-architecture/bdr-006-conflict-resolution-strategy.md) | Conflict taxonomy, resolution hints, merge boundaries (companion to offline BDR-005) |

---

## Roadmap

**Active development:** M6.6 (V2.6.0 Work Order Offline family).

Next major milestones: remaining offline/sync scope (M6.6+), and subsequent platform capability releases per [Product Roadmap](docs/06-release-notes/v2-roadmap.md).

See [Product Roadmap](docs/06-release-notes/v2-roadmap.md), [Platform Version History](docs/06-release-notes/platform-version-history.md), and [Functional Use Cases](docs/01-business-architecture/functional-use-cases.md) for scope detail.

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

Key entry points: [Product Vision](docs/00-product-vision.md) · [Capability Map](docs/01-business-architecture/business-capability-map.md) · [API Consumer Guide](docs/04-api/api-consumer-guide.md) · [Platform Version History](docs/06-release-notes/platform-version-history.md) · [ADR Index](docs/03-architecture/ADR-INDEX.md)

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
npm ci                              # Install dependencies
npm start                              # Vite dev server
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
| `JWT_EXPIRATION` | No | Access token lifetime in ms (default `86400000` = 24 hours) |
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
2. `cd frontend && npm ci && npm test -- --run && npm run build`
3. `docker compose up --build -d` — verify health at `/actuator/health`
4. Confirm Swagger works in dev and is disabled with `prod` profile
5. Confirm login rate limit returns HTTP 429 with `Retry-After`

Full checklist: [docs/05-deployment/production-checklist.md](docs/05-deployment/production-checklist.md)

---

## API Developer Guide

InfraTrack exposes a single REST API consumed by the React web app, the Android application (v1.3.0), and third-party integrations.

**Interactive documentation:** Use Swagger UI at `http://localhost:4000/swagger-ui/index.html`. Every public endpoint is annotated with operation summaries, request/response schemas, and common error codes (400, 401, 403, 404, 409). Authenticated endpoints show the **Authorize** button — paste the JWT from login as `Bearer <token>`.

**Authentication:** `POST /api/auth/login` returns a JWT. Send it on subsequent requests:

```
Authorization: Bearer <token>
```

See [Authentication Flow](#authentication-flow) below for activation and lifecycle details.

**Pagination:** Paginated endpoints accept optional `page` (zero-based, default `0`) and `size` (default `20`, maximum `100`) query parameters. Responses use Spring Data `Page` JSON (`content`, `totalElements`, `totalPages`, etc.). Operational list endpoints — including `GET /api/maintenance-activities` — return a `Page` wrapper. A few other list endpoints (for example bounded mobile assignment lists) return a plain JSON array; check OpenAPI per path.

**Versioning:** REST paths are stable under `/api/...`. The OpenAPI `info.version` matches the Maven/npm artifact version (`2.0.1`); product capability is documented separately in [Platform Version History](docs/06-release-notes/platform-version-history.md) (currently **V2.6.x** baseline). Breaking changes require a new major version; additive DTO fields may be introduced without a path change.

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
| Backend | Java 21, Spring Boot 4.0, Spring Security, Tomcat 11 |
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
npm ci
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

**Trusted reverse proxy required:** Do not expose the backend API directly to the Internet. Terminate HTTPS on a trusted reverse proxy (Nginx, Traefik, HAProxy, or a cloud load balancer) and keep backend containers on an internal network only. The proxy must forward `X-Forwarded-For` and `X-Forwarded-Proto` from the real client connection — the application uses `X-Forwarded-For` for login rate limiting and assumes trusted infrastructure. See [security hardening — trusted reverse proxy](docs/05-deployment/security.md#trusted-reverse-proxy-and-client-ip-infra-sec-1) and the [production checklist](docs/05-deployment/production-checklist.md#trusted-reverse-proxy-and-network-exposure-infra-sec-1).

The default `docker-compose.yml` publishes ports `3000`, `4000`, and `5432` to the host for **local development only** — not for production.

See [docs/README.md](docs/README.md) and [docs/05-deployment/README.md](docs/05-deployment/README.md) for deployment guides, backup/restore, troubleshooting, and secrets documentation.

---

## Version History

Authoritative product versions: [Platform Version History](docs/06-release-notes/platform-version-history.md).

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
