# BDR-004 — Configurable Organizational Policies

**Status:** Accepted  
**Date:** 2026  
**Context:** V2 Platform Evolution — Operations Intelligence, Mobile API, Reporting, Dashboard Personalization, Security Hardening

---

## 1. Decision

InfraTrack maintains **one consistent business domain** for every customer.

Organizations configure **operational policies**.

Organizations do **not** redefine business rules.

Put simply:

> **Business rules are stable. Organizational policies are configurable.**

---

## 2. Problem

InfraTrack was intentionally designed around the needs of a **small Australian Local Government**. That philosophy remains correct: the default workflows, roles, and behaviours should remain optimized for small councils.

During V2 development, InfraTrack expanded into capability areas that are heavily affected by how different organizations choose to operate:

- Operations Intelligence and dashboard personalisation
- Mobile API foundations
- CSV reporting foundations
- Security hardening across authorization and evidence access

This evolution surfaced a recurring pattern: many requested behaviours are not changes to the business domain. They are **organizational choices**.

InfraTrack must support organizations of different sizes without:

- introducing customer-specific forks;
- changing the domain model per customer;
- embedding branching business logic in every service.

The platform direction becomes:

> Build for a small council first. Configure for everyone else.

---

## 3. Business Rules vs Organizational Policies

Business rules define the product. Organizational policies adapt the product to how a council operates.

| Category | Description | Examples |
|----------|-------------|----------|
| **Business Rules (stable)** | Defines the operational lifecycle and non-negotiable product behaviour. | Inspections create operational evidence; the Decision Engine proposes only; Suggested Actions always require human approval; the scheduler never performs automatic work; backend remains the source of truth; inspections can only be completed once; operational history is immutable. |
| **Organizational Policies (configurable)** | Defines how an organization chooses to apply the stable business domain. | Inspection visibility; work order visibility; dashboard defaults; notification strategy; approval chains; scheduler execution policy; reporting defaults; mobile presentation preferences; delegation policies. |

**Why the distinction matters**

- Business rules provide consistent auditability, training, and safety across customers.
- Policies reduce friction when a council has different staffing models, operational maturity, or governance requirements.
- Policies must never bypass the business rules they sit on top of.

---

## 4. Principles

- **Business before organization.** The business domain defines what InfraTrack is.
- **Stable business domain.** Customers share one domain model and one operational lifecycle.
- **Configurable organizational behaviour.** Organizations configure policies that adapt how the domain is applied.
- **Configuration never changes history.** Policy changes affect future behaviour only; historical operational data remains immutable.
- **Policies must never bypass business rules.** A policy may restrict behaviour further, but must not skip required validation or governance.
- **Policies must remain explainable.** Configuration must be understandable to managers and auditors (“why was this allowed?”).
- **Prefer additive configuration over branching business logic.** Avoid per-role/per-customer condition trees scattered through services.
- **Defaults optimized for small councils.** The default policy set remains the reference implementation and primary UX target.

---

## 5. First Planned Application

The first concrete implementation of this decision is:

**Inspection Visibility Policy**

The policy is implemented incrementally:

- **DEPARTMENT** (default, backwards compatible)
- **ORGANIZATION** (small-council view mode)

It remains a **view-only** policy: it does not grant assignment, completion, answer saving, approvals, or other mutations.

**Notification Policy** is the second organizational policy foundation. A `NotificationPolicy` abstraction and `DefaultNotificationPolicy` now govern whether operational notifications are sent. **Notification policy modes** (`DEFAULT`, `QUIET`) are configurable via `app.policies.notification.mode`; `DEFAULT` preserves the original fixed behaviour exactly.

**Dashboard Policy** is the third organizational policy foundation. A `DashboardPolicy` abstraction and `DefaultDashboardPolicy` now govern organizational dashboard presentation defaults (widget visibility, widget order, default trend range). `DashboardPreferencesService` uses `DashboardPolicyService.getPolicy()` when no user preferences are saved and when preferences are reset. Saved user preferences always override the organizational policy. No configurable modes, properties, persistence, or admin UI exist yet — `DefaultDashboardPolicy` preserves existing behaviour exactly.

