# Architecture Decision Index

## Document Information

| Field    | Value                                      |
| -------- | ------------------------------------------ |
| Project  | InfraTrack                                 |
| Document | Architecture Decision Index                |
| Version  | 1.0                                        |
| Status   | Living Document                            |
| Audience | Developers, architects, contributors       |

---

## Introduction

InfraTrack maintains two kinds of decision records in `docs/03-architecture/`:

**Architecture Decision Records (ADRs)** document important **technical** choices — how the system is structured, persisted, secured, and versioned. They explain constraints that engineers must respect when implementing features.

**Business Decision Records (BDRs)** document important **product and domain** choices — how workflows behave, who approves what, and what the platform must never do automatically. They explain constraints that product owners and developers share when evolving capabilities.

Together, ADRs and BDRs explain **why InfraTrack is designed the way it is**. They are navigation aids and historical context — not replacements for the [Domain Engine](../07-business-architecture/domain-engine.md), [Business Capability Map](../01-business-architecture/business-capability-map.md), or live API documentation.

This index lists every current record with a short summary and link. Read the full document when you need rationale, consequences, or implementation guidance.

---

## Reading Order

A practical sequence for new contributors:

```text
Product Vision
        ↓
Business Capability Map
        ↓
Architecture Decision Index (this document)
        ↓
Business Architecture / Functional Use Cases
        ↓
Domain Engine
        ↓
API Consumer Guide
```

**Why this order:** [Product Vision](../00-product-vision.md) establishes purpose and principles. The [Business Capability Map](../01-business-architecture/business-capability-map.md) shows what the platform does today. This index explains **why** key design constraints exist before you dive into detailed business architecture. The [Domain Engine](../07-business-architecture/domain-engine.md) is authoritative for V2 behaviour; the [API Consumer Guide](../04-api/api-consumer-guide.md) shows how clients must interact with that behaviour. ADRs and BDRs are most valuable when read **alongside** those documents — they answer “why did we choose this?” not “what does the endpoint return?”.

For visual workflow context, see also [Workflow Sequence Diagrams](../02-system-blueprint/workflow-sequence-diagrams.md).

---

## Architecture Decision Records

| ADR | Title | Status | Summary |
|-----|-------|--------|---------|
| [ADR-001](ADR-001-asset-history-starts-with-registration.md) | Asset History Starts with Registration | Accepted | Asset registration (UC-001) creates the first `AssetHistoryEvent` in the same transaction, beginning permanent operational traceability at registration. |
| [ADR-002](ADR-002-inspection-produces-at-most-one-issue-v1.md) | Inspection Produces At Most One Issue in V1 | Accepted | Each Inspection may produce zero or one Issue in V1; a second Issue for the same Inspection is rejected with HTTP 409. |
| [ADR-003](adr-003-v2-domain-driven-workflow.md) | V2 Domain-Driven Workflow | Accepted | V2 organises capability into configuration, evaluation, recommendation, human validation, and execution layers — no layer may skip human validation for operational outcomes. |
| [ADR-004](adr-004-platform-versioning-strategy.md) | Platform Versioning Strategy | Accepted | Product-oriented semantic versioning describes what councils receive; major/minor/patch rules are defined in [Platform Version History](../06-release-notes/platform-version-history.md). |

---

## Business Decision Records

| BDR | Title | Status | Summary |
|-----|-------|--------|---------|
| [BDR-001](bdr-001-human-in-the-loop-decision-engine.md) | Human-in-the-Loop Decision Engine | Accepted | Decision Rules produce Suggested Actions only; Managers must approve before an Issue is created — no automatic Issues, Decisions, or Work Orders. |
| [BDR-002](bdr-002-preventive-candidates-before-automation.md) | Preventive Candidates Before Automation | Accepted | Preventive automation stops at Execution Candidate generation; the scheduler does not create Inspections — Manager approval is required. |
| [BDR-003](bdr-003-bearer-token-architecture.md) | Bearer Token Architecture | Accepted | JWT access tokens are sent via `Authorization: Bearer`; HttpOnly cookies are not used — intentional for SPA, API, and future Android clients. |
| [BDR-004](bdr-004-configurable-organizational-policies.md) | Configurable Organizational Policies | Accepted | Business rules remain stable; organizations configure operational policies (visibility, approvals, notifications) without redefining the domain. |
| [BDR-005](bdr-005-offline-synchronization-architecture.md) | Offline & Synchronization Architecture | Accepted | Backend remains source of truth; Android caches temporary working copies; deterministic sync with server-authoritative conflict resolution. Guides M5 implementation. |

