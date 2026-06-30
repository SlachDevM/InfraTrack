# InfraTrack Business Glossary

Single source of truth for business terminology used in InfraTrack V2 documentation.

Terms follow [Ubiquitous Language](../00-business-discovery/10-ubiquitous-language.md) where defined. This glossary focuses on workflow placement and relationships — not implementation.

---

## Asset

A physical or logical item of public infrastructure managed by the council (for example, a pump, road segment, or building).

**Workflow:** Entry point for all operational activity. Assets are registered, inspected, maintained, and reviewed over their lifecycle.

**Related:** Asset Category, Business Trigger, Preventive Maintenance Plan, Asset History.

---

## Asset Category

A classification that groups similar assets (for example, Pumps, Roads, Buildings).

**Workflow:** Defines the scope for reusable inspection knowledge. Inspection Templates belong to an Asset Category.

**Related:** Asset, Inspection Template.

---

## Inspection

A field assignment to examine an Asset, record observations, and optionally identify an Issue.

**Workflow:** Follows a Business Trigger (scheduled or ad hoc). Completion may include structured answers from an Inspection Template. Inspections are the primary evidence-gathering step in the operational chain.

**Related:** Business Trigger, Inspection Template, Inspection Answer, Issue, Decision Engine.

---

## Inspection Template

Reusable checklist and rule definition for a category of assets. Defines *what* should be inspected, not a specific field visit.

**Workflow:** Configured by Administrators; referenced when assigning Inspections. Published templates drive structured answers and Decision Rules at completion.

**Related:** Asset Category, Inspection Template Question, Decision Rule.

---

## Inspection Template Question

One checklist item on an Inspection Template (for example, “Is there visible leakage?”).

**Workflow:** Answered during Inspection completion when a template is attached. Questions have a stable **Inspection Question Code** separate from display text.

**Related:** Inspection Answer, Decision Rule, Value Model.

---

## Inspection Answer

The recorded response to one Inspection Template Question during Inspection completion.

**Workflow:** Captured at completion time with snapshots of question text and code. Feeds Rule Evaluation when Decision Rules exist.

**Related:** Inspection, Rule Evaluation, Rule Evaluation Report.

---

## Inspection Question Code

A stable, immutable business identifier for a template question (for example, `VISIBLE_LEAK`).

**Workflow:** Used for reporting, integrations, and Decision Rules. Display text may change; the code does not.

**Related:** Inspection Template Question, Inspection Answer.

---

## Decision Rule

A condition on an Inspection Answer and the action to recommend when the condition matches (for example, suggest a high-severity Issue).

**Workflow:** Evaluated automatically when a templated Inspection is completed. Matching rules produce Suggested Actions — not automatic Issues.

**Related:** Rule Evaluation, Suggested Action, Decision Assistant.

---

## Rule Evaluation

The process of testing Decision Rules against Inspection Answers at completion time.

**Workflow:** Runs as part of Inspection completion when a template and answers exist. Results are persisted; no operational records are created by evaluation alone.

**Related:** Decision Rule, Rule Evaluation Report, Suggested Action.

---

## Rule Evaluation Report

An audit record of one rule evaluation run for an Inspection: which rules were tested, which matched, and what was compared.

**Workflow:** Created at Inspection completion. Supports explainability in the Decision Assistant and long-term traceability.

**Related:** Rule Evaluation, Suggested Action, Decision Assistant.

---

## Suggested Action

A system recommendation produced when a Decision Rule matches. Describes a proposed outcome (for example, create an Issue with a given severity) without executing it.

**Workflow:** Awaits Manager review in the Decision Assistant. Status progresses through approve, reject, or dismiss.

**Related:** Decision Assistant, Issue, Rule Evaluation Report.

---

## Decision Assistant

The Manager capability to review Suggested Actions: approve (optionally creating an Issue), reject, or dismiss.

**Workflow:** Human validation gate between rule recommendations and Issue Management. Implements human-in-the-loop for the Decision Engine.

**Related:** Suggested Action, Issue, Manager.

---

## Issue

A recorded problem or concern about an Asset, with severity and description.

**Workflow:** May be raised during Inspection completion (field employee) or created when a Manager approves a Suggested Action. Issues feed Operational Decisions.

**Related:** Issue Type, Operational Decision, CAPA.

---

## Issue Type

Classification of an Issue: **NORMAL** (standard finding) or **REWORK** (follow-up from a failed Completion Review).

**Workflow:** Rework Issues trace back to Completion Review and require a new Operational Decision path.

**Related:** Issue, Completion Review, Rework Issue.

---

## CAPA

Corrective and Preventive Action metadata attached to an Issue: corrective action, preventive action, and lessons learned notes.

**Workflow:** Supports continuous improvement and audit. Optional on Issues; may be updated after creation.

**Related:** Issue, Lessons Learned (field), Completion Review.

