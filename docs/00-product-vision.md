# InfraTrack Product Vision

## Document Information

| Field    | Value                                              |
| -------- | -------------------------------------------------- |
| Project  | InfraTrack                                         |
| Document | Product Vision                                     |
| Version  | 1.0                                                |
| Status   | Living Document                                    |
| Audience | Developers, architects, product owners, councils, future contributors |

This document explains **why** InfraTrack exists, **who** it serves, and **where** the product is heading. It describes the product vision — not implementation. For how the platform works, see the linked documentation at the end of this document.

---

## 1. Why InfraTrack Exists

Public infrastructure must be inspected, maintained, and accounted for over decades. In practice, many organisations still operate with fragmented information spread across spreadsheets, email, shared drives, paper forms, and disconnected point solutions.

Typical consequences include:

- **Fragmented information** — asset registers, inspection records, and maintenance history live in different places with no reliable link between them.
- **Disconnected inspections** — field observations do not flow cleanly into issues, decisions, or work orders.
- **Reactive maintenance** — teams respond to failures rather than planning from evidence and preventive schedules.
- **Poor operational visibility** — managers cannot see workload, overdue work, or department performance without manual consolidation.
- **Inconsistent decision making** — similar conditions may be handled differently because there is no shared record of what was decided and why.
- **Limited historical traceability** — audits, public enquiries, and handovers suffer when evidence is scattered or incomplete.

InfraTrack addresses these problems by providing a **single operational record** for public assets. Every significant action — registration, inspection, issue, decision, work order, maintenance, review, and supporting document — contributes to the permanent history of an asset. The platform connects field work to managerial accountability without replacing human judgment.

InfraTrack is an **operational evidence platform**, not merely a maintenance log. Councils need to know what was found, what was decided, who did the work, and what evidence supports each step.

---

## 2. Primary Market

InfraTrack is built first for **Australian Local Government** operations:

- Local Governments
- Shires
- Councils
- Municipal operations teams
- Public infrastructure and parks teams

Typical assets include everyday community infrastructure:

- Street lights
- Parks and playgrounds
- Public toilets
- Community buildings and halls
- Road signs and street furniture
- Park barbecues and amenities

These assets are inspected by council staff and contractors, maintained on schedules driven by condition and community use, and subject to public accountability. InfraTrack models that reality directly.

Industrial organisations — mines, factories, heavy plant, process industries — are **not** the primary audience today. The product is not positioned as industrial CMMS or ERP software. The same operational lifecycle may apply to other sectors in future, but design priorities, language, and workflows reflect council practice first.

---

## 3. A Generic Operations Platform

Although the first market is local government, the underlying business model is **generic operational asset management**.

The core lifecycle is:

```text
Asset
    ↓
Inspection
    ↓
Issue
    ↓
Decision
    ↓
Work Order
    ↓
Maintenance
    ↓
Review
    ↓
History
```

An **Asset** is registered and owned by a department. **Inspections** observe condition in the field. **Issues** record problems identified during or after inspection. **Operational Decisions** determine the council's response. **Work Orders** authorise maintenance or contractor work. **Maintenance Activities** record execution. **Completion Reviews** validate outcomes. Each step appends to **Asset History** and supporting **Operational Documents**.

This lifecycle is not unique to councils. The same pattern can later support:

- Utilities and network operators
- Facilities management
- Transport infrastructure
- Campus and precinct operations
- Broader industrial operations

The goal is a **generic operations platform** with a council-first expression — not a council-only product with a different architecture for every future sector. Business terminology and department scoping may adapt; the lifecycle and human-in-the-loop philosophy remain stable.

---

## 4. Product Principles

InfraTrack follows principles established in Business Discovery, Architecture Decision Records, and Business Decision Records. They guide every capability added to the platform.

**Backend is the single source of truth.**  
All business rules, validation, permissions, workflows, and notifications belong on the server. Web and mobile clients collect input and display information; they do not recalculate permissions or business outcomes.

**Business before technology.**  
Operational reality defines the software. Technology choices are secondary and replaceable without redesigning the business model.

**Intelligence proposes; humans decide.**  
Inspection rules may suggest actions; preventive schedules may generate candidates; dashboards may highlight attention items. Managers and coordinators retain explicit authority over operational decisions. Automation is optional and never the default gate.

**Explainability over automation.**  
Rule evaluation reports, suggested actions, preventive execution reports, and audit trails exist so councils can answer *what was evaluated*, *what was recommended*, and *who decided*. Black-box behaviour is avoided.

