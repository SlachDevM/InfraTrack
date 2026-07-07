# Council Organisation

## Document Information

| Field    | Value                |
| -------- | -------------------- |
| Project  | InfraTrack           |
| Document | Council Organisation |
| Version  | 1.0                  |
| Status   | Living Document      |
| Phase    | Business Discovery   |

---

# 1. Purpose

This document describes how Australian Local Governments are commonly organised from an operational perspective and how InfraTrack models that organisational structure.

The objective is not to reproduce every possible council organisation but to establish a realistic operational model suitable for small and medium-sized Australian councils.

---

# 2. Business Reality

Australian Local Governments are generally organised into operational departments responsible for delivering public services.

Although department names vary between councils, their responsibilities remain broadly similar.

Typical operational departments include:

* Roads
* Parks and Gardens
* Buildings and Facilities
* Recreation
* Waste Management
* Water and Drainage
* Infrastructure Services

Each department manages its own public assets, operational staff and maintenance activities.

Operational managers are responsible for planning inspections, assigning work and ensuring service levels are maintained.

Field employees perform inspections and maintenance activities in accordance with departmental priorities.

When specialised work is required, councils frequently engage external contractors.

Operational activities are therefore distributed across multiple teams while remaining under departmental responsibility.

---

# 3. InfraTrack Decision

InfraTrack models councils using a simplified but realistic organisational hierarchy.

```text
Council

↓

Department

↓

Assets

↓

Operational Activities

↓

Operational History
```

Each operational activity belongs to exactly one department.

Departments become the primary organisational boundary for:

* assets;
* inspections;
* work orders;
* maintenance activities;
* reporting;
* operational dashboards.

This approach reflects common council practices while remaining simple to administer.

---

# 4. Organisational Structure

InfraTrack assumes the following operational hierarchy.

```text
Council

├── Roads Department

├── Parks Department

├── Facilities Department

├── Recreation Department

└── Waste Department
```

Each department operates independently while sharing the same software platform.

Departments may define their own assets, inspections and operational priorities.

---

# 5. Department Responsibilities

Departments are responsible for:

* managing public assets;
* planning inspections;
* assigning operational work;
* reviewing inspection results;
* validating completed maintenance;
* monitoring operational performance.

Departments do not manage users from other departments.

Operational responsibility remains clearly separated.

---

# 6. Asset Ownership

Every asset belongs to exactly one department.

Examples:

| Department | Typical Assets                      |
| ---------- | ----------------------------------- |
| Roads      | Streetlights, Signs, Roads          |
| Parks      | Playgrounds, BBQs, Park Furniture   |
| Facilities | Public Toilets, Community Buildings |
| Recreation | Sports Facilities, Swimming Pools   |
| Waste      | Public Bins, Recycling Stations     |

Asset ownership remains stable throughout the asset's lifecycle.

Operational activities may change.

Asset ownership does not.

---

# 7. Operational Collaboration

Although departments operate independently, operational collaboration occasionally occurs.

Examples include:

* shared inspections;
* contractor involvement;
* emergency maintenance;
* cross-department reporting.

InfraTrack supports collaboration without compromising departmental ownership.

---

# 8. Contractor Participation

External contractors are considered temporary operational participants.

Contractors:

* perform assigned work;
* submit operational evidence;
* upload completion documentation;
* report completed activities.

Contractors do not own assets.

Contractors do not perform administrative validation.

Operational responsibility always remains with the council.

---

# 9. Design Principles

The organisational model follows several guiding principles.

## Departments own assets.

Not individual employees.

---

## Operational responsibility belongs to departments.

Employees perform work on behalf of their department.

---

## Assets outlive organisational changes.

Departments, managers and employees may change over time.

The asset and its historical record remain.

---

## Contractors perform work.

Councils remain accountable.

Contractor activity becomes part of the council's operational history.

---

# 10. Future Evolution

The organisational model intentionally remains simple.

Future versions of InfraTrack may support:

* regional offices;
* operational depots;
* multi-council deployments;
* shared services;
* inter-council collaboration.

These capabilities remain outside the scope of the initial release.

---

# Summary

## Business Reality

Australian councils organise operational responsibilities through departments responsible for managing specific categories of public assets.

Operational work is performed by field employees and, when necessary, external contractors.

Administrative responsibility always remains with the council.

---

## InfraTrack Decision

InfraTrack models the council using a Department-centric organisational structure.

Departments become the primary organisational boundary for assets, inspections, maintenance activities, reporting and operational ownership.

This approach balances realism, simplicity and long-term maintainability.
