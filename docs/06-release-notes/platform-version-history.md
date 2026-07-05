# InfraTrack Platform Versions

Single source of truth for InfraTrack product versions from a **business perspective**.

For the versioning rules, see [ADR-004 — Platform Versioning Strategy](../03-architecture/adr-004-platform-versioning-strategy.md).

Historical sprint reports (for example [v2-phase-a-b.md](v2-phase-a-b.md)) remain unchanged as engineering records.

---

## Version 1.0.0

**Core CMMS**

Main capabilities:

- Asset Management
- Asset Categories
- Departments
- User Management
- Inspections
- Issues
- Operational Decisions
- Work Orders
- Maintenance Activities
- Completion Reviews
- Notifications
- Operational Documents
- Asset History

---

## Version 1.0.1

**Platform Hardening**

Main improvements:

- Security improvements
- Stability improvements
- Documentation consolidation
- Observability
- Logging
- Build improvements
- Code quality improvements
- Test coverage improvements

No business functionality added.

**Reference:** [v2-sprint0.md](v2-sprint0.md) (technical baseline sprint report)

---

## Version 2.0.0

**Inspection Intelligence & Preventive Maintenance**

Main capabilities:

### Inspection Intelligence

- Inspection Templates
- Template Questions
- Question Codes
- Structured Answers
- Choice Questions
- Unit Of Measure
- Decision Rules
- Rule Evaluation Engine
- Rule Evaluation Reports
- Suggested Actions
- Decision Assistant
- CAPA
- Rework Workflow

### Preventive Maintenance

- Preventive Maintenance Plans
- Business Trigger Definitions
- Trigger Evaluation Engine
- Execution Candidates
- Preventive Decision Assistant
- Execution Reports
- Controlled Scheduler

### Architecture improvements

- Human-in-the-loop decision engine
- Auditability
- Explainability
- Snapshot-based history

**Reference:** [v2-phase-a-b.md](v2-phase-a-b.md) (historical sprint report), [Domain Engine](../07-business-architecture/domain-engine.md)

### Logical engine versions (Version 2.0.0)

These are **logical engine versions**, independent of the platform semver. Engine evolution does not necessarily require a new platform major version.

| Engine | Version | Scope |
|--------|---------|--------|
| Decision Engine | 1.0 | Inspection Templates through Decision Assistant |
| Preventive Engine | 1.0 | Plans through Preventive Decision Assistant |
| Controlled Scheduler | 1.0 | Scheduled and manual candidate discovery |

---

## Version 2.0.1

**Security & Quality Hardening + V2 validation baseline**

Internally validated baseline — **not** a production release. Production deployment is planned for a later major version.

Main improvements:

- Authentication hardening
- Password policy
- Activation rate limiting
- HSTS / CORS hardening
- Repository cleanup
- V2 functional validation (Decision Engine, Preventive Engine, V1 regression smoke)
- UI recipe fixes
- Inspection Template lifecycle completion (publish / archive)

**Reference:** [v2-0-1-security-hardening.md](v2-0-1-security-hardening.md)

**Artifact version:** Maven and npm packages align with this validated release (`2.0.1`). See [ADR-004](../03-architecture/adr-004-platform-versioning-strategy.md).

---

## Version 2.1.0

**Dashboard & Operations Intelligence (Validated)**

Internally validated — **not** a production release.

Main capabilities:

- Operations KPI summary API (`GET /api/operations-intelligence/kpis`)
- Web dashboard at `/dashboard` (read-only)
- Trend time-series API (`GET /api/operations-intelligence/trends`)
- Recent activity feed (`GET /api/operations-intelligence/recent-activity`)
- User-scoped dashboard personalisation (`GET/PUT /api/dashboard/preferences`)

**Reference:** [v2-roadmap.md](v2-roadmap.md) (Sprints C1–C5)

---

## Version 2.2.x (foundation)

**Mobile API & CSV Reporting Foundation (Validated)**

Internally validated foundation — **not** a production release. Native Android client remains planned for Version 2.3.0.

Main capabilities:

### Mobile API (Sprint M1)

- Compact read and bundle endpoints under `/api/mobile/*`
- Identity, dashboard summary, assigned inspections and work orders
- Screen-oriented bundles for future field clients

### CSV Reporting (Sprint R1)

