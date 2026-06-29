# BDR-002 — Preventive Candidates Before Automation

**Status:** Accepted  
**Date:** 2026  
**Context:** V2 Phase B — Execution Candidates, Preventive Decision Assistant, Controlled Scheduler

---

## Decision

Preventive maintenance automation stops at **Execution Candidate** generation. Neither manual generation, nor the Controlled Preventive Scheduler, creates Inspections, Work Orders, or Maintenance Activities directly.

Managers review candidates through the **Preventive Decision Assistant**. Only explicit approval may create an Inspection (for `CREATE_INSPECTION` plans).

---

## Rationale

Preventive maintenance schedules express **intent** (what should eventually happen), not **authority** to dispatch work. Councils need to:

- defer work when resources are constrained;
- reject false-positive eligibility;
- assign the correct field employee and planned date at approval time.

A scheduler that created Inspections automatically would remove operational coordination, duplicate assignment decisions already owned by Managers, and blur the boundary between **planning** and **execution**.

Generating candidates — with audit reports and optional scheduled discovery — gives traceability without removing human control.

---

## Consequences

- `PreventiveExecutionCandidate` is the review queue; `PreventiveExecutionReport` audits its lifecycle.
- The scheduler is disabled by default and generates candidates only.
- `CREATE_WORK_ORDER` and `CREATE_MAINTENANCE` target actions remain unsupported until explicitly designed.
- METER and EVENT triggers remain deferred until business rules for those sources are defined.

---

## Related documentation

- [Domain Engine — Preventive Maintenance](../07-business-architecture/domain-engine.md)
- [BDR-001 — Human-in-the-loop decision engine](bdr-001-human-in-the-loop-decision-engine.md)
