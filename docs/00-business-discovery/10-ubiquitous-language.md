# Ubiquitous Language

## Document Information

| Field    | Value               |
| -------- | ------------------- |
| Project  | InfraTrack          |
| Document | Ubiquitous Language |
| Version  | 1.0                 |
| Status   | Living Document     |
| Phase    | Business Discovery  |

---

# 1. Purpose

This document defines the official business vocabulary used throughout InfraTrack.

Its objective is to ensure that business stakeholders, developers, architects and technical documentation all describe the operational domain using the same terminology.

Each business concept has one official meaning.

Alternative wording should be avoided whenever possible.

---

# 2. Core Domain Concepts

## Asset

A physical infrastructure object owned or managed by the council.

Examples:

* Streetlight
* Playground
* Public Toilet
* BBQ
* Community Hall

An Asset is permanent.

Operational activities occur around the Asset.

---

## Department

The organisational unit responsible for managing a group of Assets.

Examples:

* Roads
* Parks
* Facilities
* Waste Management

---

## Business Trigger

The business event initiating operational attention for an Asset.

Examples:

* Scheduled Inspection
* Customer Request
* Emergency Event
* Manager Decision
* Field Observation
* Contractor Recommendation

Business Triggers explain **why** an inspection occurs.

---

## Inspection

The operational assessment of an Asset.

An Inspection records the current condition of an Asset.

It does not determine the operational response.

---

## Issue

A defect, hazard or abnormal condition identified during an Inspection.

Issues describe problems.

They are not maintenance activities.

---

## Operational Decision

The business decision made following an Inspection.

Made by a Manager or delegated authorised business role.

Operational Coordinators do not normally make Operational Decisions.

Possible outcomes include:

* Continue Monitoring
* Internal Maintenance
* Contractor Work
* Renewal Recommendation
* Decommission Recommendation

Operational Decisions determine what should happen next.

---

## Work Order

The formal authorisation and organisation of maintenance work.

A Work Order exists only after an Operational Decision requiring physical work.

---

## Maintenance Activity

The execution of maintenance work.

Maintenance Activities produce operational evidence.

---

## Completion Review

The administrative review of completed maintenance work when required by council policy.

Routine work may not require a Completion Review.

---

## Asset History

The permanent operational record associated with an Asset.

Asset History includes:

* inspections;
* issues;
* operational decisions;
* maintenance activities;
* photographs;
* documents;
* completion reviews.

---

# 3. Official Workflow Vocabulary

The standard operational vocabulary is:

```text
Business Trigger

↓

Inspection

↓

Issue

↓

Operational Decision

↓

Work Order

↓

Maintenance Activity

↓

Completion Review

↓

Asset History
```

This sequence represents the official business language of InfraTrack.

---

# 4. Preferred Terminology

| Preferred Term       | Use                            |
| -------------------- | ------------------------------ |
| Asset                | Physical infrastructure        |
| Department           | Organisational unit            |
| Business Trigger     | Reason operational work begins |
| Inspection           | Asset assessment               |
| Issue                | Identified defect              |
| Operational Decision | Business response              |
| Work Order           | Organised maintenance work     |
| Maintenance Activity | Executed work                  |
| Completion Review    | Administrative acceptance      |
| Asset History        | Permanent operational record   |

---

# 5. Discouraged Terminology

The following terms should generally be avoided because they are ambiguous or do not accurately reflect the business domain.

| Avoid      | Preferred                                            |
| ---------- | ---------------------------------------------------- |
| Ticket     | Business Trigger / Work Order (depending on context) |
| Task       | Work Order or Maintenance Activity                   |
| Job        | Work Order                                           |
| Request    | Business Trigger or Customer Request                 |
| Validation | Completion Review                                    |
| Equipment  | Asset                                                |
| Object     | Asset                                                |
| Item       | Asset                                                |

The preferred terminology should be used consistently across documentation, APIs and user interfaces.

---

# 6. Naming Principles

The same business concept should always use the same name.

For example:

* `Asset`
* `AssetHistory`
* `AssetService`
* `AssetController`
* `AssetRepository`

The same principle applies to every domain concept.

Avoid introducing synonyms within the codebase.

---

# 7. Future Evolution

This document is intended to evolve alongside the business domain.

When new concepts are introduced:

* they should receive an official definition;
* ambiguous terminology should be avoided;
* existing terminology should remain stable whenever possible.

Changes to this document should be considered significant architectural decisions.

---

# Summary

InfraTrack adopts a single, shared business vocabulary across the entire project.

Maintaining a consistent Ubiquitous Language improves communication, reduces ambiguity and ensures that business concepts remain aligned from analysis through implementation.

The language defined in this document becomes the authoritative reference for documentation, software development and future project evolution.
