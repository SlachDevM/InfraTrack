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
| UC-004 | Perform Inspection                  | Detailed |
| UC-005 | Record Issue                        | Detailed |
| UC-006 | Make Operational Decision           | Detailed |
| UC-007 | Create Work Order                   | Detailed |
| UC-008 | Assign Work Order                   | Detailed |
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

# 7. UC-004 — Perform Inspection

## Purpose

Perform an assigned Inspection and record operational evidence about the Asset's observed condition.

This Use Case completes the field assessment started by UC-003. It produces reliable evidence for later business decisions but does not prescribe maintenance, create Work Orders or change Asset operational status.

---

## Primary Actor

Field Employee or Contractor assigned to the Inspection

---

## Supporting Actors

* Operational Coordinator;
* Manager.

---

## Preconditions

* The actor is authenticated.
* An Inspection assignment exists.
* The Inspection status is assigned.
* The actor is the assigned user for the Inspection.
* The Inspection is linked to one Asset and one Business Trigger.

---

## Main Flow

1. The actor opens the assigned Inspection.
2. The actor reviews the Asset and Business Trigger context.
3. The actor records the observed physical condition of the Asset.
4. The actor records inspection observations.
5. The actor indicates whether an Issue was identified during the Inspection.
6. The actor records the business completion date and time of the Inspection.
7. InfraTrack validates the submitted information.
8. InfraTrack marks the Inspection as completed.
9. InfraTrack records the Inspection completion in Asset History.
10. InfraTrack confirms that the Inspection has been completed.

If no Issue was identified, the operational cycle may close at this point without Manager review.

If an Issue was identified, UC-005 Record Issue becomes available to the same actor.

---

## Alternative Flows

### Inspection Not Assigned

If the Inspection is not in assigned status, InfraTrack rejects completion.

---

### Actor Not Assigned

If the actor is not the assigned user, InfraTrack rejects completion.

---

### Missing Required Information

If observed condition, observations or completion date and time are missing, InfraTrack rejects completion and explains which information must be provided.

---

### Completion Date and Time in the Future

If the recorded completion date and time is in the future, InfraTrack rejects completion.

---

### Unauthorized Role

If the actor is not a Field Employee or Contractor, InfraTrack rejects completion.

---

## Postconditions

* The Inspection status is completed.
* Observed condition, observations and issue-identified outcome are recorded on the Inspection.
* Asset History is updated with the Inspection completion event.
* Asset operational status is unchanged.
* No Work Order, Maintenance Activity or Operational Decision is created by this Use Case alone.

---

## Business Rules

* An Inspection must belong to exactly one Asset (BR-007).
* An Inspection records observations; it does not prescribe maintenance (BR-008).
* An Inspection may conclude without identifying any Issue (BR-009).
* If no Issue is identified, the Inspection may be closed immediately (BR-010).
* Only the assigned Field Employee or Contractor may perform the Inspection.
* Physical condition describes observed state; it is separate from Asset operational status.
* Inspection evidence becomes part of the permanent Asset History (BR-004, BR-030).

---

# 8. UC-005 — Record Issue

## Purpose

Record an Issue when a completed Inspection identifies a defect, risk or operational concern.

An Issue represents an observation requiring managerial review. It is not maintenance work and does not determine the final operational response.

---

## Primary Actor

Field Employee or Contractor who completed the Inspection

---

## Supporting Actors

* Manager;
* Operational Coordinator.

---

## Preconditions

* The actor is authenticated.
* The Inspection exists and is completed.
* The Inspection identified an Issue during completion.
* No Issue has already been recorded for the Inspection.
* The actor is the user who completed the Inspection.

---

## Main Flow

1. The actor opens the completed Inspection that identified an Issue.
2. The actor starts the Issue recording process.
3. The actor records the Issue description.
4. The actor selects Issue severity.
5. The actor records the business date and time when the Issue was recorded.
6. InfraTrack validates the submitted information.
7. InfraTrack creates the Issue.
8. InfraTrack links the Issue to the Inspection and Asset.
9. InfraTrack records the Issue in Asset History.
10. InfraTrack confirms that the Issue has been recorded.

The Issue is now available for UC-006 Make Operational Decision.

