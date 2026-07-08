# InfraTrack Product Roadmap

Planned evolution of InfraTrack by **product version**. Delivered and validated versions are documented in [Platform Version History](platform-version-history.md).

**Documentation baseline:** Platform capability **V2.6.x** (current); Android application **v1.3.0** (independent `v1.x` versioning). This roadmap is the **planning** view for platform capability; delivered history is authoritative in [Platform Version History](platform-version-history.md).

**Active development:** **M6.6** (V2.6.0 Work Order Offline family).

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
- **Sprint M1.x (validated):** Mobile API contract hardening — MockMvc contract tests, read-only regression tests, documented JSON examples for Android M2
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

**Status:** In progress — mobile backend (M4-BE), platform upgrade (DT-3), reporting security hardening, and frontend refactoring validated; Android scanning UI, printable labels, and remaining offline/Android integration planned. Historical DOC-1 consolidation: [v2.4.md](v2.4.md).

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
- **Sprint M5.4.2-BE (validated):** Inspection delta checklist definitions — embedded `template`, `questions`, and `choices` per inspection delta; batch-loaded; aligned with bundle endpoint mapping. `delta.inspections` self-contained for offline rendering. Additive `choiceId` on answers. No new endpoint; no Android changes.
- **Sprint RC-FIX-BE-1 (validated):** Release Candidate backend hardening — atomic sync idempotency (`PROCESSING` reservation before handler execution), synchronization watermark (delta + `nextSyncToken` share one server instant), inspection completion optimistic locking (`@Version`), `requiresFullSync` exposed when `FULL_SYNC_REQUIRED` warning is present. No API path changes; no Android/React changes.
- **Sprint RC-FIX-BE-2 (validated):** Final release polish — clearer HTTP 409 messages for rare database unique-constraint races; reporting export limits reviewed (365-day window + department scoping documented as sufficient); React global HTTP 401 handling (clear session, redirect to login with session-expired message); single-application-instance deployment note in security/deployment docs. No API contract, authentication protocol, or architectural behaviour changes.
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

## ◐ Version 2.6.0 — Work Order Offline

**Objective:** Extend the V2.5 mobile sync engine to support offline work order draft progress.

**Business value:** Field workers can queue maintenance notes while offline; server remains authoritative for completion.

**Status:** In progress — M6.1 through M6.5-BE1 delivered.

**Delivered capabilities:**

