# InfraTrack Business Capability Map

## Document Information

| Field    | Value                                                                 |
| -------- | --------------------------------------------------------------------- |
| Project  | InfraTrack                                                            |
| Document | Business Capability Map                                               |
| Version  | 1.0                                                                   |
| Status   | Living Document                                                       |
| Audience | Product owners, solution architects, developers, councils, customers |

This document answers **what InfraTrack can do today** at a business level. It describes capabilities, not APIs, databases, or code structure. For product philosophy and direction, see [Product Vision](../00-product-vision.md). For detailed use cases and domain rules, follow the links in each capability section.

---

## Introduction

InfraTrack is organised as a collection of **complementary business capabilities** within one integrated platform. Each capability supports part of the operational asset lifecycle — from registration through inspection, decision, maintenance, and review.

Capabilities are **modular in concept** but **unified in practice**. A council does not deploy separate products for inspections, work orders, and preventive maintenance. Teams work in one system where data, permissions, and history stay connected.

The map below is the high-level functional reference for understanding scope, dependencies, and maturity. Implementation detail lives in Business Discovery, the Domain Engine, API documentation, and release notes.

---

## Capability Map

```text
InfraTrack Platform

├── Core Asset Management
│
├── Inspection Management
│
├── Issue Management
│
├── Operational Decision Management
│
├── Work Order Management
│
├── Maintenance Management
│
├── Completion Review
│
├── Inspection Intelligence
│
├── Preventive Intelligence
│
├── Operations Intelligence
│
├── Reporting
│
└── Mobile Platform
```

---

## Core Asset Management

### Purpose

Establish and maintain the authoritative register of public infrastructure assets. Every operational record — inspection, issue, work order, document — links back to an asset and its owning department.

### Primary Users

- **Manager** — registers assets for the department (with delegated authority where configured)
- **Operational Coordinator** — registers assets for the department
- **Administrator** — organisation-wide visibility; does not register assets in normal council workflow
- **Manager, Operational Coordinator, Field Employee, Contractor** — view assets relevant to their role and assignments

### Core Features

- Asset register with name, category, department, location, and lifecycle status
- Department and asset category reference data
- Registration date and registration audit trail
- Asset operational history aggregating significant events across the lifecycle
- Department-scoped visibility for operational roles

### Business Value

Councils need one place to know **what they own**, **where it is**, and **who is responsible**. Without a central register, inspections and maintenance float without context and audits cannot reconstruct asset history.

### Related Capabilities

Foundation for all other capabilities. Inspection Management, Issue Management, Work Order Management, and Preventive Intelligence all depend on registered assets.

### Related Documentation

- [Functional Use Cases — UC-001, UC-002, UC-011](functional-use-cases.md)
- [Asset operational lifecycle](../00-business-discovery/05-asset-operational-lifecycle.md)
- [Glossary — Asset](glossary.md)

---

## Inspection Management

### Purpose

Plan, assign, perform, and complete inspections that observe the condition of assets in the field. Inspections are the primary entry point for condition-based maintenance and issue identification.

### Primary Users

- **Operational Coordinator** — assigns inspections from business triggers
- **Field Employee, Contractor** — perform assigned inspections
- **Manager, Operational Coordinator** — monitor inspection workload and outcomes
- **Administrator** — organisation-wide visibility

### Core Features

- Business trigger–driven inspection assignment
- Assignment to field workers with priority and expected completion date
- Field completion with observed condition, observations, and issue-identified flag
- **Legacy inspections** — completion without a structured template
- **Templated inspections** — completion with structured answers aligned to an Inspection Template at assignment time
- Status lifecycle from assignment through completion

### Business Value

Inspections turn physical condition into recorded evidence. Councils move from ad hoc site visits to traceable, assignable work with clear accountability for who inspected what and when.

### Related Capabilities

**Upstream:** Core Asset Management, Business Triggers  
**Downstream:** Issue Management, Inspection Intelligence (templates and rules), Preventive Intelligence (approved candidates may create inspections)