- Read-only exports under `/api/reporting/exports/*.csv`
- Assets, Inspections, Issues, Work Orders, Preventive Execution Candidates
- Department scoping consistent with Operations Intelligence

**Deferred:** scheduled reports, analytics summaries (see roadmap). XLSX and PDF exports added in V2.3.x; export menu and security hardening in V2.4.x.

**Reference:** [Mobile API](../04-api/mobile-api.md), [Reporting API](../04-api/reporting-api.md), [v2-roadmap.md](v2-roadmap.md)

---

## Version 2.3.x (foundation)

**Policy Engine & Extended Reporting (Validated)**

Internally validated foundation — **not** a production release.

Main capabilities:

### Organizational Policy Engine (BDR-004)

- Inspection Visibility Policy — first BDR-004 implementation
- Notification Policy — `NotificationPolicyService.getPolicy()`
- Dashboard Policy — `DashboardPolicyService.getPolicy()`
- Reporting Policy — `ReportingPolicyService.getPolicy()`
- Approval Policy — `ApprovalPolicyService.getPolicy()`

Default policies reproduce existing fixed behaviour exactly. No configurable admin UI yet.

### Extended reporting formats

- XLSX exports (`/api/reporting/exports/*.xlsx`) — Sprint C1
- PDF exports (`/api/reporting/exports/*.pdf`) — Sprint C2

**Reference:** [Domain Engine](../07-business-architecture/domain-engine.md), [BDR-004](../03-architecture/bdr-004-configurable-organizational-policies.md), [v2-roadmap.md](v2-roadmap.md)

---

## Version 2.4.x (partial)

**Mobile Asset Context, Platform Upgrade & Security Hardening (Validated — partial)**

Internally validated — **not** a production release. Offline sync and Android scanning UI remain planned.

Main capabilities:

### Mobile backend (M4-BE)

- Asset business code (`AST-XXXXXXXX`) and lookup endpoint
- QR code generation (`GET /api/assets/{assetId}/qr`)
- Asset context enrichment (last inspection, maintenance, preventive plan, documents)
- Backend-generated `allowedActions`

### Platform upgrade

- Spring Boot **4.0.7**, Tomcat **11.0.22**, Spring Framework **7.0.8**

### Reporting security

- Unified export menu with date range (default last 30 days)
- Required `from`/`to` parameters; 365-day maximum window
- CSV/XLSX formula injection protection

### Security hardening

- Frontend nginx Content Security Policy
- Disabled-user JWT revocation (30-second cache with immediate eviction)
- Authorization architecture guard

### Frontend refactoring

- Page modularization (Assets, Work Orders, Inspections)
- Shared constants and export handlers

**Artifact version:** Maven and npm packages remain `2.0.1` per ADR-004 until the next artifact bump sprint.

**Reference:** [v2.4.md](v2.4.md), [Mobile API](../04-api/mobile-api.md), [security.md](../05-deployment/security.md)

---

## Future versions

The following are **roadmap milestones** not yet delivered. Scope and numbering may evolve. See [v2-roadmap.md](v2-roadmap.md).

### Version 2.3.0 — Android Field Application

Native field client for inspections and maintenance execution.

### Version 2.4.0 — Offline Synchronisation & Mobile Asset Lookup

Field operations with offline-capable sync and QR/barcode-driven asset navigation. **Partially delivered** — mobile backend, platform upgrade, and security hardening validated; Android client and offline sync remain planned. See [v2.4.md](v2.4.md).

### Version 2.5.0 — Asset Intelligence

Deeper asset knowledge and cross-workflow insight.

### Version 2.6.0 — Inventory & Spare Parts

Parts and materials associated with maintenance work.

### Version 2.7.0 — Cost Management

Financial visibility on maintenance activity.

### Version 2.8.0 — Multi-site Management

Multiple sites or regions under one tenant.

### Version 2.9.0 — Public API & Integrations

Documented integration surface for external systems.

### Version 3.0.0 — Decision Intelligence

Optional automation layer on human-in-the-loop foundations.

---

## See also

- [V2 Roadmap](v2-roadmap.md) — planned capabilities by version
- [ADR-004](../03-architecture/adr-004-platform-versioning-strategy.md) — versioning strategy
- [Domain Engine](../07-business-architecture/domain-engine.md) — authoritative V2 business architecture
