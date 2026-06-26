# Functional Use Cases

## Document Information

| Field    | Value                |
| -------- | -------------------- |
| Project  | InfraTrack           |
| Document | Functional Use Cases |
| Version  | 1.0                  |
| Status   | Approved             |
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

| ID     | Use Case                            | Status                 |
| ------ | ----------------------------------- | ---------------------- |
| UC-001 | Register Asset                      | Detailed · Implemented |
| UC-002 | Create Business Trigger             | Detailed · Implemented |
| UC-003 | Assign Inspection                   | Detailed · Implemented |
| UC-004 | Perform Inspection                  | Detailed · Implemented |
| UC-005 | Record Issue                        | Detailed · Implemented |
| UC-006 | Make Operational Decision           | Detailed · Implemented |
| UC-007 | Create Work Order                   | Detailed · Implemented |
| UC-008 | Assign Work Order                   | Detailed · Implemented |
| UC-009 | Complete Maintenance Activity       | Detailed · Implemented |
| UC-010 | Complete Review                     | Detailed · Implemented |
| UC-011 | View Asset History                  | Detailed · Implemented |
| UC-012 | Upload Operational Document         | Detailed · Implemented |
| UC-013 | Send Notification                   | Detailed · Implemented |
| UC-014 | Manage Departments                  | Detailed · Implemented |
| UC-015 | Delegate Cross-Department Authority | Detailed · Implemented |

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
* The actor is a Manager who belongs to the Asset owning department, or holds active delegated authority for that department at the decision date and time (UC-015).
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

If the actor is not a Manager, InfraTrack rejects the decision.

Operational Coordinators do not normally perform this Use Case (BR-032).

---

### Unauthorized Department Authority

If the actor is a Manager but does not belong to the Asset owning department and has no active delegated authority covering that department at the decision date and time, InfraTrack rejects the decision.

---

## Postconditions

* An Operational Decision exists and is linked to exactly one Issue and one Asset.
* The decision outcome and rationale are recorded.
* When the decision is made under delegated authority, the Operational Decision records the Delegated Authority reference for traceability.
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
* A Manager may make an Operational Decision when the Manager belongs to the Asset owning department.
* A Manager from another department may make an Operational Decision only when an active delegated authority covers the Asset owning department at the decision date and time (UC-015).
* When a decision is made under delegated authority, the Operational Decision records the delegated authority reference; Asset History records the decision itself.
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
* Creating a Work Order must not create a Maintenance Activity (Maintenance Activity creation is handled by UC-009.).
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

The assigned user may complete maintenance under UC-009 Complete Maintenance Activity.

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

# 12. UC-009 — Complete Maintenance Activity

## Purpose

Record the maintenance work actually performed for an assigned Work Order.

A Work Order organises approved operational work. A Maintenance Activity records the work actually performed.

Completing maintenance creates a Maintenance Activity as evidence of execution. The Work Order status becomes completed, but the Maintenance Activity remains the operational proof that work was performed.

This Use Case records field execution. It does not perform Completion Review, change Asset operational status, or validate completed work beyond recording the worker's completion record.

---

## Primary Actor

Assigned Field Employee or Assigned Contractor

The actor must be the user currently assigned to the Work Order.

For internal maintenance work, the assigned Field Employee completes maintenance.

For contractor work, the assigned Contractor completes maintenance.

---

## Supporting Actors

* Operational Coordinator;
* Manager.

---

## Preconditions

* The actor is authenticated.
* A Work Order exists with status assigned.
* The Work Order records an assigned user.
* The actor is the assigned user on the Work Order.
* The assignee role matches the Work Order work type:
  * internal maintenance — assigned Field Employee;
  * contractor work — assigned Contractor.
* No Maintenance Activity has already been recorded for the Work Order.
* The linked Asset exists.

---

## Main Flow

1. The assigned worker opens the assigned Work Order.
2. The actor reviews the Asset, Operational Decision, Issue and Work Order context.
3. The actor starts the maintenance completion process.
4. The actor records completion notes describing the work actually performed.
5. The actor records the business completion date and time (`completedAt`) of the maintenance.
6. InfraTrack validates the submitted information.
7. InfraTrack creates a Maintenance Activity linked to the Work Order.
8. InfraTrack records the completion notes and `completedAt` on the Maintenance Activity.
9. InfraTrack updates the Work Order status to completed.
10. InfraTrack records maintenance completion in Asset History.
11. InfraTrack confirms that maintenance has been completed.

The Maintenance Activity is now the evidence of work performed. Completion Review, if required, remains a separate process under UC-010 Complete Review.

---

## Alternative Flows

### Work Order Not Found

If the Work Order cannot be found, maintenance completion cannot proceed.

---

### Work Order Not Assigned

If the Work Order is not in assigned status, InfraTrack rejects maintenance completion.

Examples:

* status is created;
* status is completed;
* status is cancelled.

---

### Maintenance Activity Already Exists

If a Maintenance Activity has already been recorded for the Work Order, InfraTrack rejects completion.

In V1, each Work Order produces at most one Maintenance Activity.

---

### Unauthorized User

If the actor is not the assigned user on the Work Order, InfraTrack rejects maintenance completion.

Managers and Operational Coordinators do not complete maintenance on behalf of assigned workers in V1.

---

### Assignee Role Does Not Match Work Type

If the assigned user role does not match the Work Order work type, InfraTrack rejects maintenance completion.

Examples:

* a Contractor attempting to complete internal maintenance work;
* a Field Employee attempting to complete contractor work.

---

### Missing Completion Notes

If completion notes are missing or blank, InfraTrack rejects maintenance completion.

---

### Missing Completion Date and Time

If `completedAt` is missing, InfraTrack rejects maintenance completion.

---