### Related Documentation

- [Functional Use Cases — UC-003, UC-004](functional-use-cases.md)
- [Inspection lifecycle](../00-business-discovery/07-inspection-lifecycle.md)
- [Domain Engine — Inspection Intelligence](../07-business-architecture/domain-engine.md)

---

## Issue Management

### Purpose

Record problems identified during or after inspections. Issues capture severity, description, and linkage to the inspection and asset so managers can decide the council's response.

### Primary Users

- **Field Employee, Contractor** — record issues on completed inspections where an issue was identified
- **Manager** — review issues; update CAPA fields (root cause, corrective action, preventive action, lessons learned)
- **Operational Coordinator** — departmental visibility

### Core Features

- Issue recording linked to inspection and asset
- Issue type and severity classification
- Recorded-by and recorded-at audit fields
- CAPA enrichment by managers after recording
- Resolution implied when an Operational Decision exists for the issue

### Business Value

Issues formalise **what is wrong** separately from **what the council will do**. This separation supports manager review, consistent severity handling, and audit trails for public enquiries.

### Related Capabilities

**Upstream:** Inspection Management  
**Downstream:** Operational Decision Management

### Related Documentation

- [Functional Use Cases — UC-005](functional-use-cases.md)
- [Domain Engine](../07-business-architecture/domain-engine.md)

---

## Operational Decision Management

### Purpose

Give managers explicit authority to decide how the council responds to recorded issues. Operational decisions authorise maintenance paths and create accountability for governance and audit.

### Primary Users

- **Manager** — records operational decisions (including via delegated authority where configured)
- **Operational Coordinator** — visibility; creates work orders from approved decisions
- **Administrator** — organisation-wide visibility

### Core Features

- Manager review of issues requiring a decision
- Operational decision outcomes (for example internal maintenance or contractor work)
- Decision rationale and audit trail
- Delegated authority support for acting managers
- Gateway to work order creation

### Business Value

Councils must show **who decided** and **why** — not only what work was done. Operational decisions connect field observations to authorised maintenance without bypassing managerial accountability.

### Related Capabilities

**Upstream:** Issue Management, Inspection Intelligence (approved suggested actions may create issues)  
**Downstream:** Work Order Management

### Related Documentation

- [Functional Use Cases — UC-007](functional-use-cases.md)
- [Operational decisions](../00-business-discovery/08-operational-decisions.md)
- [BDR-001 — Human-in-the-loop](../03-architecture/bdr-001-human-in-the-loop-decision-engine.md)

---

## Work Order Management

### Purpose

Authorise and track maintenance work arising from operational decisions. Work orders assign execution to field employees or contractors and carry priority, description, and status through to completion.

### Primary Users

- **Operational Coordinator** — creates and assigns work orders
- **Field Employee, Contractor** — execute assigned work orders
- **Manager** — oversight and completion review
- **Administrator** — organisation-wide visibility

### Core Features

- Work order creation from eligible operational decisions
- Internal maintenance and contractor work types
- Assignment to eligible workers by department and role
- Priority and description
- Status lifecycle including assignment and completion handoff to maintenance activities

### Business Value

Work orders translate decisions into **actionable, assignable tasks**. Councils gain visibility over backlog, assignment, and execution without losing the link to the original issue and decision.

### Related Capabilities

**Upstream:** Operational Decision Management  
**Downstream:** Maintenance Management, Completion Review

### Related Documentation

- [Functional Use Cases — UC-007, UC-008](functional-use-cases.md)
- [Glossary — Work Order](glossary.md)

---

## Maintenance Management

### Purpose

Record execution of maintenance work in the field and attach operational evidence. Maintenance activities close the loop between assignment and managerial review.

### Primary Users

- **Field Employee, Contractor** — complete maintenance on assigned work orders
- **Manager, Operational Coordinator** — visibility and document management
- **Field Employee, Contractor** — upload operational documents on assigned contexts

### Core Features

