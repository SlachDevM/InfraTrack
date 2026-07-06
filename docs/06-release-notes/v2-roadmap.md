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

**Status:** In progress — mobile backend (M4-BE), platform upgrade (DT-3), reporting security hardening, and frontend refactoring validated; Android scanning UI, printable labels, and offline sync remain planned. **V2.4 platform documentation baseline** consolidated in [v2.4.md](v2.4.md) (DOC-1).

**Delivered capabilities:**

### Mobile backend (M4)

- **Sprint M4-BE1 (validated):** Backend asset lookup — `GET /api/mobile/assets/lookup?code={assetCode}`. Stable asset business code (`asset.code`), scoped `AssetContextResponse` (asset summary, open issues, active inspections, active work orders, `allowedActions`), role/department authorization. Backend only.
- **Sprint M4-BE2 (validated):** QR code generation — `GET /api/assets/{assetId}/qr` returns PNG encoding `assetCode` only (ZXing, 512×512). Reuses `AssetAuthorizationService`.
- **Sprint M4-BE3 (validated):** Asset context enrichment — nullable `lastInspection`, `lastMaintenance`, `preventivePlan` on lookup response.
- **Sprint M4-BE4 (validated):** Asset documents context — `documents` array with `downloadUrl` for asset-owned operational documents.
- **Sprint M4-BE4.1 (validated):** Field employee/contractor document visibility fix for asset-owned documents on mobile context.

### Platform upgrade

- **Sprint DT-3 (validated):** Spring Boot **4.0.7** migration (Tomcat **11.0.22**, Spring Framework **7.0.8**). No business logic or API contract changes. See [security.md](../05-deployment/security.md#spring-boot-platform-v24x-dt-3).

### Reporting & frontend

- **Sprint Security-2.1 (validated):** Unified **Export** menu (`ExportReportingMenu`) on reporting list pages with CSV/XLSX/PDF format selection and default last-30-day date range.
- **Sprint Security-2 (validated):** Required `from`/`to` export parameters; 365-day maximum window enforced server-side.
- **Sprint Security-1 (validated):** CSV/XLSX spreadsheet formula injection protection.

### Security

- **Sprint DT-2A / DT-2A.1 (validated):** Production Content Security Policy on frontend nginx.
- **Sprint Security-3 (validated):** Disabled-user immediate JWT revocation via `UserAccountStatusService` (30-second cache, eviction on status change).
- **Authorization architecture guard (validated):** `AuthorizationArchitectureTest` enforces controller authorization dependencies.

### Technical debt

- **Frontend modularization (validated):** Assets, Work Orders, and Inspections pages decomposed into presentational components; shared constants modules.
- **Organizational Policy Engine (V2.3.x, validated):** BDR-004 foundations — Inspection Visibility, Notification, Dashboard, Reporting, and Approval policies.

**Planned within this version family (not yet delivered):**

- Printable asset labels (PDF)
- Android QR/barcode scanning UI consuming the M4-BE1 lookup endpoint
- Inspection/work-order/issue-linked documents on the asset context screen
- Full asset history on the asset context screen
- Local cache, sync reconciliation — architectural reference: [BDR-005](../03-architecture/bdr-005-offline-synchronization-architecture.md); implementation M5.1–M5.7 (see BDR §11)

**Reference:** [v2.4.md](v2.4.md), [BDR-005 — Offline & Synchronization Architecture](../03-architecture/bdr-005-offline-synchronization-architecture.md), [Mobile API](../04-api/mobile-api.md), [Reporting API](../04-api/reporting-api.md), [security.md](../05-deployment/security.md)

---

## ◐ Version 2.5.0 — Asset Intelligence & Mobile Offline Backend

**Objective:** Deeper asset knowledge beyond operational records, plus backend offline synchronization for Android.

**Business value:** Link operational history into asset-level insight; provide a stable sync contract for field offline work.

**Status:** In progress — DT-OFFLINE-1, V2.5-STAB-2, and V2.5-STAB-3 validated.

**Delivered capabilities:**

- **Sprint M5.2-BE1 (validated):** `POST /api/mobile/sync` — sync protocol DTOs and response envelope. Extension points: `SyncOperationProcessor`, `SyncTokenService`, `SyncConflictResolver`. No database changes.
- **Sprint M5.2-BE2 (validated):** Opaque `nextSyncToken`, `protocolVersion: 1`, `SyncDeltaResponse` envelope, typed operation/conflict/warning enums.
- **Sprint M5.3-BE (validated):** `SAVE_INSPECTION_PROGRESS` upload processing via `InspectionService.saveInspectionProgress`. Per-operation outcomes.
- **Sprint M5.4-BE (validated):** `delta.inspections` download — scoped inspection sync records with answers. Full delta on null/invalid token; incremental filter by `updatedAt` when token is valid. No tombstones; other delta sections empty.
- **Sprint M5.4.1-BE (validated):** Sync limits (100 operations, 256 KB payload), Micrometer metrics, structured logging, `SyncDiagnostics` helper. No new sync capabilities.
- **Sprint M5.5-BE1 (validated):** Conflict detection for `SAVE_INSPECTION_PROGRESS` — `CONFLICT` status plus `conflicts[]` with `SyncConflictType`. Metric `mobile.sync.operations.conflict`. No automatic resolution, tombstones, or Android changes.
- **Sprint M5.5-BE1.1 (validated):** Enriched conflict payload — `SyncConflictServerState`, `SyncConflictClientState`, `SyncResolutionHint` on `conflicts[]`. Detection-only; no merge or resolution.
- **Sprint M5.5-BE2 (validated):** Explicit conflict resolution — `POST /api/mobile/sync/conflicts/resolve` for `SAVE_INSPECTION_PROGRESS`. Stateless outcomes (`RESOLVED`, `RETRY_REQUIRED`, `MANUAL_REVIEW_REQUIRED`, `REJECTED`). No payload apply; no automatic merge.
- **Sprint DT-OFFLINE-1 (validated):** Protocol-level idempotency — `mobile_sync_operation` store; duplicate `operationId` returns stored outcome without handler execution. 90-day retention with scheduled cleanup. Prerequisite for future non-idempotent sync operations (work orders, issues).
- **Sprint V2.5-STAB-2 (validated):** Sync scalability prep — SQL-level incremental inspection delta filtering (`updatedAt >= syncToken.issuedAt`), batch answer loading for `delta.inspections`, indexes on `inspections.assigned_to_user_id` and `inspections.updated_at`. No API, Android, or sync protocol changes.
- **Sprint V2.5-STAB-3 (validated):** Production observability — authentication, sync, reporting, and endpoint metrics; enriched sync structured logging; Prometheus-friendly naming. No API, behaviour, or schema changes.

**Planned within this version family (not yet delivered):**

- Asset health indicators, knowledge summaries, cross-workflow timelines
- Automatic merge, tombstones, extended sync audit history, additional delta/upload types (M5.6+ backend / M5 Android)

**Reference:** [BDR-005](../03-architecture/bdr-005-offline-synchronization-architecture.md), [Mobile API](../04-api/mobile-api.md)

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

- [V2.4 release notes](v2.4.md)
- [Platform Version History](platform-version-history.md)
- [BDR-005 — Offline & Synchronization Architecture](../03-architecture/bdr-005-offline-synchronization-architecture.md)
- [Glossary](../01-business-architecture/glossary.md)