### Completion Date and Time Before Assignment

If `completedAt` is before the Work Order was assigned, InfraTrack rejects maintenance completion.

---

### Completion Date and Time in the Future

If `completedAt` is in the future, InfraTrack rejects maintenance completion.

---

## Postconditions

* A Maintenance Activity exists and is linked to exactly one Work Order.
* The Maintenance Activity records completion notes and `completedAt`.
* The Maintenance Activity is the evidence that maintenance work was performed.
* The Work Order status becomes completed.
* Asset History is updated with maintenance completion.
* No Completion Review is created automatically.
* Asset operational status remains unchanged.
* The Operational Decision remains unchanged.

---

## Business Rules

* Every Maintenance Activity must be linked to a Work Order (BR-021).
* Maintenance Activities must produce operational evidence (BR-022); in V1, completion notes satisfy this requirement.
* A Work Order organises operational work; a Maintenance Activity proves that work was performed (BR-019).
* Only the assigned Field Employee or assigned Contractor may complete maintenance for the Work Order (BR-018).
* Maintenance completion requires an assigned Work Order; unassigned Work Orders cannot be completed.
* `completedAt` is a business operational timestamp recording when maintenance was performed, not a system audit timestamp.
* Completing a Work Order does not automatically perform Completion Review (BR-023, BR-024).
* Work Order completion does not automatically change Asset operational status.
* Asset History must preserve operational traceability (BR-004, BR-027).

---

# 13. UC-010 — Complete Review

## Purpose

Record the council's validation decision regarding completed maintenance work when a Completion Review is required.

A Maintenance Activity records the work actually performed. A Completion Review records the council's validation decision regarding that completed work.

Completion Review is a separate business concept. It is not a boolean flag on a Maintenance Activity.

Completion Review is optional. Routine work may not require review. When review is required, the review decision must be traceable.

This Use Case records a managerial validation decision. It does not modify the Maintenance Activity, reopen the Work Order, create follow-up operational work, or change Asset operational status.

---

## Primary Actor

Manager

Only a Manager may perform a Completion Review in V1.

---

## Supporting Actors

* Operational Coordinator;
* Administrator.

---

## Preconditions

* The actor is authenticated.
* The actor is a Manager.
* A Maintenance Activity exists.
* The Maintenance Activity is linked to a Work Order with status completed.
* No Completion Review has already been recorded for the Maintenance Activity.
* The linked Asset exists.

---

## Main Flow

1. The Manager opens the completed maintenance context for review.
2. The Manager reviews the Asset, Operational Decision, Issue, Work Order and Maintenance Activity context.
3. The Manager starts the Completion Review process.
4. The Manager records the review decision:
   * `APPROVED` — the completed work is accepted;
   * `REWORK_REQUIRED` — the completed work is not accepted and further work is needed.
5. The Manager records review notes explaining the validation decision.
6. The Manager records the business review date and time (`reviewedAt`) of the Completion Review.
7. InfraTrack validates the submitted information.
8. InfraTrack creates a Completion Review linked to the Maintenance Activity.
9. InfraTrack records the review decision, review notes and `reviewedAt` on the Completion Review.
10. InfraTrack records the Completion Review in Asset History.
11. InfraTrack confirms that the Completion Review has been recorded.

The Completion Review is now the traceable validation decision for the completed work. If rework is required, any further work must start through a future Operational Decision and Work Order flow. The existing Work Order remains completed.

---

## Alternative Flows

### Maintenance Activity Not Found

If the Maintenance Activity cannot be found, Completion Review cannot proceed.

---

### Work Order Not Completed

If the related Work Order is not in completed status, InfraTrack rejects Completion Review.

Examples:

* status is created;
* status is assigned;
* status is cancelled.

---

### Completion Review Already Exists

If a Completion Review has already been recorded for the Maintenance Activity, InfraTrack rejects the request.

In V1, each Maintenance Activity may have at most one Completion Review.

---

### Unauthorized Role

If the actor is not a Manager, InfraTrack rejects Completion Review.

Examples:

* Operational Coordinator;
* Field Employee;
* Contractor;
* Administrator.

Administrators configure the platform but do not perform operational validation in V1.

---

### Missing Review Decision

If the review decision is missing, InfraTrack rejects Completion Review.

---

### Invalid Review Decision

If the review decision is not one of the allowed values, InfraTrack rejects Completion Review.

Allowed decisions in V1:

* `APPROVED`;
* `REWORK_REQUIRED`.

---

### Missing Review Notes

If review notes are missing or blank, InfraTrack rejects Completion Review.

---

### Missing Review Date and Time

If `reviewedAt` is missing, InfraTrack rejects Completion Review.

---

### Review Date and Time Before Maintenance Completion

If `reviewedAt` is before the Maintenance Activity was completed, InfraTrack rejects Completion Review.

---

### Review Date and Time in the Future

If `reviewedAt` is in the future, InfraTrack rejects Completion Review.

---

## Postconditions

* A Completion Review exists and is linked to exactly one Maintenance Activity.
* The Completion Review records the review decision, review notes and `reviewedAt`.
* The Completion Review is the traceable validation decision for the completed work.
* The Maintenance Activity remains unchanged.
* The Work Order status remains completed.
* No new Work Order is created automatically.
* No new Operational Decision is created automatically.
* Asset operational status remains unchanged.
* Asset History is updated with the Completion Review.

If the review decision is rework required, the council must initiate any further work through the appropriate future Operational Decision and Work Order flow. The completed Work Order is not reopened.

---

## Business Rules

