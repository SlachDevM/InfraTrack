# Actors & Responsibilities

## Document Information

| Field    | Value                     |
| -------- | ------------------------- |
| Project  | InfraTrack                |
| Document | Actors & Responsibilities |
| Version  | 1.0                       |
| Status   | Draft                     |
| Phase    | Business Discovery        |

---

# 1. Purpose

This document defines the people involved in the operational lifecycle of public assets and the responsibilities they hold within InfraTrack.

The objective is to describe business responsibilities rather than software permissions.

System permissions will later be derived from these responsibilities.

---

# 2. Business Reality

Australian Local Governments rely on multiple operational roles to maintain public infrastructure.

Although job titles differ between councils, operational responsibilities remain broadly consistent.

Infrastructure management generally involves:

* system administrators;
* operational managers;
* operational coordinators;
* field employees;
* external contractors.

Each role contributes to a different stage of the operational lifecycle.

No single role performs every activity.

Operational accountability always remains with the council.

---

# 3. InfraTrack Decision

InfraTrack models five primary actors.

```text
Business Actors

──────────────────────────────────────

Administrator
Platform Administration

Manager
Business Decisions

Operational Coordinator
Operational Coordination

Field Employee
Operational Execution

Contractor
External Execution
```

These actors represent responsibilities rather than organisational hierarchy.

---

# 4. Administrator

## Business Responsibility

The Administrator is responsible for configuring and maintaining the InfraTrack platform.

Unlike operational roles, the Administrator supports the system itself rather than participating in day-to-day asset management activities.

The Administrator may be responsible for:

* managing user accounts;
* assigning security roles;
* managing departments;
* configuring asset categories;
* configuring Business Trigger types;
* maintaining reference data;
* configuring notification settings;
* managing system-wide configuration.

The Administrator is not responsible for:

* performing inspections;
* making Operational Decisions;
* creating Maintenance Activities;
* approving Completion Reviews;
* executing Work Orders.

These responsibilities belong to operational business roles.

An individual may hold both an Administrator role and an operational business role.

In such cases, responsibilities remain distinct.

Administrative permissions do not automatically grant operational authority, and operational authority does not imply administrative privileges.

---

# 5. Operational Coordinator

## Business Responsibility

The Operational Coordinator is responsible for coordinating day-to-day operational activities.

Unlike Managers, Operational Coordinators organise operational work rather than making strategic or high-level business decisions.

Their primary responsibility is to ensure that operational activities progress efficiently through the maintenance lifecycle.

Typical responsibilities include:

* registering Assets;
* creating Business Triggers;
* assigning Inspections;
* creating Work Orders following approved Operational Decisions;
* assigning Work Orders;
* coordinating Field Employees;
* coordinating Contractors;
* monitoring operational progress.

Operational Coordinators coordinate operational work.

They do not normally make Operational Decisions.

Operational Decisions are made by Managers or delegated authorised business roles according to council policy.

After an Operational Decision is approved, Operational Coordinators coordinate execution.

Operational Coordinators work closely with:

* Managers;
* Field Employees;
* Contractors;
* Administrators (for system support only).

They act as the operational link between business planning and field execution.

Managers decide.

Operational Coordinators coordinate.

Field Employees perform operational work.

Contractors execute assigned external work.

---

# 6. Manager

## Business Responsibility

Managers are operationally responsible for the assets owned by their department.

Managers provide operational leadership and approve significant operational decisions for assets owned by their department.

Typical responsibilities include:

* approving Operational Decisions;
* approving maintenance strategies;
* validating Completion Reviews;
* reviewing inspection evidence;
* monitoring operational performance;
* ensuring departmental accountability;
* reviewing Asset History.

Managers remain accountable for every operational decision within their department.

---

# 7. Field Employee

## Business Responsibility

Field Employees perform operational activities in the field.

They provide the operational evidence required for business decisions.

Typical responsibilities include:

* inspecting assets;
* recording observations;
* capturing photographs;
* reporting issues;
* completing maintenance activities;
* updating work progress;
* submitting completion reports.

Field Employees do not perform administrative validation.

Their role is operational rather than managerial.

---

# 8. Contractor

## Business Responsibility

Contractors perform specialised maintenance on behalf of the council.

Contractors participate only when assigned operational work.

Typical responsibilities include:

* accepting assigned work;
* completing maintenance activities;
* uploading completion evidence;
* providing completion reports.

Contractors do not:

* manage assets;
* validate work;
* configure the platform.

Operational accountability always remains with the council.

---

# 9. Responsibility Matrix

| Business Activity          | Admin | Coordinator | Manager | Field Employee | Contractor |
| -------------------------- | :---: | :---------: | :-----: | :------------: | :--------: |
| Configure Platform         |   ✓   |             |         |                |            |
| Manage Users               |   ✓   |             |         |                |            |
| Register Assets            |       |      ✓      |    ✓    |                |            |
| Create Business Trigger    |       |      ✓      |         |                |            |
| Assign Inspection          |       |      ✓      |         |                |            |
| Perform Inspection         |       |             |         |        ✓       |            |
| Record Issue               |       |             |         |        ✓       |            |
| Make Operational Decision  |       |             |    ✓    |                |            |
| Create Work Order          |       |      ✓      |         |                |            |
| Assign Work Order          |       |      ✓      |         |                |            |
| Perform Maintenance        |       |             |         |        ✓       |      ✓     |
| Complete Maintenance       |       |             |         |        ✓       |      ✓     |
| Validate Completion Review |       |             |    ✓    |                |            |
| Monitor KPIs               |       |      ✓      |    ✓    |                |            |



---

# 10. Operational Collaboration

Operational workflows require collaboration between multiple actors.

A typical workflow may involve:

```text
Manager
      │
      ▼
Operational Coordinator
      │
      ▼
Field Employee
      │
      ▼
Manager
      │
      ▼
Operational Coordinator
      │
      ▼
Contractor / Field Employee
      │
      ▼
Manager
```

Each participant contributes information required for the next operational decision.

No actor completes the entire workflow independently.

---

# 11. Accountability

Administrators maintain the platform.

Managers approve operational outcomes.

Operational Coordinators organise operational work.

Field Employees and Contractors perform operational work.

This separation improves traceability while reflecting common organisational practices.

---

# 12. Guiding Principles

## Responsibility before Permission

Software permissions should always reflect business responsibilities.

---

## Operational work requires evidence.

Every operational activity performed by a Field Employee or Contractor should generate evidence supporting future audits.

---

## Validation belongs to the council.

Operational validation cannot be delegated to external contractors.

---

## Managers remain accountable.

Although operational work may be delegated, responsibility for council assets always remains with departmental managers.

---

# Summary

## Business Reality

Infrastructure management is a collaborative process involving managers, operational staff and external contractors.

Responsibilities differ according to each participant's operational role.

---

## InfraTrack Decision

InfraTrack models five primary actors whose software permissions will later be derived directly from their business responsibilities.

This approach keeps the software aligned with operational reality while maintaining a simple and understandable security model.
