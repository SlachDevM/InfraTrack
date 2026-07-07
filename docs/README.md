# InfraTrack Documentation

Documentation is organised by project phase. Each folder represents a stage of product development, from business understanding through implementation reference.

**[Product Vision](00-product-vision.md)** — why InfraTrack exists, who it serves, product principles, platform evolution, and long-term direction. Start here for the product story; use the phase folders below for detail.

**[Business Capability Map](01-business-architecture/business-capability-map.md)** — what the platform can do today; high-level functional reference by business capability.

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

---

## Platform Versions

Navigate product releases and planning:

| Topic | Document |
|-------|----------|
| **Product Vision** | [00-product-vision.md](00-product-vision.md) — why the platform exists and where it is heading |
| **Business Capability Map** | [business-capability-map.md](01-business-architecture/business-capability-map.md) — what the platform can do today |
| **API Consumer Guide** | [api-consumer-guide.md](04-api/api-consumer-guide.md) — how clients should consume the API |
| **Workflow Sequence Diagrams** | [workflow-sequence-diagrams.md](02-system-blueprint/workflow-sequence-diagrams.md) — visual workflow guides |
| **Platform Version History** | [platform-version-history.md](06-release-notes/platform-version-history.md) — authoritative product versions |
| **Validated baseline** | Product capability **V2.4** (see [v2.4.md](06-release-notes/v2.4.md)); Maven/npm artifact version **2.0.1** per ADR-004 |
| **Roadmap** | [v2-roadmap.md](06-release-notes/v2-roadmap.md) — planned versions |
| **V2.4 release notes** | [v2.4.md](06-release-notes/v2.4.md) — platform baseline consolidation |
| **Release notes** | [06-release-notes/](06-release-notes/) — sprint and version reports |
| **Architecture decisions** | [**ADR Index**](03-architecture/ADR-INDEX.md) · [ADR-004 Versioning](03-architecture/adr-004-platform-versioning-strategy.md) · [ADR-003 V2 workflow](03-architecture/adr-003-v2-domain-driven-workflow.md) · [all ADRs/BDRs](03-architecture/) |

Versioning rules: [ADR-004 — Platform Versioning Strategy](03-architecture/adr-004-platform-versioning-strategy.md)

---

## Quick links (Version 2.4 baseline)