* Completion Review is a separate business concept from Maintenance Activity; it is not a boolean flag on maintenance completion (BR-023, BR-024).
* Completion Review is optional; routine maintenance may be closed without a Completion Review (BR-024).
* When review is required, the review decision must be traceable.
* Only a Manager may perform a Completion Review in V1 (BR-025).
* A Completion Review must be linked to exactly one Maintenance Activity.
* A Maintenance Activity may have zero or one Completion Review in V1.
* Completion Review requires an existing Maintenance Activity and a Work Order with status completed.
* A Work Order represents one intervention; once completed, it remains completed. Additional work requires a new Work Order through the appropriate operational flow.
* Completion Review does not modify the Maintenance Activity.
* Completion Review does not reopen the Work Order.
* Completion Review does not automatically create a new Work Order or Operational Decision.
* Completion Review does not automatically change Asset operational status.
* If rework is required, further work must start through a future Operational Decision and Work Order flow; the system does not reopen completed Work Orders.
* `reviewedAt` is a business operational timestamp recording when the review decision was made, not a system audit timestamp.
* Asset History must preserve operational traceability (BR-004, BR-027).

---

# 14. UC-011 — View Asset History

## Purpose

Present the permanent operational memory of an Asset as a chronological record of significant operational events.

Asset History is the permanent operational memory of an Asset. It provides a chronological view of all significant operational events that occurred during the Asset's lifetime.

This Use Case is read-only. It allows users to understand what happened to an Asset and who was responsible, without modifying any operational data.

---

## Primary Actor

Authenticated User

Any authenticated InfraTrack user may view Asset History in V1.

---

## Supporting Actors

* Administrator;
* Manager;
* Operational Coordinator;
* Field Employee;
* Contractor.

---

## Preconditions

* The actor is authenticated.
* The Asset exists.

---

## Main Flow

1. The actor opens an Asset.
2. The actor requests the Asset History for the Asset.
3. InfraTrack verifies that the actor is authenticated.
4. InfraTrack retrieves the Asset.
5. InfraTrack retrieves all Asset History entries linked to the Asset.
6. InfraTrack orders the history entries in reverse chronological order (most recent first).
7. InfraTrack presents each history entry with:
   * the business event date;
   * the event type;
   * the responsible user, when available.
8. InfraTrack confirms that the Asset History has been displayed.

The actor may review the operational timeline without changing any business object.

---

## Scope of History

In V1, Asset History includes eleven operational event types recorded by UC-001 through UC-012:

* Asset Registered;
* Business Trigger Created;
* Inspection Assigned;
* Inspection Completed;
* Issue Recorded;
* Operational Decision Made;
* Work Order Created;
* Work Order Assigned;
* Maintenance Completed;
* Completion Review Recorded;
* Operational Document Uploaded.

These eleven event types are the implemented V1 Asset History event types displayed by UC-011.

---

## Alternative Flows

### Asset Not Found

If the Asset cannot be found, Asset History cannot be displayed.

---

### Unauthenticated User

If the actor is not authenticated, InfraTrack rejects the request.

---

### No History Entries

If the Asset exists but no history entries have been recorded, InfraTrack presents an empty history.

This is valid. Asset registration itself creates the first history entry under UC-001 Register Asset.

---

## Postconditions

* The Asset History is displayed in reverse chronological order.
* Each entry shows the business event date, event type and responsible user when available.
* No business object is modified.
* No Asset History entry is created, modified or deleted.
* The Asset and all linked operational records remain unchanged.

---

## Business Rules

* Asset History is the permanent operational memory of an Asset (BR-004).
* Asset History is read-only; operational history must never be edited or deleted (BR-026).
* Operational History records both actions and decisions (BR-027).
* Every operational event must be associated with a date and time (BR-029).
* Any authenticated user may view Asset History in V1.
* Viewing Asset History does not change any business object.
* Viewing Asset History does not create new history events.
* History entries reflect operational events recorded by the system; they are not audit logs, version records or event-sourcing artefacts.

---

# 15. UC-012 — Upload Operational Document

## Purpose

Upload a document or photograph as operational evidence and link it permanently to an Asset and, when applicable, to exactly one operational owner.

Operational documents and photographs are business artefacts, not optional attachments. They form part of the permanent operational record of an Asset and support traceability across inspections, maintenance and managerial review.

Every Operational Document has exactly one Asset and optionally exactly one operational owner. The document has exactly one operational context.

An Operational Document is described by three independent concepts: business document type, operational context and file format. These concepts must not be mixed.

This Use Case records supporting evidence. It does not perform inspections, record issues, create Work Orders, complete maintenance, perform Completion Review or change Asset operational status.

In V1, UC-012 satisfies the evidence upload capabilities referenced by UC-001 Register Asset, UC-004 Perform Inspection and UC-009 Complete Maintenance Activity.

---

## Primary Actor

Operational Coordinator, Manager, Field Employee or Contractor

The actor must be authorised for the operational context in which the document is uploaded.

---

## Supporting Actors

* Administrator;
* assigned Field Employee or Contractor (when a Coordinator or Manager uploads on behalf of visible operational context);
* Manager (when evidence supports review or reference documentation).

Administrators configure the platform but do not upload operational evidence in V1.

---

## Preconditions

* The actor is authenticated.
* The actor is authorised to upload operational evidence for the selected context.
* The Asset exists.
* When an operational owner is selected, that record exists, belongs to one of the allowed operational owner types, and belongs to the same Asset as the Operational Document.

Typical authorised contexts in V1:

* **Asset-level reference documentation** — Operational Coordinator or Manager; no operational owner.
* **Inspection context** — Field Employee or Contractor assigned to the Inspection, or Operational Coordinator.
* **Issue, Operational Decision, Work Order or Maintenance Activity context** — Operational Coordinator, Manager, assigned Field Employee or assigned Contractor according to the linked operational record.
* **Completion Review context** — Manager.

---

## Main Flow