**Mobile clients never recalculate permissions.**  
Field applications consume the same authoritative API. Role and assignment checks happen on the server; the client reflects allowed actions only.

**Simplicity before sophistication.**  
Explicit workflows, readable code, and a modular monolith are preferred over microservices, generic frameworks, and speculative abstractions. Complexity is justified only by measurable business value.

**Modular monolith before microservices.**  
InfraTrack is one deployable platform organised by business capability. Distribution across services is deferred until a real operational need requires it.

**Incremental validated delivery.**  
Capabilities ship in focused sprints with tests, documentation, and regression protection. Portfolio features and speculative roadmaps do not drive implementation.

**Evidence matters.**  
Inspections, photographs, reports, decisions, and reviews form the asset's permanent record. Documentation is a first-class deliverable alongside code.

**Build for a small council.**  
InfraTrack targets small to medium Australian Local Governments — not enterprise ERP replacement. Scope stays proportionate to real council operations.

---

## 5. Platform Evolution

InfraTrack has grown in deliberate layers. Each layer adds capability without discarding the foundation beneath it.

```text
Core CMMS
        ↓
Inspection Intelligence
        ↓
Preventive Intelligence
        ↓
Operations Intelligence
        ↓
Mobile Platform
        ↓
Reporting Foundation
        ↓
Future Field Operations Platform
```

**Core CMMS (Version 1.0)** — The complete operational workflow: assets, inspections, issues, operational decisions, work orders, maintenance, completion reviews, operational documents, notifications, and role-based access for council teams.

**Inspection Intelligence (Version 2.0)** — Structured inspection templates, checklist questions, decision rules, rule evaluation reports, suggested actions, and the Decision Assistant. The system evaluates answers and proposes next steps; managers approve or dismiss.

**Preventive Intelligence (Version 2.0)** — Preventive maintenance plans, trigger evaluation, execution candidates, preventive decision assistance, execution reports, and a controlled scheduler that generates candidates — not automatic field dispatch.

**Operations Intelligence (Version 2.1)** — Read-only KPIs, trends, recent activity, and a personalised operations dashboard for managers and coordinators. Observes operational data; does not mutate workflows.

**Mobile Platform (Version 2.2)** — Compact read and bundle APIs for a future native Android field client. Identity, assigned work, and screen-oriented payloads without duplicating business rules on the device.

**Reporting Foundation (Version 2.2)** — Read-only CSV exports for operational review, supervisor reporting, and spreadsheet analysis. Reporting observes; it does not create or approve records.

**Future Field Operations Platform (planned)** — Native Android application, offline synchronisation, and field-first execution using the same backend authority. Mobile becomes a primary channel for inspection and maintenance completion, not a secondary viewer.

---

## 6. Current Platform Capabilities

At a product level, InfraTrack today includes:

| Capability | Purpose |
|------------|---------|
| **Asset Management** | Register and track public assets by department and category |
| **Inspection Management** | Assign, perform, and complete inspections with evidence |
| **Issue Management** | Record problems linked to inspections and assets |
| **Operational Decisions** | Manager decisions that authorise maintenance responses |
| **Work Orders** | Create, assign, and track maintenance and contractor work |
| **Maintenance Activities** | Record field execution against work orders |
| **Completion Reviews** | Manager validation of completed maintenance |
| **Operational Documents** | Upload, download, and manage evidence linked to operational records |
| **Inspection Intelligence** | Templates, questions, rules, suggestions, and decision assistance |
| **Preventive Intelligence** | Plans, candidates, reviews, scheduler, and execution reports |
| **Operations Intelligence** | KPIs, trends, activity feed, and dashboard personalisation |
| **Mobile API** | Authoritative compact endpoints for future field clients |
| **Reporting** | Department-scoped CSV exports for key operational entities |
| **Notifications** | In-app and push awareness of operational events |
| **User & Access Management** | Roles, departments, invitation, and activation lifecycle |

Implementation details, API paths, and data models are documented elsewhere. This list describes **what councils can do**, not how the code is organised.

---

## 7. What InfraTrack Is Not

Clarity about scope protects councils and contributors from misplaced expectations.

**Not an ERP.** InfraTrack does not manage finance, procurement ledgers, payroll, or general council administration.

**Not an accounting system.** Cost capture and budget modules may appear in future roadmaps; they are not the core product today.

**Not an HR system.** User accounts support operational roles; workforce management and payroll sit outside scope.

