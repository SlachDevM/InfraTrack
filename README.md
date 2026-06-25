# Business Platform Template

Production-ready foundation for building professional business applications.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![React](https://img.shields.io/badge/React-Frontend-61DAFB?logo=react)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-Backend-6DB33F?logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql)

---

## Overview

The Business Platform Template is a reusable, production-ready foundation for building professional business applications. It provides proven infrastructure for authentication, user management, notifications, and deployment while remaining intentionally free of business-specific logic.

This template is extracted from a real production application after its architecture and production configuration have been validated in business use. Only infrastructure that proved to be reusable is retained.

The backend is designed to support multiple client applications:

- React Web application for administrators and managers
- Additional clients (mobile, desktop, etc.) share the same REST API

---

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

The Business Platform Template provides only infrastructure that has demonstrated value across business applications.

**Backend:**
- Spring Boot configuration
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
- Admin shell
- HTTP API client with JWT token management
- Environment-based configuration

**Infrastructure:**
- Development environment (Docker Compose with Mailpit)
- Production environment (Docker Compose with security best practices)
- PostgreSQL database
- Environment variable strategy
- Production-grade configuration

---

## What the Platform Does NOT Provide

Intentionally excluded:

- Business entities (projects, jobs, orders, tickets, etc.)
- Business workflows
- Fake CRUD examples
- Customer-specific branding
- Customer documentation

Each application built from this template must create its own business domain.

---

## Quick Start

```bash
git clone <repository-url>
cd business-platform-template
docker compose up --build
```

After starting:

- Frontend: `http://localhost:3000`
- Swagger API: `http://localhost:4000/swagger-ui/index.html`
- Mailpit (dev email): `http://localhost:8025`

---

## Architecture

The platform follows a simple, proven architecture:

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

- **ADMIN** — Full user management, all platform capabilities
- **MANAGER** — Can manage business entities and other users (role-dependent)
- **EMPLOYEE** — Can access assigned work and their profile

---

## Configuration

All configuration is externalized via environment variables. See `application.properties` for available options.

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

## Building a New Application

1. Clone this template
2. Rename the project
3. Update package names (`com.company.backend` → `com.yourcompany.backend`)
4. Replace branding (app name, colors, logos)
5. Create your first business entity (inherit the platform architecture)
6. Implement your business workflows
7. Deploy using the provided Docker configuration

The platform requires minimal modification for new applications.

---

## Design Philosophy

The platform follows these principles:

- **Simplicity before abstraction** — Explicit code over clever patterns
- **Business before technology** — Backend owns business rules
- **Remove instead of generalize** — Delete MRRG-specific code, don't create fake examples
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

---

## Production Deployment

Comprehensive deployment instructions including:
- HTTPS/TLS configuration
- Reverse proxy setup
- Database backup strategy
- Monitoring and logging
- Scaling considerations

See [docs/README.md](docs/README.md) for details.

---

## License

This Business Platform Template is a production-ready foundation extracted from a real business application after validation in production use.

It is provided as a template for building custom business applications.