---

## Alternative Flows

### Inspection Did Not Identify an Issue

If the completed Inspection did not identify an Issue, InfraTrack rejects Issue recording.

---

### Issue Already Recorded

If an Issue already exists for the Inspection, InfraTrack rejects a second Issue.

In InfraTrack V1, each completed Inspection may produce at most one Issue. Multiple concerns observed during the same Inspection must be described together in the Issue description (BR-012a).

---

### Actor Did Not Complete the Inspection

If the actor is not the user who completed the Inspection, InfraTrack rejects Issue recording.

---

### Missing Required Information

If description, severity or recorded date and time are missing, InfraTrack rejects Issue recording.

---

### Recorded Date and Time Before Inspection Completion

If the recorded date and time is before the Inspection was completed, InfraTrack rejects Issue recording.

---

### Recorded Date and Time in the Future

If the recorded date and time is in the future, InfraTrack rejects Issue recording.

---

### Unauthorized Role

If the actor is not a Field Employee or Contractor, InfraTrack rejects Issue recording.

---

## Postconditions

* An Issue exists and is linked to exactly one Inspection and one Asset.
* Asset History is updated.
* Asset operational status is unchanged.
* UC-006 Make Operational Decision becomes available for the Issue.
* No Operational Decision, Work Order or Maintenance Activity is created by this Use Case.

---

## Business Rules

* Every Issue must originate from an Inspection (BR-011).
* An Issue represents an observation; it is not a maintenance activity (BR-012).
* An Inspection may produce at most one Issue in InfraTrack V1 (BR-012a).
* Operational Decisions are only required when an Issue has been identified (BR-013).
* Only the user who completed the Inspection may record the Issue in V1.
* Asset History must preserve operational traceability (BR-004, BR-028, BR-029).

---

# 9. UC-006 — Make Operational Decision

## Purpose

Review inspection evidence and select the appropriate operational response to a recorded Issue.

This Use Case separates business decision-making from field observation and from work execution. It determines what the council intends to do next but does not automatically create Work Orders or Maintenance Activities.

---

## Primary Actor

Manager

---

## Supporting Actors

* delegated authorised business roles (when council policy grants temporary authority);
* Operational Coordinator;
* Field Employee or Contractor (as evidence providers).

---

## Preconditions

* The actor is authenticated.
* The actor has authority to make Operational Decisions.
* An Issue exists for a completed Inspection.
* No Operational Decision has already been made for the Issue.
* The Issue is linked to one Asset.

---

## Main Flow

1. The actor opens the Issue and reviews linked Inspection evidence.
2. The actor reviews Asset context and operational history as needed.
3. The actor selects the Operational Decision outcome.
4. The actor records the decision rationale.
5. The actor records the business date and time of the decision.
6. InfraTrack validates the submitted information.
7. InfraTrack creates the Operational Decision.
8. InfraTrack links the Operational Decision to the Issue and Asset.
9. InfraTrack records the Operational Decision in Asset History.
10. InfraTrack confirms that the Operational Decision has been recorded.

The selected outcome determines whether physical work may follow:

* `CONTINUE_MONITORING`, `RENEWAL_RECOMMENDATION` and `DECOMMISSION_RECOMMENDATION` do not authorise Work Order creation in V1.
* `INTERNAL_MAINTENANCE` and `CONTRACTOR_WORK` authorise physical work but do not automatically create a Work Order.

When physical work is authorised, UC-007 Create Work Order becomes available to an Operational Coordinator.

---

## Decision Outcomes

Operational Decisions may result in one of the following outcomes:

* Continue Monitoring;
* Internal Maintenance;
* Contractor Work;
* Renewal Recommendation;
* Decommission Recommendation.

These outcomes align with the approved Business Discovery model for Operational Decisions.

---

## Alternative Flows

### Issue Not Found

If the Issue cannot be found, the Operational Decision cannot be created.

---

### Decision Already Made

If an Operational Decision already exists for the Issue, InfraTrack rejects a second decision.

---

### Missing Required Information

If outcome, rationale or decision date and time are missing, InfraTrack rejects the decision.

---

### Decision Date and Time Before Issue Recorded

