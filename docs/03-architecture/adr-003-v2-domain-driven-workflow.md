# ADR-003 — V2 Domain-Driven Workflow

**Status:** Accepted  
**Date:** 2026  
**Context:** V2 Phase A+B — Domain Engine, Decision Engine, Preventive Maintenance Engine

---

## Decision

InfraTrack V2 organises business capability into **distinct layers** that separate configuration, evaluation, recommendation, human validation, and execution. No layer may skip human validation for outcomes that affect operational work (Issues, Inspections from preventive approval, Work Orders, Maintenance Activities).

---

## Operational workflow (V1 foundation)

The core asset operational lifecycle remains the authority for day-to-day council work:

```text
Assets
   │
   ▼
Inspection
   │
   ▼
Issue Management
   │
   ▼
Operational Decision
   │
   ▼
Work Order
   │
   ▼
Maintenance Activity
   │
   ▼
Completion Review
```

**Inspection Domain** covers assignment and completion. **Issue Management** records identified problems. **Operational Decision** is the Manager's formal response to an Issue. **Work Order** and **Maintenance Activity** execute physical work. **Completion Review** validates outcomes; rework may create a **Rework Issue** linked to the review.

This chain is unchanged by V2. V2 adds intelligence *before* and *beside* this chain — not a replacement for it.

---

## Decision Engine overlay (Phase A)

Structured inspection knowledge and rule evaluation sit between template configuration and Issue creation:

```text
Asset Category
   │
   ▼
Inspection Template → Questions → Answers
   │
   ▼
Decision Rules
   │
   ▼
Rule Evaluation → Rule Evaluation Report
   │
   ▼
Suggested Action
   │
   ▼
Decision Assistant (Manager)
   │
   ▼
Issue (optional, on approval only)
```

The Decision Engine does **not** replace field-employee Issue recording during Inspection completion. It adds a **manager-reviewed** path from structured checklist evidence to Issues when rules match.

---

## Preventive Maintenance flow (Phase B)

Preventive maintenance expresses *intent* separately from operational execution:

```text
Preventive Maintenance Plan
        │
        ▼
Plan Business Trigger / Trigger Definition
        │
        ▼
Trigger Evaluation
        │
        ▼
Execution Candidate
        │
        ▼
Preventive Decision Assistant (Manager)
        │
        ▼
Inspection (optional, on approval only)
```

The **Controlled Scheduler** may discover eligible plans and generate **Execution Candidates** only. It never approves candidates or creates Inspections directly.

```text
Scheduler → Trigger Evaluation → Execution Candidate
```

Each candidate has an **Execution Report** (audit trail) independent of the candidate queue status.

---

## Why separate configuration, evaluation, recommendation, validation, and execution

| Layer | Role | Example |
|-------|------|---------|
| **Configuration** | Define reusable business knowledge | Inspection Template, Decision Rule, Preventive Plan |
| **Evaluation** | Determine what conditions are met | Rule Evaluation, Trigger Evaluation |
| **Recommendation** | Surface proposed next steps without side effects | Suggested Action, Execution Candidate |
| **Human validation** | Manager accountability | Decision Assistant, Preventive Decision Assistant |
| **Execution** | Create operational records | Issue, Inspection, Work Order |

Mixing layers — for example, auto-creating Issues when a rule matches, or auto-creating Inspections when a plan is due — would:

- remove operational judgment at the point of dispatch;
- blur audit trails between *evidence* and *decisions*;
- make it harder for councils to defer, reject, or re-prioritise work.

InfraTrack intentionally keeps these layers explicit so each step is traceable and role-appropriate.

---

## Human-in-the-loop philosophy

```text
The system proposes.
The Manager decides.
```

This applies uniformly:

- **Decision Engine:** Rules evaluate answers; Managers approve Suggested Actions before Issues are created.
- **Preventive Engine:** Triggers evaluate plans; Managers approve Execution Candidates before Inspections are created.
- **Scheduler:** Automates *discovery* (candidates), not *decisions*.

Automation of execution without review is reserved for explicitly designed future phases and must not weaken the default gate. See [BDR-001](bdr-001-human-in-the-loop-decision-engine.md) and [BDR-002](bdr-002-preventive-candidates-before-automation.md).

---

## Relationship between the two V2 engines

| | Decision Engine | Preventive Maintenance Engine |
|--|-----------------|-------------------------------|
| **Starts from** | Inspection completion with template answers | ACTIVE preventive plan and trigger |
| **Produces** | Suggested Action | Execution Candidate |
| **Manager tool** | Decision Assistant | Preventive Decision Assistant |
| **May create** | Issue (on approval) | Inspection (on approval, `CREATE_INSPECTION` only) |
| **Audit artifact** | Rule Evaluation Report | Execution Report |

Both engines converge on the **Inspection** domain when approved preventive work requires a field inspection. The Decision Engine may additionally create **Issues** from inspection evidence. Neither engine creates Work Orders or Maintenance Activities directly in Phase A+B.

---

## Cross-references

| Record | Topic |
|--------|-------|
| [ADR-001](ADR-001-asset-history-starts-with-registration.md) | Asset history begins at registration |
| [ADR-002](ADR-002-inspection-produces-at-most-one-issue-v1.md) | V1 Inspection produces at most one Issue via field flow |
| [BDR-001](bdr-001-human-in-the-loop-decision-engine.md) | Rules suggest; Managers decide |
| [BDR-002](bdr-002-preventive-candidates-before-automation.md) | Scheduler generates candidates only |
| [Domain Engine](../07-business-architecture/domain-engine.md) | Detailed sprint documentation |
| [Glossary](../01-business-architecture/glossary.md) | Business terminology |
| [V2 Roadmap](../06-release-notes/v2-roadmap.md) | Phase evolution |

---

## Consequences

- New V2 capabilities must declare which layer they belong to.
- Features that skip human validation require an explicit ADR/BDR and business approval.
- Operational V1 workflows remain the execution backbone; V2 engines attach as propose-and-review overlays.
- Documentation, APIs, and UI should use consistent terms from the [Glossary](../01-business-architecture/glossary.md).