---

## Operational Decision

A Manager's formal decision on how to respond to an Issue (for example, schedule work, monitor, or accept risk).

**Workflow:** Bridges Issue Management and Work Order creation. Only Managers (or delegated authority) make Operational Decisions.

**Related:** Issue, Work Order, Manager.

---

## Work Order

Authorisation to perform physical maintenance work on an Asset, linked to an Operational Decision.

**Workflow:** Created by Operational Coordinators after a decision. Assigned to Field Employees or Contractors for execution.

**Related:** Maintenance Activity, Operational Coordinator.

---

## Maintenance Activity

Recorded completion of physical work against a Work Order.

**Workflow:** Performed by Field Employees or Contractors. Feeds Completion Review.

**Related:** Work Order, Completion Review.

---

## Completion Review

Manager validation of completed maintenance: accepted or rework required.

**Workflow:** Final quality gate in the operational chain. Rework required may create a Rework Issue.

**Related:** Maintenance Activity, Rework Issue, Manager.

---

## Rework Issue

An Issue of type REWORK created when a Completion Review requires further work.

**Workflow:** Starts a new Issue → Operational Decision path without reopening the completed Work Order.

**Related:** Issue Type, Completion Review, Operational Decision.

---

## Business Trigger

An operational event that initiates work on an Asset. In V1, triggers include scheduled inspections and reported problems.

**Workflow:** Precedes Inspection or other operational steps. Distinct from **Plan Business Trigger** (preventive configuration).

**Related:** Inspection, Preventive Maintenance Plan (plan-level trigger is separate).

---

## Preventive Maintenance Plan

A council-defined plan stating *when* preventive attention may be needed and *what* should eventually happen (for example, create an Inspection).

**Workflow:** Configured per Asset. ACTIVE plans may be evaluated for eligibility and generate Execution Candidates.

**Related:** Trigger Evaluation, Execution Candidate, Asset.

---

## Execution Candidate

A review queue item proposing that an ACTIVE preventive plan is eligible for action now.

**Workflow:** Created by manual generation or the Controlled Scheduler. Awaits Manager review in the Preventive Decision Assistant.

**Related:** Preventive Decision Assistant, Execution Report, Trigger Evaluation.

---

## Execution Report

The audit record of one Execution Candidate lifecycle: generated, approved, rejected, dismissed, or Inspection created.

**Workflow:** Created with the candidate; updated on Manager decisions. Read-only for traceability and future KPIs.

**Related:** Execution Candidate, Preventive Decision Assistant.

---

## Scheduler

The **Controlled Preventive Scheduler** that evaluates ACTIVE plans and generates Execution Candidates on a schedule or manual run.

**Workflow:** Discovery only — disabled by default. Does not approve candidates or create Inspections.

**Related:** Execution Candidate, Preventive Maintenance Plan, Trigger Evaluation.

---

## Department

An organisational unit within the council (for example, Water Services, Roads).

**Workflow:** Scopes assets and user access. Managers typically act within their own department.

**Related:** Manager, Operational Coordinator, Asset.

---

## Manager

Council officer who makes operational decisions, reviews suggestions and preventive candidates, and records completion reviews.

**Workflow:** Decision Assistant, Preventive Decision Assistant, Operational Decision, Completion Review.

**Related:** Operational Coordinator, Administrator.

---

## Operational Coordinator

Council officer who coordinates assignments: Inspections, Work Orders, and related operational tasks.

**Workflow:** Assigns field work; views preventive and template data; does not approve Suggested Actions or Execution Candidates.

**Related:** Field Employee, Work Order, Inspection.

---

## Field Employee

Council staff who perform Inspections and internal maintenance in the field.

**Workflow:** Completes Inspections, records Issues during inspections, executes assigned Work Orders.

**Related:** Contractor, Inspection, Maintenance Activity.

---

## Contractor

External party engaged to perform assigned maintenance work.

**Workflow:** Executes Work Orders; same maintenance completion path as Field Employees where applicable.

**Related:** Field Employee, Work Order.

---

## Administrator

Platform user who configures reference data, templates, preventive plans, and user accounts.

**Workflow:** Full configuration access; may review across departments where policy allows.

**Related:** Inspection Template, Preventive Maintenance Plan, Manager.

---

## See also

- [Platform Version History](../06-release-notes/platform-version-history.md) — product versions
- [Domain Engine](../07-business-architecture/domain-engine.md) — detailed V2 architecture
- [ADR-003 — V2 domain-driven workflow](../03-architecture/adr-003-v2-domain-driven-workflow.md) — how domains interact
- [ADR-004 — Platform versioning](../03-architecture/adr-004-platform-versioning-strategy.md) — version numbering
- [Ubiquitous Language](../00-business-discovery/10-ubiquitous-language.md) — discovery-era terminology
