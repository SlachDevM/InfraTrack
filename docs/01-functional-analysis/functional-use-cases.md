# Functional Use Cases

## Document Information

| Field    | Value                |
| -------- | -------------------- |
| Project  | InfraTrack           |
| Document | Functional Use Cases |
| Version  | 1.0                  |
| Status   | Draft                |
| Phase    | Functional Analysis  |

---

# 1. Purpose

This document defines the first functional Use Cases for InfraTrack.

The objective is to translate the Business Discovery documentation into actionable functional specifications.

Business Discovery explains what the domain means.

Use Cases explain how users interact with the domain.

This document does not define technical architecture, database design, REST endpoints or user interface details.

---

# 2. Functional Analysis Principles

InfraTrack Use Cases must respect the approved Business Discovery model.

Each Use Case should describe one complete business capability.

A Use Case should include:

* primary actor;
* supporting actors;
* preconditions;
* main flow;
* alternative flows;
* postconditions;
* related business rules.

Use Cases should remain technology-agnostic until the architecture phase.

---

# 3. Use Case Catalogue

| ID     | Use Case                            | Status   |
| ------ | ----------------------------------- | -------- |
| UC-001 | Register Asset                      | Detailed |
| UC-002 | Create Business Trigger             | Detailed |
| UC-003 | Assign Inspection                   | Detailed |
| UC-004 | Perform Inspection                  | Listed   |
| UC-005 | Record Issue                        | Listed   |
| UC-006 | Make Operational Decision           | Listed   |
| UC-007 | Create Work Order                   | Listed   |
| UC-008 | Assign Work Order                   | Listed   |
| UC-009 | Complete Maintenance Activity       | Listed   |
| UC-010 | Complete Review                     | Listed   |
| UC-011 | View Asset History                  | Listed   |
| UC-012 | Upload Operational Document         | Listed   |
| UC-013 | Send Notification                   | Listed   |
| UC-014 | Manage Departments                  | Listed   |
| UC-015 | Delegate Cross-Department Authority | Listed   |

---

# 4. UC-001 — Register Asset

## Purpose

Register a new public infrastructure Asset within InfraTrack.

This Use Case creates the initial business identity of an Asset and makes it available for future inspections, maintenance activities and operational history tracking.

---

## Primary Actor

Operational Coordinator

---

## Supporting Actors

* Manager;
* Administrator.

---

## Preconditions

* The actor is authenticated.
* The actor has permission to register Assets.
* The owning department exists in the system.
* The Asset category exists in the system.

---

## Main Flow

1. The actor starts the asset registration process.
2. The actor provides the required Asset information.
3. The actor selects the owning department.
4. The actor selects the Asset category.
5. The actor provides the Asset location.
6. The actor optionally attaches initial supporting documents or photographs.
7. InfraTrack validates the submitted information.
8. InfraTrack creates the Asset.
9. InfraTrack records the Asset creation in Asset History.
10. InfraTrack confirms that the Asset has been registered.

---

## Required Information

Typical required information includes:

* Asset name or label;
* Asset category;
* owning department;
* location;
* operational status;
* registration date.

Additional information may be collected depending on the Asset category.

---

## Alternative Flows

## Missing Required Information

If required information is missing, InfraTrack rejects the registration and explains which information must be provided.

---

## Duplicate Asset Suspected

If InfraTrack detects a possible duplicate Asset, the actor is warned before registration continues.

The actor may cancel the registration or confirm that the Asset is distinct.

---

## Invalid Owning Department

If the selected owning department is not valid, InfraTrack rejects the registration.

---

## Postconditions

* A new Asset exists in InfraTrack.
* The Asset belongs to one owning department.
* The Asset is available for future operational activity.
* The Asset creation is recorded in Asset History.

---

## Business Rules

* Every Asset must have exactly one owning department.
* Asset registration must preserve long-term operational traceability.
* Asset History begins when the Asset is registered.
* Operational documents must be linked to the Asset or to an operational event.

---

# 5. UC-002 — Create Business Trigger

## Purpose

Create a Business Trigger explaining why operational attention is required for an Asset.

A Business Trigger starts operational attention but does not determine what work will be performed.

---

## Primary Actor

Operational Coordinator

---

## Supporting Actors

* Manager;
* Field Employee;
* Customer request source.

---

## Preconditions

* The actor is authenticated.
* The Asset exists.
* The actor has permission to create Business Triggers.
* The trigger type exists in the system.

---

## Main Flow

1. The actor identifies an Asset requiring operational attention.
2. The actor starts the Business Trigger creation process.
3. The actor selects the trigger type.
4. The actor records the reason for the trigger.
5. The actor optionally attaches supporting information.
6. InfraTrack validates the trigger information.
7. InfraTrack creates the Business Trigger.
8. InfraTrack links the Business Trigger to the Asset.
9. InfraTrack records the Business Trigger in Asset History.
10. InfraTrack confirms that the Business Trigger has been created.

---

## Trigger Types

Typical Business Trigger types include:

* scheduled inspection;
* customer request;
* emergency event;
* manager request;
* field observation.

---

