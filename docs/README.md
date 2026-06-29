# InfraTrack Documentation

Documentation is organised by project phase. Each folder represents a stage of product development, from business understanding through implementation reference.

Read documents in phase order when starting a new capability:

```text
00-business-discovery
        ↓
01-business-architecture
        ↓
02-system-blueprint
        ↓
03-architecture
        ↓
04-api
        ↓
05-deployment
```

---

## Documentation Tree

```text
docs/
│
├── 00-business-discovery/
│   ├── 00-development-philosophy.md
│   ├── 01-project-context.md
│   ├── 02-domain-overview.md
│   ├── 03-council-organisation.md
│   ├── 04-actors-responsibilities.md
│   ├── 05-asset-operational-lifecycle.md
│   ├── 06-business-triggers.md
│   ├── 07-inspection-lifecycle.md
│   ├── 08-operational-decisions.md
│   ├── 09-business-rules.md
│   ├── 10-ubiquitous-language.md
│   ├── 11-asset-status-model.md
│   ├── 12-notification-lifecycle.md
│   └── 13-department-collaboration.md
│
├── 01-business-architecture/
│   ├── business-architecture.md
│   └── functional-use-cases.md
│
├── 02-system-blueprint/
│   └── INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md
│
├── 03-architecture/
│   ├── ADR-001-asset-history-starts-with-registration.md
│   └── ADR-002-inspection-produces-at-most-one-issue-v1.md
│
├── 04-api/
│   └── README.md
│
├── 05-deployment/
│   ├── README.md
│   ├── backup-restore.md
│   ├── production-checklist.md
│   ├── secrets.md
│   ├── security.md
│   └── troubleshooting.md
│
├── 06-release-notes/
│   └── v2-sprint0.md
│
├── 07-business-architecture/
│   └── domain-engine.md
│
└── README.md
```

---

## Phase Guide

### 00 — Business Discovery

Defines the business domain: terminology, actors, workflows, rules and council operational practices.

Start with [00-development-philosophy.md](00-business-discovery/00-development-philosophy.md), then [02-domain-overview.md](00-business-discovery/02-domain-overview.md).

### 01 — Business Architecture

Defines the long-term business architecture and translates business discovery into use cases. See [business-architecture.md](01-business-architecture/business-architecture.md) and [functional-use-cases.md](01-business-architecture/functional-use-cases.md).

### 02 — System Blueprint

Engineering handbook: architecture principles, coding standards, AI collaboration rules and development workflow. See [INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md](02-system-blueprint/INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md).

### 03 — Architecture

Architecture Decision Records (ADRs) and detailed architecture documentation. See [ADR-001](03-architecture/ADR-001-asset-history-starts-with-registration.md) and [ADR-002](03-architecture/ADR-002-inspection-produces-at-most-one-issue-v1.md).

### 04 — API

Live OpenAPI documentation is served by the backend (Swagger UI). See [04-api/README.md](04-api/README.md) and the [project README API Developer Guide](../README.md#api-developer-guide).

### 05 — Deployment

Deployment, secrets, security hardening, backup/restore, and troubleshooting. See [05-deployment/README.md](05-deployment/README.md), [secrets.md](05-deployment/secrets.md), [security.md](05-deployment/security.md), [backup-restore.md](05-deployment/backup-restore.md), [production-checklist.md](05-deployment/production-checklist.md), and [troubleshooting.md](05-deployment/troubleshooting.md).

### 06 — Release Notes

Sprint and version release notes. See [v2-sprint0.md](06-release-notes/v2-sprint0.md) for the V2 Sprint 0 technical baseline.

### 07 — Business Architecture (V2)

Long-term domain engine and V2 business evolution. See [domain-engine.md](07-business-architecture/domain-engine.md).

### Continuous Integration

GitHub Actions (`.github/workflows/ci.yml`) runs backend tests, frontend tests, Docker validation, and OWASP dependency scanning on every push to `main`.

**Backend code coverage (JaCoCo):**

| Output | Location |
|--------|----------|
| Local HTML report | `backend/target/site/jacoco/index.html` |
| Local XML report | `backend/target/site/jacoco/jacoco.xml` |
| CI HTML artifact | Actions → workflow run → **jacoco-html-report** |
| CI XML artifact | Actions → workflow run → **jacoco-xml-report** |
| CI summary | Job summary tab on the backend job |

Generate locally with `cd backend && mvn clean test`. Coverage is informational only in Sprint 0; an 80% instruction coverage gate may be enabled later.

See also the [project README Testing section](../README.md#testing).