1. The actor opens an Asset or an operational record linked to an Asset.
2. The actor starts the operational document upload process.
3. The actor confirms the target Asset, either directly or through the selected operational owner.
4. The actor optionally selects exactly one operational owner to link the document to.
5. The actor provides a document title or description.
6. The actor selects the business document type.
7. The actor provides one or more files as part of the same upload action.
8. The actor records the business document date when applicable.
9. InfraTrack validates the submitted information and the actor's authorisation.
10. InfraTrack determines the file format from each uploaded file as technical metadata.
11. InfraTrack stores the uploaded operational evidence linked to exactly one Asset.
12. When selected, InfraTrack links the Operational Document to exactly one operational owner.
13. InfraTrack records exactly one Asset History event for the upload action.
14. InfraTrack confirms that the Operational Document has been uploaded.

The Operational Document becomes part of the permanent operational record for the Asset.

---

## Document Ownership

Every Operational Document has:

* exactly one Asset;
* optionally exactly one operational owner.

When present, the operational owner must be exactly one of:

* Inspection;
* Issue;
* Operational Decision;
* Work Order;
* Maintenance Activity;
* Completion Review.

An Operational Document shall never belong to more than one operational entity.

Examples that are not allowed:

* one document linked to two Work Orders;
* one document linked to both an Inspection and a Maintenance Activity.

When no operational owner is selected, the document is Asset-level reference or supporting documentation. The operational context is the Asset itself.

When an operational owner is selected, the operational context is that single operational entity. The business document type does not replace or duplicate that context.

---

## Asset Relationship

Every Operational Document belongs to exactly one Asset.

The Asset may be:

* referenced directly by the upload; or
* derived through the linked operational owner.

When an operational owner is selected, the Operational Document must belong to the same Asset as that operational owner. InfraTrack rejects any upload where the confirmed Asset and the operational owner's Asset are not the same.

This is a business invariant. An Operational Document cannot exist without an Asset anchor.

---

## Document Classification

An Operational Document is described by three independent concepts:

1. **Business document type** — the business nature of the document;
2. **Operational context** — the Asset and optional operational owner that give the document its place in the operational record;
3. **File format** — technical metadata derived from the uploaded file.

These concepts must not be mixed. Business document type does not describe workflow context or file format. Operational context does not describe document nature or file format. File format does not describe business meaning or workflow context.

---

## Business Document Type

The business document type describes the business nature of the document.

It does not describe:

* the workflow where the document is used;
* the file format.

Allowed business document types in V1:

* `PHOTO`;
* `REPORT`;
* `MANUAL`;
* `PROCEDURE`;
* `QUOTATION`;
* `DRAWING`;
* `OTHER`.

The actor selects the business document type during upload. The same business document type may appear in different operational contexts.

Business document types do not change Asset operational status by themselves.

---

## Operational Context

The operational context is determined exclusively by the linked business entity: the Asset and, when present, exactly one operational owner.

The business document type never encodes the operational context.

Examples:

* `PHOTO` attached to an Inspection → inspection evidence;
* `PHOTO` attached to a Maintenance Activity → maintenance evidence;
* `REPORT` attached to a Completion Review → completion review report;
* `MANUAL` attached directly to an Asset → permanent asset documentation.

The operational context explains where the document belongs in the operational record. It is independent of business document type and file format.

---

## File Format

The file format is technical metadata describing the physical uploaded file.

Examples include:

* pdf;
* jpg;
* png;
* docx;
* xlsx;
* heic.

The file format is determined automatically from the uploaded file. The actor does not select it as part of business classification.

File format is not a business document type. InfraTrack does not define a business enum for file formats.

No business rules depend on the physical file format. Business meaning and operational context remain independent of whether the evidence is stored as pdf, jpg, png or any other supported format.

---

## Asset History

Uploading operational evidence creates exactly one Asset History event per user upload action.

The history event represents the upload operation. It does not represent each individual file.

When the actor submits multiple files in one upload action, InfraTrack still records exactly one Asset History event for that action.

Asset History remains append-only. The upload event preserves traceability of who uploaded evidence, when, and in which operational context, without creating one history entry per file.

---

## Alternative Flows

### Asset Not Found

If the Asset cannot be found, the upload cannot proceed.

---

### Operational Owner Not Found

If a selected operational owner cannot be found, InfraTrack rejects the upload.

---

### Operational Owner Belongs to Another Asset

If the selected operational owner is not linked to the confirmed Asset, InfraTrack rejects the upload.

Operational evidence must remain linked to the correct Asset throughout its lifetime.

---

### Multiple Operational Owners

If the upload attempts to link one Operational Document to more than one operational entity, InfraTrack rejects the upload.

An Operational Document may have at most one operational owner.

---

### Unauthorized Role

If the actor is not authorised for the selected upload context, InfraTrack rejects the upload.

Examples:

* a Contractor uploading evidence for a Work Order they are not assigned to;
* a Field Employee uploading Asset-level reference documentation reserved for Coordinators or Managers;
* an Administrator uploading operational evidence.

---

### Missing Required Information

If required information is missing, InfraTrack rejects the upload and explains which information must be provided.

Required information includes:

* target Asset;
* document title or description;
* business document type;
* at least one file in the upload action.

---

### Invalid Document

If the submitted file is missing, unreadable or not accepted as operational evidence, InfraTrack rejects the upload.

---

### Unauthenticated User

If the actor is not authenticated, InfraTrack rejects the upload.

---

## Postconditions

* Uploaded operational evidence exists and is linked to exactly one Asset.
* When selected, the Operational Document is linked to exactly one operational owner belonging to that Asset.
* The Operational Document is never linked to more than one operational entity.
* The document title or description, business document type, file format, responsible user and business document date when provided are recorded.
* Exactly one Asset History event is recorded for the upload action, regardless of how many files were included in that action.
* The linked operational owner is not modified except for the document association.
* Asset operational status remains unchanged.
* No Inspection is completed, no Issue is recorded, no Operational Decision is made, no Work Order is created or completed, and no Completion Review is recorded by this Use Case alone.

