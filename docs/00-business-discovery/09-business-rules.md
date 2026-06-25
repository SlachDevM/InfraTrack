# Business Rules

## Document Information

| Field    | Value              |
| -------- | ------------------ |
| Project  | InfraTrack         |
| Document | Business Rules     |
| Version  | 1.0                |
| Status   | Draft              |
| Phase    | Business Discovery |

---

# 1. Purpose

This document defines the business rules governing the operational domain of InfraTrack.

Business Rules represent constraints that must remain true regardless of how the software is implemented.

These rules ensure that business processes remain consistent, traceable and aligned with council operational practices.

---

# 2. Asset Rules

### BR-001

Every Asset belongs to exactly one Department.

---

### BR-002

An Asset may experience many operational cycles throughout its lifetime.

---

### BR-003

Operational activities never change the identity of an Asset.

---

### BR-004

Every operational event associated with an Asset contributes to its permanent operational history.

---

# 3. Business Trigger Rules

### BR-005

Every Inspection must originate from a Business Trigger.

---

### BR-006

Business Triggers explain why operational attention is required.

They do not determine the operational response.

---

# 4. Inspection Rules

### BR-007

Every Inspection is associated with exactly one Asset.

---

### BR-008

An Inspection records observations.

It does not prescribe maintenance.

---

### BR-009

An Inspection may conclude without identifying any Issue.

---

### BR-010

If no Issue is identified, the Inspection may be closed immediately.

---

# 5. Issue Rules

### BR-011

Every Issue must originate from an Inspection.

---

### BR-012

An Issue represents an observation.

It is not a maintenance activity.

---

### BR-012a

An Inspection may produce at most one Issue in InfraTrack V1.

Multiple defects identified during the same Inspection should be described within that single Issue.

---

# 6. Operational Decision Rules

### BR-013

Operational Decisions are only required when an Issue has been identified.

---

### BR-014

Operational Decisions determine the appropriate operational response.

---

### BR-015

Different Issues may result in the same Operational Decision.

---

### BR-016

The same Issue may result in different Operational Decisions depending on operational context.

---

### BR-031

Operational Decisions are made by Managers or delegated authorised business roles.

---

### BR-032

Operational Coordinators coordinate execution after an approved Operational Decision.

They do not normally make Operational Decisions.

---

# 7. Work Order Rules

### BR-017

A Work Order may only exist after an Operational Decision.

---

### BR-018

A Work Order must be assigned to either:

* a Field Employee, or
* an external Contractor.

---

### BR-019

A Work Order exists to organise operational work.

It does not constitute proof that work has been completed.

---

### BR-020

Following an approved Operational Decision, Work Orders are coordinated by an Operational Coordinator or another authorised business role according to council policy.

---

# 8. Maintenance Rules

### BR-021

Every Maintenance Activity must be linked to a Work Order.

---

### BR-022

Maintenance Activities must produce operational evidence.

Examples include:

* photographs;
* notes;
* completion reports.

---

# 9. Completion Review Rules

### BR-023

Completion Reviews are only performed when required by organisational policy.

---

### BR-024

Routine maintenance may be closed without a Completion Review.

---

### BR-025

Higher-risk or contractor-performed work may require managerial review before closure.

---

# 10. Asset History Rules

### BR-026

Asset History is permanent.

Operational information must not be removed because work has been completed.

---

### BR-027

Operational History records both actions and decisions.

Understanding why work was performed is considered as important as understanding what work was performed.

---

# 11. Traceability Rules

### BR-028

Every operational decision must be attributable to a responsible person.

---

### BR-029

Every operational event must be associated with a date and time.

---

### BR-030

Operational evidence must remain linked to the corresponding Asset throughout its lifetime.

---

# Summary

Business Rules define the invariant principles governing InfraTrack.

They describe the operational constraints of the business domain independently from any technical implementation.

These rules form the foundation upon which functional specifications and software architecture will be built.