## Alternative Flows

## Asset Not Found

If the Asset cannot be found, the Business Trigger cannot be created.

---

## Emergency Trigger

If the trigger represents an emergency event, InfraTrack may mark it as urgent.

This does not automatically create maintenance work.

---

## Supporting Information Missing

If optional supporting information is not provided, the Business Trigger may still be created if required information is complete.

---

## Postconditions

* A Business Trigger exists.
* The Business Trigger is linked to one Asset.
* The Business Trigger explains why operational attention is required.
* Asset History is updated.

---

## Business Rules

* A Business Trigger must be linked to exactly one Asset.
* A Business Trigger explains why attention is required.
* A Business Trigger does not determine the operational outcome.
* A Business Trigger does not automatically create a Work Order.

---

# 6. UC-003 — Assign Inspection

## Purpose

Assign an Inspection to an appropriate user following a Business Trigger.

This Use Case ensures that operational evidence can be collected by a responsible person.

---

## Primary Actor

Operational Coordinator

---

## Supporting Actors

* Manager;
* Field Employee;
* Contractor.

---

## Preconditions

* The actor is authenticated.
* The Asset exists.
* A Business Trigger exists.
* The Business Trigger requires inspection.
* The assigned user exists.
* The assigned user is eligible to perform the inspection.

---

## Main Flow

1. The actor opens the Business Trigger.
2. The actor reviews the Asset and trigger information.
3. The actor starts the inspection assignment process.
4. The actor selects the assigned user.
5. The actor optionally defines priority or expected completion timing.
6. InfraTrack validates the assignment.
7. InfraTrack creates the Inspection assignment.
8. InfraTrack notifies the assigned user.
9. InfraTrack records the assignment in Asset History.
10. InfraTrack confirms that the Inspection has been assigned.

---

## Alternative Flows

## Cross-Department Assignment

If the assigned user belongs to another department, InfraTrack allows the assignment when cross-department collaboration is permitted.

The Asset owning department remains unchanged.

---

## Contractor Assignment

If the assigned user is an external contractor, the assignment remains linked to the council's operational responsibility.

---

## Assigned User Unavailable

If the selected user is unavailable, InfraTrack prevents or warns against the assignment depending on council policy.

---

## Postconditions

* An Inspection assignment exists.
* The assigned user can access the assigned task.
* The Asset owning department remains unchanged.
* A notification may be sent to the assigned user.
* Asset History is updated.

---

## Business Rules

* An Inspection must originate from one Business Trigger.
* An Inspection must belong to one Asset.
* Task assignment determines operational access.
* Cross-department work must remain traceable.
* Notifications inform users but do not replace the business record.

---

# 7. Listed Use Cases

The following Use Cases are intentionally listed but not yet detailed.

They will be expanded when the corresponding business capability is ready for implementation.

---

## UC-004 — Perform Inspection

A Field Employee or authorised Contractor performs an assigned Inspection and records operational evidence.

---

## UC-005 — Record Issue

An Issue is recorded when an Inspection identifies a defect, risk or operational concern.

In InfraTrack V1, each completed Inspection may produce at most one Issue. If multiple concerns are observed, they are recorded together in the Issue description.

---

## UC-006 — Make Operational Decision

## Primary Actor

Manager

---

## Supporting Actors

* delegated authorised business roles (when council policy grants temporary authority).

---

A Manager or delegated authorised business role reviews inspection evidence and selects the appropriate operational outcome.

Operational Coordinators do not normally perform this Use Case.

---

## UC-007 — Create Work Order

## Primary Actor

Operational Coordinator

---

A Work Order is created when an approved Operational Decision authorises physical maintenance work.

The Operational Coordinator creates the Work Order following the approved decision.

---

## UC-008 — Assign Work Order

A Work Order is assigned to an internal employee, department or external contractor.

---

## UC-009 — Complete Maintenance Activity

The assigned worker records the maintenance activity actually performed in the field.

---

## UC-010 — Complete Review

An authorised role reviews completed work when a Completion Review is required.

---

## UC-011 — View Asset History

A user views the complete operational history of an Asset.

---

## UC-012 — Upload Operational Document

A user uploads a document or photograph and links it to an Asset or operational event.

---

## UC-013 — Send Notification

InfraTrack creates a notification when a business event requires user attention.

---

## UC-014 — Manage Departments

An Administrator manages the departments used for ownership and operational collaboration.

---

## UC-015 — Delegate Cross-Department Authority

A Manager or authorised role delegates temporary authority to another Manager or department.

---

# 8. Notes for Future Expansion

Each listed Use Case should be detailed only when its implementation becomes relevant.

Future detailed Use Cases should avoid introducing new business concepts unless they are already approved in Business Discovery.

If a Use Case reveals ambiguity in the business model, Business Discovery should be updated before architecture or implementation begins.

---

# Summary

This document starts the Functional Analysis phase for InfraTrack.

It translates the approved Business Discovery model into functional Use Cases.

Only the first Use Cases are detailed at this stage.

Remaining Use Cases are intentionally listed and will be expanded incrementally as each business capability approaches implementation.