- Maintenance activity recording with completion notes and timestamps
- Linkage to work order and asset
- Operational document upload (photos, reports, manuals) linked to assets and operational owners
- Operational document download with role- and assignment-based authorisation
- Asset history events for document upload and deletion

### Business Value

Maintenance records prove **work was performed** and preserve **evidence** for audits, insurance, and handovers. Councils replace informal photos in email with evidence tied to the asset lifecycle.

### Related Capabilities

**Upstream:** Work Order Management  
**Downstream:** Completion Review  
**Parallel:** Operational documents span inspections, issues, work orders, and maintenance activities

### Related Documentation

- [Functional Use Cases — UC-009, UC-012](functional-use-cases.md)
- [Security — operational document authorisation](../05-deployment/security.md)

---

## Completion Review

### Purpose

Allow managers to validate completed maintenance before the operational cycle closes. Reviews support quality assurance, rework when needed, and formal closure of work orders.

### Primary Users

- **Manager** — records completion review decisions
- **Field Employee, Contractor** — may receive rework assignments following review
- **Operational Coordinator** — visibility

### Core Features

- Review of completed maintenance activities
- Approve or require rework outcomes
- Rework linkage back into operational decisions where business rules require
- Review notes and audit timestamps

### Business Value

Completion review prevents **silent acceptance** of substandard work. Councils can demonstrate supervisory oversight — important for contractor management and internal quality programs.

### Related Capabilities

**Upstream:** Maintenance Management  
**Downstream:** Asset history; may trigger further decisions on rework

### Related Documentation

- [Functional Use Cases — UC-010](functional-use-cases.md)
- [Domain Engine](../07-business-architecture/domain-engine.md)

---

## Inspection Intelligence

### Purpose

Capture reusable inspection knowledge through templates, structured questions, and decision rules. The engine evaluates answers and **proposes** follow-up actions; it does not automatically create issues or work orders.

### Primary Users

- **Administrator** — manages inspection templates and decision rules
- **Manager** — reviews suggested actions via the Decision Assistant
- **Operational Coordinator** — assigns templated inspections
- **Field Employee, Contractor** — complete inspections with template answers

### Core Features

- Inspection templates and publish lifecycle
- Structured checklist questions with validated answer types
- Decision rules evaluated at inspection completion
- Rule evaluation reports (audit of what matched)
- Suggested actions surfaced to managers
- Decision Assistant for approve, reject, or dismiss
- Approved suggestions create issues with full traceability

**The engine proposes. Managers decide.**

### Business Value

Councils gain **consistent checklists** and **repeatable evaluation** without surrendering managerial judgment. Explainability supports training, audit, and gradual improvement of rules over time.

### Related Capabilities

**Integrates with:** Inspection Management, Issue Management  
**Distinct from:** Preventive Intelligence (plan-driven rather than answer-driven)

### Related Documentation

- [Domain Engine — Decision Engine](../07-business-architecture/domain-engine.md)
- [BDR-001 — Human-in-the-loop](../03-architecture/bdr-001-human-in-the-loop-decision-engine.md)
- [ADR-003 — V2 workflow](../03-architecture/adr-003-v2-domain-driven-workflow.md)

---

## Preventive Intelligence

### Purpose

Plan and evaluate preventive maintenance using maintenance plans, triggers, and execution candidates. The scheduler generates **candidates** for manager review — not automatic field dispatch.

### Primary Users

- **Administrator** — manages preventive maintenance plans
- **Manager** — generates candidates, approves, rejects, or dismisses; approval creates inspections
- **Operational Coordinator** — reviews candidate queue
- **Administrator, Manager** — may run the controlled scheduler

### Core Features

- Preventive maintenance plans with target actions and triggers
- Trigger evaluation (time-based and related types per plan configuration)
- Preventive execution candidates with snapshot reports
- Manager approval workflow creating assigned inspections
- Reject and dismiss with reasons
- Controlled scheduler producing candidates only
- Preventive execution reports for traceability

**Candidates require approval. No automatic inspection generation without manager action.**

### Business Value