---

## Relationship Between ADRs and BDRs

ADRs and BDRs operate at different levels but chain together:

```text
Business Decision (BDR)
        ↓
Architecture Decision (ADR)
        ↓
Implementation
```

A **BDR** states what the product must do from a governance or workflow perspective. An **ADR** states how the platform implements that constraint technically. Code and APIs must satisfy both.

**Example 1 — Human-in-the-loop**

- **BDR-001** decides that rule evaluation must not auto-create Issues.
- **ADR-003** structures V2 into layers so evaluation, recommendation, and execution remain separate.
- Implementation: `RuleEvaluationReport` and `SuggestedAction` at completion; Issue creation only on Manager approval.

**Example 2 — Preventive maintenance**

- **BDR-002** decides the scheduler generates candidates only, not Inspections.
- **ADR-003** places preventive planning in the recommendation layer with explicit human validation before execution.
- Implementation: `PreventiveExecutionCandidate`, disabled-by-default scheduler, approval before UC-003 assignment.

When in doubt: if the question is “should managers approve this?” — it is a BDR. If the question is “how do we version or structure this?” — it is an ADR.

---

## Evolution

ADRs and BDRs are **historical records**. Once **Accepted**, they should not normally be rewritten to reflect new reality — that erases the reasoning trail.

When circumstances change:

1. Accept that the old decision was correct **at the time**.
2. Create a **new** ADR or BDR that supersedes or amends the earlier record.
3. Mark the old record as **Superseded** (with a link to the replacement) if the project adopts that convention.

Minor clarifications (typos, broken links) are fine. Changing the substance of an Accepted decision in place is not.

---

## When to Create a New ADR

Create an ADR when a **technical** choice has long-term impact and alternatives were considered.

**Do create** for:

- Architecture or module structure changes
- Deployment or infrastructure strategy
- Authentication or authorization model changes
- Persistence, migration, or data-model strategy
- API versioning or breaking contract changes
- Cross-cutting technical standards (logging, observability, security patterns)

**Do not create** for:

- Ordinary feature implementation within existing patterns
- Refactors that do not change architectural constraints
- Bug fixes that restore intended behaviour
- Naming or formatting preferences

---

## When to Create a New BDR

Create a BDR when a **product or domain** choice changes how councils operate or what the platform promises.

**Do create** for:

- Workflow or lifecycle changes (new statuses, new approval gates)
- Governance or delegation rule changes
- Automation philosophy (what may run without human action)
- Fundamental product behaviour visible to users or auditors
- Cross-cutting business rules that outlive a single sprint

**Do not create** for:

- Bug fixes
- UI copy or cosmetic changes
- Implementation details already covered by an ADR
- Features that simply implement existing documented behaviour

---

## Current Status

| Category | Count |
|----------|------:|
| ADR | 4 |
| BDR | 4 |
| **Total** | **8** |

All records listed above are **Accepted** as of the last index update.

---

## Related Documentation

Read ADRs and BDRs together with:

| Document | Role |
| -------- | ---- |
| [Product Vision](../00-product-vision.md) | Product purpose and principles |
| [Business Capability Map](../01-business-architecture/business-capability-map.md) | What the platform can do today |
| [Business Architecture](../01-business-architecture/business-architecture.md) | Long-term business structure |
| [Domain Engine](../07-business-architecture/domain-engine.md) | Authoritative V2 business architecture |
| [API Consumer Guide](../04-api/api-consumer-guide.md) | How clients consume the API |
| [Workflow Sequence Diagrams](../02-system-blueprint/workflow-sequence-diagrams.md) | Visual workflow overview |
| [Platform Version History](../06-release-notes/platform-version-history.md) | Product version authority (ADR-004) |

ADRs explain **technical** constraints. BDRs explain **domain** constraints. Business architecture and the Domain Engine explain **behaviour**. The API Consumer Guide explains **consumption**. Use all four when designing or reviewing a change.
