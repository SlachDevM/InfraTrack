# ADR-004 — Platform Versioning Strategy

**Status:** Accepted  
**Date:** 2026  
**Context:** V2.0.2 — Versioning & Platform Identity

---

## Decision

InfraTrack adopts a **product-oriented semantic versioning** model for platform releases. Version numbers describe **what the product delivers to councils**, not internal sprint or phase names.

The authoritative version history is [Platform Version History](../06-release-notes/platform-version-history.md).

---

## Semantic versioning rules

| Level | Meaning | When to increment |
|-------|---------|-------------------|
| **MAJOR** (`X.0.0`) | New business domain or major platform capability | Inspection Intelligence, Preventive Maintenance, Android platform, Decision Intelligence |
| **MINOR** (`2.x.0`) | Significant capability within an existing platform generation | Dashboard, Reporting, Inventory, Offline mode |
| **PATCH** (`2.0.x`) | Security, performance, documentation, bug fixes, hardening | No new major business capability |

### MAJOR — examples

- **2.0.0** — Inspection Intelligence and Preventive Maintenance (Domain Engine)
- **2.3.0** — Android Field Application (new client platform)
- **3.0.0** — Decision Intelligence (new automation domain)

### MINOR — examples

- **2.1.0** — Dashboard & KPI
- **2.2.0** — Reporting & Export
- **2.6.0** — Inventory & Spare Parts

### PATCH — examples

- **1.0.1** — Platform hardening after V1 core delivery
- **2.0.1** — Security and quality hardening after Version 2.0.0

---

## Logical engine versions

The **Decision Engine**, **Preventive Engine**, and **Controlled Scheduler** each carry a **logical engine version** (currently 1.0) documented in [Platform Version History](../06-release-notes/platform-version-history.md).

Engine versions track capability evolution within a domain. A minor engine release (for example Decision Engine 1.1) does **not** automatically require a new platform major version unless it introduces a new business domain or platform-wide capability.

---

## Why product versions over sprint/phase naming

Internal names such as “Phase A”, “Phase B”, or “Sprint B4” are useful for **engineering delivery** but are opaque to:

- Council stakeholders and product owners
- Release notes and procurement documentation
- Contributors joining after a milestone shipped

Product semver answers: *“What business capability does this release add?”*

Historical sprint reports (for example [v2-phase-a-b.md](../06-release-notes/v2-phase-a-b.md)) are **preserved unchanged** as delivery records. Navigation and release documentation refer to **Version 2.0.0** when describing delivered functionality.

---

## Documentation conventions

| Context | Preferred reference |
|---------|---------------------|
| Delivered product capability | Version 2.0.0, Version 2.0.1 |
| Future planning | Version 2.1.0+ (see roadmap) |
| Historical engineering record | Phase A, Sprint B4 (inside sprint reports only) |
| Domain implementation detail | Domain Engine sprint sections (A2.x, B3, etc.) |

---

## Related

- [Platform Version History](../06-release-notes/platform-version-history.md)
- [V2 Roadmap](../06-release-notes/v2-roadmap.md)
- [ADR-003 — V2 domain-driven workflow](adr-003-v2-domain-driven-workflow.md)
