# BDR-001 — Human-in-the-Loop Decision Engine

**Status:** Accepted  
**Date:** 2026  
**Context:** V2 Phase A — Decision Rules, Suggested Actions, Decision Assistant

---

## Decision

When a Decision Rule matches during Inspection completion, InfraTrack records the evaluation and generates a **Suggested Action**. It does **not** automatically create an Issue, Operational Decision, or Work Order.

A Manager must explicitly approve, reject, or dismiss each suggestion through the **Decision Assistant**.

---

## Rationale

Australian Local Government operational decisions require human accountability. Automated Issue creation from checklist answers would:

- bypass manager judgment on severity and context;
- create noise when rules misfire or answers are borderline;
- conflate **inspection evidence** with **operational decisions**.

Separating **evaluation** (what the rule matched) from **execution** (what the council does next) preserves audit clarity and aligns with the V1 principle that Managers make operational decisions.

---

## Consequences

- Rule Evaluation Reports provide a permanent audit trail of what was evaluated.
- Suggested Actions surface recommendations without side effects.
- Approval explicitly creates an Issue and links it to the suggestion and report.
- Future automation (A3.6) remains optional and must not weaken the default human gate.

---

## Related documentation

- [Domain Engine — Decision Assistant](../07-business-architecture/domain-engine.md)
- [BDR-002 — Preventive candidates before automation](bdr-002-preventive-candidates-before-automation.md)
