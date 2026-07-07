# Operational Decisions

## Document Information

| Field    | Value                 |
| -------- | --------------------- |
| Project  | InfraTrack            |
| Document | Operational Decisions |
| Version  | 1.0                   |
| Status   | Accepted              |
| Phase    | Business Discovery    |

---

# 1. Purpose

This document defines how operational decisions are made following an asset inspection.

The objective of an inspection is not simply to identify issues.

Its primary purpose is to provide sufficient operational evidence to support an informed business decision.

Operational Decisions determine the appropriate response to the current condition of an asset.

---

# 2. Business Reality

Australian Local Governments continuously make operational decisions regarding public infrastructure.

These decisions balance multiple factors, including:

* public safety;
* asset condition;
* service continuity;
* operational priorities;
* available resources;
* maintenance costs;
* contractor availability;
* long-term asset strategy.

An inspection alone does not solve a problem.

It provides the evidence required to determine the most appropriate course of action.

---

# 3. InfraTrack Decision

InfraTrack explicitly separates:

* observation;
* decision;
* execution.

This distinction preserves the operational reasoning behind every maintenance activity.

Rather than automatically generating work, InfraTrack requires an operational decision whenever an issue has been identified.

---

# 4. Decision Flow

This flow describes only the decision stage.

The complete operational lifecycle is defined in:
02-domain-overview.md

```text
Inspection
        │
        ▼
Issue Identified?
        │
        ├────────────── No
        │
        │   Close Inspection
        │
        └────────────── Yes
                        │
                        ▼
             Operational Decision
                        │
      ┌─────────────────┼────────────────────────────────────────┐
      ▼                 ▼                ▼                       ▼
 Continue         Internal         Contractor            Asset Lifecycle
Monitoring       Maintenance       Work Order              Decision
                                                           │
                                                           ├── Renewal
                                                           └── Decommission
```

---

# 5. Decision Outcomes

Operational Decisions may result in one of the following outcomes.

## Continue Monitoring

The issue does not currently justify maintenance.

The asset remains in service and may be inspected again during a future inspection cycle.

---

## Internal Maintenance

The issue can be resolved by council field employees.

A Work Order is created and assigned internally.

---

## Contractor Work

The required work falls outside the council's operational capabilities.

A Work Order is assigned to an external contractor.

Operational responsibility remains with the council.

---

## Renewal Recommendation

The asset remains operational but should be considered for replacement during future capital works planning.

Renewal recommendations support long-term asset management.

They do not automatically initiate replacement.

---

## Decommission Recommendation

The asset should be permanently removed from service.

This recommendation requires further administrative processes outside the scope of InfraTrack.

---

# 6. Decision Factors

Operational Decisions should be based on objective operational evidence rather than subjective judgement.

Typical inputs and decision criteria include:

• Inspection findings
• Asset condition
• Safety risk
• Operational impact
• Previous maintenance history
• Frequency of recurring failures
• Regulatory obligations
• Available resources
• Estimated repair effort
• Maintenance cost
• Photographic evidence
• Temporary mitigation measures

The platform records the resulting decision but does not replace professional judgement.

---

# 7. Guiding Principles

## Evidence precedes decisions.

Operational decisions should always be supported by inspection evidence.

---

## Decisions precede execution.

Maintenance work should never begin without an operational decision.

---

## Different issues may produce identical decisions.

Different defects may require the same operational response.

---

## Identical issues may produce different decisions.

Operational context always matters.

The same defect may result in monitoring, maintenance or renewal depending on the asset's condition and history.

---

# 8. Relationship with Asset History

Operational Decisions form part of the permanent operational history of an asset.

Understanding why a decision was made is often as valuable as understanding what work was performed.

InfraTrack therefore preserves both the inspection evidence and the resulting operational decision.

---

# 9. Decision Example

```text
Customer reports broken playground swing.

↓

Business Trigger

↓

Inspection

↓

Issue identified

↓

Operational Decision

Safety Risk:
Medium

Public Impact:
High

Repair Complexity:
Low

Decision:
Internal Maintenance

Reason (optional):

The issue presents no immediate structural danger.

The council maintenance team possesses the required skills.

Replacement is unnecessary due to the asset's overall good condition.

Contractor involvement would provide no additional benefit.

↓

Work Order created

↓

Maintenance completed
```

---

# 10. Decision Responsibility

Operational Decisions are made by Managers or delegated authorised business roles.

Operational Coordinators do not normally make Operational Decisions.

They coordinate execution after an approved Operational Decision, including:

* assigning inspections;
* creating Work Orders following approved decisions;
* assigning Work Orders;
* coordinating Field Employees and Contractors.

Field Employees provide operational evidence through inspections but do not determine the final operational response unless explicitly authorised by council policy.

Managers decide.

Operational Coordinators coordinate.

---

# Summary

## Business Reality

Councils inspect assets in order to make informed operational decisions.

Maintenance is only one possible outcome.

Operational context, safety, cost and long-term planning all influence the final decision.

---

## InfraTrack Decision

InfraTrack models Operational Decisions as a distinct business concept separating observation from execution.

This approach preserves business reasoning, improves traceability and creates a more faithful representation of real-world council operations.