- **Sprint M6.1-BE1 (validated):** `SAVE_WORK_ORDER_PROGRESS` on `WORK_ORDER` through existing `POST /api/mobile/sync`. Payload: `SaveWorkOrderProgressRequest` (`completionNotes` draft only, max 4000 characters). Stored on `work_orders.draft_completion_notes`. Reuses V2.5 idempotency store, conflict classification/enrichment infrastructure, and sync metrics. No Android or React changes.
- **Sprint M6.1-BE2 (validated):** Work order delta download — `delta.workOrders` populated with scoped `SyncWorkOrderDeltaResponse` records (including `draftCompletionNotes` and `completionEligible`). Same sync token semantics as inspections: null/invalid token → full delta + optional `FULL_SYNC_REQUIRED`; valid token → SQL filter `updatedAt >= issuedAt` with watermark upper bound. Reuses existing `POST /api/mobile/sync`; no new endpoint. Offline completion sync (`COMPLETE_MAINTENANCE`) remains future work.
- **Sprint M6.2-BE1 (validated):** Dashboard sync contract — `delta.dashboard` populated on every successful sync with a server-computed `SyncDashboardDeltaResponse` snapshot matching `GET /api/mobile/dashboard` counters. Always returned (not token-incremental). Android must store and display the snapshot; must not recompute counters locally. No new endpoint; no dashboard business rule changes.
- **Sprint M6.3-BE1 (validated):** Asset context delta — `delta.assets` populated with compact `SyncAssetDeltaResponse` records for assets linked to scoped inspections/work orders in the same sync response. Reuses `MobileService.buildAssetContext` (aligned with `GET /api/mobile/assets/lookup`). Document metadata only; no binary sync; no public/citizen portal. No new endpoint.
- **Sprint M6.4-BE1 (validated):** Work order progress conflict detection — `SAVE_WORK_ORDER_PROGRESS` on `WORK_ORDER` classified via existing `SyncConflictType` taxonomy (`WORKFLOW_COMPLETED`, `ENTITY_DELETED`, `PERMISSION_DENIED`, `VERSION_MISMATCH`, `UNKNOWN`). Enriched `conflicts[]` payload mirrors inspection strategy (`serverState`, `clientState`, `resolutionHint`). No automatic resolution; no offline completion; Android retains conflicting work order pending operations. No new endpoint; no Android/React changes.
- **Sprint M6.5-BE1 (validated):** Reference data offline delta — `delta.referenceData` with `SyncReferenceDataDeltaResponse` on every successful sync. Asset categories, departments, work order types, and mobile enum dictionaries with server-authoritative labels. Always-returned full snapshot (`generatedAt` = watermark, `schemaVersion` = 1). Reuses `AssetCategoryService` / `DepartmentService.listAll`. No offline mutation; no users/policies/documents/citizen options. No new endpoint; no Android/React changes.
- **Sprint M6.5-STAB-1 (validated):** Sync performance and observability — per-section delta counters and generation timers, response size distribution, enriched structured logging for all delta sections. Performance review of inspection/work order/asset/reference/dashboard delta builders; no new indexes required. No API, behaviour, or schema changes.
- **Sprint M6.5-STAB-2 (validated):** Full mobile sync integration tests — PostgreSQL/Testcontainers end-to-end coverage of `POST /api/mobile/sync` (first sync envelope, mixed pending operations, invalid token/full sync, idempotency replay, conflict batching, role scoping). No production code changes.
- **Sprint M6.5-STAB-3 (validated):** Backend query performance and scalability audit — reviewed sync, reporting, operational documents, and preventive/decision query paths. Safe composite indexes added for mobile sync scoping and asset-context lookups (`V35__sync_and_asset_query_indexes.sql`). `@EntityGraph` aligned on work-order assigned-user queries. No API, behaviour, Android, or React changes.
- **Sprint M6.5-STAB-3-FIX (validated):** Backend performance hardening — bulk `buildAssetContextsForSync` repository access, SQL open-issues filter, configurable reporting export row guard (`reporting.export.max-rows`), field-employee operational document SQL pagination, XLSX auto-size threshold, and inclusive issue export date alignment. No API, behaviour, Android, or React changes.

**Planned within this version family (not yet delivered):**

- **M6.6** — next milestone (active development)
- Queued maintenance completion sync (`COMPLETE_MAINTENANCE`)
- Extended conflict resolution endpoint scope for work orders

**Reference:** [BDR-005](../03-architecture/bdr-005-offline-synchronization-architecture.md), [Mobile API](../04-api/mobile-api.md)

---

## ⬜ Version 2.7.0 — Inventory & Spare Parts

**Objective:** Parts and materials associated with maintenance work.

**Business value:** Trace consumption against work orders and assets.

**Major capabilities:** Stock items, issue to work order, low-stock awareness (scope to be defined).

---

## ⬜ Version 2.8.0 — Cost Management

**Objective:** Financial visibility on maintenance activity.

**Business value:** Support budget tracking and contractor cost reconciliation.

**Major capabilities:** Cost capture on maintenance, basic aggregation by asset/department (scope to be defined).

---

## ⬜ Version 2.9.0 — Multi-site Management

**Objective:** Support councils operating multiple sites or regions under one tenant.

**Business value:** Separate visibility and administration where organisational structure requires it.

**Major capabilities:** Site or region scoping on assets and users (scope to be defined).

---

## ⬜ Version 2.10.0 — Public API & Integrations

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

- [Platform Version History](platform-version-history.md) — delivered capability history (authoritative)
- [BDR-005 — Offline & Synchronization Architecture](../03-architecture/bdr-005-offline-synchronization-architecture.md)
- [Glossary](../01-business-architecture/glossary.md)