Uploading evidence does not substitute for completing the operational workflow step to which the document relates.

---

## Business Rules

* Operational documents and photographs are business artefacts that form part of the permanent operational record (Domain Overview — Document).
* Every Operational Document belongs to exactly one Asset (BR-030).
* Every Operational Document has at most one operational owner; the operational owner, when present, must be exactly one Inspection, Issue, Operational Decision, Work Order, Maintenance Activity or Completion Review.
* An Operational Document shall never be linked to more than one operational entity.
* The Asset of an Operational Document may be referenced directly or derived through its operational owner; in all cases the document remains anchored to exactly one Asset.
* Operational documents must be linked to the Asset and may optionally be linked to one operational owner (UC-001).
* The document type describes the business nature of the document. Operational context is determined by the linked business entity. File format is technical metadata determined from the uploaded file.
* Business document type, operational context and file format are independent concepts and must not be mixed.
* Business document type does not describe workflow context or file format.
* Operational context is determined exclusively by the Asset and optional operational owner, not by business document type.
* File format is technical metadata determined automatically from the uploaded file; it is not a business document type and no business rules depend on the physical file format.
* Every upload action contributes exactly one Asset History event associated with the Asset (BR-004).
* A multi-file upload action still produces exactly one Asset History event for that user action.
* Asset History is permanent and read-only; uploaded evidence must not be removed to erase operational history (BR-026, BR-037).
* Operational History records both actions and decisions; evidence supports understanding what happened and why (BR-027).
* Every operational event must be associated with a date and time (BR-029).
* Uploading an Operational Document does not automatically change Asset operational status (BR-039).
* Asset Status changes are explicit business actions handled through dedicated capabilities, not through document upload.
* Inspection, Maintenance Activity and Completion Review do not change Asset Status automatically; uploading related evidence does not change Asset Status either.
* When Asset Status is changed through a future dedicated capability, that change must be recorded separately in Asset History with previous and new status traceable; document upload does not perform that change.
* Only authorised business roles may upload operational evidence for a given context.
* Uploading a document or photograph does not complete an Inspection, Maintenance Activity or other operational workflow step.
* Maintenance Activities must produce operational evidence (BR-022); completion notes remain the minimum V1 execution record under UC-009, while uploaded photographs and reports extend operational evidence through UC-012 Upload Operational Document.
* Uploading evidence does not reopen a completed Work Order.
* Uploading evidence does not create a new Operational Decision or Work Order.
* The backend remains the source of truth for document metadata, links and authorisation.

---

# 16. UC-013 — Send Notification

## Purpose

Create and deliver a notification when an operational business event requires user attention.

Notifications improve operational awareness and coordination. They inform users that a business event has already occurred and may require action.

Notifications are communication mechanisms, not business records. They do not initiate business processes, modify workflow state, change Asset operational status, or create Asset History entries.

The underlying business event remains the source of truth.

---

## Primary Actor

InfraTrack

InfraTrack creates and delivers notifications automatically when an approved operational business event requires user attention.

Under normal V1 operation, no human actor manually initiates this Use Case. Operational Use Cases request notification delivery through UC-013 when an approved notification event occurs.

In the running V1 application, UC-003 Assign Inspection, UC-008 Assign Work Order and UC-009 Complete Maintenance Activity request notification delivery when the corresponding business event succeeds.

---

## Supporting Actors

* Field Employee (notification recipient);
* Contractor (notification recipient);
* Operational Coordinator (notification recipient);
* Manager (notification recipient);
* Administrator (recipient for system-related notifications only).

---

## Preconditions

* A triggering operational business event has already been recorded successfully.
* The triggering business event is one of the V1 notification events defined for this Use Case.
* The intended recipient user exists and is enabled.
* The recipient is identified by the triggering operational context.
* Notification delivery is configured for the council environment.

---

## Main Flow

1. An operational Use Case completes a business event that requires user attention.
2. That Use Case requests notification delivery through UC-013 Send Notification.
3. InfraTrack identifies the notification type from the business event.
4. InfraTrack identifies the intended recipient from the operational context.
5. InfraTrack creates a Notification linked to the triggering business event.
6. InfraTrack delivers the Notification through the appropriate communication channel.
7. InfraTrack records the Notification as created and delivered when delivery succeeds.
8. The recipient may later view, acknowledge or dismiss the Notification.

Dismissing or viewing a Notification does not modify the underlying business event.

---

## V1 Notification Events

In V1, UC-013 creates and delivers notifications for the following operational business events:

| Triggering Business Event | Source Use Case | Recipient | Purpose |
| ------------------------- | --------------- | --------- | ------- |
| Inspection Assigned | UC-003 Assign Inspection | assigned Field Employee or Contractor | Inform the assignee that inspection work is available |
| Work Order Assigned | UC-008 Assign Work Order | assigned Field Employee or Contractor | Inform the assignee that maintenance work is available |
| Maintenance Completed | UC-009 Complete Maintenance Activity | Operational Coordinators in the Asset owning department | Inform coordination staff that field execution is complete |
| Completion Review Required | UC-009 Complete Maintenance Activity | Managers in the Asset owning department | Inform a Manager that contractor maintenance requires Completion Review |

Additional notification events may be introduced in future versions without changing the principle that notifications originate from business events.

### Recipient Resolution