Future dashboard policy modes may include `OPERATIONAL`, `FIELD`, `MANAGEMENT`, and `EXECUTIVE` (documentation only).

**Reporting Policy** is the fourth organizational policy foundation. A `ReportingPolicy` abstraction and `DefaultReportingPolicy` now govern organizational reporting defaults (default export format, enabled export formats, default reporting date range). `ReportingExportService` uses `ReportingPolicyService.getPolicy()` where reporting defaults are resolved. No user reporting preferences exist yet. No configurable modes, properties, persistence, or admin UI exist yet — `DefaultReportingPolicy` preserves existing behaviour exactly.

Future reporting policy modes may include `OPERATIONAL`, `MANAGEMENT`, `EXECUTIVE`, `CSV_ONLY`, and `FULL_EXPORT` (documentation only).

**Approval Policy** is the fifth organizational policy foundation. An `ApprovalPolicy` abstraction and `DefaultApprovalPolicy` now govern whether approval steps are required for completion review, manager operational decisions, suggested action approval, and preventive candidate approval. Integrated workflow services consult `ApprovalPolicyService.getPolicy()` at existing approval decision points. No configurable modes, properties, persistence, or admin UI exist yet — `DefaultApprovalPolicy` preserves existing behaviour exactly.

> **Architectural constraint (BDR-001 alignment):** Approval Policies may customize organizational approval behaviour but must **never bypass the mandatory human validation** defined by [BDR-001 — Human-in-the-Loop Decision Engine](bdr-001-human-in-the-loop-decision-engine.md) unless a future BDR explicitly supersedes that requirement. Policies may add stricter gates; they must not auto-create Issues, Operational Decisions, or Work Orders from rule evaluation.

Future approval policy modes may include `STANDARD`, `SIMPLIFIED`, and `STRICT` (documentation only).

Inspection visibility may eventually support modes such as:

- **Organization** (broader visibility)
- **Department** (department-scoped visibility)
- **Assigned Only** (strict assignment visibility)

BDR-004 defines the decision framework. It does not define the implementation details.

---

## 6. Long-Term Vision

Over time, InfraTrack may introduce a configuration module that supports organizational policies such as:

- Visibility Policies
- Approval Policies
- Notification Policies
- Dashboard Policies
- Scheduler Policies
- Reporting Policies
- Mobile Policies
- Delegation Policies

This configurability must preserve the same stable operational lifecycle and business domain, ensuring consistent Web and Android behaviour through shared backend policy enforcement.

---

## 7. Relationship with Existing Decisions

BDR-004 extends the existing architecture; it does not replace it.

- [BDR-001 — Human-in-the-Loop Decision Engine](bdr-001-human-in-the-loop-decision-engine.md) remains the default governance constraint; policies must not bypass human approval.
- [BDR-002 — Preventive Candidates Before Automation](bdr-002-preventive-candidates-before-automation.md) remains the default scheduler constraint; policies may configure discovery and visibility but must not introduce automatic execution.
- [BDR-003 — Bearer Token Architecture](bdr-003-bearer-token-architecture.md) remains the client authentication model; policies do not change authentication.
- [ADR-003 — V2 Domain-Driven Workflow](adr-003-v2-domain-driven-workflow.md) provides the layered architecture that makes policy configuration feasible without breaking the workflow model.
- [Product Vision](../00-product-vision.md) remains the authoritative statement of purpose and design principles.
- [Business Capability Map](../01-business-architecture/business-capability-map.md) remains the authoritative “what the platform does today”.
- [Domain Engine](../07-business-architecture/domain-engine.md) remains the authoritative description of V2 behaviour.

BDR-004 introduces a decision framework for future evolution:

> **If a behaviour derives from the business domain, it should remain fixed.**  
> **If a behaviour derives from how an organization chooses to operate, it should become a configurable organizational policy.**

---

## 8. Consequences

Expected benefits:

- InfraTrack scales from small councils to larger organizations without forks.
- The business domain remains stable, reducing training and audit complexity.
- APIs remain stable and workflows remain consistent.
- Authorization becomes cleaner as policies formalize visibility and access boundaries.
- A future configuration module becomes possible without redesigning the domain.
- Web and Android clients remain consistent through backend-enforced policies.

