# Domain Overview

## Document Information

| Field    | Value              |
| -------- | ------------------ |
| Project  | InfraTrack         |
| Document | Domain Overview    |
| Version  | 1.0                |
| Status   | Draft              |
| Phase    | Business Discovery |

---

# 1. Purpose

This document defines the core business domain of InfraTrack.

Its purpose is to establish a common understanding of the operational concepts used throughout the project.

Every document, workflow, API, user interface and software component should use the terminology defined here.

This document intentionally describes the business domain rather than its technical implementation.

---

# 2. Domain Overview

InfraTrack is an Asset and Field Operations Management Platform designed for Australian Local Governments.

The platform manages the complete operational lifecycle of public infrastructure assets by supporting inspections, maintenance activities, contractor interventions and administrative validation.

Rather than focusing on maintenance alone, InfraTrack records every operational event associated with an asset, creating a complete and traceable history throughout its lifecycle.

The software enables councils to transform operational activities into structured, searchable and auditable business information.

---

# 3. Core Business Concepts

## Department

A Department represents an organisational unit responsible for managing a group of public assets.

Examples include:

* Roads
* Parks
* Facilities
* Buildings
* Recreation
* Waste Management

Every operational activity belongs to exactly one department.

---

## Asset

An Asset is a long-lived physical object owned or managed by the council.

Examples include:

* Streetlight
* Public Toilet
* Playground Equipment
* Park BBQ
* Sports Facility
* Community Building
* Signage
* Drainage Infrastructure

Assets exist independently from inspections, maintenance activities and work orders.

An asset represents the physical object, not the work performed on it.

---

## Business Trigger

A Business Trigger represents the business event that initiates operational attention for an asset.

Business Triggers explain why an inspection takes place.

Typical examples include:

- scheduled inspections;
- customer requests;
- manager decisions;
- field observations;
- emergency events;
- contractor recommendations;
- compliance programs.

Business Triggers provide operational context.

They do not prescribe the work to be performed.

---

## Inspection

An Inspection is the operational assessment of an asset performed by a field employee.

Its objective is to determine the current condition of the asset.

An inspection may include:

* observations;
* photographs;
* GPS location;
* condition assessment;
* safety concerns;
* recommendations.

An inspection does not necessarily imply that maintenance is required.

---

## Issue

An Issue represents a problem identified during an inspection.

Examples include:

* damage;
* deterioration;
* vandalism;
* missing components;
* safety hazards;
* operational failures.

Issues represent findings.

They are not maintenance activities.

---

## Work Order

A Work Order authorises and organises maintenance work resulting from an operational decision.

A Work Order defines:

- what work must be performed;
- who is responsible for performing it;
- operational priority;
- expected completion;
- supporting documentation.

Work Orders may be assigned to internal employees or external contractors.

A Work Order only exists after an operational decision has been made.

---

## Maintenance Activity

A Maintenance Activity represents the execution of a Work Order.

It records:

* performed work;
* labour;
* observations;
* photographs;
* completion notes;
* completion date.

Maintenance represents the operational execution of the required work.

---

## Completion Review

A Completion Review is the administrative verification of completed maintenance work when organisational policy requires formal approval.

Not every maintenance activity requires a Completion Review.

Routine activities may be closed automatically.

Higher-risk or contractor-performed work may require managerial review before the operational cycle is considered complete.

---

## Document

A Document represents formal evidence generated or attached during an operational process.

Examples include:

* Inspection Reports
* Work Orders
* Completion Reports
* Cost Estimates
* Asset Manuals
* Maintenance Procedures
* Contractor Reports

Documents form part of the permanent operational record.

---

## Notification

Notifications communicate important operational events to users.

Typical events include:

* assignment;
* inspection completed;
* work order created;
* maintenance completed;
* managerial review required.

Notifications support operational coordination but do not replace the official business record.

---

# 4. Domain Relationships

The operational workflow is centred around the Asset.

For the canonical operational lifecycle, see section 5.

---

# 5. Operational Lifecycle

An asset may experience many operational cycles during its lifetime.

Each cycle follows a similar pattern.

```text
      Asset
        │
        ▼
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
          Operational Decision
                     │
     ┌───────────────┼──────────────────────────────┐
     │               │              │               │
     ▼               ▼              ▼               ▼
Continue         Internal      Contractor      Renewal /
Monitoring      Maintenance    Work Order     Decommission
                     │              │
                     └──────┬───────┘
                            ▼
                 Maintenance Activity
                            │
                            ▼
             Completion Review (if required)
                            │
                            ▼
                  Asset History Updated
                            │
                            ▼
                 Operational Cycle Closed```

The same asset may complete hundreds of operational cycles over many years.

The asset remains constant while operational activities continuously evolve.

---

# 6. Business Trigger

A Business Trigger is an event requiring operational attention for an asset.

Examples include:

- Scheduled Inspection
- Customer Request
- Manager Decision
- Emergency Event
- Previous Inspection Findings
- Contractor Recommendation

---

# 7. Domain Boundaries

InfraTrack manages operational asset activities.

Its responsibilities include:

* Asset Management
* Business Triggers
* Inspections
* Issues
* Operational Decisions
* Work Orders
* Maintenance
* Contractors
* Documentation
* Notifications
* Operational Reporting
* Asset History

InfraTrack intentionally excludes:

* Accounting
* Payroll
* Procurement
* Inventory
* Human Resources
* Financial Budgeting
* GIS Editing
* Customer Relationship Management

These domains may integrate with InfraTrack but remain outside its responsibilities.

---

# 8. Ubiquitous Language

The following terminology is considered official throughout the project.

| Term                 | Definition                                              |
| -------------------- | ------------------------------------------------------- |
| Asset                | Physical infrastructure managed by the council          |
| Department           | Organisational unit responsible for assets              |
| Business Trigger     | Business event initiating operational attention         |
| Inspection           | Operational assessment of an asset                      |
| Issue                | Problem identified during inspection                    |
| Operational Decision | Business decision determining the appropriate response  |
| Work Order           | Authorised maintenance activity                         |
| Maintenance Activity | Execution of maintenance work                           |
| Completion Review    | Administrative review of completed work when required   |
| Document             | Operational evidence                                    |
| Asset History        | Permanent operational record of an asset                |

No alternative terminology should be used for these concepts within the project.

---

# 9. Guiding Domain Principles

The following principles define the business domain.

## Assets are permanent.

Operational activities are temporary.

---

## Every operational action should have a business reason.

No inspection, maintenance activity or work order should exist without a valid operational context.

---

## Every important action should generate evidence.

Operational history is one of the platform's primary responsibilities.

---

## Business workflows are traceable.

Every operational decision should be attributable to a person, a date and a business justification.

---

## Documentation is part of the business process.

Reports, photographs and supporting documents are business artefacts, not optional attachments.

---

## The Asset is the centre of the domain.

Every operational workflow exists because of an asset.

The platform therefore revolves around preserving the complete operational history of every asset throughout its lifecycle.

---

## Decisions separate observation from execution.

An inspection records the condition of an asset.

An operational decision determines the appropriate response.

Maintenance activities execute that decision.

These responsibilities must remain distinct throughout the platform.

---

## Operational decisions drive operational work.

An inspection provides evidence.

An operational decision determines the appropriate response.

Maintenance activities execute that decision.

The decision itself forms part of the permanent operational history of the asset.
