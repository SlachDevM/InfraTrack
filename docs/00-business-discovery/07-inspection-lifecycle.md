# Inspection Lifecycle

## Document Information

| Field    | Value                |
| -------- | -------------------- |
| Project  | InfraTrack           |
| Document | Inspection Lifecycle |
| Version  | 1.0                  |
| Status   | Draft                |
| Phase    | Business Discovery   |

---

# 1. Purpose

This document describes how inspections operate within InfraTrack.

An inspection is the business activity used to assess the condition of an asset and determine whether further action is required.

Inspections do not automatically create maintenance work.

Their primary responsibility is to produce reliable operational evidence.

---

# 2. Business Reality

Australian councils inspect public assets for multiple reasons, including scheduled programs, public reports, emergencies, compliance obligations and field observations.

The result of an inspection may be:

* no issue found;
* monitoring required;
* minor maintenance required;
* formal work order required;
* contractor involvement required;
* renewal or decommissioning recommendation.

The inspection supports decision-making.

It is not the decision itself.

---

# 3. InfraTrack Decision

InfraTrack treats Inspection as the first operational assessment step after a Business Trigger.

Every inspection must be linked to:

* an asset;
* a business trigger;
* an inspector;
* a date and time;
* recorded observations.

The inspection determines whether an issue exists.

---

# 4. Inspection Segment of the Operational Lifecycle

This section only describes the inspection segment.

The complete operational lifecycle is defined in:
02-domain-overview.md

```text
Business Trigger
        │
        ▼
   Inspection
        │
        ▼
Issue Identified?
   │
   ├────────────── No
   │                 │
   │                 ▼
   │        Inspection Closed
   │                 │
   │                 ▼
   │      Asset History Updated
   │
   └────────────── Yes
                     │
                     ▼
               Issue Recorded
                     │
                     ▼
          Operational Decision
```

---

# 5. Inspection Evidence

An inspection may collect:

* condition rating;
* notes;
* photographs;
* GPS location;
* safety observations;
* visible defects;
* recommended follow-up;
* inspection report.

Inspection evidence becomes part of the permanent asset history.

---

# 6. No-Issue Outcome

When no issue is identified, the inspection can be closed without manager review.

The asset history is updated to show that the inspection occurred and no further action was required.

This avoids unnecessary administrative workload.

---

# 7. Issue Outcome

When an issue is identified, the inspection does not close the operational cycle.

Instead, the issue is recorded and an operational decision is required.

Possible decisions include:

* continue monitoring;
* minor internal maintenance;
* internal work order;
* contractor work order;
* renewal recommendation;
* decommission recommendation.

---

# 8. Guiding Principles

## Inspection records condition.

It does not automatically create work.

## No issue means closure.

A clean inspection should not require unnecessary administrative validation.

## Issues require decisions.

When a problem is found, the next step is an operational decision.

## Evidence matters.

Every inspection contributes to the asset’s operational history.

---

# Summary

An inspection is the council’s formal assessment of an asset’s condition.

InfraTrack uses inspections to transform field observations into structured evidence, enabling managers to make informed operational decisions when issues are found.
