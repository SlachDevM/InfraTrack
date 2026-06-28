# InfraTrack V2 — Business Architecture

## Purpose

This document defines the long-term business architecture of InfraTrack V2.

Unlike the technical Blueprint, this document describes the business concepts, their relationships and the evolution strategy of the platform.

It is the reference for all future business features.

---

# Vision

InfraTrack is no longer only an Asset Maintenance Management System.

Its objective is to become an **Asset Intelligence Platform**.

The platform should not only record maintenance activities but also accumulate operational knowledge, assist decision-making and continuously improve maintenance quality.

---

# Business Layers

## Layer 1 — Operational Execution

This layer represents day-to-day maintenance operations.

Business Trigger

↓

Inspection

↓

Issue

↓

Operational Decision

↓

Work Order

↓

Maintenance Activity

↓

Completion Review

This workflow remains the operational backbone of InfraTrack.

---

## Layer 2 — Knowledge Capture

Every workflow execution generates knowledge.

Knowledge includes:

* Issue Type
* Root Cause
* Corrective Action
* Preventive Action
* Lessons Learned
* Asset History
* Notifications
* Operational Documents

The objective is to preserve organisational knowledge.

---

## Layer 3 — Business Intelligence

Knowledge should later feed:

* Dashboards
* KPIs
* Reports
* Asset Intelligence
* AI assistance
* Preventive Maintenance

This layer never replaces business decisions.

It assists users.

---

# Issue Model

Issues now represent several business situations.

IssueType

* NORMAL
* REWORK

Future values may include:

* PREVENTIVE
* PREDICTIVE
* AUDIT

Issue classification must always be explicit.

Business logic must never infer Issue type from foreign key presence.

---

# CAPA

Every Issue may contain:

Root Cause

↓

Corrective Action

↓

Preventive Action

↓

Lessons Learned

CAPA information is optional during V2 but becomes the foundation of continuous improvement.

---

# Asset Knowledge

Assets progressively become intelligent entities.

Future capabilities:

* Manufacturer
* Model
* Serial Number
* Criticality
* Risk
* Warranty
* Parent / Child Assets
* Operational History
* Preventive Plans
* Templates

---

# Domain Engine

The Domain Engine will progressively introduce reusable business knowledge.

Examples:

Inspection Templates

↓

Checklists

↓

Decision Matrix

↓

Preventive Plans

↓

Knowledge Base

↓

Analytics

Business knowledge should be attached to Asset Categories rather than individual Assets whenever possible.

---

# Preventive Maintenance

Preventive Maintenance is generated from Preventive Plans.

Preventive Plans generate Business Triggers.

Business Triggers continue using the existing workflow.

No parallel workflow should be introduced.

---

# Mobile

Android is a client of the Business Engine.

It consumes:

* Templates
* Checklists
* Work Orders
* Notifications
* Assets
* Preventive Tasks

Business rules remain on the backend.

---

# Guiding Principles

* One business workflow.
* Explicit business concepts.
* Traceability before automation.
* Knowledge before artificial intelligence.
* Backend is the source of truth.
* Frontend improves usability but never owns business rules.
* New features should enrich the existing workflow rather than bypass it.

---

# Long-Term Vision

InfraTrack evolves through three stages:

## Stage 1

Operational Management

(V1)

---

## Stage 2

Business Knowledge

(Phase A)

---

## Stage 3

Operational Intelligence

(Android, Preventive Maintenance, Analytics, AI)

---

# Success Criteria

Every new feature added to InfraTrack should answer at least one of the following questions:

* Does it improve operational execution?
* Does it capture business knowledge?
* Does it improve future decision-making?

If the answer is "no", the feature should be reconsidered.

This principle guides the long-term evolution of the platform.
