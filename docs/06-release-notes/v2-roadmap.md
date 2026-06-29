# InfraTrack V2 Roadmap

High-level evolution of InfraTrack beyond V1.0.1. Detailed sprint documentation lives in [Domain Engine](../07-business-architecture/domain-engine.md) and [release notes](v2-phase-a-b.md).

**Legend:** ✓ Completed · ◐ In progress · ⬜ Future

---

## ✓ Sprint 0 — Platform hardening

**Objective:** Strengthen security, CI, deployment documentation, and code quality before functional V2 work.

**Business value:** Safer, reproducible foundation for council deployments and faster feature delivery.

**Major capabilities:** Secret externalisation, Swagger production lockdown, login rate limiting, unified CI, JaCoCo coverage, deployment guides.

**Reference:** [v2-sprint0.md](v2-sprint0.md)

---

## ✓ Phase A — Inspection Intelligence (Decision Engine)

**Objective:** Reusable inspection knowledge, rule evaluation, and manager-reviewed recommendations.

**Business value:** Consistent checklists, auditable rule outcomes, and decision support without removing Manager accountability.

**Major capabilities:** Inspection Templates, Questions, Answers, Decision Rules, Rule Evaluation Reports, Suggested Actions, Decision Assistant; rework/CAPA cross-cutting with V1.

**Reference:** [v2-phase-a-b.md](v2-phase-a-b.md), [Domain Engine — Phase A](../07-business-architecture/domain-engine.md)

---

## ✓ Phase B — Preventive Maintenance Engine

**Objective:** Plan-driven preventive work discovery with human approval before execution.

**Business value:** Proactive maintenance planning without unattended dispatch of field work.

**Major capabilities:** Preventive Maintenance Plans, Trigger Definitions, Trigger Evaluation, Execution Candidates, Preventive Decision Assistant, Execution Reports, Controlled Scheduler (disabled by default).

**Reference:** [v2-phase-a-b.md](v2-phase-a-b.md), [Domain Engine — Phase B](../07-business-architecture/domain-engine.md)

---

## ⬜ Phase C — Dashboard & KPI

**Objective:** Operational visibility over inspection, decision, and preventive audit data.

**Business value:** Managers see trends — approval rates, overdue preventive candidates, rule match frequency — without exporting raw data.

**Major capabilities:** Summary dashboards, preventive execution KPIs, decision-engine metrics, department-scoped views.

---

## ⬜ Phase D — Android Mobile

**Objective:** Native field client for Inspections and maintenance execution.

**Business value:** Field Employees work offline-capable with the same business rules as the web client.

**Major capabilities:** Inspection completion with template answers, work order execution, operational document access, push notifications.

---

## ⬜ Phase E — Asset Intelligence

**Objective:** Deeper asset knowledge beyond operational records.

**Business value:** Link operational history, templates, and preventive plans into asset-level insight for planning.

**Major capabilities:** Asset health indicators, knowledge summaries, cross-workflow timelines (scope to be defined).

---

## ⬜ Phase F — Reporting

**Objective:** Council-ready operational and compliance reporting.

**Business value:** Exportable reports for audit, asset management plans, and executive briefings.

**Major capabilities:** Scheduled reports, template-aligned inspection analytics, preventive compliance summaries.

---

## ⬜ Phase G — Inventory

**Objective:** Parts and materials associated with maintenance work.

**Business value:** Trace consumption against work orders and assets.

**Major capabilities:** Stock items, issue to work order, low-stock awareness (scope to be defined).

---

## ⬜ Phase H — Cost Management

**Objective:** Financial visibility on maintenance activity.

**Business value:** Support budget tracking and contractor cost reconciliation.

**Major capabilities:** Cost capture on maintenance, basic aggregation by asset/department (scope to be defined).

---

## ⬜ Phase I — Multi-site

**Objective:** Support councils operating multiple sites or regions under one tenant.

**Business value:** Separate visibility and administration where organisational structure requires it.

**Major capabilities:** Site or region scoping on assets and users (scope to be defined).

---

## ⬜ Phase J — Public API

**Objective:** Documented integration surface for external systems.

**Business value:** GIS, asset registers, and council ERP can exchange data without custom coupling.

**Major capabilities:** Integration authentication, stable integration endpoints, webhook or event patterns (scope to be defined).

---

## ⬜ Phase K — Decision Intelligence

**Objective:** Optional automation layer on top of human-in-the-loop foundations.

**Business value:** Reduce manual review for low-risk, high-confidence patterns while preserving override and audit.

**Major capabilities:** Configurable auto-execution policies, enhanced confidence models, policy governance (must not replace default Manager gate without explicit council opt-in).

---

## Principles across all phases

1. **Human-in-the-loop by default** — see [BDR-001](../03-architecture/bdr-001-human-in-the-loop-decision-engine.md) and [ADR-003](../03-architecture/adr-003-v2-domain-driven-workflow.md).
2. **Backend is authoritative** — business rules stay in the Spring Boot backend.
3. **One capability at a time** — vertical slices, no speculative frameworks.

---

## See also

- [Glossary](../01-business-architecture/glossary.md)
- [ADR-003 — V2 domain-driven workflow](../03-architecture/adr-003-v2-domain-driven-workflow.md)
