# InfraTrack Product Roadmap

Planned evolution of InfraTrack by **product version**. Delivered versions are documented in [Platform Version History](platform-version-history.md).

**Legend:** ✓ Released · ⬜ Planned

Versioning rules: [ADR-004](../03-architecture/adr-004-platform-versioning-strategy.md)

---

## ✓ Version 1.0.0 — Core CMMS

Complete V1 operational workflow for Australian Local Governments.

**Reference:** [Functional Use Cases](../01-business-architecture/functional-use-cases.md)

---

## ✓ Version 1.0.1 — Platform Hardening

Security, stability, documentation, CI, and code quality baseline. No new business capabilities.

**Reference:** [v2-sprint0.md](v2-sprint0.md) (technical sprint report)

---

## ✓ Version 2.0.0 — Inspection Intelligence & Preventive Maintenance

**Objective:** Reusable inspection knowledge and plan-driven preventive maintenance with human validation.

**Business value:** Consistent checklists, auditable decisions, proactive maintenance without unattended field dispatch.

**Major capabilities:** Decision Engine, Preventive Maintenance Engine, Controlled Scheduler (candidates only).

**Reference:** [Platform Version History](platform-version-history.md), [Domain Engine](../07-business-architecture/domain-engine.md), [v2-phase-a-b.md](v2-phase-a-b.md) (historical sprint report)

---

## ✓ Version 2.0.1 — Security & Quality Hardening

**Objective:** Strengthen authentication, transport security, and deployment documentation.

**Business value:** Safer council deployments without changing operational workflows.

**Reference:** [v2-0-1-security-hardening.md](v2-0-1-security-hardening.md)

---

## ⬜ Version 2.1.0 — Dashboard & KPI

**Objective:** Operational visibility over inspection, decision, and preventive audit data.

**Business value:** Managers see trends without exporting raw data.

**Major capabilities:**

- **Sprint C1 (in progress):** Read-only Operations Intelligence KPI Engine (`GET /api/operations-intelligence/kpis`) — backend aggregation only; no dashboard UI
- **Sprint C2 (planned):** Web dashboard UI consuming the KPI API
- Summary dashboards, preventive execution KPIs, decision-engine metrics, department-scoped views

The KPI API is designed for reuse by the React web client, future Android application, and reporting exports.

---

## ⬜ Version 2.2.0 — Reporting & Export

**Objective:** Council-ready operational and compliance reporting.

**Business value:** Exportable reports for audit, asset management plans, and executive briefings.

**Major capabilities:** Scheduled reports, template-aligned inspection analytics, preventive compliance summaries.

---

## ⬜ Version 2.3.0 — Android Field Application

**Objective:** Native field client for Inspections and maintenance execution.

**Business value:** Field Employees use the same business rules as the web client.

**Major capabilities:** Inspection completion with template answers, work order execution, operational documents, push notifications.

---

## ⬜ Version 2.4.0 — Offline Synchronisation

**Objective:** Field operations with offline-capable sync.

**Business value:** Reliable field work in low-connectivity areas.

**Major capabilities:** Local cache, sync reconciliation (scope to be defined).

---

## ⬜ Version 2.5.0 — Asset Intelligence

**Objective:** Deeper asset knowledge beyond operational records.

**Business value:** Link operational history, templates, and preventive plans into asset-level insight.

**Major capabilities:** Asset health indicators, knowledge summaries, cross-workflow timelines (scope to be defined).

---

## ⬜ Version 2.6.0 — Inventory & Spare Parts

**Objective:** Parts and materials associated with maintenance work.

**Business value:** Trace consumption against work orders and assets.

**Major capabilities:** Stock items, issue to work order, low-stock awareness (scope to be defined).

---

## ⬜ Version 2.7.0 — Cost Management

**Objective:** Financial visibility on maintenance activity.

**Business value:** Support budget tracking and contractor cost reconciliation.

**Major capabilities:** Cost capture on maintenance, basic aggregation by asset/department (scope to be defined).

---

## ⬜ Version 2.8.0 — Multi-site Management

**Objective:** Support councils operating multiple sites or regions under one tenant.

**Business value:** Separate visibility and administration where organisational structure requires it.

**Major capabilities:** Site or region scoping on assets and users (scope to be defined).

---

## ⬜ Version 2.9.0 — Public API & Integrations

**Objective:** Documented integration surface for external systems.

**Business value:** GIS, asset registers, and council ERP can exchange data without custom coupling.

**Major capabilities:** Integration authentication, stable integration endpoints, webhook or event patterns (scope to be defined).

---

## ⬜ Version 3.0.0 — Decision Intelligence

**Objective:** Optional automation layer on human-in-the-loop foundations.

**Business value:** Reduce manual review for low-risk patterns while preserving override and audit.

**Major capabilities:** Configurable auto-execution policies, enhanced confidence models, policy governance (must not replace default Manager gate without explicit council opt-in).

---

## Principles across all versions

1. **Human-in-the-loop by default** — see [BDR-001](../03-architecture/bdr-001-human-in-the-loop-decision-engine.md) and [ADR-003](../03-architecture/adr-003-v2-domain-driven-workflow.md).
2. **Backend is authoritative** — business rules stay in the Spring Boot backend.
3. **One capability at a time** — vertical slices, no speculative frameworks.

---

## See also

- [Platform Version History](platform-version-history.md)
- [ADR-004 — Platform versioning strategy](../03-architecture/adr-004-platform-versioning-strategy.md)
- [Glossary](../01-business-architecture/glossary.md)
