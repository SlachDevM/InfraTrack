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

**Security & Quality Hardening**

Main improvements:

- Password policy improvements
- Activate-account rate limiting
- HSTS
- CORS hardening
- Documentation improvements
- Repository cleanup
- Security documentation
- Bearer Token architecture decision

No business functionality added.

**Reference:** [v2-0-1-security-hardening.md](v2-0-1-security-hardening.md)

---

## Future versions

The following are **roadmap milestones**. Scope and numbering may evolve. See [v2-roadmap.md](v2-roadmap.md).

### Version 2.1.0 — Dashboard & KPI

Operational visibility over inspection, decision, and preventive data.

### Version 2.2.0 — Reporting & Export

Council-ready operational and compliance reporting.

### Version 2.3.0 — Android Field Application

Native field client for inspections and maintenance execution.

### Version 2.4.0 — Offline Synchronisation

Field operations with offline-capable sync (scope to be defined).

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