* **Inspection Assigned** and **Work Order Assigned** — the recipient is the user assigned by the triggering Use Case.
* **Maintenance Completed** — InfraTrack resolves the Asset owning department and notifies each user whose role is Operational Coordinator and whose department matches that Asset department.
* **Completion Review Required** — InfraTrack resolves the Asset owning department and notifies each user whose role is Manager and whose department matches that Asset department. A Department Manager is a user whose role is Manager and whose department is that department; InfraTrack does not use a separate manager assignment entity.

If no matching recipient exists for a department-based notification event, InfraTrack does not create a Notification and the underlying business operation still succeeds.

UC-003 Assign Inspection, UC-008 Assign Work Order and UC-009 Complete Maintenance Activity request notification delivery when the corresponding business event succeeds.

---

## Completion Review Required Rule

In V1, InfraTrack sends a **Completion Review Required** notification only when:

* a Maintenance Activity is completed; and
* the linked Work Order has Work Type `CONTRACTOR_WORK`.

Internal maintenance (`INTERNAL_MAINTENANCE`) does not automatically require Completion Review in V1. No Completion Review Required notification is sent for internal maintenance completion.

This rule is fixed in V1. InfraTrack does not use a review policy engine or configurable review rules to determine whether the notification is sent.

When the rule applies, the notification is sent to each matching Manager in the Asset owning department after UC-009 Complete Maintenance Activity succeeds. The notification informs the Manager that Completion Review may be required under UC-010 Complete Review; it does not perform the review.

---

## Communication Channels

V1 may deliver notifications through:

* Dashboard Notifications;
* Email Notifications;
* Mobile Push Notifications.

The same notification may be presented through more than one channel when appropriate. This does not create multiple business events.

Channel selection depends on urgency, context and council configuration. Notification delivery is not guaranteed; temporary delivery failure must not affect the underlying business process.

---

## Alternative Flows

### Triggering Business Event Not Found

If the referenced business event cannot be found, InfraTrack does not create a Notification.

The underlying operational process remains authoritative.

---

### Recipient Not Found

If the intended recipient cannot be determined or does not exist, InfraTrack does not create a Notification for that recipient.

For department-based notification events, if no user with the required role belongs to the Asset owning department, InfraTrack does not create a Notification. The underlying business operation still succeeds.

---

### Recipient Unavailable

If the recipient account is disabled or otherwise unavailable, InfraTrack may skip delivery or record a delivery failure according to council configuration.

The underlying business event remains unchanged.

---

### Delivery Failure

If delivery through a selected channel fails temporarily, InfraTrack records the failure without modifying the underlying business event.

Operational records remain authoritative regardless of notification status.

---

### Notification Already Exists

If a Notification has already been created for the same business event and recipient, InfraTrack avoids creating duplicate notifications for that event in V1.

---

## Postconditions

* A Notification exists and references the triggering business event when delivery is initiated successfully.
* The triggering business event is unchanged.
* No Asset History entry is created by notification delivery alone.
* Asset operational status remains unchanged.
* No Inspection, Issue, Operational Decision, Work Order, Maintenance Activity, Completion Review or Operational Document is created, modified or completed by this Use Case alone.

---

## Business Rules

* Notifications always originate from an existing operational business event (Notification Lifecycle).
* Notifications never initiate business processes.
* Notifications do not replace the official business record (Domain Overview — Notification).
* Notifications inform users but do not replace the business record (UC-003, UC-008).
* Every notification should reference the operational event that generated it.
* Users must be able to navigate from a notification to its associated business record.
* Receiving, viewing or dismissing a notification does not create, modify or complete any business activity.
* Notifications are not part of an Asset's operational history; the underlying business event is preserved in Asset History (Notification Lifecycle, BR-004).
* Notification delivery is not guaranteed; operational records remain authoritative regardless of notification status.
* The same notification may be delivered through multiple channels without creating multiple business events.
* Notifications do not automatically change Asset operational status (BR-039).
* A **Completion Review Required** notification is sent in V1 only when maintenance is completed for a Work Order with Work Type `CONTRACTOR_WORK`; internal maintenance does not trigger this notification automatically.
* V1 does not use a review policy engine or configurable review rules for Completion Review Required notifications.
* **Maintenance Completed** notifications are sent to Operational Coordinators in the Asset owning department after UC-009 Complete Maintenance Activity succeeds.
* Notification delivery failure does not roll back the triggering business operation; operational records remain authoritative.
* The backend remains the source of truth for operational business data; notifications are temporary communication records.

---

# 17. UC-014 — Manage Departments

## Purpose

Maintain the departments used to organise council operational responsibility within InfraTrack.

Departments are the primary organisational boundary for Asset ownership, operational collaboration and reporting. Every Asset belongs to exactly one owning department throughout its operational lifecycle.

This Use Case maintains reference organisational data. It does not perform inspections, make Operational Decisions, assign work, or change Asset operational status.

---

## User and Department Relationship

In V1, a User may optionally belong to one Department. Department membership supports operational collaboration, notification recipient resolution and managerial authority boundaries. A User without a department may still use InfraTrack but is not resolved as a department-based notification recipient or as a Department Manager.

A **Department Manager** is a User whose role is Manager and whose department is that department. InfraTrack does not use a separate manager assignment entity.

Administrators assign or clear department membership through user management capabilities outside this Use Case. UC-014 maintains department reference data; it does not assign users to departments.

---

## Primary Actor

Administrator

Only an Administrator may create, update or remove departments in V1.

---

## Supporting Actors

* Manager;
* Operational Coordinator.

Managers and Operational Coordinators use departments operationally but do not manage department reference data in V1.

---

## Preconditions

* The actor is authenticated.
* The actor is an Administrator.

For update or removal:

* The target department exists.

For removal:

* No Asset currently belongs to the department.
* No User currently belongs to the department.

---

## Main Flow

### Create Department