Councils shift from purely reactive maintenance toward **planned prevention** while keeping humans in control of what enters the field. Audit reports show why a candidate was generated and what was decided.

### Related Capabilities

**Upstream:** Core Asset Management, Inspection Management (approved candidates create inspections)  
**Feeds:** Operations Intelligence KPIs and reporting

### Related Documentation

- [Domain Engine — Preventive Maintenance](../07-business-architecture/domain-engine.md)
- [BDR-002 — Candidates before automation](../03-architecture/bdr-002-preventive-candidates-before-automation.md)
- [Functional Use Cases](../01-business-architecture/functional-use-cases.md)

---

## Operations Intelligence

### Purpose

Provide read-only operational visibility through KPIs, trends, recent activity, and personalised dashboards. Operations Intelligence **observes** the platform; it does not approve workflows or mutate records.

### Primary Users

- **Administrator** — organisation-wide KPIs and trends
- **Manager, Operational Coordinator** — department-scoped dashboards
- **Field Employee, Contractor** — not authorised for operations intelligence exports or KPI APIs

### Core Features

- Operations KPI summary (inspections, issues, work orders, preventive candidates, and related counts)
- Trend time-series by day, week, or month
- Recent activity feed from operational events
- Dashboard widget layout and user-scoped personalisation
- Attention alerts derived from operational thresholds

**Read-only operational visibility.**

### Business Value

Managers see workload, overdue items, and trends without exporting spreadsheets manually. Councils gain a management cockpit aligned with the same data field teams produce.

### Related Capabilities

Spans the entire lifecycle — consumes data from inspections, issues, work orders, preventive candidates, and decisions  
**Complements:** Reporting (detailed exports for offline analysis)

### Related Documentation

- [Domain Engine — Operations Intelligence](../07-business-architecture/domain-engine.md)
- [Product Roadmap — Version 2.1.0](../06-release-notes/v2-roadmap.md)
- [V2 Domain Engine API](../04-api/v2-domain-engine-api.md) *(endpoint reference only)*

---

## Reporting

### Purpose

Export operational data for council review, supervisor reporting, spreadsheet analysis, and audit preparation. Reporting **observes** data only — it never creates, updates, or approves records.

### Primary Users

- **Administrator** — organisation-wide exports
- **Manager, Operational Coordinator** — department-scoped exports
- **Field Employee, Contractor** — not authorised

### Core Features

**Current (foundation):**

- CSV exports for assets, inspections, issues, work orders, and preventive execution candidates
- Department scoping consistent with operations intelligence
- Optional date filters where reliable business timestamps exist
- UTF-8 CSV suitable for Excel and LibreOffice

**Future (planned, not yet delivered):**

- PDF council-ready reports
- Excel (`.xlsx`) exports
- Scheduled and email reports

### Business Value

Councils need simple, universal exports for operational review and external sharing without building a separate reporting database. CSV foundation supports immediate practical use while richer formats remain on the roadmap.

### Related Capabilities

Reads from all operational capabilities; does not alter workflows  
**Complements:** Operations Intelligence (interactive dashboard vs. downloadable datasets)

### Related Documentation

- [Reporting API](../04-api/reporting-api.md)
- [Product Roadmap — Version 2.2.0](../06-release-notes/v2-roadmap.md)

---

## Mobile Platform

### Purpose

Provide a **field interface** to the same authoritative backend used by the web application. Mobile is a client channel — not a second business layer. Permissions, workflows, and rules remain on the server.

### Primary Users

- **Field Employee, Contractor** — primary future audience for assigned inspections and work orders
- **Manager, Operational Coordinator, Administrator** — mobile identity and scoped read access per API rules

### Core Features

**Current:**

- Mobile API foundation — compact read and bundle endpoints under `/api/mobile/*`
- Identity, dashboard summary, assigned inspections and work orders
- Screen-oriented bundles reducing round-trips for future clients
- Write operations continue through existing web APIs (inspection complete, maintenance complete)

**Future (planned):**