| Topic | Document |
|-------|----------|
| Product Vision | [00-product-vision.md](00-product-vision.md) |
| Business Capability Map | [business-capability-map.md](01-business-architecture/business-capability-map.md) |
| API Consumer Guide | [api-consumer-guide.md](04-api/api-consumer-guide.md) |
| Workflow Sequence Diagrams | [workflow-sequence-diagrams.md](02-system-blueprint/workflow-sequence-diagrams.md) |
| ADR Index | [ADR-INDEX.md](03-architecture/ADR-INDEX.md) |
| Platform versions | [platform-version-history.md](06-release-notes/platform-version-history.md) |
| Domain Engine (authoritative) | [domain-engine.md](07-business-architecture/domain-engine.md) |
| Business Glossary | [glossary.md](01-business-architecture/glossary.md) |
| V2 workflow architecture | [adr-003-v2-domain-driven-workflow.md](03-architecture/adr-003-v2-domain-driven-workflow.md) |
| Version 2.0.0 sprint report | [v2-phase-a-b.md](06-release-notes/v2-phase-a-b.md) |
| V2.4 release notes | [v2.4.md](06-release-notes/v2.4.md) |
| Product Roadmap | [v2-roadmap.md](06-release-notes/v2-roadmap.md) |
| V2 API endpoint groups | [v2-domain-engine-api.md](04-api/v2-domain-engine-api.md) |
| Human-in-the-loop (BDR) | [bdr-001](03-architecture/bdr-001-human-in-the-loop-decision-engine.md) |
| Preventive candidates (BDR) | [bdr-002](03-architecture/bdr-002-preventive-candidates-before-automation.md) |
| Deployment & scheduler config | [05-deployment/README.md](05-deployment/README.md) |
| Security | [05-deployment/security.md](05-deployment/security.md) |
| V2.0.1 hardening | [v2-0-1-security-hardening.md](06-release-notes/v2-0-1-security-hardening.md) |
| Bearer token BDR | [bdr-003](03-architecture/bdr-003-bearer-token-architecture.md) |
| Offline sync BDR | [bdr-005](03-architecture/bdr-005-offline-synchronization-architecture.md) |
| Conflict resolution strategy (BDR-006) | [bdr-006-conflict-resolution-strategy](03-architecture/bdr-006-conflict-resolution-strategy.md) |
| CI / testing | [Testing (project README)](../README.md#testing) |

---

## Documentation Tree

```text
docs/
│
├── 00-product-vision.md
│
├── 00-business-discovery/
│   └── … domain model, actors, workflows, business rules
│
├── 01-business-architecture/
│   ├── business-architecture.md
│   ├── business-capability-map.md
│   ├── functional-use-cases.md
│   └── glossary.md
│
├── 02-system-blueprint/
│   ├── README.md
│   ├── workflow-sequence-diagrams.md
│   └── INFRATRACK_SYSTEM_BLUEPRINT.md
│
├── 03-architecture/
│   ├── README.md
│   ├── ADR-INDEX.md
│   ├── ADR-001-asset-history-starts-with-registration.md
│   ├── ADR-002-inspection-produces-at-most-one-issue-v1.md
│   ├── adr-003-v2-domain-driven-workflow.md
│   ├── adr-004-platform-versioning-strategy.md
│   ├── bdr-001-human-in-the-loop-decision-engine.md
│   ├── bdr-002-preventive-candidates-before-automation.md
│   ├── bdr-003-bearer-token-architecture.md
│   ├── bdr-004-configurable-organizational-policies.md
│   ├── bdr-005-offline-synchronization-architecture.md
│   └── bdr-006-conflict-resolution-strategy.md
│
├── 04-api/
│   ├── README.md
│   ├── api-consumer-guide.md
│   ├── mobile-api.md
│   ├── reporting-api.md
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
│   ├── platform-version-history.md
│   ├── v2-sprint0.md
│   ├── v2-phase-a-b.md
│   ├── v2-0-1-security-hardening.md
│   ├── v2.4.md
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

Defines the long-term business architecture and translates business discovery into use cases. See [business-architecture.md](01-business-architecture/business-architecture.md), [business-capability-map.md](01-business-architecture/business-capability-map.md), [functional-use-cases.md](01-business-architecture/functional-use-cases.md), and [glossary.md](01-business-architecture/glossary.md).

### 02 — System Blueprint

Engineering handbook and visual workflow guides. See [02-system-blueprint/README.md](02-system-blueprint/README.md), [workflow-sequence-diagrams.md](02-system-blueprint/workflow-sequence-diagrams.md), and [INFRATRACK_SYSTEM_BLUEPRINT.md](02-system-blueprint/INFRATRACK_SYSTEM_BLUEPRINT.md).

### 03 — Architecture

Architecture Decision Records (ADRs) and Business Decision Records (BDRs). **Start with the [ADR Index](03-architecture/ADR-INDEX.md)** for a complete list and reading guidance. See also [03-architecture/README.md](03-architecture/README.md).

- [ADR-001](03-architecture/ADR-001-asset-history-starts-with-registration.md) — Asset history starts with registration
- [ADR-002](03-architecture/ADR-002-inspection-produces-at-most-one-issue-v1.md) — Inspection produces at most one Issue (V1)
- [ADR-003](03-architecture/adr-003-v2-domain-driven-workflow.md) — V2 domain-driven workflow
- [ADR-004](03-architecture/adr-004-platform-versioning-strategy.md) — Platform versioning strategy
- [BDR-001](03-architecture/bdr-001-human-in-the-loop-decision-engine.md) — Rules suggest; managers decide
- [BDR-002](03-architecture/bdr-002-preventive-candidates-before-automation.md) — Scheduler generates candidates only
- [BDR-003](03-architecture/bdr-003-bearer-token-architecture.md) — Bearer token architecture
- [BDR-004](03-architecture/bdr-004-configurable-organizational-policies.md) — Configurable organizational policies
- [BDR-005](03-architecture/bdr-005-offline-synchronization-architecture.md) — Offline & synchronization architecture
- [BDR-006 — Conflict Resolution Strategy](03-architecture/bdr-006-conflict-resolution-strategy.md) — Conflict taxonomy and resolution philosophy (companion to offline BDR-005)

### 04 — API

Live OpenAPI documentation is served by the backend (Swagger UI). See [04-api/README.md](04-api/README.md), [api-consumer-guide.md](04-api/api-consumer-guide.md), and [v2-domain-engine-api.md](04-api/v2-domain-engine-api.md) for V2 endpoint groups. The [project README API Developer Guide](../README.md#api-developer-guide) covers authentication, pagination, and errors.

### 05 — Deployment

Deployment, secrets, security hardening, backup/restore, and troubleshooting. See [05-deployment/README.md](05-deployment/README.md), [secrets.md](05-deployment/secrets.md), [security.md](05-deployment/security.md), [backup-restore.md](05-deployment/backup-restore.md), [production-checklist.md](05-deployment/production-checklist.md), and [troubleshooting.md](05-deployment/troubleshooting.md).

### 06 — Release Notes

Product versions and sprint reports:

- [platform-version-history.md](06-release-notes/platform-version-history.md) — **Platform version history** (authoritative)
- [v2-roadmap.md](06-release-notes/v2-roadmap.md) — Product roadmap by version
- [v2.4.md](06-release-notes/v2.4.md) — V2.4 platform baseline
- [v2-0-1-security-hardening.md](06-release-notes/v2-0-1-security-hardening.md) — Version 2.0.1
- [v2-phase-a-b.md](06-release-notes/v2-phase-a-b.md) — Version 2.0.0 (historical sprint report)
- [v2-sprint0.md](06-release-notes/v2-sprint0.md) — V2 Sprint 0 technical baseline (Version 1.0.1)

### 07 — Business Architecture (V2)

The **Domain Engine** — Inspection Intelligence and Preventive Maintenance (Version 2.0.0). See [domain-engine.md](07-business-architecture/domain-engine.md).

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

See also the [project README Testing section](../README.md#testing) and the [Version 2.0.0 validation checklist](06-release-notes/v2-phase-a-b.md#manual-validation-checklist).