**Not a fully autonomous maintenance platform.** Preventive schedules and inspection rules inform humans; they do not dispatch unattended work or auto-approve operational decisions by default.

**Not an AI-first black box.** Recommendations are auditable. The preferred term is **AI-assisted** — supporting judgment, not replacing it. Avoid **AI-driven** autonomy that bypasses council accountability.

**Not a microservices showcase.** One modular monolith serves council deployments proportionately. Service decomposition is not a product goal.

**Not industrial CMMS (today).** Language, actors, and workflows reflect local government first. Other sectors are future opportunities on the same lifecycle model.

---

## 8. Long-Term Direction

InfraTrack aims to become the **operational backbone** for public infrastructure teams — starting with Australian councils, extensible to similar organisations later.

Planned and emerging directions include:

- **Mobile-first field operations** — Native Android client for inspections and maintenance using the Mobile API
- **Offline support** — Reliable field work in low-connectivity areas with server-side reconciliation
- **QR and barcode** — Faster asset identification in the field
- **Asset Intelligence** — Deeper asset-level insight from operational history, templates, and preventive plans
- **Better reporting** — Expanded export formats and council-ready reports beyond CSV
- **Multi-site support** — Organisations operating multiple sites or regions under one tenant
- **Public APIs** — Documented integration surface for GIS, asset registers, and external systems
- **AI-assisted recommendations** — Optional assistance on patterns, prioritisation, and review — always with human override and audit

The long-term philosophy remains **human-in-the-loop**. Technology should make council operations more visible, traceable, and consistent — not remove accountability from managers and coordinators.

Version planning and delivery status are maintained in the [Product Roadmap](06-release-notes/v2-roadmap.md) and [Platform Version History](06-release-notes/platform-version-history.md).

---

## 9. Related Documentation

Use this vision as the starting point; drill into the documents below for detail.

| Document | Description |
|----------|-------------|
| [Business Capability Map](01-business-architecture/business-capability-map.md) | What the platform can do today by business capability |

### Business and domain

| Document | Description |
|----------|-------------|
| [Development Philosophy](00-business-discovery/00-development-philosophy.md) | How product and technical decisions are made |
| [Project Context](00-business-discovery/01-project-context.md) | Business problem and council focus |
| [Domain Overview](00-business-discovery/02-domain-overview.md) | Core domain concepts |
| [Business Architecture](01-business-architecture/business-architecture.md) | Long-term business structure |
| [Functional Use Cases](01-business-architecture/functional-use-cases.md) | Authoritative V1 use cases |
| [Business Glossary](01-business-architecture/glossary.md) | Official terminology |
| [Domain Engine](07-business-architecture/domain-engine.md) | Inspection and Preventive Intelligence (V2) |

### Architecture and philosophy

| Document | Description |
|----------|-------------|
| [ADR-003 — V2 domain-driven workflow](03-architecture/adr-003-v2-domain-driven-workflow.md) | How V2 business domains interact |
| [ADR-004 — Platform versioning](03-architecture/adr-004-platform-versioning-strategy.md) | Product version semantics |
| [BDR-001 — Human-in-the-loop](03-architecture/bdr-001-human-in-the-loop-decision-engine.md) | Rules suggest; managers decide |
| [BDR-002 — Preventive candidates](03-architecture/bdr-002-preventive-candidates-before-automation.md) | Scheduler generates candidates only |
| [BDR-003 — Bearer token architecture](03-architecture/bdr-003-bearer-token-architecture.md) | Authentication model for clients |

### APIs, deployment, and releases

| Document | Description |
|----------|-------------|
| [API documentation index](04-api/README.md) | OpenAPI, Mobile API, Reporting API |
| [Mobile API](04-api/mobile-api.md) | Compact field client contract |
| [Reporting API](04-api/reporting-api.md) | CSV export endpoints |
| [Security](05-deployment/security.md) | Production security controls |
| [Platform Version History](06-release-notes/platform-version-history.md) | Delivered product versions |
| [Product Roadmap](06-release-notes/v2-roadmap.md) | Planned versions and capabilities |

### Engineering

| Document | Description |
|----------|-------------|
| [System Blueprint](02-system-blueprint/INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md) | Engineering standards and workflow |
| [Project README](../README.md) | Quick start, testing, and API developer guide |

---

*InfraTrack — Operational evidence platform designed for local governments, with an architecture intentionally generic enough to support other asset-intensive organisations over time.. One lifecycle, one source of truth, human accountability at every decision point.*