- Native Android field application (Version 2.3.0)
- Offline synchronisation (Version 2.4.0)
- QR and barcode asset identification
- Push notification delivery via existing notification infrastructure

### Business Value

Field workers need fast, focused tools on site. A dedicated mobile channel improves usability without duplicating business logic or splitting operational truth across platforms.

### Related Capabilities

Consumes: Inspection Management, Work Order Management, Maintenance Management, Core Asset Management  
**Does not replace:** Any backend capability — mirrors web client responsibilities

### Related Documentation

- [Mobile API](../04-api/mobile-api.md)
- [Product Vision — Mobile Platform](../00-product-vision.md)
- [Product Roadmap — Versions 2.2.0, 2.3.0, 2.4.0](../06-release-notes/v2-roadmap.md)

---

## Operational Lifecycle

The core operational path connects capabilities in sequence:

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
Completion Review
    ↓
History
```

**Inspection Intelligence** interacts at **inspection completion** — rules evaluate structured answers and may suggest actions for manager review before issues enter the standard path.

**Preventive Intelligence** interacts **before inspection creation** — approved execution candidates generate assigned inspections from plans rather than from field-observed condition alone.

**Operations Intelligence** spans the **entire lifecycle** — aggregating counts, trends, and recent events across assets, inspections, issues, work orders, and preventive activity.

**Reporting** spans the **entire lifecycle** — exporting register and transactional data for offline analysis without changing operational state.

**Mobile Platform** provides **field access** across assigned inspections and work orders using the same lifecycle and rules as the web client.

---

## Capability Maturity

| Capability | Status | Notes |
|------------|--------|-------|
| Core Asset Management | Mature | V1 core; department scoping established |
| Inspection Management | Mature | Legacy and templated completion |
| Issue Management | Mature | CAPA enrichment by managers |
| Operational Decision Management | Mature | Human-in-the-loop gateway |
| Work Order Management | Mature | Internal and contractor paths |
| Maintenance Management | Mature | Includes operational documents |
| Completion Review | Mature | Approve and rework flows |
| Inspection Intelligence | Mature | Templates, rules, suggested actions |
| Preventive Intelligence | Mature | Plans, candidates, controlled scheduler |
| Operations Intelligence | Mature | KPIs, trends, activity, dashboard personalisation |
| Reporting | Foundation | CSV exports delivered; PDF/XLSX deferred |
| Mobile Platform | In Progress | Mobile API foundation delivered; Android app planned |

*Maturity reflects delivered product capability as documented in [Platform Version History](../06-release-notes/platform-version-history.md) and [Product Roadmap](../06-release-notes/v2-roadmap.md).*

---

## Related Documentation

Use this map as the **functional navigation hub**. Drill down as needed:

| Document | Role |
|----------|------|
| [Product Vision](../00-product-vision.md) | Why InfraTrack exists and long-term direction |
| [Business Architecture](business-architecture.md) | V2 business layers and evolution |
| [Functional Use Cases](functional-use-cases.md) | Authoritative V1 use case catalogue |
| [Business Glossary](glossary.md) | Official terminology |
| [Domain Engine](../07-business-architecture/domain-engine.md) | Inspection and Preventive Intelligence detail |
| [Mobile API](../04-api/mobile-api.md) | Mobile client contract |
| [Reporting API](../04-api/reporting-api.md) | CSV export behaviour |
| [Product Roadmap](../06-release-notes/v2-roadmap.md) | Planned versions |
| [Platform Version History](../06-release-notes/platform-version-history.md) | Delivered versions |
| [BDR-001 — Human-in-the-loop](../03-architecture/bdr-001-human-in-the-loop-decision-engine.md) | Decision philosophy |
| [BDR-002 — Preventive candidates](../03-architecture/bdr-002-preventive-candidates-before-automation.md) | Scheduler philosophy |
| [Development Philosophy](../00-business-discovery/00-development-philosophy.md) | Decision-making principles |

---

*InfraTrack — one platform, twelve capabilities, one operational lifecycle.*