If the decision date and time is before the Issue was recorded, InfraTrack rejects the decision.

---

### Decision Date and Time in the Future

If the decision date and time is in the future, InfraTrack rejects the decision.

---

### Unauthorized Role

If the actor is not a Manager or delegated authorised business role, InfraTrack rejects the decision.

Operational Coordinators do not normally perform this Use Case (BR-032).

---

## Postconditions

* An Operational Decision exists and is linked to exactly one Issue and one Asset.
* The decision outcome and rationale are recorded.
* Asset History is updated.
* Asset operational status is unchanged in V1.
* No Work Order is created automatically.
* No Maintenance Activity is created.
* When the outcome authorises physical work, UC-007 may follow.

---

## Business Rules

* Operational Decisions are only required when an Issue has been identified (BR-013).
* Operational Decisions determine the appropriate operational response (BR-014).
* Operational Decisions are made by Managers or delegated authorised business roles (BR-031).
* Operational Coordinators coordinate execution; they do not normally make Operational Decisions (BR-032).
* Every operational decision must be attributable to a responsible person (BR-028).
* Decisions precede execution; maintenance work must not begin without an Operational Decision.
* Work Orders are not created automatically by an Operational Decision in V1.
* Asset operational status is not changed automatically by the decision in V1.

---

# 10. UC-007 — Create Work Order

## Purpose

Create a Work Order to organise physical maintenance work following an approved Operational Decision.

A Work Order structures operational work. It does not represent completed work and does not execute maintenance.

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
* An Operational Decision exists.
* The Operational Decision outcome authorises physical work (`INTERNAL_MAINTENANCE` or `CONTRACTOR_WORK`).
* No Work Order has already been created for the Operational Decision.
* The linked Asset exists.

---

## Main Flow

1. The actor opens the Operational Decision authorising physical work.
2. The actor reviews the Asset, Issue and decision context.
3. The actor starts the Work Order creation process.
4. The actor records the Work Order description.
5. The actor selects Work Order priority.
6. The actor records the business creation date and time of the Work Order.
7. InfraTrack validates the submitted information.
8. InfraTrack creates the Work Order with status created.
9. InfraTrack links the Work Order to the Operational Decision and Asset.
10. InfraTrack derives the work type from the Operational Decision outcome.
11. InfraTrack records the Work Order in Asset History.
12. InfraTrack confirms that the Work Order has been created.

The Work Order is now available for assignment under UC-008 Assign Work Order.

---

## Work Types

In V1, Work Order work type is derived from the Operational Decision outcome:

* Internal Maintenance — when the decision outcome is internal maintenance;
* Contractor Work — when the decision outcome is contractor work.

No other work types are used in V1.

---

## Alternative Flows

### Operational Decision Not Found

If the Operational Decision cannot be found, the Work Order cannot be created.

---

### Decision Does Not Authorise Physical Work

If the Operational Decision outcome is Continue Monitoring, Renewal Recommendation or Decommission Recommendation, InfraTrack rejects Work Order creation.

---

### Work Order Already Exists

If a Work Order already exists for the Operational Decision, InfraTrack rejects creation.

---

### Missing Required Information

If description, priority or business creation date and time are missing, InfraTrack rejects creation.

---

### Creation Date and Time Before Decision

If the business creation date and time is before the Operational Decision was made, InfraTrack rejects creation.

---

### Creation Date and Time in the Future

If the business creation date and time is in the future, InfraTrack rejects creation.

---

### Unauthorized Role

If the actor is not an Operational Coordinator, InfraTrack rejects creation.

Managers decide; Operational Coordinators create and coordinate execution.

---

## Postconditions

* A Work Order exists with status created.
* The Work Order is linked to exactly one Operational Decision and one Asset.
* Work type reflects the authorised physical work outcome.
* Asset History is updated.
* The Operational Decision remains open and unchanged.
* No Maintenance Activity is created.
* Asset operational status is unchanged.
* The Work Order status is created and the Work Order is not yet assigned.

---

## Business Rules

