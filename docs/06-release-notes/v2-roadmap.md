# InfraTrack Product Roadmap

Planned evolution of InfraTrack by **product version**. Delivered and validated versions are documented in [Platform Version History](platform-version-history.md).

**Legend:**

| Symbol | Meaning |
|--------|---------|
| ✓ | **Validated** — implemented and internally validated; not a production release unless stated |
| ◐ | **In Progress** — partial delivery; remaining scope on roadmap |
| ⬜ | **Planned** — not yet implemented |

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

**Status:** Validated baseline (current artifact version).

**Reference:** [v2-0-1-security-hardening.md](v2-0-1-security-hardening.md)

---

## ✓ Version 2.1.0 — Dashboard & Operations Intelligence

**Objective:** Operational visibility over inspection, decision, and preventive audit data.

**Business value:** Managers see trends and workload without exporting raw data manually.

**Status:** Validated — all foundation sprints complete.

**Delivered capabilities:**

- **Sprint C1 (validated):** Read-only Operations Intelligence KPI Engine (`GET /api/operations-intelligence/kpis`)
- **Sprint C2 (validated):** Web dashboard UI at `/dashboard` consuming the KPI API — read-only, no exports
- **Sprint C3 (validated):** Read-only trend time-series API (`GET /api/operations-intelligence/trends`) and dashboard trend widgets
- **Sprint C4 (validated):** Dashboard widget structure and read-only recent activity feed (`GET /api/operations-intelligence/recent-activity`)
- **Sprint C5 (validated):** User-scoped dashboard personalisation (`GET/PUT /api/dashboard/preferences`, reset endpoint)

The KPI API is designed for reuse by the React web client, future Android application, and CSV reporting exports.

---

## ✓ Version 2.2.x — Mobile API & CSV Reporting Foundation

**Objective:** Prepare backend and reporting capabilities for field and council use.

**Business value:** Future Android field client can consume compact APIs; councils can export operational data today.

**Status:** Validated foundation — M1 and R1 complete; XLSX exports added in V2.3.x C1; PDF exports added in V2.3.x C2.

**Delivered capabilities:**

- **Sprint M1 (validated):** Mobile API foundation — read/bundle endpoints under `/api/mobile/*` (identity, dashboard, my inspections/work orders, screen bundles)
- **Sprint R1 (validated):** CSV reporting foundation — read-only exports under `/api/reporting/exports/*.csv` (Assets, Inspections, Issues, Work Orders, Preventive Execution Candidates)
- **Sprint C1 (V2.3.x):** XLSX reporting foundation — read-only exports under `/api/reporting/exports/*.xlsx` with the same scoping and columns as CSV
- **Sprint C2 (V2.3.x):** PDF reporting foundation — read-only exports under `/api/reporting/exports/*.pdf` with the same scoping, filters, and columns as CSV/XLSX

**Planned within this version family (not yet delivered):**

- Scheduled reports
- Template-aligned inspection analytics
- Preventive compliance summaries

**Reference:** [Mobile API](../04-api/mobile-api.md), [Reporting API](../04-api/reporting-api.md)

---

## ⬜ Version 2.3.0 — Android Field Application

**Objective:** Native field client for Inspections and maintenance execution.

**Business value:** Field Employees use the same business rules as the web client.

**Major capabilities:** Inspection completion with template answers, work order execution, operational documents, push notifications.

---

## ◐ Version 2.4.0 — Offline Synchronisation & Mobile Asset Lookup

**Objective:** Field operations with offline-capable sync, plus QR/barcode-driven asset navigation.

**Business value:** Reliable field work in low-connectivity areas; scanning a physical asset tag opens its operational context instead of manual search.

**Status:** In progress — Sprints M4-BE1, M4-BE2, and M4-BE3 (backend only) validated; Android scanning UI, printable labels, and offline sync remain planned.

**Delivered capabilities:**

- **Sprint M4-BE1 (validated):** Backend asset lookup endpoint for QR/barcode navigation — `GET /api/mobile/assets/lookup?code={assetCode}`. Adds a stable asset business code (`asset.code`), a scoped `AssetContextResponse` (asset summary, open issues, active inspections, active work orders, backend-generated allowed actions), and role/department authorization reusing existing rules. Backend only — no Android scanner, no new workflows.
- **Sprint M4-BE2 (validated):** Backend QR code generation — `GET /api/assets/{assetId}/qr` returns a PNG QR encoding `assetCode` only (ZXing, 512×512, high error correction). Reuses `AssetAuthorizationService` for view authorization. No printable labels, batch export, or frontend integration.
- **Sprint M4-BE3 (validated):** Asset lookup response enrichment — adds nullable `lastInspection`, `lastMaintenance`, and `preventivePlan` sections to `AssetContextResponse` for field context after QR scan. Read-only; reuses M4-BE1 authorization. No Android, frontend, or workflow changes.

**Planned within this version family (not yet delivered):**

- Printable asset labels (PDF)
- Android QR/barcode scanning UI consuming the M4-BE1 lookup endpoint
- Asset documents and full history on the asset context screen
- Local cache, sync reconciliation (scope to be defined)

**Reference:** [Mobile API](../04-api/mobile-api.md)

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
