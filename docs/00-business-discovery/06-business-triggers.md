# Business Triggers

## Document Information

| Field    | Value              |
| -------- | ------------------ |
| Project  | InfraTrack         |
| Document | Business Triggers  |
| Version  | 1.0                |
| Status   | Accepted           |
| Phase    | Business Discovery |

---

# 1. Purpose

This document defines the business events that initiate operational activities within InfraTrack.

Operational work never starts without a business reason.

Understanding why an inspection or maintenance activity begins is essential for maintaining operational traceability and preserving the historical context of every public asset.

Business Triggers explain **why operational attention is required**.

They do not describe **how work will be performed**.

---

# 2. Business Reality

Australian Local Governments manage thousands of public assets over long service lives.

Operational work is initiated by a variety of internal and external events.

Although the source of an event may differ, each trigger requires the council to evaluate the condition of an asset before deciding what action, if any, should be taken.

Different triggers therefore converge into a common operational decision-making process.

---

# 3. InfraTrack Decision

InfraTrack does not model a generic "Request" entity.

Instead, it recognises that operational work may originate from different business triggers.

The trigger records **why** the operational process started.

The subsequent inspection determines **what** should happen next.

This distinction keeps the business model aligned with real operational practices.

---

# 4. Trigger Categories

InfraTrack recognises several categories of business triggers.

## Scheduled Trigger

Operational work initiated through planned activities.

Examples:

* routine inspections;
* preventive maintenance;
* compliance inspections;
* periodic condition assessments.

---

## Customer Trigger

Operational work initiated by the public.

Examples:

* damaged infrastructure;
* vandalism reports;
* safety concerns;
* service requests submitted by residents.

---

## Management Trigger

Operational work initiated internally.

Examples:

* manager request;
* departmental priority;
* asset review;
* operational planning.

---

## Field Trigger

Operational work initiated by field observations.

Examples:

* issue discovered during another inspection;
* deterioration noticed while performing unrelated work;
* recommendation from a field employee.

---

## Emergency Trigger

Operational work initiated by unexpected events.

Examples:

* storms;
* flooding;
* vehicle collisions;
* fallen trees;
* public safety incidents.

Emergency triggers generally require accelerated operational response.

---

## Contractor Trigger

Operational work initiated by external contractors.

Examples:

* recommendation following specialised maintenance;
* identification of additional defects;
* contractor safety observations.

Contractors may recommend further action but operational decisions always remain under council authority.

---

# 5. Relationship with the Operational Lifecycle

A Business Trigger starts operational attention.

It explains why an inspection is required, but it does not decide the outcome.

The complete operational lifecycle is defined in:
02-domain-overview.md

---

# 6. Operational Principles

## Every operational activity begins with a trigger.

No inspection should exist without an identifiable business reason.

---

## Triggers are historical evidence.

The reason an inspection occurred remains part of the permanent operational history of the asset.

---

## Different triggers may produce identical outcomes.

A customer complaint and a scheduled inspection may both result in the same maintenance activity.

The trigger differs.

The operational workflow remains consistent.

---

## Triggers never determine the solution.

Only the inspection and subsequent operational assessment determine the appropriate course of action.

---

# 7. Relationship with the Operational Lifecycle

Business Triggers do not replace inspections.

They precede them.

Their responsibility is limited to establishing operational context.

Once an inspection begins, all operational decisions follow the standard asset lifecycle defined elsewhere within the project.

---

# Summary

## Business Reality

Operational work within Australian Local Governments originates from many different business events, including planned activities, public requests, emergencies and internal operational decisions.

Although their origins differ, these events ultimately lead to a common inspection and decision-making process.

---

## InfraTrack Decision

InfraTrack models these events as Business Triggers rather than forcing every operational activity into a generic request model.

This approach better reflects operational reality, preserves historical context and keeps the business domain simple, explicit and traceable.