* A Work Order may only exist after an Operational Decision (BR-017).
* A Work Order must result from exactly one Operational Decision.
* Work Orders are created only for decisions that approve physical work in V1.
* A Work Order exists to organise operational work; it is not proof that work has been completed (BR-019).
* Work Orders are coordinated by an Operational Coordinator following an approved Operational Decision (BR-020).
* Creating a Work Order must not create a Maintenance Activity (BR-021 applies later).
* Work Orders are not created automatically by Operational Decisions.
* Asset History must preserve operational traceability (BR-004, BR-027).

---

# 11. UC-008 — Assign Work Order

## Purpose

Assign a created Work Order to the Field Employee or Contractor responsible for performing the authorised physical work.

This Use Case enables execution to begin but does not record maintenance completion or prove that work has been performed.

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
* A Work Order exists with status created.
* The Work Order is not yet assigned.
* The Work Order resulted from an Operational Decision authorising physical work.
* At least one eligible assignee exists in the system.

---

## Main Flow

1. The actor opens the Work Order awaiting assignment.
2. The actor reviews the Asset, Operational Decision and Work Order context.
3. The actor starts the assignment process.
4. The actor selects the assignee based on work type:
   * internal maintenance work is assigned to a Field Employee;
   * contractor work is assigned to a Contractor.
5. InfraTrack validates the assignment.
6. InfraTrack records the assignment on the Work Order.
7. InfraTrack notifies the assigned user when notification delivery is configured.
8. InfraTrack records the assignment in Asset History.
9. InfraTrack confirms that the Work Order has been assigned.

The assigned user may later perform maintenance under UC-009 Complete Maintenance Activity when that capability is implemented.

---

## Alternative Flows

### Work Order Not Found

If the Work Order cannot be found, assignment cannot proceed.

---

### Work Order Already Assigned

If the Work Order has already been assigned, InfraTrack rejects reassignment unless a future cancellation or reassignment capability is explicitly approved.

---

### Invalid Assignee

If the selected user is not eligible for the work type, InfraTrack rejects the assignment.

Examples:

* a Contractor selected for internal maintenance work;
* a Field Employee selected for contractor work;
* a Manager or Operational Coordinator selected as assignee.

---

### Unauthorized Role

If the actor is not an Operational Coordinator or other authorised coordinating role, InfraTrack rejects assignment.

---

## Postconditions

* The Work Order status becomes assigned.
* The Work Order records the assigned user.
* The assigned user can access the assigned Work Order.
* Asset History is updated when assignment is recorded.
* No Maintenance Activity is created by assignment alone.
* Asset operational status remains unchanged unless changed by a separate approved process.

---

## Business Rules

* A Work Order must be assigned to either a Field Employee or an external Contractor (BR-018).
* Work Orders organise work; assignment does not prove completion (BR-019).
* Work Order assignment is coordinated by an Operational Coordinator or other authorised coordinating role (BR-020).
* Task assignment determines operational access.
* Notifications inform users but do not replace the business record.
* Assignment must preserve cross-department traceability when applicable.

---

# 12. Listed Use Cases

The following Use Cases remain listed and will be expanded when the corresponding business capability approaches implementation.

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

# 13. Notes for Future Expansion

Each listed Use Case should be detailed only when its implementation becomes relevant.

Future detailed Use Cases should avoid introducing new business concepts unless they are already approved in Business Discovery.

If a Use Case reveals ambiguity in the business model, Business Discovery should be updated before architecture or implementation begins.

UC-004 through UC-008 are now detailed to reflect the implemented V1 behaviour.

UC-009 is the next listed Use Case to be expanded when implementation begins.

## Known V1 Limitations

The following V1 limitations are intentional and reflect the current implementation state:

* Notifications are documented in flows (for example, inspection assignment) but are not yet wired into UC-003.
* Photos, documents and GPS evidence are deferred.
* Delegated decision authority is documented but not implemented; only Managers can make Operational Decisions in V1.

---

# Summary

This document defines the Functional Analysis phase for InfraTrack.

It translates the approved Business Discovery model into functional Use Cases.

UC-001 through UC-008 are detailed to reflect implemented or approved V1 behaviour.

UC-009 is documented as the next planned Use Case.

Remaining Use Cases are intentionally listed and will be expanded incrementally as each business capability approaches implementation.