1. The Administrator opens department management.
2. The Administrator starts the department creation process.
3. The Administrator provides the department name.
4. InfraTrack validates the submitted information.
5. InfraTrack creates the Department.
6. InfraTrack confirms that the Department has been created.

The new Department becomes available for Asset ownership and operational collaboration.

---

### Update Department

1. The Administrator opens an existing Department.
2. The Administrator starts the department update process.
3. The Administrator updates the department name.
4. InfraTrack validates the submitted information.
5. InfraTrack updates the Department.
6. InfraTrack confirms that the Department has been updated.

Updating a department name does not change Asset ownership or operational history.

---

### Remove Department

1. The Administrator opens an existing Department.
2. The Administrator requests department removal.
3. InfraTrack verifies that no Asset and no User currently belongs to the Department.
4. InfraTrack removes the Department.
5. InfraTrack confirms that the Department has been removed.

---

## Required Information

Typical required information includes:

* department name.

---

## Alternative Flows

### Unauthorized Role

If the actor is not an Administrator, InfraTrack rejects department management actions.

Operational Coordinators and Managers may view departments for operational purposes but may not manage department reference data in V1.

---

### Missing Required Information

If the department name is missing or blank, InfraTrack rejects the request and explains which information must be provided.

---

### Duplicate Department Name

If a department with the same name already exists, InfraTrack rejects creation or update.

---

### Department Not Found

If the Department cannot be found, update or removal cannot proceed.

---

### Department Owns Assets

If one or more Assets belong to the Department, InfraTrack rejects removal.

Asset ownership must be preserved. Departments, managers and employees may change over time, but Assets and their historical record remain (Council Organisation).

An Asset's owning department may be changed only through a separate approved Asset management capability outside this Use Case.

---

### Department Has Users

If one or more Users belong to the Department, InfraTrack rejects removal.

User department membership must be cleared or reassigned before the Department can be removed.

---

## Postconditions

### After Create

* A new Department exists.
* The Department is available for Asset registration and operational use.
* No Asset ownership is changed.

### After Update

* The Department name is updated.
* Existing Assets remain linked to the same Department identifier.
* Asset History and operational records remain unchanged.

### After Remove

* The Department no longer exists.
* No Asset referenced the removed Department.
* No User referenced the removed Department.
* Asset History remains unchanged.

In all cases, Asset operational status remains unchanged and no operational workflow is initiated by department management alone.

---

## Business Rules

* Every Asset belongs to exactly one Department (BR-001).
* A User may optionally belong to one Department; department membership supports notification resolution and managerial authority (UC-013, UC-006, UC-015).
* A Department Manager is a User whose role is Manager and whose department is that department.
* Departments are organisational units responsible for managing groups of public assets (Domain Overview — Department).
* Departments own assets; ownership establishes long-term accountability (Council Organisation, Department Collaboration).
* Departments do not manage users from other departments (Council Organisation).
* Operational work may cross departmental boundaries without changing Asset ownership (Department Collaboration).
* Department management is an administrative reference-data capability, not an operational workflow step.
* Removing a Department that owns Assets or that has Users assigned is not allowed.
* Updating a Department does not modify Asset operational status.
* Department management does not create Asset History events; Asset History records operational events, not administrative reference-data maintenance.
* The backend remains the source of truth for department reference data.

---

# 18. UC-015 — Delegate Cross-Department Authority

## Purpose

Grant temporary authority for a Manager to make Operational Decisions outside their own department when council policy requires cross-department collaboration.

Delegation supports operational continuity while preserving Asset ownership, departmental accountability and traceability.

This Use Case records a temporary authority grant. It does not make an Operational Decision, create a Work Order, assign work, change Asset ownership, or change Asset operational status.

When delegation is active, the delegated Manager may perform UC-006 Make Operational Decision within the defined scope according to council policy and BR-031.

---

## Primary Actor

Manager

The delegating Manager must have authority to grant operational decision authority for the defined scope.

---

## Supporting Actors

* delegated Manager;
* Administrator;
* Operational Coordinator.

Operational Coordinators do not create delegations in V1 but may coordinate execution after a delegated Operational Decision is made.

---

## Preconditions

* The actor is authenticated.
* The actor is a Manager who belongs to the source department.
* The delegate exists and is a Manager.
* The source department and target department are defined.
* The delegation validity period is defined with a start and end.
* The delegation reason is provided.
* No conflicting active delegation already covers the same delegate and target department, unless council policy explicitly allows replacement.

---

## Main Flow

1. The Manager opens the delegation process.
2. The Manager selects the delegate Manager who will receive temporary authority.
3. The Manager defines the source department and target department.
4. The Manager records the business reason for delegation.
5. The Manager records the delegation start date and time.
6. The Manager records the delegation end date and time.
7. InfraTrack validates the submitted information and the actor's authority.
8. InfraTrack creates the Delegated Authority record.
9. InfraTrack confirms that cross-department authority has been delegated.

While the delegation is active, the delegate may perform UC-006 Make Operational Decision for Assets owned by the target department. The Operational Decision remains attributable to the delegate as the responsible user.

When the delegation expires or is revoked, the delegate may no longer rely on that delegation for new Operational Decisions.

---

### Revoke Delegation

1. The delegating Manager opens an active Delegated Authority record.
2. The Manager requests revocation.
3. InfraTrack verifies that the actor is the delegating Manager.
4. InfraTrack marks the Delegated Authority as revoked.
5. InfraTrack confirms that the delegation has been revoked.

Revoked delegations remain visible for traceability but are no longer active authority.

---

## Delegated Authority and Asset History

Creating a Delegated Authority record does not create an Asset History event by itself.

Delegation is an authorisation record, not an Asset operational event. No **Delegation Recorded** Asset History event exists in V1.

When a delegate makes an Operational Decision under UC-006 using valid delegated authority, traceability is preserved through:

* the Operational Decision record;
* the deciding user recorded on that decision;
* the optional Delegated Authority reference recorded on the Operational Decision when the decision is made under delegation;
* the Asset History event created by UC-006 Make Operational Decision.

The delegation grant and the operational decision remain separate concepts. Asset History records the decision, not the authorisation grant.

---

## Delegation Scope

In the implemented V1 application, delegation scope is department-based:

* **Source department** — the department to which the delegating Manager belongs;
* **Target department** — the department whose Asset owning context the delegate may decide for while the delegation is active.

An active delegation authorises the delegate to make UC-006 Operational Decisions for Issues linked to Assets owned by the target department, subject to the validity period.

Delegation grants temporary operational decision authority only. It does not transfer Asset ownership and does not assign field execution work.

---

## Alternative Flows

### Unauthorized Role

If the actor is not a Manager, InfraTrack rejects delegation.

Operational Coordinators, Field Employees, Contractors and Administrators do not create cross-department delegations in V1.

---

### Invalid Delegate

If the selected delegate is not eligible according to council policy, InfraTrack rejects delegation.

Examples:

* a Field Employee or Contractor selected as delegate;
* a disabled user selected as delegate.

---

### Missing Required Information

If delegate, scope, reason, start date or end date is missing, InfraTrack rejects delegation and explains which information must be provided.

---

### Invalid Validity Period

If the delegation end date is before the start date, InfraTrack rejects delegation.

If the delegation start date is unreasonably in the future according to council policy, InfraTrack rejects delegation.

---

### Delegation Not Found

If an requested delegation record cannot be found, dependent actions cannot rely on it.

---

### Expired Delegation

If a delegate attempts to rely on an expired delegation for a new Operational Decision, InfraTrack rejects the decision unless the actor has independent authority.

Expired delegations remain visible for traceability but are not active authority.

---

### Revoked Delegation

If a delegate attempts to rely on a revoked delegation for a new Operational Decision, InfraTrack rejects the decision unless the actor has independent authority.

Revoked delegations remain visible for traceability but are not active authority.

---

### Conflicting Active Delegation

If an active delegation already exists for the same delegate and target department, InfraTrack rejects the new delegation or requires explicit replacement according to council policy.

---

## Postconditions

* A Delegated Authority record exists with delegate, source department, target department, reason and validity period.
* The delegate may perform UC-006 for Assets owned by the target department while the delegation is active.
* When revoked, the delegation is no longer active for new Operational Decisions.
* No Asset History entry is created by delegation creation or revocation alone.
* Asset ownership remains unchanged.
* Asset operational status remains unchanged.
* No Work Order, Maintenance Activity or Completion Review is created by delegation alone.
* Operational Coordinators may still coordinate execution only after an Operational Decision is made; delegation does not bypass coordination responsibilities (BR-032).

---

## Business Rules

* Operational Decisions are made by Managers or delegated authorised business roles (BR-031, BR-014).
* In V1, delegation scope is defined by source department and target department.
* Only the delegating Manager may revoke an active delegation.
* Operational Coordinators coordinate execution; they do not normally make Operational Decisions (BR-032).
* Cross-department operational decisions should only occur when responsibility has been explicitly delegated or assigned according to council policy (Department Collaboration).
* Delegation grants temporary authority; it does not transfer permanent Asset ownership (Department Collaboration).
* Delegation does not modify Asset owning department (BR-001).
* Delegation does not automatically change Asset operational status (BR-039).
* Delegated authority must remain traceable within the operational record (Department Collaboration).
* Creating a Delegated Authority record does not create an Asset History event; delegation is an authorisation record, not an Asset operational event.
* When a delegate makes an Operational Decision, traceability is preserved through the Operational Decision record, the deciding user, the optional Delegated Authority reference when applicable, and the Asset History event created by UC-006; V1 does not define a Delegation Recorded Asset History event.
* When a delegate makes an Operational Decision, the decision must remain attributable to the responsible person (BR-028).
* Delegation does not assign Inspections or Work Orders; task assignment remains separate from delegated decision authority (Department Collaboration).
* Delegation does not reopen completed Work Orders.
* Delegation does not create a new Issue, Work Order, Maintenance Activity or Completion Review by itself.
* One Issue may produce at most one Operational Decision in V1, whether decided by the owning Manager or a valid delegate (BR-033).
* The backend remains the source of truth for delegation validity and authorisation.

---

# 19. V1 Functional Analysis Completion Notes

UC-001 through UC-015 are fully detailed and implemented in the running V1 application.

This document describes the implemented V1 functional model, not an intermediate delivery state.

## Known V1 Implementation State

All fifteen Use Cases in the catalogue are implemented. The following intentional V1 limitations remain documented in Business Discovery and Functional Analysis but are not missing Use Case implementation:

* Asset Status changes are not automated by inspection, decision, work order, maintenance or completion review (BR-039); a dedicated Asset Status capability is outside V1 scope.
* Notification delivery is best-effort; delivery failure does not roll back operational records.
* UC-012 stores operational document evidence separately; Asset History records an upload event but does not embed document files (ADR-001).

If implementation reveals ambiguity in the business model, Business Discovery should be updated before architecture changes begin.

---

# 20. Summary

This document defines the Functional Analysis phase for InfraTrack.

It translates the approved Business Discovery model into functional Use Cases.

UC-001 through UC-015 are fully detailed and implemented to describe the complete V1 functional model.

The V1 operational lifecycle spans Asset registration, operational attention, inspection, issue recording, decision-making, work order execution, maintenance completion, optional review, evidence upload, history viewing, notification coordination, department reference management and cross-department delegation.

This document is the authoritative functional description of implemented V1 for architecture and consistency review.
