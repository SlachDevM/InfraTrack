# Asset Operational Lifecycle

## Document Information

| Field    | Value                       |
| -------- | --------------------------- |
| Project  | InfraTrack                  |
| Document | Asset Operational Lifecycle |
| Version  | 1.0                         |
| Status   | Draft                       |
| Phase    | Business Discovery          |

---

# 1. Purpose

This document describes how public assets evolve operationally throughout their lifetime within InfraTrack.

The objective is not to describe the physical lifecycle of an asset but the operational events that occur while the asset remains in service.

Every operational workflow within InfraTrack exists because of an asset.

---

# 2. Business Reality

Public infrastructure assets often remain in service for decades.

During that time, councils continuously inspect, maintain, repair and monitor these assets.

Although operational activities change constantly, the asset itself usually remains the same physical object.

Examples include:

* streetlights;
* playground equipment;
* public toilets;
* community buildings;
* park infrastructure;
* drainage systems.

Each asset accumulates operational history throughout its service life.

The asset therefore becomes the permanent reference point for every operational decision.

---

# 3. InfraTrack Decision

InfraTrack considers the Asset to be the central business entity of the platform.

Assets are long-lived.

Operational activities are temporary.

Every inspection, issue, maintenance activity, document and decision exists because of a specific asset.

The platform therefore stores operational history around the asset rather than around individual work orders.

---

# 4. Relationship with the Operational Lifecycle

The Asset is the permanent anchor of the operational lifecycle.

The complete lifecycle is defined in:
02-domain-overview.md

This document focuses only on how operational events accumulate around the Asset over time.

---

# 5. Operational History

Every operational cycle contributes to the permanent history of an asset.

Examples include:

* inspections;
* issues;
* maintenance activities;
* contractor interventions;
* generated reports;
* photographs;
* approvals;
* operational decisions.

Historical information must never be lost simply because operational work has been completed.

---

# 6. Asset Status

Every Asset has an operational status that reflects its current service availability throughout its lifecycle.

The complete Asset Status model, including:

* status definitions;
* permitted transitions;
* business rules;

is documented in:

**11-asset-status-model.md**

This document focuses on the operational lifecycle of Assets rather than Asset Status management.

---

# 7. Operational Activities

Operational activities are temporary.

They begin.

They evolve.

They complete.

Assets remain.

Typical operational activities include:

* inspections;
* maintenance;
* repairs;
* emergency interventions;
* contractor work;
* scheduled servicing.

Multiple operational activities may exist during the lifetime of one asset.

---

# 8. Operational Evidence

Every operational activity should produce evidence.

Evidence may include:

* photographs;
* inspection reports;
* maintenance reports;
* contractor reports;
* generated PDFs;
* cost estimates;
* observations;
* validation records.

Operational evidence becomes part of the permanent asset history.

---

# 9. Lifecycle Principles

## Assets are permanent.

Operational activities are temporary.

---

## Assets accumulate knowledge.

Every completed workflow increases the historical understanding of the asset.

---

## Operational history is never discarded.

Completed work remains valuable for future inspections, audits and planning.

---

## Every operational event has context.

No operational activity exists independently.

Every inspection, work order and report belongs to an asset.

---

## The asset is the anchor of the domain.

Every major business concept within InfraTrack ultimately relates back to an asset.

---

# Summary

## Business Reality

Australian councils manage assets over very long service lives.

Operational work continuously changes while the underlying asset remains stable.

Historical information becomes increasingly valuable over time.

---

## InfraTrack Decision

InfraTrack models the asset as the permanent centre of the business domain.

Every operational activity contributes to the evolving history of that asset.

Rather than managing isolated jobs, the platform manages the complete operational story of every public asset.
