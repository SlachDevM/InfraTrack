# InfraTrack Documentation

Documentation is organised by project phase. Each folder represents a stage of product development, from business understanding through implementation reference.

Read documents in phase order when starting a new capability:

```text
00-business-discovery
        ↓
01-functional-analysis
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
├── 01-functional-analysis/
│   └── functional-use-cases.md
│
├── 02-system-blueprint/
│   └── INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md
│
├── 03-architecture/
│
├── 04-api/
│
├── 05-deployment/
│
└── README.md
```

---

## Phase Guide

### 00 — Business Discovery

Defines the business domain: terminology, actors, workflows, rules and council operational practices.

Start with [00-development-philosophy.md](00-business-discovery/00-development-philosophy.md), then [02-domain-overview.md](00-business-discovery/02-domain-overview.md).

### 01 — Functional Analysis

Translates business discovery into use cases. See [functional-use-cases.md](01-functional-analysis/functional-use-cases.md).

### 02 — System Blueprint

Engineering handbook: architecture principles, coding standards, AI collaboration rules and development workflow. See [INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md](02-system-blueprint/INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md).

### 03 — Architecture

Reserved for detailed architecture documentation (in progress).

### 04 — API

Live OpenAPI documentation is served by the backend (Swagger UI). See [04-api/README.md](04-api/README.md) and the [project README API Developer Guide](../README.md#api-developer-guide).

### 05 — Deployment

Deployment and secrets management. See [05-deployment/README.md](05-deployment/README.md) and [secrets.md](05-deployment/secrets.md).
