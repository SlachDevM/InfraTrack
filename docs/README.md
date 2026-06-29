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
        ↓
06-release-notes
        ↓
07-business-architecture   (V2 Domain Engine)
```

---

## Quick links (V2 Phase A+B)

| Topic | Document |
|-------|----------|
| Domain Engine (authoritative) | [domain-engine.md](07-business-architecture/domain-engine.md) |
| Business Glossary | [glossary.md](01-business-architecture/glossary.md) |
| V2 workflow architecture | [adr-003-v2-domain-driven-workflow.md](03-architecture/adr-003-v2-domain-driven-workflow.md) |
| Phase A+B milestone | [v2-phase-a-b.md](06-release-notes/v2-phase-a-b.md) |
| V2 Roadmap | [v2-roadmap.md](06-release-notes/v2-roadmap.md) |
| V2 API endpoint groups | [v2-domain-engine-api.md](04-api/v2-domain-engine-api.md) |
| Human-in-the-loop (BDR) | [bdr-001](03-architecture/bdr-001-human-in-the-loop-decision-engine.md) |
| Preventive candidates (BDR) | [bdr-002](03-architecture/bdr-002-preventive-candidates-before-automation.md) |
| Deployment & scheduler config | [05-deployment/README.md](05-deployment/README.md) |
| Security | [05-deployment/security.md](05-deployment/security.md) |
| CI / testing | [Testing (project README)](../README.md#testing) |

---

## Documentation Tree

```text
docs/
│
├── 00-business-discovery/
│   └── … domain model, actors, workflows, business rules
│
├── 01-business-architecture/
│   ├── business-architecture.md
│   ├── functional-use-cases.md
│   └── glossary.md
│
├── 02-system-blueprint/
│   └── INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md
│
├── 03-architecture/
│   ├── ADR-001-asset-history-starts-with-registration.md
│   ├── ADR-002-inspection-produces-at-most-one-issue-v1.md
│   ├── adr-003-v2-domain-driven-workflow.md
│   ├── bdr-001-human-in-the-loop-decision-engine.md
│   └── bdr-002-preventive-candidates-before-automation.md
│
├── 04-api/
│   ├── README.md
│   └── v2-domain-engine-api.md
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
│   ├── v2-sprint0.md
│   ├── v2-phase-a-b.md
│   └── v2-roadmap.md
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

Defines the long-term business architecture and translates business discovery into use cases. See [business-architecture.md](01-business-architecture/business-architecture.md), [functional-use-cases.md](01-business-architecture/functional-use-cases.md), and [glossary.md](01-business-architecture/glossary.md).

### 02 — System Blueprint

Engineering handbook: architecture principles, coding standards, AI collaboration rules and development workflow. See [INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md](02-system-blueprint/INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md).

### 03 — Architecture

Architecture Decision Records (ADRs) and Business Decision Records (BDRs):

- [ADR-001](03-architecture/ADR-001-asset-history-starts-with-registration.md) — Asset history starts with registration
- [ADR-002](03-architecture/ADR-002-inspection-produces-at-most-one-issue-v1.md) — Inspection produces at most one Issue (V1)
- [ADR-003](03-architecture/adr-003-v2-domain-driven-workflow.md) — V2 domain-driven workflow
- [BDR-001](03-architecture/bdr-001-human-in-the-loop-decision-engine.md) — Rules suggest; managers decide
- [BDR-002](03-architecture/bdr-002-preventive-candidates-before-automation.md) — Scheduler generates candidates only

### 04 — API

Live OpenAPI documentation is served by the backend (Swagger UI). See [04-api/README.md](04-api/README.md) and [v2-domain-engine-api.md](04-api/v2-domain-engine-api.md) for V2 endpoint groups. The [project README API Developer Guide](../README.md#api-developer-guide) covers authentication, pagination, and errors.

### 05 — Deployment

Deployment, secrets, security hardening, backup/restore, and troubleshooting. See [05-deployment/README.md](05-deployment/README.md), [secrets.md](05-deployment/secrets.md), [security.md](05-deployment/security.md), [backup-restore.md](05-deployment/backup-restore.md), [production-checklist.md](05-deployment/production-checklist.md), and [troubleshooting.md](05-deployment/troubleshooting.md).

### 06 — Release Notes

Sprint and milestone release notes:

- [v2-sprint0.md](06-release-notes/v2-sprint0.md) — V2 Sprint 0 technical baseline
- [v2-phase-a-b.md](06-release-notes/v2-phase-a-b.md) — **V2 Phase A+B milestone** (Decision Engine + Preventive Maintenance Engine)
- [v2-roadmap.md](06-release-notes/v2-roadmap.md) — V2 phase evolution (C–K)

### 07 — Business Architecture (V2)

The **Domain Engine** — Inspection Intelligence (Phase A) and Preventive Maintenance (Phase B). See [domain-engine.md](07-business-architecture/domain-engine.md).

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

Generate locally with `cd backend && mvn clean test`. Coverage is informational; an 80% instruction coverage gate may be enabled later.

See also the [project README Testing section](../README.md#testing) and the [V2 Phase A+B validation checklist](06-release-notes/v2-phase-a-b.md#manual-validation-checklist).
