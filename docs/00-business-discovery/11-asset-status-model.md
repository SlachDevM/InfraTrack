# Asset Status Model

## Document Information

| Field    | Value              |
| -------- | ------------------ |
| Project  | InfraTrack         |
| Document | Asset Status Model |
| Version  | 1.0                |
| Status   | Draft              |
| Phase    | Business Discovery |

---

# 1. Purpose

This document defines the operational status model used for Assets within InfraTrack.

Its objective is to clarify what an Asset status represents, how it differs from physical condition, and which transitions are allowed throughout the operational lifecycle of an Asset.

Asset status is a business concept.

It must not be confused with inspection results, physical condition ratings or maintenance workflow states.

---

# 2. Business Reality

Public assets may remain in service even when they show signs of deterioration.

For example, a park bench may require repainting but still remain safe and usable.

Likewise, an asset may be physically damaged but only partially restricted rather than fully removed from service.

Councils therefore need to distinguish between:

* the observed condition of an asset;
* the operational availability of that asset to the public.

These concepts are related but not identical.

---

# 3. InfraTrack Decision

InfraTrack separates **Physical Condition** from **Operational Status**.

## Physical Condition

Physical Condition describes the observed state of the asset during an inspection.

Examples:

* Excellent
* Good
* Fair
* Poor
* Critical

Physical Condition is recorded through inspections.

---

## Operational Status

Operational Status describes whether the asset is currently available for use.

Examples:

* Active
* Limited Service
* Out of Service
* Decommissioned

Operational Status is changed through an operational decision, not directly by an inspection.

---

# 4. Official Asset Statuses

InfraTrack uses four official Asset statuses.

## Active

The asset is available for normal public or operational use.

Minor defects may exist, but they do not materially affect availability, safety or service delivery.

Examples:

* a public BBQ with cosmetic wear;
* a streetlight working normally;
* a park bench requiring repainting.

---

## Limited Service

The asset remains available but with reduced functionality, restricted use or increased monitoring.

This status indicates that the council is aware of an issue but has not fully removed the asset from service.

Examples:

* one tap unavailable in a public toilet block;
* one swing removed from a playground while the rest remains open;
* a sports facility partially closed.

---

## Out of Service

The asset is temporarily unavailable for public or operational use.

This status is used when the asset should not be used until corrective action has been completed.

Examples:

* unsafe playground equipment;
* damaged public toilet block closed for repair;
* electrical asset requiring urgent isolation.

---

## Decommissioned

The asset has been permanently removed from operational service.

This status indicates that the asset is no longer part of the active operational asset base.

Examples:

* a playground removed permanently;
* a public BBQ removed and not replaced;
* obsolete infrastructure retired from service.

---

# 5. Status Transition Model

Asset status transitions must be intentional.

An inspection may recommend a status change, but the status itself is changed through an Operational Decision.

```text
Active
   │
   ├──► Limited Service
   │
   ├──► Out of Service
   │
   └──► Decommissioned
   

Limited Service
   │
   ├──► Active
   │
   ├──► Out of Service
   │
   └──► Decommissioned
   

Out of Service
   │
   ├──► Active
   │
   ├──► Limited Service
   │
   └──► Decommissioned
   

Decommissioned
   │
   └──► No standard transition back
```

---

# 6. Transition Rules

## Active to Limited Service

Allowed when an issue reduces functionality but does not require full removal from service.

Example:

A playground remains open but one piece of equipment is restricted.

---

## Active to Out of Service

Allowed when continued use creates unacceptable safety, operational or compliance risk.

Example:

A damaged public toilet block is closed until repair.

---

## Active to Decommissioned

Allowed when the asset is permanently retired.

This transition should normally require managerial decision and supporting evidence.

---

## Limited Service to Active

Allowed when corrective action or further assessment confirms that normal service can resume.

---

## Limited Service to Out of Service

Allowed when the issue worsens or risk assessment determines that partial service is no longer acceptable.

---

## Limited Service to Decommissioned

Allowed when the asset is no longer worth maintaining or is permanently withdrawn from service.

---

## Out of Service to Active

Allowed when corrective action has been completed and the asset can safely return to normal service.

---

## Out of Service to Limited Service

Allowed when partial service can resume but restrictions remain.

---

## Out of Service to Decommissioned

Allowed when the asset will not return to service.

---

## Decommissioned to Active

Not allowed as a standard operational transition.

If a previously decommissioned asset must return to service, this should normally be treated as a new asset registration or a special administrative correction.

---

# 7. Relationship with Inspection

Inspection records observed condition.

Inspection does not directly change Asset status.

An Inspection may identify evidence suggesting that an Asset status should change.

The status change occurs only after an Operational Decision.

```text
Inspection

↓

Condition Observed

↓

Issue Recorded

↓

Operational Decision

↓

Asset Status Updated
```

This preserves the distinction between observation and decision.

---

# 8. Relationship with Operational Decision

Operational Decisions may result in an Asset status change.

Examples:

| Operational Decision        | Possible Status Impact              |
| --------------------------- | ----------------------------------- |
| Continue Monitoring         | No status change                    |
| Internal Maintenance        | No status change or Limited Service |
| Contractor Work Order       | Limited Service or Out of Service   |
| Renewal Recommendation      | Usually no immediate status change  |
| Decommission Recommendation | Decommissioned after approval       |

Status changes should always be justified by operational reasoning.

---

# 9. Relationship with Work Orders

Work Orders may be created while an Asset is in any non-decommissioned status.

However, status affects operational priority.

Examples:

* Active assets may receive routine work orders.
* Limited Service assets may require prioritised maintenance.
* Out of Service assets usually require urgent corrective action.
* Decommissioned assets should not receive normal maintenance work orders.

---

# 10. Relationship with Asset History

Every Asset status change must be recorded in Asset History.

The history should preserve:

* previous status;
* new status;
* date and time;
* responsible user;
* business reason;
* related Inspection or Operational Decision where applicable.

Status changes are business events.

They are never silent technical updates.

---

# 11. Business Rules

### AS-001

Asset Status represents operational availability, not physical condition.

---

### AS-002

Physical Condition is recorded during inspections.

---

### AS-003

Asset Status changes only through an Operational Decision or authorised administrative correction.

---

### AS-004

An Inspection may recommend a status change but must not directly change Asset Status.

---

### AS-005

Every Asset Status change must be recorded in Asset History.

---

### AS-006

Decommissioned Assets cannot receive standard operational work orders.

---

### AS-007

A Decommissioned Asset cannot return to Active through a standard operational transition.

---

### AS-008

Out of Service Assets should remain visible in the system for operational and historical traceability.

---

# 12. Guiding Principles

## Condition is observed.

Inspection describes what was found.

---

## Status is decided.

Operational Decision determines the business impact.

---

## History is preserved.

Every status transition becomes part of the permanent Asset History.

---

## Decommissioning is final.

Removing an Asset from service is a significant business decision and should not be treated like a temporary status change.

---

# Summary

Asset Status represents the operational availability of an Asset.

It is distinct from Physical Condition and must only change through intentional business decision-making.

This separation preserves the core InfraTrack philosophy:

Observation

↓

Decision

↓

Action

↓

History
