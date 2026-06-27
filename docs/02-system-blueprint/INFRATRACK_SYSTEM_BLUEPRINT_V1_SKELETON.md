# INFRATRACK_SYSTEM_BLUEPRINT.md (V1 Skeleton)

## Purpose

This document is the living system blueprint for InfraTrack.

It defines the engineering philosophy, architecture principles, coding
standards, documentation expectations and AI collaboration rules used
throughout the project.

Unlike the Business Discovery documents, this blueprint is intended for
developers and AI assistants contributing to the project.

It is the primary reference that should be provided at the beginning of
every new AI session.

---

# Planned Structure

1. Purpose
2. Project Identity
3. Product Vision
4. Development Philosophy
5. Business Domain Summary
6. System Overview
7. Core Architecture Principles
8. Backend Architecture
9. React Responsibilities
10. Android Responsibilities
11. Offline First Philosophy
12. Security Model
13. Role Model
14. Operational Lifecycle
15. Business Rules
16. Domain Model Philosophy
17. Database Philosophy
18. API Design Rules
19. Java Coding Standards
20. React Coding Standards
21. Android Coding Standards
22. PostgreSQL Standards
23. Docker Standards
24. Testing Philosophy
25. Documentation Rules
26. AI Collaboration Rules
27. Prompt Templates
28. Review Process
29. Release Philosophy
30. Communication Style

---

## Guiding Principles

* Business drives architecture.
* Architecture drives implementation.
* The backend is the single source of business truth.
* Every feature is developed one business capability at a time.
* Every capability follows the lifecycle:

Business Discovery ↓ Use Case ↓ Architecture ↓ Implementation ↓ Testing
↓ Documentation

---

## Notes

This is intentionally a Version 1 skeleton.

Each chapter will be expanded carefully, following the same philosophy
used to build the MRRG blueprint, while adapting every section to
InfraTrack's domain.

The final blueprint is expected to become substantially larger than the
MRRG blueprint and act as the definitive engineering handbook for the
project.

# 1. Purpose

This document is the living system blueprint for InfraTrack.

It defines the engineering principles, architecture expectations, development workflow, coding standards, documentation rules and AI collaboration process used to design, implement and maintain the project.

InfraTrack is developed as a professional software product, not as a technology demonstration. Every contribution must therefore preserve the product’s business consistency, technical quality and long-term maintainability.

This blueprint is intended for:

* human developers;
* AI coding assistants;
* technical reviewers;
* architecture reviewers;
* documentation contributors.

When starting a new AI-assisted development session, this document should be provided before any implementation task so that the assistant understands the current project constraints, domain model and expected development process.

---

## Role of this Blueprint

This blueprint is not the user manual.

It is not the API documentation.

It is not the business discovery documentation.

Its purpose is to explain **how InfraTrack must be developed**.

It acts as the project’s engineering reference and collaboration contract.

Every implementation should be checked against this blueprint before being accepted.

---

## Source of Truth

This blueprint is the source of truth for:

* architecture principles;
* coding standards;
* AI collaboration rules;
* review expectations;
* prompt structure;
* documentation expectations;
* development workflow;
* quality standards.

Business-specific concepts remain defined in the Business Discovery documents.

Technical implementation details may evolve, but they must remain consistent with this blueprint unless an intentional architecture decision is made.

---

## Living Document

This document is expected to evolve.

When the project architecture, development workflow, collaboration process or quality expectations change, this blueprint must be updated.

Changes to this blueprint should be treated as significant project decisions because they affect how future contributors and AI assistants understand InfraTrack.

---

# 2. Project Identity

## Project Name

**InfraTrack**

---

## Project Category

Operational Asset Management Platform

---

## Target Audience

InfraTrack is designed for Australian Local Governments (Councils and Shires) responsible for managing public infrastructure and coordinating operational maintenance activities.

The platform focuses on supporting operational teams involved in:

* asset inspections;
* maintenance planning;
* field operations;
* contractor coordination;
* operational reporting;
* asset history management.

---

## Product Vision

InfraTrack is a modern Operational Asset Management Platform designed to support the complete operational lifecycle of public infrastructure.

The platform enables councils to coordinate inspections, record operational evidence, manage maintenance activities and preserve the complete operational history of every managed asset.

Rather than replacing existing Enterprise Asset Management (EAM) solutions, InfraTrack focuses on delivering an intuitive operational platform dedicated to field operations and day-to-day infrastructure management.

---

## Product Philosophy

InfraTrack is built around a simple principle:

> **Operational decisions should always be based on reliable field evidence.**

Every inspection, photograph, document, maintenance activity and operational decision contributes to the long-term history of an asset.

The software exists to support informed operational decisions rather than simply recording maintenance activities.

---

## Business Scope

Version 1 focuses exclusively on **Operational Asset Management**.

The project includes:

* Asset Registration
* Business Triggers
* Inspections
* Issue Recording
* Operational Decisions
* Work Orders
* Internal Maintenance
* Contractor Coordination
* Completion Reviews
* Asset History
* Operational Documents
* Dashboards and KPIs

The project intentionally excludes strategic asset planning, financial asset management and capital works planning.

Those capabilities may become future extensions but are not part of the current product vision.

---

## Primary Technology Stack

Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL

Frontend

* React
* TypeScript
* Material UI

Mobile

* Native Android
* Kotlin
* Room Database
* Offline-first synchronisation

Infrastructure

* Docker
* Docker Compose

---

## Core Design Principles

InfraTrack is designed around the following principles.

### Business First

Business understanding always precedes technical implementation.

---

### Decision Driven

The platform supports operational decision-making rather than simply recording completed work.

---

### Offline Ready

Field operations must remain functional even when network connectivity is unavailable.

Offline capability is considered a core requirement rather than an optional feature.

---

### Evidence Based

Operational decisions should always be supported by inspection evidence.

Photos, documents and observations are considered first-class business data.

---

### Traceability

Every operational action performed within the system should remain historically traceable.

Nothing important should disappear because work has been completed.

---

### Simplicity

Business processes should remain simple, explicit and understandable.

Complexity should only be introduced when justified by genuine business requirements.

---

## Intended Outcome

InfraTrack is intended to demonstrate the design and implementation of a production-quality enterprise application using modern software engineering practices.

The project showcases not only technical implementation skills but also business analysis, domain modelling, architectural thinking and long-term maintainability.

Every design decision should reinforce this objective.

---

# 3. Product Vision

## Purpose

This chapter defines the long-term vision of InfraTrack.

It explains why the platform exists, what business problem it solves and which engineering philosophy should guide future development.

Every implementation should reinforce this vision.

When making architectural or implementation decisions, AI assistants should verify that their proposed solution remains aligned with the principles described in this chapter.

---

# The Problem

Australian Local Governments are responsible for maintaining thousands of public assets distributed across large geographical areas.

Managing these assets requires continuous operational coordination between:

* administrative staff;
* operational managers;
* field employees;
* external contractors.

Operational information is often fragmented across emails, paper reports, spreadsheets, photographs and disconnected software systems.

This fragmentation makes it difficult to understand:

* why work was initiated;
* who performed it;
* what decisions were made;
* what evidence supported those decisions;
* how the asset has evolved over time.

InfraTrack exists to centralise this operational history.

---

# The Product

InfraTrack is an Operational Asset Management Platform.

Its responsibility is not to replace an Enterprise Asset Management (EAM) solution.

Its responsibility is to support the operational lifecycle of public infrastructure.

The platform focuses on:

* operational coordination;
* inspections;
* evidence collection;
* maintenance execution;
* operational decision support;
* historical traceability.

Future strategic asset planning may integrate with InfraTrack but is intentionally outside Version 1.

---

# Engineering Vision

InfraTrack is not built around database tables.

It is not built around REST endpoints.

It is not built around user interfaces.

InfraTrack is built around the business domain.

Every technical implementation should emerge naturally from business understanding.

When implementation decisions conflict with business consistency, business consistency always takes priority.

---

# Decision-Driven Development

InfraTrack is designed around operational decision-making.

The objective of an inspection is not to create maintenance work.

Its objective is to provide enough operational evidence to support an informed decision.

This distinction is fundamental.

AI assistants should avoid introducing implementations that automatically couple inspections, work orders and maintenance activities when business decisions are intended to separate these responsibilities.

---

# Evidence First

Operational evidence is one of the most valuable business assets managed by the platform.

Evidence includes:

* inspection observations;
* photographs;
* reports;
* documents;
* timestamps;
* operational notes.

Evidence should never be considered secondary information.

Whenever implementation choices exist, preserving operational evidence should be prioritised over implementation convenience.

---

# Operational Traceability

InfraTrack is designed around complete operational traceability.

Every significant business action should be explainable after it has occurred.

The system should always be capable of answering questions such as:

* Why was this inspection performed?
* Who inspected the asset?
* What issue was identified?
* Why was this operational decision made?
* Who approved the work?
* What evidence supported the decision?
* When did each event occur?

AI assistants should preserve this traceability when proposing database schemas, APIs and workflows.

---

# Long-Term Maintainability

InfraTrack is expected to evolve over many years.

New features should integrate naturally into the existing business model rather than introducing parallel workflows or duplicated concepts.

Whenever possible:

* extend existing concepts;
* avoid creating unnecessary entities;
* avoid introducing duplicate business concepts with different names.

Business consistency has higher priority than implementation speed.

---

# Scope Discipline

InfraTrack intentionally focuses on Operational Asset Management.

AI assistants should resist introducing features outside the current business scope unless explicitly requested.

Examples of capabilities outside the current scope include:

* financial asset management;
* procurement;
* inventory management;
* predictive maintenance;
* IoT integration;
* capital works planning;
* depreciation management;
* GIS editing.

These features may become future products or future project phases but should never be introduced implicitly.

---

# AI Design Principle

When assisting with InfraTrack development, always ask the following questions before proposing a solution.

1. Does this respect the business domain?
2. Does this simplify or unnecessarily complicate the architecture?
3. Does this preserve operational traceability?
4. Does this improve maintainability?
5. Is this solving a real business problem?

If the answer to one or more of these questions is "no", reconsider the proposed solution.

---

# Vision Statement

InfraTrack aims to become a professional Operational Asset Management Platform whose architecture, documentation and implementation demonstrate enterprise-level software engineering practices.

The project values business understanding, maintainability and technical consistency above feature quantity.

Every accepted contribution should move the project closer to that objective.

---

# 4. Development Philosophy

## Purpose

This chapter defines the development methodology used throughout InfraTrack.

It explains how business analysis, software architecture and implementation are connected.

All contributors, whether human or AI, are expected to follow this methodology.

The objective is to preserve architectural consistency throughout the lifetime of the project.

---

# Business Before Technology

InfraTrack is developed from the business domain outward.

Business understanding always precedes software implementation.

The project never starts by designing:

* database tables;
* REST APIs;
* user interfaces;
* entity relationships.

Instead, development always begins by understanding the business problem being solved.

Technology exists to implement business behaviour.

Business behaviour must never be modified simply to simplify implementation.

---

# Domain-Driven Development

The domain model is the foundation of the entire project.

Business concepts are identified before implementation.

Examples include:

* Asset
* Business Trigger
* Inspection
* Issue
* Operational Decision
* Work Order
* Maintenance Activity
* Completion Review
* Asset History

These concepts represent the business.

They are not technical abstractions.

AI assistants should avoid introducing additional domain concepts unless they represent genuine business requirements.

---

# One Business Capability at a Time

InfraTrack is developed incrementally.

Each iteration focuses on a single business capability.

Examples include:

* Register Asset
* Perform Inspection
* Create Work Order
* Complete Maintenance

The objective is to deliver complete vertical slices of functionality rather than isolated technical components.

---

# Standard Development Workflow

Every business capability follows the same development lifecycle.

```text
Business Discovery
        │
        ▼
Functional Analysis
        │
        ▼
Use Case
        │
        ▼
Technical Design
        │
        ▼
Implementation
        │
        ▼
Testing
        │
        ▼
Documentation
```

No implementation should skip any of these stages.

If business behaviour is unclear, implementation should pause until the behaviour has been clarified.

---

# Vertical Slice Development

InfraTrack is developed using vertical slices.

A capability is considered complete only when all application layers have been implemented.

This includes:

* backend implementation;
* database persistence;
* React interface;
* Android interface (when applicable);
* security;
* tests;
* documentation.

Avoid implementing large horizontal layers independently.

For example:

❌ Implement every database entity first.

❌ Build every REST endpoint before any UI exists.

Preferred approach:

✔ Complete one capability from backend to user interface before moving to the next.

---

# Business Consistency

Business consistency has higher priority than implementation convenience.

AI assistants should never modify business behaviour simply because another implementation appears technically simpler.

If implementation becomes difficult, revisit the architecture rather than changing the business domain.

---

# Simplicity Over Cleverness

InfraTrack intentionally favours simple, explicit solutions.

Avoid introducing complexity unless it provides measurable business value.

Examples of unnecessary complexity include:

* premature abstraction;
* speculative extensibility;
* generic frameworks;
* unnecessary inheritance hierarchies;
* overuse of design patterns.

Readable code is preferred over clever code.

Explicit code is preferred over magical code.

---

# Incremental Evolution

The architecture is expected to evolve.

However, evolution should occur intentionally.

Avoid speculative architecture designed for hypothetical future requirements.

Build only what is required to support the current business capability.

Future requirements should influence architecture only when they are reasonably certain to occur.

---

# Refactoring Philosophy

Refactoring is encouraged when it improves:

* readability;
* maintainability;
* consistency;
* business clarity.

Refactoring should not:

* change business behaviour;
* introduce unnecessary abstractions;
* expand project scope.

During bug fixes, refactoring should remain minimal and focused.

---

# Decision-Driven Engineering

Whenever multiple technical solutions are possible, contributors should evaluate them using the following order of priority.

1. Business correctness
2. Architectural consistency
3. Maintainability
4. Readability
5. Performance

Micro-optimisations should never compromise business clarity.

---

# AI Responsibilities

AI assistants are expected to behave as senior software engineers.

They should:

* analyse requirements;
* identify architectural risks;
* challenge inconsistent decisions;
* explain trade-offs;
* preserve project consistency.

AI assistants should not:

* blindly implement requests;
* introduce unnecessary complexity;
* invent business rules;
* silently modify project architecture;
* prioritise novelty over maintainability.

Whenever uncertainty exists, the assistant should ask for clarification rather than making assumptions.

---

# Definition of Done

A business capability is considered complete only when:

* business behaviour matches the approved Use Case;
* backend implementation is complete;
* frontend implementation is complete;
* Android implementation is complete (if applicable);
* security rules are enforced;
* tests pass;
* documentation has been updated.

Partial implementation should not be considered complete.

---

# Engineering Principle

InfraTrack is not developed feature by feature.

It is developed capability by capability.

Every completed capability should represent a coherent piece of business value that can be understood, tested and maintained independently.

---

# 5. Business Domain Summary

## Purpose

This chapter provides a concise summary of the InfraTrack business domain.

It is intended primarily for AI assistants and new contributors who need to understand the project's operational model before implementing new functionality.

This chapter is not a replacement for the Business Discovery documentation.

Instead, it acts as a condensed reference containing the essential business concepts that every implementation must respect.

---

# Business Context

InfraTrack is an Operational Asset Management Platform developed for Australian Local Governments.

Its purpose is to manage the operational lifecycle of public infrastructure.

Typical managed assets include:

* streetlights;
* playgrounds;
* public toilets;
* parks;
* community facilities;
* public BBQs;
* traffic infrastructure;
* other council-owned assets.

The platform supports operational teams responsible for inspecting, maintaining and documenting these assets.

---

# Core Business Philosophy

InfraTrack is not a maintenance application.

It is not a work order application.

It is not a ticketing system.

InfraTrack is a decision-driven operational platform.

Its objective is to preserve the complete operational history of every managed asset while supporting informed maintenance decisions.

Every feature should reinforce this philosophy.

---

# Core Business Concepts

The operational domain is built around a small number of well-defined concepts.

These concepts should remain stable throughout the lifetime of the project.

```text
Asset
↓
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
↓
Asset History
```

Each concept has a single responsibility.

AI assistants should avoid combining multiple business responsibilities into a single entity.

---

# Operational Philosophy

Operational work always begins with a Business Trigger.

Examples include:

* scheduled inspections;
* customer requests;
* emergency events;
* manager decisions;
* field observations.

A Business Trigger does not determine what work will be performed.

It simply explains why operational attention is required.

---

# Inspections

An Inspection records the current condition of an Asset.

Its objective is to collect operational evidence.

Possible inspection outcomes include:

* no issue identified;
* issue identified.

If no issue is identified:

* the inspection is closed;
* asset history is updated.

If an issue is identified:

* an Operational Decision becomes necessary.

Inspections do not automatically create maintenance work.

---

# Operational Decisions

Operational Decisions separate observation from execution.

Possible outcomes include:

* continue monitoring;
* internal maintenance;
* contractor work;
* renewal recommendation;
* decommission recommendation.

The decision determines the next operational step.

The inspection only provides evidence.

---

# Work Orders

A Work Order exists only when physical work has been approved.

Its purpose is to organise maintenance.

A Work Order defines:

* responsibility;
* priority;
* scheduling;
* execution.

It does not represent completed work.

---

# Maintenance Activities

Maintenance Activities record what was actually performed in the field.

Operational evidence may include:

* photographs;
* notes;
* reports;
* timestamps.

Maintenance Activities contribute directly to the Asset History.

---

# Completion Review

Not every maintenance activity requires administrative review.

Routine maintenance may be closed automatically.

Higher-risk work or contractor-performed activities may require a Completion Review before the operational cycle is closed.

AI assistants should never assume that every maintenance activity requires approval.

---

# Asset History

Asset History is one of the most important business concepts within InfraTrack.

Its objective is to preserve the complete operational history of every Asset.

Asset History includes:

* inspections;
* issues;
* operational decisions;
* maintenance activities;
* documents;
* photographs;
* completion reviews.

Historical information should never be discarded simply because work has been completed.

---

# Business Scope

Version 1 intentionally focuses on Operational Asset Management.

The following capabilities are outside the current scope:

* capital works;
* strategic asset planning;
* financial asset management;
* procurement;
* inventory management;
* predictive maintenance;
* IoT integration;
* GIS editing.

AI assistants should not introduce these concepts unless explicitly requested.

---

# Business Invariants

The following principles should always remain true.

* Every Inspection belongs to one Asset.
* Every Inspection originates from one Business Trigger.
* Every Issue originates from one Inspection.
* Every Work Order results from one Operational Decision.
* Every Maintenance Activity belongs to one Work Order.
* Every operational event contributes to the Asset History.

These rules represent the backbone of the business domain.

They should not be modified without explicit architectural discussion.

---

# AI Reminder

Before proposing any implementation, always ask:

* Which business concept is being modified?
* Does this change respect the existing domain model?
* Is a new entity really necessary?
* Does the implementation preserve operational traceability?
* Does it simplify or complicate the business model?

Business consistency always takes priority over implementation convenience.

---

# 6. System Overview

## Purpose

This chapter describes the high-level architecture of InfraTrack.

Its objective is to help contributors understand how the major system components interact before discussing implementation details.

This chapter intentionally remains technology-agnostic where possible.

Technology choices are explained in later chapters.

---

# System Philosophy

InfraTrack is composed of several independent applications working together around a single business domain.

Each application has a clearly defined responsibility.

Business logic must never be duplicated across applications.

The backend is always the authoritative source of business truth.

---

# High-Level Architecture

```text
                    +----------------------+
                    |     React Client     |
                    | Administration Portal|
                    +----------+-----------+
                               |
                               | HTTPS / REST API
                               |
                               ▼
                  +---------------------------+
                  |      Spring Boot API      |
                  | Business & Domain Logic   |
                  +-------------+-------------+
                                |
                +---------------+---------------+
                |                               |
                ▼                               ▼
        PostgreSQL Database            File Storage
        Business Data                  Photos / Documents
                ▲
                |
         Synchronisation
                |
                ▼
        +----------------------+
        |   Android Client     |
        | Offline Field App    |
        +----------------------+
```

---

# Core Components

InfraTrack consists of four primary components.

## Spring Boot Backend

The backend contains:

* business rules;
* domain model;
* security;
* validation;
* persistence;
* REST API;
* synchronisation logic.

The backend is responsible for enforcing business consistency.

No client application may bypass backend business rules.

---

## React Application

The React application is the administrative portal.

Typical users include:

* managers;
* administrators;
* operational coordinators.

Responsibilities include:

* asset management;
* dashboards;
* reporting;
* work planning;
* completion reviews;
* document management.

Business rules should never be implemented exclusively inside React.

React displays and interacts with business information.

The backend validates it.

---

## Android Application

The Android application supports field operations.

Typical users include:

* field employees;
* contractors.

Responsibilities include:

* inspections;
* photographs;
* maintenance reports;
* QR code scanning;
* offline operation;
* synchronisation.

Android is designed for operational efficiency.

It should minimise user interaction while maximising data quality.

---

## PostgreSQL

PostgreSQL stores the permanent operational history of the platform.

The database represents the long-term memory of InfraTrack.

Business logic should never depend on database-specific behaviour.

Business rules belong in the application layer.

---

# System Responsibilities

The responsibilities of each component are intentionally separated.

| Component   | Primary Responsibility        |
| ----------- | ----------------------------- |
| React       | Administrative user interface |
| Android     | Field operations              |
| Spring Boot | Business logic                |
| PostgreSQL  | Persistent storage            |

Every component should remain focused on its own responsibility.

---

# Source of Truth

InfraTrack follows a strict Source of Truth philosophy.

## Backend

The Spring Boot backend is the only authoritative source of business truth.

Only the backend may:

* validate business rules;
* create domain objects;
* enforce permissions;
* apply business decisions;
* update operational history.

---

## Clients

React and Android are presentation applications.

They may perform user experience validation.

They must never become the source of business truth.

Any validation implemented on the client should also exist on the backend.

---

# Offline Architecture

Offline support exists exclusively to improve field operations.

Offline storage is temporary.

It is not the permanent business record.

Once connectivity becomes available:

Android

↓

Synchronises

↓

Spring Boot validates

↓

PostgreSQL persists

If conflicts occur, the backend determines the final business state.

---

# File Management

Operational evidence such as:

* photographs;
* reports;
* PDF documents;

is considered part of the business domain.

Files should never exist independently of the business objects they describe.

Every uploaded file must be traceable to an operational event.

---

# Security Boundary

Authentication and authorisation are enforced by the backend.

Client applications should never rely on hidden UI elements as security mechanisms.

Permissions must always be verified server-side.

---

# Evolution Strategy

InfraTrack is expected to evolve gradually.

Future applications may be added without modifying the existing business domain.

Examples include:

* GIS viewer;
* citizen portal;
* reporting portal;
* analytics platform.

These applications should consume the same backend rather than implementing separate business logic.

---

# AI Design Rules

When proposing new features:

Always identify which component owns the responsibility.

Before implementing any logic, ask:

* Should this be handled by the backend?
* Should this be handled by React?
* Should this be handled by Android?
* Should this remain inside the database?

If multiple components appear capable of performing the same responsibility, choose the one closest to the business domain.

Business rules almost always belong in the backend.

Avoid duplicating business behaviour across applications.

---

# 7. Core Architecture Principles

## Purpose

This chapter defines the architectural principles that govern every implementation within InfraTrack.

These principles are intentionally independent of frameworks and programming languages.

They describe **how architectural decisions should be made**, not merely **which technologies should be used**.

Every contributor should understand these principles before modifying the codebase.

---

# Business Domain First

The business domain is the foundation of the entire architecture.

Architecture exists to support the business.

The business never exists to satisfy the architecture.

When technical constraints conflict with business correctness, contributors should first attempt to improve the architecture rather than modifying the business model.

---

# The Domain Drives Everything

Every implementation should originate from an existing business concept.

Examples include:

* Asset
* Business Trigger
* Inspection
* Issue
* Operational Decision
* Work Order
* Maintenance Activity
* Completion Review
* Asset History

If a proposed implementation cannot be linked to an existing business concept, reconsider whether it belongs in the project.

New domain concepts should only be introduced after explicit discussion.

---

# Single Responsibility

Every architectural component should have one clear responsibility.

Examples:

An Inspection records observations.

It does not decide maintenance.

An Operational Decision determines the response.

It does not execute work.

A Work Order organises work.

It does not prove completion.

A Maintenance Activity records completed work.

It does not approve itself.

A Completion Review confirms completed work when required.

Maintaining these boundaries is essential.

---

# Backend Owns Business Behaviour

The backend is responsible for:

* business rules;
* validation;
* permissions;
* workflows;
* operational consistency.

No business behaviour should exist exclusively in React or Android.

Client applications may improve usability.

They must never redefine business logic.

---

# Thin Clients

React and Android are intentionally thin.

Their responsibilities include:

* displaying information;
* collecting user input;
* improving user experience;
* performing lightweight client-side validation.

Clients should not:

* make business decisions;
* enforce permissions;
* own workflow logic;
* maintain business state independently.

Whenever uncertainty exists, place the behaviour in the backend.

---

# Explicit Architecture

InfraTrack favours explicit architecture over hidden behaviour.

Preferred:

```java
inspectionService.completeInspection(...)
```

Avoid:

* hidden callbacks;
* implicit side effects;
* magical framework behaviour;
* excessive annotations that obscure business logic.

Business behaviour should be understandable by reading the application code.

---

# Composition Over Complexity

Prefer small, composable services rather than large multi-purpose classes.

Large service classes should usually indicate that responsibilities are becoming mixed.

AI assistants should actively identify opportunities to simplify responsibilities without introducing unnecessary abstractions.

---

# Avoid Generic Solutions

Generic code should never exist simply because it appears elegant.

InfraTrack models a real business domain.

The architecture should therefore reflect business terminology.

Avoid introducing concepts such as:

* GenericEntity
* GenericWorkflow
* BaseManager
* AbstractBusinessObject

unless they solve an actual business problem.

Explicit domain terminology is preferred.

---

# Domain Language in Code

Code should use exactly the same terminology defined in the Business Discovery documentation.

Example:

```text
InspectionService
InspectionController
InspectionRepository
InspectionMapper
InspectionDto
InspectionEntity
```

Avoid creating synonyms.

For example:

❌ JobService

if the business concept is:

✔ WorkOrderService

Consistency of language improves maintainability.

---

# Predictability

The architecture should remain predictable.

Developers should be able to locate functionality without searching the entire project.

If one service behaves differently from every other service, reconsider its implementation.

Consistency is generally more valuable than cleverness.

---

# Evolution Without Disruption

InfraTrack will continue to evolve.

New capabilities should integrate naturally into the existing architecture.

Avoid introducing parallel workflows or duplicate implementations.

Whenever possible:

* extend existing concepts;
* preserve naming consistency;
* preserve package organisation;
* preserve business terminology.

---

# AI Architectural Checklist

Before proposing any implementation, verify:

□ Which business concept owns this behaviour?

□ Does this introduce a new responsibility?

□ Is a new abstraction genuinely required?

□ Could an existing component be extended instead?

□ Does the implementation preserve business terminology?

□ Does it make the architecture easier to understand?

If one or more answers are uncertain, reconsider the proposed design before implementation.

---

# Engineering Principle

Architecture is not measured by the number of design patterns used.

Architecture is measured by how clearly it expresses the business domain.

Every architectural decision should make the business easier to understand, not more difficult.

---

# 8. Backend Architecture

## Purpose

This chapter defines the architectural organisation of the Spring Boot backend.

It describes how business logic should be structured, where responsibilities belong and how the different application layers interact.

Every backend contribution must follow these architectural rules.

Consistency is considered more valuable than personal coding preferences.

---

# Architectural Philosophy

The backend is the heart of InfraTrack.

It is responsible for:

* enforcing business rules;
* protecting business consistency;
* coordinating operational workflows;
* validating user actions;
* persisting business data.

No other application is allowed to redefine business behaviour.

The backend is the single source of truth.

---

# Architectural Style

InfraTrack follows a layered architecture inspired by Domain-Driven Design principles.

The objective is not to implement DDD mechanically.

The objective is to keep business behaviour clearly separated from technical concerns.

Each layer has a single responsibility.

---

# Layer Overview

```text
Controller
      │
      ▼
Application Service
      │
      ▼
Domain
      │
      ▼
Repository
      │
      ▼
Database
```

Dependencies always point downwards.

Reverse dependencies should never exist.

---

# Controller Layer

Controllers expose REST endpoints.

Responsibilities include:

* receiving HTTP requests;
* validating request format;
* calling application services;
* returning HTTP responses.

Controllers should remain extremely small.

Controllers must never:

* implement business rules;
* perform persistence;
* make operational decisions;
* communicate directly with repositories.

Preferred:

```java
inspectionService.completeInspection(...)
```

Avoid:

```java
inspectionRepository.save(...)
```

inside controllers.

---

# Application Services

Application Services orchestrate business operations.

Typical responsibilities include:

* coordinating multiple domain services;
* transaction boundaries;
* permission verification;
* workflow execution;
* calling repositories.

Application Services should describe business actions.

Examples:

```text
AssetService
InspectionService
WorkOrderService
MaintenanceService
AuthenticationService
```

Service names should always represent business capabilities.

---

# Domain Model

The domain model represents the business.

Entities should contain business meaning rather than acting as simple database records.

Domain objects should remain consistent throughout their lifecycle.

Business terminology should always match the Business Discovery documentation.

Avoid introducing technical terminology into the domain.

---

# Repository Layer

Repositories provide persistence access.

Responsibilities:

* loading entities;
* saving entities;
* querying entities.

Repositories should never:

* implement workflows;
* enforce permissions;
* perform business validation.

Repositories answer one question:

> How is business data stored?

They do not answer:

> How does the business behave?

---

# DTO Layer

DTOs exist to isolate the REST API from the domain model.

DTOs should contain only information required by the API.

They should never contain business behaviour.

Separate DTOs should be created when business requirements differ.

Avoid creating universal DTOs intended to satisfy every endpoint.

---

# Mapper Layer

Mappers convert between:

* DTOs;
* domain entities;
* response models.

Mappers should perform data transformation only.

Business rules should never exist inside mappers.

---

# Validation

Two kinds of validation exist.

## Request Validation

Performed using Jakarta Validation.

Examples:

* required fields;
* string length;
* email format.

---

## Business Validation

Performed inside application services.

Examples:

* permission checks;
* workflow rules;
* operational constraints;
* business invariants.

Business validation must never rely solely on annotations.

---

# Transactions

Transactions belong to the service layer.

Controllers should never manage transactions.

Repositories should never manage business transactions.

A transaction should represent one complete business operation.

---

# Exception Handling

Business exceptions should be explicit.

Examples:

```text
AssetNotFoundException
InspectionAlreadyCompletedException
UnauthorizedInspectionException
WorkOrderClosedException
```

Avoid generic exceptions.

Business errors should describe business problems.

---

# Package Organisation

The backend should be organised by business capability rather than technical layer.

Preferred:

```text
asset/
inspection/
workorder/
maintenance/
notification/
security/
```

Inside each module:

```text
controller/
service/
repository/
domain/
dto/
mapper/
exception/
```

Avoid organising the entire project like this:

```text
controllers/
services/
repositories/
entities/
```

Feature-first organisation scales better and keeps related code together.

---

# Module Independence

Each module owns its own business responsibility.

Modules may collaborate.

They should not become tightly coupled.

Whenever possible:

* communicate through services;
* expose clear interfaces;
* avoid circular dependencies.

---

# Logging

Logs should describe business operations.

Preferred:

```text
Inspection completed
Work Order created
Maintenance assigned
```

Avoid logs describing implementation details only.

Logs should help reconstruct operational history during debugging.

---

# AI Implementation Rules

Before creating any new backend class, verify:

* Which business capability does it belong to?
* Which module owns this responsibility?
* Does an existing service already solve this problem?
* Does this class introduce unnecessary coupling?
* Does its name match the Business Discovery terminology?

Never create a new class simply because it "might be useful later."

---

# Engineering Principle

The backend should read like the business.

A developer unfamiliar with InfraTrack should be able to understand the operational workflow simply by reading the service names, package structure and business objects.

If the architecture no longer reflects the business domain, it should be considered for refactoring before adding new functionality.

---

# 9. React Responsibilities

## Purpose

This chapter defines the role and responsibilities of the React application within the InfraTrack architecture.

The React application is the administrative interface of the platform.

Its purpose is to provide an efficient user experience for council employees while respecting the architectural principles defined throughout this blueprint.

React is not responsible for enforcing business rules.

Business consistency always remains the responsibility of the backend.

---

# Application Philosophy

The React application exists to help users interact with the business domain.

It presents information.

It collects user input.

It visualises operational data.

It never becomes the owner of business behaviour.

Whenever implementation decisions are required, business logic should remain inside the backend whenever possible.

---

# Target Users

The React application is primarily intended for office-based users.

Typical roles include:

* Administrator
* Manager
* Operational Coordinator

These users perform activities such as:

* registering assets;
* creating business triggers;
* assigning inspections;
* creating work orders;
* reviewing completed work;
* viewing dashboards;
* generating reports.

Field operations are intentionally delegated to the Android application.

---

# Primary Responsibilities

React is responsible for:

* displaying business information;
* collecting user input;
* navigation;
* dashboards;
* search and filtering;
* document viewing;
* reporting interfaces;
* administrative workflows.

React should optimise usability without changing business behaviour.

---

# What React Must Never Do

React must never become responsible for:

* business validation;
* permission enforcement;
* workflow decisions;
* operational rules;
* persistence logic;
* audit history.

These responsibilities belong to the backend.

If React performs client-side validation, the backend must perform the same validation independently.

---

# State Management Philosophy

React state represents the current user interface.

It does not represent the permanent business state.

Examples of UI state:

* selected asset;
* open dialog;
* current filters;
* pagination;
* temporary form values.

Examples of business state:

* inspection status;
* work order status;
* operational decisions;
* asset history.

Business state always originates from the backend.

---

# Component Philosophy

Components should have a single responsibility.

Preferred examples:

```text
AssetTable
AssetDetailsCard
InspectionTimeline
WorkOrderForm
DashboardCard
```

Avoid components responsible for multiple unrelated business concepts.

Large components should be decomposed into smaller reusable components.

---

# Page Organisation

Pages should represent business capabilities.

Examples:

```text
Assets
Business Triggers
Inspections
Work Orders
Maintenance
Reports
Administration
```

Avoid organising pages around technical concepts.

Navigation should reflect how council employees think about their work.

---

# Forms

Forms should remain simple and predictable.

Responsibilities include:

* collecting user input;
* displaying validation errors;
* submitting requests;
* displaying backend responses.

Forms should never implement business workflows.

---

# API Communication

Every business operation should pass through the REST API.

React must never:

* bypass backend validation;
* manipulate database identifiers directly;
* simulate business workflows locally.

API calls should remain explicit and predictable.

---

# Error Handling

React should present clear, user-friendly error messages.

Unexpected technical details should never be exposed to users.

Business errors returned by the backend should be displayed using terminology consistent with the Business Discovery documentation.

---

# Performance Philosophy

Optimise user experience.

Do not optimise by moving business logic into React.

Performance improvements should include:

* pagination;
* lazy loading;
* request caching where appropriate;
* efficient rendering.

Business correctness always has higher priority than rendering performance.

---

# UI Consistency

The user interface should remain consistent across the application.

Examples include:

* identical buttons for identical actions;
* consistent colours;
* predictable navigation;
* common dialog behaviour;
* shared form components.

Users should not need to relearn the interface when moving between modules.

---

# AI Implementation Rules

Before implementing a React feature, verify:

* Is this purely presentation logic?
* Should this behaviour exist in the backend instead?
* Does this component represent a single business responsibility?
* Is the terminology consistent with the Business Discovery documents?
* Would another developer immediately understand this component?

Avoid creating components that mix presentation, business rules and networking logic.

---

# Engineering Principle

The React application should feel like a professional administrative tool.

It should be intuitive, predictable and responsive while remaining completely aligned with the business behaviour implemented by the backend.

A clean user interface is valuable.

A consistent architecture is essential.

---

# 10. Android Responsibilities

## Purpose

This chapter defines the responsibilities of the Android application within the InfraTrack ecosystem.

Unlike the React application, the Android client is designed specifically for field operations.

Its primary objective is to maximise operational efficiency while collecting reliable business evidence directly from the field.

The Android application is not a simplified administration portal.

It is a specialised operational tool.

---

# Application Philosophy

Field employees work under very different conditions than office staff.

They often operate:

* outdoors;
* under time pressure;
* with poor or no network coverage;
* while wearing PPE;
* using only one hand;
* in bright sunlight;
* in adverse weather conditions.

Every Android implementation should prioritise operational efficiency over feature richness.

---

# Target Users

Typical Android users include:

* Field Employees
* Maintenance Technicians
* Supervisors (field operations)
* External Contractors

These users perform operational work.

They do not administer the platform.

---

# Primary Responsibilities

The Android application is responsible for:

* receiving assigned work;
* scanning QR codes;
* locating assets;
* performing inspections;
* recording issues;
* capturing photographs;
* completing maintenance activities;
* synchronising offline work.

Its objective is to minimise friction during field operations.

---

# User Experience Philosophy

Every unnecessary interaction reduces productivity.

The Android application should minimise:

* typing;
* navigation;
* repeated data entry;
* unnecessary confirmations.

Preferred interaction methods include:

* QR code scanning;
* dropdown selections;
* predefined values;
* photo capture;
* GPS acquisition;
* voice input (future capability).

---

# Offline-First Philosophy

Offline capability is a core architectural requirement.

It is not an optional enhancement.

The Android application must continue operating when:

* mobile coverage is unavailable;
* Wi-Fi is unavailable;
* synchronisation is temporarily impossible.

Field operations must never stop because connectivity has been lost.

---

# Local Storage

Local storage exists only to support offline work.

Its purpose is to temporarily retain:

* assigned inspections;
* work orders;
* photographs;
* inspection reports;
* maintenance reports.

Local storage should never become the permanent operational record.

The backend remains the authoritative source of business truth.

---

# Synchronisation Philosophy

Synchronisation should be predictable and resilient.

Whenever connectivity becomes available:

Android

↓

Synchronisation

↓

Spring Boot Validation

↓

Database Persistence

↓

Confirmation

The backend validates every synchronised operation.

Android should never assume that local data has become permanent until synchronisation has been confirmed.

---

# Photo Capture

Photographs are considered business evidence.

Every photograph should be linked to an operational event.

Examples include:

* Inspection
* Maintenance Activity
* Completion Evidence

Photos should never exist without business context.

---

# GPS

GPS information supports operational traceability.

When available, inspections and maintenance activities should include location information.

GPS should improve operational evidence.

It should not prevent users from completing critical work when location services are unavailable.

---

# QR Codes

QR codes provide rapid identification of physical assets.

Scanning a QR code should immediately retrieve the associated asset information.

QR codes should never contain sensitive business information.

They should function only as asset identifiers.

---

# Notifications

Android notifications should assist operational work.

Typical notifications include:

* new assignment;
* updated work order;
* synchronisation result;
* urgent operational request.

Notifications should inform users.

They should not become the permanent business record.

---

# Security

Authentication is managed by the backend.

Android should securely store authentication credentials using platform security mechanisms.

Sensitive business data should never be stored without appropriate protection.

Local cached information should remain encrypted whenever practical.

---

# Performance Philosophy

Field users value responsiveness over visual effects.

Preferred characteristics include:

* fast startup;
* immediate camera access;
* rapid QR scanning;
* responsive forms;
* minimal waiting time.

Animations and visual effects should never reduce operational efficiency.

---

# AI Implementation Rules

Before implementing Android functionality, verify:

* Does this improve field productivity?
* Does it reduce user interaction?
* Does it preserve business evidence?
* Does it continue to function offline?
* Does it rely on the backend for business decisions?

Avoid implementing administrative workflows on Android unless explicitly requested.

---

# Engineering Principle

The Android application is an operational field tool.

Every design decision should help field employees complete inspections and maintenance activities as quickly, safely and accurately as possible.

Operational efficiency always has higher priority than interface sophistication.

---

# 11. Offline First Philosophy

## Purpose

This chapter defines the offline architecture philosophy of InfraTrack.

Offline capability is not considered an optional feature.

It is one of the fundamental architectural constraints of the platform.

Every implementation affecting the Android application must remain compatible with offline operation unless explicitly stated otherwise.

---

# Why Offline Matters

InfraTrack is designed for Australian Local Governments.

Field employees frequently operate:

* in remote locations;
* inside public facilities with poor reception;
* in underground infrastructure;
* in parks and reserves;
* during emergency situations.

Reliable network connectivity cannot be assumed.

Operational work must continue regardless of network availability.

---

# Offline Is a Business Requirement

Offline capability exists because of business requirements.

It is not implemented to improve application performance.

The primary objective is to ensure uninterrupted operational workflows.

An inspection delayed because of poor reception is considered a business failure.

---

# Backend Remains the Source of Truth

Offline support does not change the ownership of business data.

Business ownership remains:

```text id="w0zpkh"

Android

↓

Temporary Local Storage

↓

Synchronisation

↓

Spring Boot Validation

↓

PostgreSQL

↓

Official Business Record

```
The Android application temporarily stores operational work.
The backend validates and accepts it.
Only after backend validation does the information become part of the permanent business history.
---
# Local Database
The local database exists to support operational continuity.
Typical locally stored data includes:
* assigned inspections;
* assigned work orders;
* asset information;
* captured photographs;
* maintenance reports;
* synchronisation queue.
Local data should never be considered authoritative.
It represents pending operational work.
---
# Synchronisation Philosophy
Synchronisation is eventually consistent.
Temporary differences between Android and the backend are expected.
The objective is reliable synchronisation rather than immediate consistency.
Whenever connectivity becomes available:
1. Pending operations are uploaded.
2. The backend validates each operation.
3. Successful operations become permanent.
4. Android updates its local state.
5. Synchronisation status is refreshed.
---
# Synchronisation Strategy
Synchronisation should always be:
* resumable;
* fault tolerant;
* incremental;
* repeatable;
* idempotent whenever possible.
Interrupted synchronisation should continue safely without creating duplicate operational events.
---
# Business Conflicts
Conflicts are expected.
Examples include:
* an asset updated while the device was offline;
* a work order cancelled before synchronisation;
* another employee completing the same maintenance activity.
The backend is responsible for resolving business conflicts.
Android should report conflicts to the user but should never resolve them independently.
---
# Local Identifiers
Temporary local identifiers may exist before synchronisation.
Permanent identifiers are assigned by the backend.
Android should be capable of replacing temporary identifiers with permanent identifiers without affecting the user experience.
---
# Photo Synchronisation
Photographs may be captured before network connectivity exists.
Images should:
* remain locally available;
* preserve metadata;
* remain associated with their operational event;
* upload automatically when possible.
A photograph should never lose its business relationship because synchronisation was interrupted.
---
# Synchronisation Queue
All pending operations should be processed through a dedicated synchronisation queue.
The queue should:
* preserve execution order where required;
* survive application restarts;
* tolerate temporary failures;
* retry safely.
Business operations should never disappear because the application was closed.
---
# User Experience
Users should always understand the current synchronisation state.
Examples include:
* Pending
* Synchronising
* Synchronized
* Failed
* Conflict Detected
The application should never silently discard operational work.
---
# Engineering Principles
Offline support should be invisible whenever possible.
Users should interact with business workflows rather than synchronisation mechanics.
The platform should reduce operational interruptions instead of introducing additional complexity.
---
# AI Implementation Rules
Before implementing any Android feature, verify:
* Does it function without connectivity?
* Does it preserve business evidence?
* Does it create synchronisation risks?
* Does it remain idempotent where appropriate?
* Does the backend remain the final authority?
If a feature cannot function offline, the implementation should explicitly justify why.
Offline compatibility should always be the default assumption.
---
# Engineering Principle
Offline support is not a mobile feature.
It is an architectural commitment.
Every layer of the system should contribute to making offline field operations reliable, predictable and recoverable.
Business continuity always has higher priority than network availability.
--- 
# 12. Application Boundaries
## Purpose
This chapter defines the responsibilities and ownership boundaries of each application within the InfraTrack ecosystem.
Its objective is to eliminate ambiguity regarding where business behaviour should be implemented.
Every new feature should clearly belong to one application.
When uncertainty exists, contributors should consult this chapter before implementing any functionality.
---
# Architectural Principle
InfraTrack is composed of multiple applications sharing a single business domain.
Each application owns a specific responsibility.
Responsibilities must never be duplicated simply for convenience.
When multiple applications appear capable of implementing the same behaviour, ownership should always be assigned to the application closest to the business domain.
---
# System Components
InfraTrack currently consists of:
* Spring Boot Backend
* React Administration Portal
* Android Field Application
* PostgreSQL Database
Each component has a clearly defined responsibility.
---
# Responsibility Matrix
| Responsibility              |  Backend |  React  | Android | PostgreSQL |
| --------------------------- | :------: | :-----: | :-----: | :--------: |
| Business Rules              |     ✅    |    ❌    |    ❌    |      ❌     |
| Business Validation         |     ✅    |    ❌    |    ❌    |      ❌     |
| Workflow Execution          |     ✅    |    ❌    |    ❌    |      ❌     |
| Permission Verification     |     ✅    |    ❌    |    ❌    |      ❌     |
| Authentication              |     ✅    | UI only | UI only |      ❌     |
| Asset Registration          |     ✅    |    UI   |    ❌    |   Storage  |
| Business Trigger Management |     ✅    |    UI   | Limited |   Storage  |
| Inspection Execution        |     ✅    |   View  |    ✅    |   Storage  |
| Operational Decisions       |     ✅    |    UI   |    ❌    |   Storage  |
| Work Order Creation         |     ✅    |    UI   |    ❌    |   Storage  |
| Maintenance Reporting       |     ✅    |   View  |    ✅    |   Storage  |
| Completion Review           |     ✅    |    UI   |    ❌    |   Storage  |
| Asset History               |     ✅    |   View  |   View  |   Storage  |
| Synchronisation             |     ✅    |    ❌    |    ✅    |   Storage  |
| Offline Storage             |     ❌    |    ❌    |    ✅    |      ❌     |
| Dashboards                  |    API   |    ✅    |    ❌    |      ❌     |
| Reporting                   |    API   |    ✅    |    ❌    |      ❌     |
| File Storage                | Metadata |   View  |  Upload |   Storage  |
---
# Backend Ownership
The backend owns every business decision.
Examples include:
* workflow transitions;
* permission enforcement;
* business validation;
* operational decisions;
* audit history;
* status changes.
The backend always determines whether an operation is valid.
---
# React Ownership
React owns the administrative experience.
Responsibilities include:
* dashboards;
* administration screens;
* planning interfaces;
* reporting;
* search;
* document viewing.
React should optimise productivity for office users.
It should never redefine business behaviour.
---
# Android Ownership
Android owns field operations.
Responsibilities include:
* inspections;
* maintenance execution;
* QR scanning;
* GPS capture;
* photo capture;
* offline operation;
* synchronisation.
Android should optimise operational efficiency.
Administrative workflows should remain in React unless explicitly required.
---
# Database Ownership
PostgreSQL owns persistence.
The database should:
* store information;
* preserve history;
* support querying.
It should not:
* implement workflows;
* enforce permissions;
* contain business behaviour.
Business logic belongs in the backend.
---
# Shared Responsibilities
Some responsibilities naturally involve multiple applications.
Examples include:
## Inspection
Backend
* validation;
* persistence;
* workflow.
Android
* execution;
* evidence collection.
React
* viewing;
* reporting.
Each application contributes according to its responsibility.
---
# Ownership Rules
Every business capability should have one primary owner.
Examples:
Asset Registration
Primary Owner
Backend
React
User Interface
Android
Not Applicable
---
Inspection
Primary Owner
Backend
Android
Execution Interface
React
Read-only / Reporting
---
Completion Review
Primary Owner
Backend
React
Execution Interface
Android
Not Applicable
---
# AI Decision Checklist
Before implementing a feature, determine:
1. Which application owns the business behaviour?
2. Which application presents the user interface?
3. Which application stores temporary state?
4. Which application owns permanent state?
5. Could this responsibility already exist elsewhere?
If ownership is unclear, implementation should pause until the responsibility has been clarified.
---
# Forbidden Patterns
Avoid implementations where:
* React performs business decisions.
* Android determines workflow transitions.
* PostgreSQL contains business rules.
* Multiple applications independently validate business behaviour.
* Clients become the source of truth.
These patterns create inconsistent behaviour and increase long-term maintenance costs.
---
# Engineering Principle
Every application has a purpose.
Every responsibility has an owner.
Business consistency depends on preserving these ownership boundaries.
Whenever uncertainty exists, prefer strengthening the backend rather than expanding client responsibilities.
---
# 13. Security Model
## Purpose
This chapter defines the security philosophy of InfraTrack.
Security is not considered an isolated technical concern.
It is an integral part of the business domain.
Authentication, authorisation and data protection must work together to preserve business integrity while allowing authorised users to perform their operational responsibilities.
---
# Security Philosophy
InfraTrack protects business operations rather than simply protecting endpoints.
Every authenticated user should only be able to perform actions that match their operational responsibilities.
Security should feel invisible for authorised users while remaining impossible to bypass.
---
# Authentication
Authentication answers one question:
> **Who is the user?**
Authentication is performed exclusively by the Spring Boot backend.
Neither React nor Android should implement independent authentication mechanisms.
Client applications are responsible only for:
* collecting credentials;
* securely storing authentication tokens;
* refreshing sessions when required.
Authentication decisions always belong to the backend.
---
# Authorisation
Authorisation answers a different question:
> **What is this user allowed to do?**
Authorisation is entirely business-driven.
Permissions are determined by:
* user role;
* organisational responsibilities;
* business workflow;
* ownership of operational tasks.
The backend is solely responsible for enforcing authorisation.
---
# Business Security
Business security extends beyond authentication.
Examples include:
* preventing unauthorised work order completion;
* preventing inspection modification after closure;
* preventing users from reviewing their own contractor work;
* preventing workflow transitions that violate business rules.
These are business rules.
They must be enforced even when requests originate from trusted clients.
---
# Principle of Least Privilege
Users should receive only the permissions required to perform their responsibilities.
Avoid granting broad administrative permissions simply for implementation convenience.
Permissions should remain explicit and understandable.
---
# Client Trust Model
React and Android are trusted only as user interfaces.
They are **never trusted as business authorities**.
Clients may:
* display information;
* collect user input;
* perform user experience validation.
Clients must never:
* bypass permission checks;
* enforce business security;
* modify business workflows independently.
Every business operation must be verified by the backend.
---
# Data Protection
Sensitive business information should always be protected.
Examples include:
* user accounts;
* authentication credentials;
* contractor information;
* internal operational notes;
* uploaded documents.
Confidential information should never be unnecessarily exposed through API responses.
---
# API Security
Every REST endpoint should explicitly define:
* who may access it;
* which roles are authorised;
* which business conditions must be satisfied.
Endpoint security should remain predictable and consistent throughout the application.
---
# Auditability
Every important business action should be attributable.
Examples include:
* asset creation;
* inspection completion;
* operational decision creation;
* work order assignment;
* completion review.
Audit information should include:
* user;
* timestamp;
* operation;
* affected business object.
Audit information is part of the business domain.
It should never be considered optional logging.
---
# Android Security
Offline capability should never weaken security.
Authentication tokens should be stored securely.
Sensitive local information should remain protected.
When synchronisation occurs, the backend should validate permissions exactly as if the operation had been performed online.
Offline operation should never bypass backend security.
---
# React Security
React should never hide functionality as the only security mechanism.
Removing buttons does not secure business operations.
Backend permission checks remain mandatory regardless of the user interface.
The interface should improve usability, not enforce security.
---
# Security During Development
AI assistants should assume that every endpoint may eventually receive malicious or invalid requests.
Security should therefore be implemented defensively.
Never rely on:
* client validation;
* hidden interface elements;
* trusted network assumptions;
* client-generated permissions.
---
# AI Security Checklist
Before implementing any business operation, verify:
□ Who is performing this action?
□ Should this user be allowed to perform it?
□ Does the backend verify this permission?
□ Can the workflow be bypassed?
□ Is the action recorded for audit purposes?
□ Could sensitive information be exposed unintentionally?
If any answer is uncertain, the implementation should be reconsidered before being accepted.
---
# Engineering Principle
Security is not a feature.
Security is a property of the architecture.
Every business operation should remain secure, auditable and consistent regardless of which client initiated the request.
The backend is always responsible for enforcing these guarantees.
---
# 14. Role Model
## Purpose
This chapter defines the operational roles used throughout InfraTrack.
Roles represent business responsibilities rather than technical permissions.
Permissions should emerge naturally from these responsibilities.
Authentication determines **who** the user is.
The Role Model determines **what business responsibilities that user may perform**.
---
# Role Philosophy
InfraTrack is designed around organisational responsibilities rather than software permissions.
Each role represents a real position within a council's operational organisation.
The objective is to model how work is performed in reality rather than creating arbitrary software roles.
Whenever uncertainty exists, contributors should ask:
> **"Would this responsibility belong to this person in a real council?"**
---
# Role Hierarchy
```text

Administrator

        │

        ▼

Manager

        │

        ▼

Operational Coordinator

        │

        ▼

Field Employee

        │

        ▼

Contractor

```
This hierarchy reflects increasing operational specialisation.
It **does not** imply that higher roles automatically inherit every lower-level permission.
Permissions are determined by business responsibilities, not hierarchy alone.
---
# Administrator
## Business Responsibility
Administrators configure and maintain the platform.
They are responsible for ensuring that InfraTrack operates correctly.
Typical responsibilities include:
* user management;
* department management;
* role assignment;
* system configuration;
* asset category management;
* organisational configuration.
Administrators do **not** manage day-to-day operational work.
Operational decisions belong to managers.
---
# Manager
## Business Responsibility
Managers provide operational leadership and approve significant operational decisions for assets owned by their department.
Typical responsibilities include:
* approving Operational Decisions;
* approving maintenance strategies;
* validating Completion Reviews;
* reviewing inspection evidence;
* monitoring operational performance;
* ensuring departmental accountability;
* reviewing Asset History.
Managers decide.
They do not normally perform field work.
---
# Operational Coordinator
## Business Responsibility
Operational Coordinators coordinate day-to-day operational activities after business decisions have been made.
Typical responsibilities include:
* registering Assets;
* creating Business Triggers;
* assigning Inspections;
* creating Work Orders following approved Operational Decisions;
* assigning Work Orders;
* coordinating Field Employees;
* coordinating Contractors;
* monitoring operational progress.
Operational Coordinators coordinate.
They do not normally make Operational Decisions.
---
# Field Employee
## Business Responsibility
Field Employees perform operational work.
Their objective is to inspect and maintain public assets.
Typical responsibilities include:
* perform inspections;
* record issues;
* capture photographs;
* execute maintenance;
* complete work reports;
* submit operational evidence.
Field Employees describe reality.
They do not make administrative decisions.
---
# Contractor
## Business Responsibility
Contractors perform maintenance activities delegated by the council.
They are external organisations or individuals.
Typical responsibilities include:
* receive assigned work;
* complete maintenance;
* submit completion evidence;
* upload photographs;
* report completed work.
Contractors should have access only to the operational information required to perform their assigned work.
They should never have unrestricted access to council operational data.
---
# Responsibility Matrix
| Capability                  | Administrator |  Manager | Operational Coordinator | Field Employee |      Contractor      |
| --------------------------- | :-----------: | :------: | :---------------------: | :------------: | :------------------: |
| Configure Platform          |       ✅       |     ❌    |            ❌            |        ❌       |           ❌          |
| Manage Users                |       ✅       |     ❌    |            ❌            |        ❌       |           ❌          |
| Register Asset              |      ⚙️*      |     ✅    |            ✅            |        ❌       |           ❌          |
| Create Business Trigger     |       ❌       |     ❌    |            ✅            |        ❌       |           ❌          |
| Assign Inspection           |       ❌       |     ❌    |            ✅            |        ❌       |           ❌          |
| Perform Inspection          |       ❌       | Optional |            ❌            |        ✅       |       Optional       |
| Record Issue                |       ❌       | Optional |            ❌            |        ✅       |           ✅          |
| Create Operational Decision |       ❌       |     ✅    |            ❌            |        ❌       |           ❌          |
| Create Work Order           |       ❌       |     ❌    |            ✅            |        ❌       |           ❌          |
| Assign Work Order           |       ❌       |     ❌    |            ✅            |        ❌       |           ❌          |
| Perform Maintenance         |       ❌       | Optional |            ❌            |        ✅       |           ✅          |
| Submit Completion Evidence  |       ❌       |     ❌    |            ❌            |        ✅       |           ✅          |
| Complete Completion Review  |       ❌       |     ✅    |            ❌            |        ❌       |           ❌          |
| View Operational Dashboard  |       ⚙️      |     ✅    |            ✅            |     Limited    |           ❌          |
| View Asset History          |       ✅       |     ✅    |            ✅            |     Limited    | Assigned Assets Only |
> **⚙️ Administrator access exists for support and maintenance purposes. It should not replace normal operational workflows.**
---
# Separation of Responsibilities
InfraTrack intentionally separates operational decision-making, coordination and execution.
Examples:
Managers approve Operational Decisions.
Operational Coordinators create and assign Work Orders following approved decisions.
Field Employees execute Work Orders.
Managers review completed work when a Completion Review is required.
Contractors perform delegated maintenance.
Managers decide.
Operational Coordinators coordinate.
This separation improves accountability and operational traceability.
---
# Temporary Responsibility
Business responsibilities may occasionally differ from organisational roles.
For example:
* a Manager may perform an inspection;
* a Supervisor may temporarily execute field work;
* an Administrator may create an asset during system configuration.
These situations are exceptions.
The application should support them without redefining the standard operational model.
---
# Role Evolution
New organisational roles may be introduced in future versions.
Examples include:
* Regional Supervisor;
* Compliance Officer;
* Asset Engineer;
* Operations Director.
The security architecture should allow future expansion without requiring changes to the existing business model.
---
# AI Implementation Rules
When implementing permissions, always ask:
* Is this permission based on a business responsibility?
* Does this role perform this activity in reality?
* Is this responsibility already owned by another role?
* Would granting this permission reduce operational traceability?
Never grant permissions solely because they simplify implementation.
Business responsibilities always take priority.
---
# Engineering Principle
Roles exist to model organisational responsibilities.
Permissions are a technical implementation of those responsibilities.
Whenever the two conflict, the business responsibility should always guide the implementation.
InfraTrack protects not only system access but also the integrity of operational accountability.
---
# 15. Domain Model Philosophy
## Purpose
This chapter defines how the business domain should be represented within the Spring Boot backend.
It establishes the principles governing domain modelling and explains how business concepts should be translated into software.
The objective is not simply to model data.
The objective is to model the business.
---
# Business Before Entities
The domain model represents business concepts.
It does not represent database tables.
Entities exist because the business requires them.
They do not exist because a database needs structure.
Every entity should answer one simple question:
> **"Which business concept does this represent?"**
If the answer is unclear, the entity probably should not exist.
---
# One Entity, One Business Concept
Each entity should represent exactly one business concept.
Examples include:
* Asset
* BusinessTrigger
* Inspection
* Issue
* OperationalDecision
* WorkOrder
* MaintenanceActivity
* CompletionReview
* AssetHistory
Avoid entities representing multiple responsibilities.
If an entity begins to describe several business concepts, it should be reconsidered.
---
# Stable Business Vocabulary
The domain model must always use the official terminology defined in the Business Discovery documentation.
For example:
✔ Inspection
✔ WorkOrder
✔ OperationalDecision
Avoid synonyms such as:
❌ Task
❌ Job
❌ Ticket
❌ Request
Consistency of language is considered an architectural requirement.
---
# Rich Domain, Thin Persistence
Entities should express business meaning.
They should not become simple collections of fields.
Whenever appropriate, business invariants should be protected inside the domain model.
However, orchestration of workflows remains the responsibility of application services.
Business responsibilities should therefore be distributed intentionally between:
* entities;
* services;
* repositories.
---
# Identity
Every entity has a stable identity.
An Asset remains the same Asset throughout its operational lifetime.
Operational events do not replace business identity.
Examples:
The Asset changes.
The identity does not.
The Inspection changes.
The identity does not.
The WorkOrder changes.
The identity does not.
Business identity should remain stable.
---
# Relationships
Relationships should reflect business reality.
Example:
```text id="1ivl2j"

Asset

↓

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

↓

Asset History

```
Relationships should emerge naturally from the business domain.
Avoid introducing relationships purely for implementation convenience.
---
# Aggregate Thinking
Entities should be grouped according to business consistency.
The objective is to preserve business invariants.
Contributors should ask:
> Which entity is responsible for protecting this business rule?
Rather than:
> Which entity owns this foreign key?
Business ownership is more important than database ownership.
---
# Avoid Anemic Models
The domain model should contain business meaning.
Examples of appropriate responsibilities include:
* state validation;
* business invariants;
* lifecycle consistency.
Entities should not become passive containers manipulated entirely by services.
At the same time, avoid placing complete workflows inside entities.
The balance should remain pragmatic.
---
# Persistence Is Secondary
JPA is an implementation detail.
Annotations should never dictate business modelling.
Business concepts should remain understandable even if persistence technology changes in the future.
---
# Evolution
The domain model is expected to evolve.
However, evolution should preserve business consistency.
Before introducing a new entity, contributors should verify:
* Does this represent a genuine business concept?
* Is an existing entity already responsible?
* Would a value object be sufficient?
* Is the business vocabulary becoming less consistent?
Avoid expanding the domain simply because implementation becomes more complex.
---
# AI Domain Checklist
Before creating any new entity, verify:
□ Which business concept does this represent?
□ Does this concept already exist?
□ Does the entity own a single responsibility?
□ Does its name match the Business Discovery terminology?
□ Would a business analyst recognise this concept?
□ Does it improve understanding of the business domain?
If one or more answers are uncertain, reconsider the proposed model before implementation.
---
# Engineering Principle
The domain model is the language of the business.
Every entity should read like a real operational concept rather than a technical abstraction.
A developer unfamiliar with InfraTrack should be able to understand how a council operates simply by exploring the domain model.
When the domain model accurately reflects the business, architecture, APIs and user interfaces naturally become easier to design and maintain.
---
# 16. Database Philosophy
## Purpose
This chapter defines the philosophy governing persistence within InfraTrack.
The database exists to preserve the operational history of the business.
It is not responsible for implementing business logic.
Database design should always emerge from the domain model rather than driving it.
---
# Database as Memory
The PostgreSQL database represents the long-term memory of InfraTrack.
Its responsibility is to preserve:
* business entities;
* operational events;
* historical evidence;
* relationships;
* audit information.
It should never become responsible for business decision-making.
---
# Business Before Schema
Database schemas should reflect the business domain.
Tables exist because business concepts exist.
Never create tables simply because a technical implementation appears easier.
Preferred reasoning:
```text

Business Concept

↓

Domain Model

↓

Database Schema

```
Avoid the opposite:
```text

Database Schema

↓

Entities

↓

Business Model

```
The database follows the business.
The business never follows the database.
---
# Persistence is an Implementation Detail
Business discussions should never start with:
* table names;
* foreign keys;
* indexes;
* SQL queries.
Instead, discussions should begin with:
* business concepts;
* business relationships;
* operational workflows;
* business invariants.
Database structures should naturally emerge afterwards.
---
# Historical Integrity
InfraTrack preserves operational history.
Business information should rarely be deleted.
Instead, operational events should remain permanently available whenever practical.
Examples include:
* completed inspections;
* completed maintenance;
* operational decisions;
* uploaded evidence;
* completion reviews.
Historical integrity has higher priority than storage optimisation.
---
# Referential Integrity
Relationships should accurately reflect business ownership.
The database should prevent impossible business states whenever appropriate.
Examples include:
* an Inspection cannot exist without an Asset;
* a Work Order cannot exist without an Operational Decision;
* a Maintenance Activity cannot exist without a Work Order.
Referential integrity supports business consistency.
It does not replace business validation.
---
# Database Responsibilities
The database is responsible for:
* persistence;
* querying;
* indexing;
* constraints;
* transactional consistency.
The database is **not** responsible for:
* workflow logic;
* permission enforcement;
* business validation;
* operational decisions.
Business behaviour belongs in Spring Boot.
---
# Soft Delete Philosophy
Operational information should almost never be physically deleted.
Where business requirements demand removal, prefer logical deletion or archival mechanisms.
Historical records should remain available whenever legally and operationally appropriate.
Data loss should never be the default behaviour.
---
# Auditability
Every significant operational event should remain traceable.
The database should preserve sufficient information to reconstruct:
* who performed the action;
* when it occurred;
* what changed;
* why the change occurred (when applicable).
Auditability is considered part of the business domain rather than an optional technical feature.
---
# Performance Philosophy
Performance optimisation should never compromise business clarity.
Optimisations should be introduced only after identifying genuine performance requirements.
Premature optimisation should be avoided.
Readable schemas are generally preferred over highly optimised but difficult-to-understand designs.
---
# Migration Philosophy
Database evolution should remain predictable.
Every schema modification should:
* preserve existing data;
* remain reversible whenever practical;
* include explicit migration scripts;
* maintain compatibility with the domain model.
Schema evolution should follow business evolution.
---
# Naming Conventions
Database objects should use terminology consistent with the business domain.
Preferred examples:
```text

asset

inspection

work_order

maintenance_activity

completion_review

```
Avoid technical or abbreviated names that obscure business meaning.
Consistency between database names and domain terminology improves maintainability.
---
# AI Database Checklist
Before proposing any database modification, verify:
□ Which business concept requires this change?
□ Does the schema reflect the domain model?
□ Is historical information preserved?
□ Does the relationship exist because of the business?
□ Is business terminology respected?
□ Is the database beginning to own business behaviour?
If the final answer is yes to the last question, the design should be reconsidered.
---
# Engineering Principle
The database exists to preserve the operational memory of InfraTrack.
It should accurately represent business reality while remaining independent of application behaviour.
Business logic belongs in the application.
Business history belongs in the database.
Maintaining this separation is essential for long-term maintainability.
--- 
# 17. API Design Philosophy
## Purpose
This chapter defines the principles governing the design of the InfraTrack REST API.
The objective is to ensure that every endpoint, request and response follows a consistent philosophy regardless of who implements it.
API consistency is considered an architectural requirement.
Every new endpoint should feel like a natural extension of the existing API.
---
# API Philosophy
The REST API represents the business domain.
Endpoints should expose business capabilities rather than database operations.
The API should read like the language used by council employees.
Preferred examples:
```text

POST   /api/assets

POST   /api/business-triggers

POST   /api/inspections

POST   /api/work-orders

```
Avoid APIs exposing technical implementation details.
---
# Business Capabilities
Endpoints should represent business actions.
Prefer:
```text

POST /api/inspections

POST /api/work-orders

POST /api/maintenance-activities

```
Rather than:
```text

POST /api/createInspection

POST /api/updateWorkOrderStatus

POST /api/executeMaintenanceWorkflow

```
HTTP methods already describe the action.
Endpoint names should describe the business resource.
---
# Consistency
Every resource should follow the same conventions.
Examples:
```text

GET    /api/assets

GET    /api/assets/{id}

POST   /api/assets

PUT    /api/assets/{id}

DELETE /api/assets/{id}

```
If one resource behaves differently from every other resource, reconsider the design.
Predictability is more valuable than originality.
---
# DTO Philosophy
The REST API should never expose JPA entities directly.
Requests and responses must use dedicated DTOs.
Examples:
```text

CreateAssetRequest

UpdateAssetRequest

AssetResponse

InspectionSummaryResponse

InspectionDetailsResponse

```
Avoid generic DTOs attempting to satisfy multiple business scenarios.
DTOs should represent a specific use case.
---
# Request Validation
Input validation should occur before business execution.
Examples include:
* mandatory fields;
* maximum length;
* valid formats;
* numeric ranges.
Business validation belongs to the service layer.
Request validation belongs to the API boundary.
---
# Response Philosophy
API responses should communicate business information.
Responses should never expose:
* database implementation;
* internal exceptions;
* framework details.
Responses should contain only the information required by clients.
---
# Error Handling
Errors should describe business problems.
Preferred examples:
```text

Inspection already completed.

Asset not found.

User is not authorised to perform this operation.

Work Order has already been closed.

```
Avoid:
```text

NullPointerException

ConstraintViolationException

SQLIntegrityConstraintViolationException

```
Framework exceptions should never reach API consumers.
---
# Pagination
Collections should always support pagination when growth is expected.
Large datasets should never be returned entirely.
Pagination should remain consistent across all resources.
Preferred response:
```text

content

page

size

totalElements

totalPages

```
---
# Filtering
Filtering should remain explicit.
Examples:
```text

GET /api/assets?department=PARKS

GET /api/work-orders?status=OPEN

GET /api/inspections?assignedTo=15

```
Avoid creating dedicated endpoints for every filter combination.
---
# Sorting
Sorting should follow a consistent convention.
Example:
```text

?sort=createdAt,desc

```
Avoid inventing resource-specific sorting syntax.
---
# Versioning
The API should be designed for long-term evolution.
Breaking changes should be introduced intentionally.
Versioning strategy will be determined when public API stability becomes necessary.
Until then, maintain backward compatibility whenever practical.
---
# Idempotency
Whenever appropriate:
GET
PUT
DELETE
should remain idempotent.
Repeated requests should not create inconsistent business states.
Business operations that naturally create new operational events should remain non-idempotent.
---
# Documentation
Every endpoint should clearly define:
* purpose;
* request DTO;
* response DTO;
* permissions;
* business rules;
* possible errors.
Documentation should explain business behaviour rather than technical implementation.
---
# AI API Checklist
Before creating a new endpoint, verify:
□ Does this endpoint represent a business capability?
□ Does a similar endpoint already exist?
□ Are request and response DTOs specific to this use case?
□ Does the endpoint expose implementation details?
□ Is pagination required?
□ Are business errors properly communicated?
If uncertainty exists, simplify the API rather than expanding it.
---
# Engineering Principle
The API is the public language of the backend.
Every endpoint should express business intent clearly, consistently and predictably.
Developers should be able to understand the operational workflow simply by reading the API structure.
The API should expose the business.
Never the implementation.
---
# 18. Project Structure
## Purpose
This chapter defines the physical organisation of the InfraTrack codebase.
The objective is to ensure that every contributor, whether human or AI, organises source code consistently throughout the project.
A predictable project structure improves:
* maintainability;
* onboarding;
* code navigation;
* AI consistency;
* long-term scalability.
The directory structure should reflect the business architecture rather than personal preferences.
---
# Repository Organisation
InfraTrack is a multi-application project.
Each application remains independent while sharing the same business domain.
The repository should remain organised around products rather than technologies.
Example:
```text

InfraTrack/

├── blueprint/

├── business-discovery/

├── functional-analysis/

├── backend/

├── frontend/

├── android/

├── infrastructure/

├── documentation/

└── scripts/

```
Each directory has a single responsibility.
---
# Backend Structure
The backend is organised by **business capability**.
Preferred structure:
```text

backend/src/main/java/

asset/

businesstrigger/

inspection/

issue/

operationaldecision/

workorder/

maintenance/

notification/

security/

shared/

```
Each business capability owns its implementation.
Inside every capability:
```text

inspection/

controller/

service/

repository/

domain/

dto/

mapper/

exception/

```
This structure should remain consistent across the project.
---
# Frontend Structure
React follows the same business organisation.
Example:
```text

src/

modules/

asset/

inspection/

workorder/

maintenance/

dashboard/

administration/

shared/

components/

hooks/

layouts/

```
Avoid organising the project around technical layers.
Users think in business capabilities.
The project should reflect that.
---
# Android Structure
Android mirrors the same business capabilities whenever practical.
Example:
```text

features/

asset/

inspection/

workorder/

maintenance/

authentication/

sync/

shared/

```
Feature names should remain identical across Backend, React and Android whenever possible.
Consistency reduces cognitive load.
---
# Shared Terminology
Every application should use the same vocabulary.
Example:
Backend
```text

InspectionService

```
React
```text

InspectionPage

```
Android
```text

InspectionScreen

```
Business terminology remains identical.
Only implementation terminology changes.
---
# Shared Components
Shared code should remain genuinely shared.
Examples include:
Backend
* exception handling
* utilities
* configuration
React
* UI components
* layouts
* theme
Android
* common UI components
* networking
* synchronisation
* utilities
Avoid creating generic shared modules simply because duplication exists.
Duplication is often preferable to incorrect abstraction.
---
# Feature Ownership
Every source file should clearly belong to one business capability.
If a file appears equally relevant to multiple modules, reconsider its responsibility.
Feature ownership should always remain obvious.
---
# Package Naming
Packages should use:
* singular business names;
* lowercase;
* meaningful terminology.
Preferred:
```text

inspection

workorder

maintenance

```
Avoid:
```text

common2

misc

helper

manager

util

```
Package names should explain their purpose.
---
# Documentation Location
Documentation should remain close to the project.
Preferred structure:
```text

business-discovery/

functional-analysis/

architecture/

blueprint/

documentation/

```
Documentation is part of the project.
It should evolve together with the code.
---
# AI Organisation Checklist
Before creating a new directory, package or module, verify:
□ Does this represent a business capability?
□ Does a similar package already exist?
□ Does the name follow the official business terminology?
□ Would another developer immediately understand where this belongs?
□ Is the new structure simpler than extending the existing one?
Avoid creating new folders simply to satisfy implementation convenience.
---
# Engineering Principle
A contributor should be able to understand the project structure without reading the code.
The directory tree itself should communicate how the business domain is organised.
Project organisation is therefore considered an architectural concern rather than a cosmetic one.
---
# 19. Java Coding Standards
## Purpose
This chapter defines the Java development standards used throughout the InfraTrack backend.
Its objective is to ensure that every class, method and package follows the same engineering principles regardless of who implements the code.
Consistency is considered more valuable than personal coding preferences.
Every contributor, whether human or AI, is expected to follow these standards.
---
# General Philosophy
Java code should express the business domain clearly.
A developer unfamiliar with InfraTrack should understand what the system does by reading the code.
Code is written for humans first.
The compiler is a secondary audience.
Always optimise for:
* readability;
* maintainability;
* explicitness;
* business clarity.
Never optimise primarily for reducing the number of lines.
---
# Simplicity
Prefer simple solutions.
Avoid writing code that requires explanation.
If a solution appears clever, ask whether a simpler implementation exists.
Explicit code is almost always preferable to compact code.
---
# Readability
Methods should read like business operations.
Example:
```java

inspectionService.completeInspection(...)

```
is preferable to
```java

inspectionService.execute(...)

```
Names should communicate intent.
Implementation details should remain secondary.
---
# Class Responsibilities
Every class should have one primary responsibility.
Examples:
✔ InspectionService
✔ AssetRepository
✔ WorkOrderController
Avoid classes responsible for multiple business concepts.
If a class becomes difficult to describe in one sentence, it probably has too many responsibilities.
---
# Method Responsibilities
Every method should perform one business action.
Avoid methods implementing complete workflows.
Instead:
```text

validate

↓

load

↓

execute

↓

persist

↓

return

```
Long methods generally indicate mixed responsibilities.
---
# Method Size
There is no fixed line limit.
However:
If a method can no longer be understood without scrolling extensively, it should probably be refactored.
Aim for methods that express one complete business action.
Avoid artificial extraction of tiny methods that reduce readability.
The objective is clarity, not metrics.
---
# Class Size
Classes should remain focused.
Large classes usually indicate missing business concepts.
Rather than imposing an arbitrary line limit, ask:
> Does this class still represent one business responsibility?
If not, split it.
---
# Constructor Injection
Always prefer constructor injection.
Never use field injection.
Preferred:
```java

private final InspectionRepository inspectionRepository;

public InspectionService(

        InspectionRepository inspectionRepository) {

    this.inspectionRepository = inspectionRepository;

}

```
Constructor injection improves:
* immutability;
* testability;
* dependency visibility.
---
# Immutability
Prefer immutable objects whenever practical.
Use `final` whenever values should not change.
Mutable state should remain intentional.
---
# Records
Use Java Records for immutable DTOs whenever appropriate.
Examples:
* request DTOs;
* response DTOs;
* lightweight projections.
Do not use Records for JPA entities.
---
# Optional
Use `Optional` only when representing the absence of a value.
Do not:
* store Optional in entities;
* use Optional as method parameters;
* overuse Optional for internal logic.
Optional improves API readability.
It should not complicate implementation.
---
# Streams
Streams should improve readability.
Prefer Streams for:
* filtering;
* mapping;
* collecting.
Avoid complex Stream pipelines containing multiple nested operations.
If a Stream becomes difficult to understand, use a loop.
Readable code always wins.
---
# Exceptions
Use explicit business exceptions.
Preferred:
```text

InspectionAlreadyCompletedException

AssetNotFoundException

WorkOrderClosedException

```
Avoid:
```text

RuntimeException

Exception

```
Exceptions should describe business failures.
---
# Logging
Logs should describe business operations.
Preferred:
```

Inspection completed.

Maintenance assigned.

Work Order created.

```
Avoid logs that simply repeat method names.
Logging exists to explain business events.
---
# Comments
Good code rarely requires comments.
Prefer improving names rather than explaining implementation.
Use comments only when describing:
* business rationale;
* architectural decisions;
* complex algorithms.
Never comment obvious code.
---
# Magic Values
Avoid hardcoded business values.
Prefer:
* enums;
* constants;
* configuration.
Business rules should remain explicit.
---
# Null Handling
Prefer preventing null values.
Validate inputs early.
Fail fast when mandatory information is missing.
Avoid defensive null checks scattered throughout the codebase.
---
# Business Terminology
Variable names should use business language.
Preferred:
```java

inspection

workOrder

maintenanceActivity

operationalDecision

```
Avoid technical abbreviations.
---
# Dependencies
Keep dependencies minimal.
Before introducing a new library, ask:
* Does Java already provide this?
* Does Spring already provide this?
* Is the dependency justified?
Avoid dependency proliferation.
---
# Refactoring
Refactor continuously.
Do not accumulate technical debt.
Small improvements performed regularly are preferable to large refactoring phases.
Refactoring should improve:
* readability;
* consistency;
* maintainability.
It should never silently modify business behaviour.
---
# AI Java Checklist
Before submitting Java code, verify:
□ Does every class have a single responsibility?
□ Do method names express business intent?
□ Is the code easier to read than to write?
□ Are exceptions business-oriented?
□ Is constructor injection used?
□ Are business terms consistent?
□ Would another developer understand this without additional explanation?
If the answer to any question is "no", improve the implementation before considering it complete.
---
# Engineering Principle
Java is the language used to express the business domain.
Every class should communicate business intent.
Every method should represent a meaningful business operation.
A contributor should be able to understand how a council operates simply by reading the service layer.
Readable business code is the highest quality standard of the InfraTrack backend.
---
# 20. React Coding Standards
## Purpose
This chapter defines the React development standards used throughout the InfraTrack administrative portal.
Its objective is to ensure that every screen, component and feature follows the same engineering principles regardless of who implements it.
React should remain predictable, maintainable and business-oriented.
User interface quality is considered part of the overall product quality.
---
# React Philosophy
React exists to present the business domain.
It is not responsible for implementing business rules.
The purpose of React is to make business operations intuitive, efficient and predictable.
Every component should help users understand and interact with the operational workflow.
---
# Business Before Components
Components should represent business concepts.
Preferred examples:
```text

AssetDetailsCard

InspectionTimeline

InspectionForm

WorkOrderSummary

MaintenanceHistory

```
Avoid technical component names.
Examples:
❌ DataCard
❌ GenericForm
❌ CustomComponent
Business terminology should always take priority.
---
# Feature First Organisation
React should follow the same business capability structure as the backend.
Preferred:
```text

src/

modules/

asset/

inspection/

workorder/

maintenance/

dashboard/

administration/

shared/

```
Avoid organising the application purely by technical layers.
Business capabilities should remain immediately recognisable.
---
# Component Responsibilities
Each component should have one responsibility.
Examples:
* display information;
* collect user input;
* display a table;
* display a timeline;
* display a dialog.
Avoid components responsible for multiple business workflows.
Large components should usually be decomposed into smaller business-oriented components.
---
# Smart vs Presentational Components
Separate business interaction from presentation whenever practical.
Container components coordinate:
* API calls;
* page state;
* navigation;
* orchestration.
Presentation components display:
* business information;
* forms;
* cards;
* tables.
Presentation components should remain reusable and predictable.
---
# State Management
State should remain as local as possible.
Before introducing global state, ask:
* Does multiple unrelated pages require this information?
* Is the state truly shared?
* Would local state be simpler?
Avoid global state simply for convenience.
---
# Business State
React displays business state.
It does not own business state.
Business state always originates from the backend.
Examples include:
* inspection status;
* work order status;
* asset lifecycle;
* operational decisions.
React should never attempt to recreate business workflows locally.
---
# API Communication
Business operations should remain explicit.
Preferred:
```typescript

await inspectionApi.completeInspection(...)

```
Avoid hiding important business operations behind multiple abstraction layers.
Developers should easily understand which backend operation is being executed.
---
# Forms
Forms should focus on user interaction.
Responsibilities include:
* collecting input;
* displaying validation errors;
* submitting requests;
* handling success and failure.
Business validation always belongs to the backend.
---
# Tables
Tables should present operational information clearly.
Common capabilities include:
* pagination;
* sorting;
* filtering;
* search;
* row actions.
Table behaviour should remain consistent across every module.
---
# Dialogs
Dialogs should support focused business actions.
Examples:
* Create Asset
* Complete Inspection
* Assign Work Order
Avoid dialogs containing complete workflows.
Complex workflows should remain full pages.
---
# Routing
Routes should represent business capabilities.
Examples:
```text

/assets

/inspections

/work-orders

/maintenance

/reports

```
Routes should remain intuitive.
Navigation should follow how council employees perform their daily work.
---
# Error Handling
React should present meaningful business errors.
Users should understand:
* what happened;
* why it happened;
* what they should do next.
Technical exceptions should never be exposed directly.
---
# Loading States
Every asynchronous operation should clearly communicate progress.
Typical states include:
* loading;
* empty;
* success;
* error.
Avoid interfaces where users cannot determine whether an action is still processing.
---
# Reusability
Reuse components only when they genuinely represent the same business concept.
Avoid forcing different workflows into generic components.
Duplication is preferable to incorrect abstraction.
---
# Styling
Styling should remain consistent throughout the application.
Use the project design system.
Avoid inline styling except for exceptional cases.
Consistency has higher priority than visual originality.
---
# Accessibility
Administrative software should remain accessible.
Consider:
* keyboard navigation;
* screen readers;
* colour contrast;
* focus visibility;
* semantic HTML.
Accessibility should be considered during implementation rather than added later.
---
# Performance
Optimise rendering only when measurable improvements exist.
Avoid premature optimisation.
Readability always has higher priority than micro-optimisations.
---
# AI React Checklist
Before submitting React code, verify:
□ Does the component represent one business concept?
□ Is business terminology respected?
□ Does business logic remain in the backend?
□ Is state kept as local as possible?
□ Is the UI predictable?
□ Is the component understandable without additional explanation?
□ Does this implementation improve the user experience?
---
# Engineering Principle
React should make complex operational workflows feel simple.
Users should think about managing public assets.
They should never think about how the software works.
Every component should reduce cognitive load while remaining fully aligned with the business domain implemented by the backend.
---
# 21. Android Coding Standards
## Purpose
This chapter defines the development standards for the InfraTrack Android application.
The Android application is a specialised operational tool designed for field employees working in real-world environments.
Every implementation should prioritise operational efficiency, reliability and maintainability over technical sophistication.
The Android application should always remain aligned with the backend business model.
---
# Android Philosophy
Android exists to support field operations.
It is **not** a mobile version of the React application.
The application should optimise how work is performed in the field rather than reproducing every administrative feature.
Whenever implementation choices exist, always ask:
> **Does this help a field employee complete their work faster, safer or more accurately?**
If not, reconsider the implementation.
---
# Business Before Screens
Screens represent business capabilities.
Preferred examples:
```text

Asset Details

Assigned Inspections

Perform Inspection

Maintenance Activity

Synchronization Status

```
Avoid screens organised around technical concepts.
Navigation should follow the operational workflow performed in the field.
---
# Feature First Organisation
Android should mirror the backend business capabilities whenever possible.
Example:
```text

features/

asset/

inspection/

workorder/

maintenance/

authentication/

sync/

shared/

```
Using the same terminology across Backend, React and Android reduces cognitive load and simplifies collaboration.
---
# One Screen, One Responsibility
Every screen should focus on one business activity.
Examples:
✔ Perform Inspection
✔ View Asset
✔ Complete Maintenance
Avoid screens combining multiple unrelated workflows.
Complex workflows should be divided into several focused screens.
---
# State Management
UI state belongs to the screen.
Business state belongs to the backend.
Temporary state may exist locally while offline.
Business ownership never changes.
---
# Offline Behaviour
Offline capability is the default behaviour.
Every new feature should be implemented assuming network connectivity may be unavailable.
Before writing code, ask:
* What happens if the device loses connectivity?
* Can the user still complete the task?
* How will this operation synchronise later?
Offline behaviour should be designed before online behaviour.
---
# Synchronisation Awareness
The application should always know the synchronisation state of business data.
Typical states include:
* Pending
* Synchronising
* Synchronized
* Failed
* Conflict
Users should never lose confidence that their work has been preserved.
---
# User Interaction
Every interaction should minimise operational effort.
Preferred:
* QR scanning
* Camera capture
* Dropdown selections
* Toggle buttons
* Date pickers
Avoid:
* unnecessary typing
* repeated confirmations
* long forms
* excessive navigation
---
# Forms
Forms should collect only information required to complete the operational task.
Every additional field increases operational friction.
Ask:
> Does this information provide measurable business value?
If not, remove it.
---
# Camera Integration
Photographs represent operational evidence.
Camera access should therefore feel immediate and reliable.
The application should minimise the number of interactions required to capture evidence.
Photo quality should support business documentation rather than artistic quality.
---
# GPS Usage
GPS exists to improve traceability.
Location information should support inspections.
It should never prevent operational work from being completed.
If GPS is unavailable, the user should still be able to finish the task whenever business rules permit.
---
# Error Handling
Errors should be understandable by operational staff.
Avoid technical language.
Preferred:
> Synchronization failed.
>
> Your inspection has been saved locally and will be retried automatically.
Avoid:
> HTTP 500
> Serialization Exception
Business language should always be preferred.
---
# Performance
Field employees expect immediate responsiveness.
Optimise for:
* application startup;
* camera launch;
* QR scanning;
* form responsiveness;
* offline persistence.
Visual effects should never delay operational work.
---
# Battery Usage
Battery consumption is an operational concern.
Avoid:
* unnecessary background processing;
* continuous GPS usage;
* excessive synchronisation;
* unnecessary polling.
Efficient resource usage improves field productivity.
---
# Notifications
Notifications should support operational work.
Examples:
* New Assignment
* Work Order Updated
* Synchronization Completed
* Synchronization Failed
Notifications should remain concise and actionable.
---
# Security
Sensitive information should remain protected on the device.
Authentication tokens must use secure platform storage.
Business data should never remain exposed unnecessarily.
Offline capability should never reduce security.
---
# Accessibility
Field conditions vary significantly.
Interfaces should remain usable:
* in bright sunlight;
* while wearing gloves where possible;
* with limited attention;
* with one-handed operation.
Operational usability is part of the architecture.
---
# AI Android Checklist
Before submitting Android code, verify:
□ Can this feature operate without connectivity?
□ Does it reduce field effort?
□ Is business terminology respected?
□ Is operational evidence preserved?
□ Does the backend remain the source of truth?
□ Would a field employee understand this screen immediately?
□ Is the workflow optimised for real operational conditions?
If one or more answers are uncertain, improve the implementation before considering it complete.
---
# Engineering Principle
The Android application should disappear behind the operational workflow.
Field employees should focus on inspecting assets, recording evidence and completing maintenance.
The software should become almost invisible.
Every implementation should reduce operational friction while preserving business quality.
---
# 22. Testing Philosophy
## Purpose
This chapter defines the testing strategy used throughout InfraTrack.
Testing is not considered a final validation step.
Testing is part of the implementation process.
A business capability is not complete until its behaviour can be verified reliably.
The objective of testing is to provide confidence that the implemented behaviour matches the approved business requirements.
---
# Testing Philosophy
InfraTrack tests business behaviour.
The primary objective is not code coverage.
The primary objective is confidence.
Tests should answer:
> Does the system behave as the business expects?
Rather than:
> Did we execute every line of code?
Coverage metrics may be useful indicators.
They should never become the goal.
---
# Testing Hierarchy
InfraTrack follows a testing pyramid.
```text id="qqcb5l"

            E2E Tests

                 ▲

                 │

      Integration Tests

                 ▲

                 │

          Unit Tests

```
The majority of tests should exist at the lower levels.
Higher-level tests should focus on validating complete business workflows.
---
# Business Capability Testing
Every business capability should have tests.
Examples:
```text id="j5c8v0"

Register Asset

Perform Inspection

Create Work Order

Complete Maintenance

Review Completed Work

```
A capability is not considered complete until its expected behaviour has been validated.
---
# Unit Tests
Unit tests verify isolated behaviour.
Typical candidates include:
* domain rules;
* validation logic;
* business calculations;
* state transitions.
Unit tests should remain:
* fast;
* deterministic;
* easy to understand.
Unit tests should rarely require infrastructure.
---
# Integration Tests
Integration tests verify collaboration between components.
Examples:
* service and repository interaction;
* REST endpoint behaviour;
* database persistence;
* security enforcement.
Integration tests provide confidence that architectural components work together correctly.
---
# End-to-End Tests
End-to-End tests validate complete user workflows.
Examples:
```text id="ysvzbv"

Create Asset

↓

Create Business Trigger

↓

Perform Inspection

↓

Create Work Order

↓

Complete Maintenance

↓

Review Completion

```
E2E tests should focus on critical operational workflows.
Avoid creating excessive E2E tests for every minor scenario.
---
# Business Rules First
Tests should describe business expectations.
Preferred:
```text id="q9l3z4"

shouldRejectClosedInspectionModification

```
Rather than:
```text id="j8l1nc"

testInspectionServiceMethod

```
Test names should explain business behaviour.
---
# Testing Through Use Cases
Whenever possible, tests should be derived from approved Use Cases.
Use Cases become the source of truth.
Implementation exists to satisfy the Use Case.
Tests exist to verify the Use Case.
This creates clear traceability between:
```text id="5v9a5p"

Use Case

↓

Implementation

↓

Test

```
---
# Happy Path and Failure Scenarios
Every capability should test:
## Happy Path
Expected behaviour.
Example:
* Inspection completed successfully.
---
## Failure Scenarios
Unexpected situations.
Examples:
* Asset not found.
* Inspection already completed.
* Unauthorized access.
* Invalid workflow transition.
Failure scenarios are often more important than happy paths.
---
# Testing Business Invariants
Critical business rules should always be protected by tests.
Examples:
* Closed inspections cannot be modified.
* Work Orders require an Operational Decision.
* Maintenance Activities require a Work Order.
* Asset History remains immutable.
Business invariants should never rely solely on implementation discipline.
---
# Test Readability
Tests are documentation.
A developer should understand business behaviour by reading the tests.
Prefer:
```text id="w2n5rp"

givenOpenInspection

whenInspectionCompleted

thenStatusBecomesCompleted

```
Over highly compact but difficult-to-read tests.
---
# Mocking Philosophy
Mock external dependencies.
Do not mock the business domain.
Excessive mocking often hides architectural problems.
Mock only what is necessary to isolate the behaviour being tested.
---
# Database Testing
Persistence behaviour should be tested using real database interactions whenever practical.
Avoid assuming repository behaviour without verification.
Critical persistence workflows deserve integration coverage.
---
# Security Testing
Security is a business requirement.
Tests should verify:
* authentication;
* authorization;
* permission boundaries;
* workflow restrictions.
Security behaviour should be treated as first-class functionality.
---
# Regression Testing
Every bug fix should include a test.
The preferred sequence is:
```text id="cjlwm7"

Bug Identified

↓

Reproduce With Test

↓

Fix

↓

Verify Test Passes

```
This prevents future regressions.
---
# Test Maintenance
Tests should evolve with the business.
Obsolete tests should be updated or removed.
A failing test caused by an intentional business change is not a useful test.
Tests should remain aligned with current business behaviour.
---
# AI Testing Checklist
Before marking a capability as complete, verify:
□ Are the business rules tested?
□ Are failure scenarios covered?
□ Are security rules verified?
□ Can the Use Case be traced to tests?
□ Would another developer understand the expected behaviour from the tests alone?
□ Do the tests improve confidence?
If the answer is no, testing is incomplete.
---
# Engineering Principle
Tests exist to protect business behaviour.
The objective is not to prove that code executes.
The objective is to prove that the software behaves correctly.
When implementation changes, tests provide confidence.
When business requirements change, tests reveal what must evolve.
Testing is therefore considered part of the architecture rather than a separate activity.
---
# 23. Documentation Standards
## Purpose
This chapter defines the documentation standards used throughout InfraTrack.
Documentation is considered an integral part of software engineering rather than a separate activity.
Its objective is to preserve business understanding, architectural consistency and long-term maintainability.
Every significant engineering decision should remain understandable months or years after it was made.
---
# Documentation Philosophy
Documentation exists to explain **why** the system works the way it does.
The source code already explains **how** the system works.
Good documentation should therefore avoid repeating implementation details.
Instead, it should capture:
* business reasoning;
* architectural decisions;
* design intent;
* operational workflows;
* engineering constraints.
---
# Documentation Hierarchy
InfraTrack documentation is organised into distinct layers.
```text

Business Discovery

↓

Blueprint

↓

Functional Analysis

↓

Architecture

↓

Implementation

↓

Tests

```
Each layer answers a different question.
Contributors should update the appropriate layer rather than duplicating information.
---
# Business Discovery
Business Discovery explains:
* business terminology;
* operational workflows;
* council organisation;
* business rules;
* use case context.
It should never describe implementation details.
Business Discovery changes only when business understanding evolves.
---
# Blueprint
The Blueprint defines:
* engineering philosophy;
* architectural principles;
* coding standards;
* AI collaboration rules;
* development methodology.
The Blueprint explains **how the project should be developed**, not how individual features work.
---
# Functional Analysis
Functional Analysis documents business capabilities.
Each capability should include:
* objective;
* actors;
* preconditions;
* business flow;
* alternative flows;
* postconditions;
* related business rules.
Functional Analysis should remain concise.
Its purpose is to guide implementation.
It is not intended to become a complete functional specification.
---
# Technical Documentation
Technical documentation explains implementation decisions that cannot be understood directly from the source code.
Examples include:
* architectural diagrams;
* deployment architecture;
* synchronisation strategy;
* authentication flow;
* infrastructure configuration.
Avoid documenting information already obvious from the implementation.
---
# Source Code
Source code is documentation.
Contributors should prefer improving:
* naming;
* structure;
* readability;
before adding comments.
If code requires extensive comments to be understood, the implementation should be reconsidered.
---
# Comments
Comments should explain:
* business reasoning;
* architectural intent;
* non-obvious implementation decisions;
* unavoidable technical complexity.
Avoid comments describing obvious behaviour.
Example:
Good:
```java

// Work Orders cannot be completed until an Operational Decision has been approved.

```
Poor:
```java

// Increment i by one.

```
---
# Diagrams
Whenever possible, prefer diagrams over long paragraphs.
Examples include:
* workflow diagrams;
* lifecycle diagrams;
* architecture diagrams;
* sequence diagrams;
* relationship diagrams.
A clear diagram often communicates more effectively than several pages of text.
---
# Documentation Maintenance
Documentation should evolve with the software.
Whenever implementation changes:
Ask:
> Does the documentation still describe reality?
If not, update it immediately.
Outdated documentation is more harmful than missing documentation.
---
# Documentation Scope
Document only information that provides long-term value.
Avoid documenting:
* obvious implementation details;
* temporary workarounds;
* framework behaviour;
* generated code.
Focus on information that future contributors cannot easily infer from the source code.
---
# AI Documentation Rules
AI assistants should determine whether a change affects documentation.
Documentation should be updated whenever changes impact:
* business behaviour;
* architecture;
* public APIs;
* operational workflows;
* security;
* synchronisation;
* deployment.
Minor internal refactoring generally does not require documentation updates.
---
# Documentation Quality Checklist
Before considering documentation complete, verify:
□ Does it explain **why**, not only **how**?
□ Is it consistent with the Business Discovery?
□ Is it consistent with the Blueprint?
□ Could a new developer understand the design decisions?
□ Is any information duplicated unnecessarily?
□ Will this documentation still provide value in one year?
If the answer to the final question is "no", reconsider whether the documentation should exist.
---
# Living Documentation
InfraTrack documentation is intentionally designed as a living knowledge base.
Documentation should grow naturally alongside the project.
It should never become a frozen specification disconnected from the implementation.
Every completed business capability should leave the project better documented than before.
---
# Engineering Principle
Documentation preserves engineering knowledge.
The objective is not to produce documents.
The objective is to ensure that future developers and AI assistants understand **why** the system was designed the way it was.
Code may evolve.
Business requirements may evolve.
Technology may evolve.
Engineering knowledge should not be lost.
---
# 24. AI Collaboration Rules
## Purpose
This chapter defines how AI assistants are expected to collaborate during the development of InfraTrack.
The objective is not simply to generate code.
The objective is to participate as a senior software engineer capable of reasoning about business requirements, architecture, maintainability and long-term product evolution.
AI assistants are expected to preserve project consistency above implementation speed.
---
# AI Role
AI assistants are collaborators.
They are not code generators.
Their responsibilities include:
* analysing business requirements;
* identifying architectural risks;
* proposing maintainable solutions;
* challenging inconsistent decisions;
* preserving engineering quality;
* protecting business consistency.
Whenever uncertainty exists, reasoning should take priority over implementation.
---
# Project Awareness
Before proposing any implementation, AI assistants should understand:
* the Business Discovery documents;
* the Blueprint;
* the current Use Case;
* the existing architecture.
Implementation should never begin without sufficient project context.
If important information is missing, the assistant should request clarification rather than making assumptions.
---
# Business First
Every technical decision should begin with the business domain.
The assistant should always ask:
* What business problem is being solved?
* Which business concept owns this behaviour?
* Does this implementation preserve operational consistency?
Technology should never dictate business behaviour.
---
# Think Before Coding
Implementation should be the final step.
Before writing code, AI assistants should identify:
* business concepts;
* responsibilities;
* architectural implications;
* affected modules;
* potential risks.
The assistant should explain important design decisions before implementation whenever appropriate.
---
# Challenge Assumptions
AI assistants are encouraged to challenge inconsistent requests.
Examples include:
* introducing duplicate business concepts;
* violating architectural principles;
* bypassing business workflows;
* adding unnecessary complexity;
* creating speculative abstractions.
Constructive disagreement is encouraged when it improves the project.
Blind implementation is discouraged.
---
# Respect the Blueprint
The Blueprint defines the engineering standards of InfraTrack.
AI assistants should never knowingly violate these standards.
If a requested implementation conflicts with the Blueprint:
* explain the conflict;
* propose alternatives;
* allow the project owner to make the final decision.
The assistant should never silently ignore project standards.
---
# Respect the Business Discovery
Business Discovery documents define the business domain.
AI assistants must not invent:
* new workflows;
* new business terminology;
* new operational rules;
* organisational responsibilities.
Business evolution should always result from explicit discussion.
---
# One Capability at a Time
InfraTrack is developed incrementally.
AI assistants should remain focused on the current business capability.
Avoid introducing code for future requirements unless explicitly requested.
Avoid speculative architecture.
Deliver complete vertical slices.
---
# Consistency Over Novelty
Novel solutions are not automatically better.
When several valid implementations exist, prefer the solution that best preserves project consistency.
Consistency improves:
* readability;
* maintainability;
* onboarding;
* AI collaboration.
---
# Communication Style
AI responses should be:
* analytical;
* structured;
* explicit;
* technically justified.
Avoid unnecessary enthusiasm.
Avoid exaggerated praise.
Avoid presenting opinions as facts.
When making recommendations:
* explain reasoning;
* explain trade-offs;
* explain consequences.
---
# Explain Decisions
When proposing architecture or implementation changes, explain:
* why the change is needed;
* which problem it solves;
* possible alternatives;
* expected consequences.
Do not simply provide code.
Provide engineering reasoning.
---
# Admit Uncertainty
If business behaviour is unclear:
Do not guess.
Instead:
* identify the uncertainty;
* explain why clarification is needed;
* wait for additional information.
Incorrect assumptions are generally more expensive than delayed implementation.
---
# Protect Simplicity
AI assistants should actively reduce unnecessary complexity.
Whenever proposing a solution, ask:
* Is this the simplest architecture that satisfies the business?
* Does this abstraction provide measurable value?
* Can another developer understand this immediately?
Complexity requires justification.
Simplicity is the default.
---
# Preserve Terminology
Use the terminology defined by the Business Discovery documents.
Do not introduce synonyms.
The same business concept should always use the same name across:
* documentation;
* code;
* database;
* API;
* user interface.
Language consistency is considered an architectural requirement.
---
# AI Collaboration Checklist
Before responding, every AI assistant should mentally verify:
□ Do I fully understand the business request?
□ Am I respecting the Blueprint?
□ Am I respecting the Business Discovery?
□ Am I introducing unnecessary complexity?
□ Is this solution maintainable?
□ Am I solving today's problem rather than tomorrow's?
□ Have I explained my reasoning?
If one or more answers are uncertain, improve the response before presenting it.
---
# Engineering Principle
InfraTrack is not built by individual implementations.
It is built through consistent engineering decisions accumulated over time.
Every AI contribution should strengthen the project's architecture, business consistency and maintainability.
The objective is not to write more code.
The objective is to build a better product.
---
# 25. Prompt Engineering Standards
## Purpose
This chapter defines the communication standards between the project owner and AI assistants.
The quality of an AI response depends primarily on the quality of the information it receives.
Poor prompts produce inconsistent engineering decisions.
Well-structured prompts enable AI assistants to reason about the business domain, respect the project architecture and produce maintainable implementations.
Prompt engineering is therefore considered an engineering activity rather than a convenience.
---
# General Philosophy
A prompt should describe a business problem.
It should not describe implementation details unless implementation constraints already exist.
Good prompts explain:
* what problem exists;
* why it matters;
* what constraints apply;
* what success looks like.
The assistant should determine the implementation.
---
# Context Before Task
Never begin with:
> Implement this.
Instead, provide enough context for the assistant to understand the problem.
Preferred structure:
```text

Business Context

↓

Current Capability

↓

Objective

↓

Constraints

↓

Expected Deliverables

```
Understanding always precedes implementation.
---
# Business Context
Every implementation prompt should begin by explaining the business context.
Example:
```text

We are currently implementing UC-003 Perform Inspection.

The inspection workflow has already been validated during Business Discovery.

The objective is to implement the backend service responsible for completing an inspection.

```
The assistant should understand where the requested work fits into the project.
---
# Current Capability
Always identify the business capability currently being developed.
Examples:
* Register Asset
* Create Business Trigger
* Perform Inspection
* Create Work Order
Avoid requesting multiple capabilities within the same prompt.
One capability should produce one implementation cycle.
---
# Scope
Every prompt should clearly define what is included.
Example:
```text

Included

- REST Controller
- Service
- DTO
- Mapper
- Validation

```
Explicit scope prevents unnecessary implementation.
---
# Out of Scope
Equally important is defining what should **not** be implemented.
Example:
```text

Do not implement:

- Notifications
- Mobile Synchronisation
- Reporting
- Authentication Changes

```
AI assistants should never expand the requested scope on their own initiative.
---
# Architectural Constraints
Prompts should explicitly mention architectural constraints whenever relevant.
Examples:
* Backend remains the source of truth.
* Follow Feature-First Architecture.
* Respect Business Discovery terminology.
* No business logic inside controllers.
* Android is offline-first.
The assistant should never infer architectural exceptions.
---
# Existing Knowledge
Avoid repeating information already documented.
Instead, reference project documentation.
Example:
```text

Respect:

- Business Discovery
- Blueprint
- UC-003

```
Project documentation remains the source of truth.
Prompts provide temporary context.
---
# Expected Deliverables
Every implementation prompt should define expected outputs.
Examples:
```text

Expected Deliverables

- Controller
- Service
- DTOs
- Mapper
- Unit Tests
- Documentation Impact

```
Clear expectations reduce incomplete implementations.
---
# Validation Requirements
Every implementation prompt should specify how success will be verified.
Examples:
```text

Requirements

- Project compiles.
- Existing tests continue passing.
- New tests added.
- Business Rules respected.
- Blueprint respected.

```
Completion should always be objectively verifiable.
---
# Documentation Impact
Every implementation prompt should ask:
```text

Does this implementation require documentation updates?

If yes,

which documents?

```
This reinforces living documentation.
---
# Review Expectations
Implementation should not be considered complete until the assistant performs a self-review.
Expected review includes:
* architecture consistency;
* naming consistency;
* business consistency;
* Blueprint compliance;
* documentation impact.
---
# Communication Style
Prompts should remain:
* explicit;
* structured;
* concise;
* objective.
Avoid:
* ambiguous language;
* multiple unrelated objectives;
* hidden assumptions.
Clarity improves engineering quality.
---
# Good Prompt Example
```text

Business Context

We are implementing UC-004 Create Work Order.

Objective

Implement the Spring Boot backend.

Scope

- REST Controller
- Service
- DTO
- Repository
- Mapper

Out of Scope

- React
- Android
- Notifications

Constraints

- Follow Blueprint
- Backend owns business logic
- Feature-first architecture

Deliverables

- Production-ready implementation
- Unit Tests
- Documentation impact analysis

```
---
# Poor Prompt Example
```text

Create Work Orders.

```
The assistant has insufficient context.
This increases implementation risk.
---
# AI Prompt Checklist
Before acting on a prompt, verify:
□ Do I understand the business capability?
□ Is the implementation scope clear?
□ Are architectural constraints defined?
□ Do I know what is intentionally excluded?
□ Are expected deliverables explicit?
□ Do I understand how success will be evaluated?
If any answer is "no", clarification should be requested before implementation begins.
---
# Engineering Principle
Prompt engineering is part of software engineering.
Well-designed prompts produce better architectural decisions, better implementations and better collaboration between humans and AI.
The objective is not to control the AI.
The objective is to provide enough context for the AI to think like a member of the engineering team.
---
# 26. AI Code Review Standards
## Purpose
This chapter defines the self-review process that every AI assistant must perform before presenting an implementation.
Generating code is only one part of software engineering.
Reviewing the implementation is equally important.
AI assistants should therefore perform an internal engineering review before considering any task complete.
The objective is to identify architectural issues, unnecessary complexity and business inconsistencies before they reach the project owner.
---
# Review Philosophy
Every implementation should be reviewed as if it had been submitted by another developer.
The assistant should actively search for:
* mistakes;
* inconsistencies;
* unnecessary complexity;
* architectural violations;
* business misunderstandings.
The review process should be critical rather than defensive.
The objective is not to justify the implementation.
The objective is to improve it.
---
# Business Review
The first review should always be business-oriented.
Questions to ask:
* Does the implementation satisfy the approved Use Case?
* Does it respect the Business Discovery documents?
* Has any business behaviour been invented?
* Has any approved business rule been ignored?
* Does the terminology remain consistent?
Business correctness has the highest priority.
---
# Architecture Review
Verify architectural consistency.
Questions:
* Does this follow the Blueprint?
* Does every responsibility belong to the correct layer?
* Has business logic leaked into the UI?
* Has persistence leaked into controllers?
* Does the implementation respect Feature-First Architecture?
Architecture violations should be corrected before submission.
---
# Responsibility Review
Every class should justify its existence.
Questions:
* Why does this class exist?
* Could an existing component be extended?
* Does it own exactly one responsibility?
* Is it introducing unnecessary coupling?
New classes should solve business problems rather than organisational preferences.
---
# Complexity Review
Complexity should always require justification.
Questions:
* Is this the simplest implementation that satisfies the business?
* Have unnecessary abstractions been introduced?
* Would another developer immediately understand this code?
* Is this solving today's problem or a hypothetical future problem?
If complexity cannot be justified, simplify the implementation.
---
# Naming Review
Verify consistency of terminology.
Questions:
* Does every class use official business terminology?
* Have new synonyms been introduced?
* Do method names describe business actions?
* Would a council employee recognise these terms?
Business language should remain identical across:
* backend;
* frontend;
* Android;
* database;
* documentation.
---
# Security Review
Every implementation should undergo a security review.
Questions:
* Is authentication respected?
* Is authorisation enforced?
* Can business workflows be bypassed?
* Is sensitive information exposed?
* Does the backend remain the source of truth?
Security should never depend on the client.
---
# Data Review
Review persistence behaviour.
Questions:
* Does this preserve business history?
* Could important information be lost?
* Are relationships consistent with the domain?
* Has the database been given business responsibilities?
Business history should always remain protected.
---
# Testing Review
Every implementation should be evaluated from a testing perspective.
Questions:
* Which business rules require tests?
* Which failure scenarios exist?
* Are regression tests required?
* Does the implementation remain testable?
If testing appears difficult, the architecture should be reconsidered.
---
# Documentation Review
Review documentation impact.
Questions:
* Has project knowledge changed?
* Does the Blueprint require updates?
* Does the Business Discovery remain accurate?
* Does the Functional Analysis require revision?
Documentation should evolve only when project knowledge evolves.
---
# Maintainability Review
Evaluate long-term maintainability.
Questions:
* Will another developer understand this implementation in two years?
* Is the code self-explanatory?
* Does it reduce or increase technical debt?
* Can future business capabilities integrate naturally?
Maintainability should always outweigh implementation speed.
---
# Performance Review
Performance should be considered without compromising clarity.
Questions:
* Is there an obvious performance issue?
* Has premature optimisation been introduced?
* Would optimisation reduce readability?
Optimise only when measurable value exists.
---
# Final Engineering Review
Before presenting any implementation, mentally answer the following checklist.
### Business
□ Business behaviour is correct.
□ Business terminology is consistent.
□ No business rules were invented.
---
### Architecture
□ Blueprint respected.
□ Responsibilities correctly assigned.
□ No architectural shortcuts introduced.
---
### Code Quality
□ Readable.
□ Maintainable.
□ Simple.
□ Explicit.
---
### Security
□ Backend remains authoritative.
□ Permissions enforced.
□ No obvious vulnerabilities introduced.
---
### Testing
□ Business behaviour is testable.
□ Critical scenarios identified.
---
### Documentation
□ Documentation impact evaluated.
□ Living documentation preserved.
---
### Maintainability
□ Future developers can understand the implementation.
□ Complexity remains justified.
---
# Reporting Review Results
When appropriate, AI assistants should summarise important review findings.
Example:
```text id="7z4wsm"

Self Review

✔ Blueprint respected.

✔ Business terminology preserved.

✔ No architectural violations detected.

✔ Unit tests required for inspection state transitions.

✔ Business Discovery unchanged.

✔ Functional documentation unchanged.

```
If concerns remain, they should be explicitly communicated.
---
# Engineering Principle
Writing code is only half of software engineering.
Reviewing that code with a critical and objective mindset is equally important.
Every implementation should leave the project:
* easier to understand;
* easier to maintain;
* more consistent;
* more aligned with the business domain.
AI assistants should therefore behave not only as developers, but also as reviewers responsible for protecting the long-term quality of InfraTrack.
---
# 27. Development Workflow
## Purpose
This chapter defines the mandatory development workflow used throughout InfraTrack.
Every business capability follows the same engineering lifecycle regardless of its size or complexity.
The objective is to ensure consistency across all implementations while preserving business understanding, architectural quality and long-term maintainability.
This workflow applies equally to human developers and AI assistants.
---
# Core Principle
InfraTrack is developed **one business capability at a time**.
The project is never developed:
* screen by screen;
* database table by database table;
* endpoint by endpoint.
Instead, development follows complete vertical business slices.
Each completed capability should provide measurable business value.
---
# Standard Workflow
Every capability follows the same lifecycle.
```text

Business Discovery

        │

        ▼

Functional Analysis

        │

        ▼

Use Case

        │

        ▼

Technical Design

        │

        ▼

Implementation

        │

        ▼

Self Review

        │

        ▼

Testing

        │

        ▼

Documentation

        │

        ▼

Capability Complete

```
No stage should be skipped.
---
# Step 1 — Business Discovery
Before implementation begins, contributors must understand:
* the business objective;
* the business terminology;
* the operational workflow;
* the actors involved;
* the business rules.
If business understanding is incomplete, implementation must not begin.
---
# Step 2 — Functional Analysis
Every capability starts with a concise Use Case.
The Use Case defines:
* objective;
* actors;
* trigger;
* main flow;
* alternative flows;
* postconditions;
* related business rules.
The Use Case becomes the functional contract.
---
# Step 3 — Technical Design
Before writing code, determine:
* affected modules;
* affected APIs;
* affected entities;
* required DTOs;
* required services;
* required tests;
* documentation impact.
Architecture should always precede implementation.
---
# Step 4 — Implementation
Implementation should remain focused on the current capability.
Avoid introducing functionality unrelated to the approved scope.
Deliver complete vertical slices.
Do not leave partially implemented workflows.
---
# Step 5 — AI Self Review
Before considering implementation complete, perform the review defined in:
> AI Code Review Standards.
The objective is to identify issues before they reach the project owner.
---
# Step 6 — Testing
Every capability should include appropriate tests.
Tests should verify:
* business behaviour;
* failure scenarios;
* security;
* workflow consistency.
Testing validates the implementation against the Use Case.
---
# Step 7 — Documentation
After implementation, determine whether project knowledge has changed.
If yes:
Update the appropriate documentation.
If no:
No documentation changes are required.
Documentation should remain proportional to the impact of the change.
---
# Definition of Done
A capability is complete only when:
✔ Business behaviour matches the approved Use Case.
✔ Implementation follows the Blueprint.
✔ AI Self Review completed.
✔ Tests implemented.
✔ Documentation reviewed.
✔ Project remains buildable.
Partial completion is not considered completion.
---
# Scope Discipline
During implementation, contributors should resist expanding the scope.
Examples of inappropriate behaviour:
* implementing future capabilities;
* introducing speculative abstractions;
* adding unrelated features;
* redesigning existing modules unnecessarily.
Every capability should solve one business problem.
Nothing more.
---
# Incremental Development
InfraTrack evolves incrementally.
Each completed capability should leave the project:
* more valuable;
* more consistent;
* better documented;
* easier to extend.
Large refactoring phases should become unnecessary through continuous incremental improvement.
---
# Decision Hierarchy
Whenever uncertainty exists, decisions should follow this priority order:
```text

Business Discovery

        │

        ▼

Blueprint

        │

        ▼

Use Case

        │

        ▼

Architecture

        │

        ▼

Implementation

```
Higher levels always take precedence over lower levels.
Implementation should never contradict Business Discovery or the Blueprint.
---
# AI Workflow Checklist
Before beginning implementation, verify:
□ Business Discovery reviewed.
□ Blueprint reviewed.
□ Use Case approved.
□ Scope understood.
□ Constraints identified.
Before submitting implementation, verify:
□ AI Self Review completed.
□ Tests completed.
□ Documentation impact analysed.
□ Capability satisfies the Definition of Done.
---
# Engineering Principle
InfraTrack is developed through disciplined, repeatable engineering.
Every completed capability represents a complete business solution rather than an isolated technical achievement.
The objective is not rapid feature delivery.
The objective is continuous delivery of high-quality business capabilities while preserving the integrity of the entire system.
---
# 28. AI Session Startup Protocol
## Purpose
This chapter defines the mandatory startup procedure that every AI assistant should follow before contributing to InfraTrack.
AI assistants work across independent conversations and sessions.
Project consistency therefore depends on rebuilding the correct engineering context before any implementation begins.
This protocol ensures that every session starts from the same architectural and business understanding.
---
# General Principle
An AI assistant should never begin implementation immediately.
The first responsibility is to rebuild project understanding.
Correct context is more valuable than immediate code generation.
A slower start generally produces a much higher quality implementation.
---
# Startup Objective
Before writing code, the assistant should understand:
* what InfraTrack is;
* why the current capability exists;
* where the capability fits within the project;
* which engineering rules apply;
* which documents govern the implementation.
Implementation should always begin with understanding.
---
# Required Reading Order
When beginning a new development session, documents should be reviewed in the following order.
```text

Blueprint

↓

Business Discovery

↓

Current Use Case

↓

Existing Implementation

↓

Current Task

```
Each document provides a different level of context.
Skipping earlier documents increases implementation risk.
---
# Step 1 — Understand the Product
Review the Blueprint to understand:
* engineering philosophy;
* architecture;
* project standards;
* AI collaboration rules.
The Blueprint explains how the project is built.
---
# Step 2 — Understand the Business
Review the Business Discovery documentation.
Understand:
* business terminology;
* workflows;
* operational responsibilities;
* council organisation;
* business rules.
Never begin implementation without understanding the business.
---
# Step 3 — Understand the Current Capability
Review the approved Use Case.
Identify:
* objective;
* actors;
* workflow;
* alternative flows;
* business rules;
* Definition of Done.
The Use Case defines the implementation scope.
---
# Step 4 — Understand the Existing System
Review the existing implementation before proposing changes.
Determine:
* existing architecture;
* current services;
* naming conventions;
* module organisation;
* existing tests.
Always extend existing architecture before introducing new concepts.
---
# Step 5 — Understand the Requested Task
Only after understanding the project should the assistant analyse the current request.
Determine:
* requested capability;
* implementation scope;
* affected modules;
* documentation impact.
Implementation should begin only after the task has been fully understood.
---
# Clarification Protocol
If any required information is missing:
Do not guess.
Instead:
* identify the missing information;
* explain why clarification is required;
* wait for confirmation.
Incorrect assumptions create technical debt.
Clarification prevents it.
---
# Existing Before New
Before creating:
* a class;
* a service;
* a DTO;
* an endpoint;
* a component;
verify whether an equivalent implementation already exists.
Extending existing architecture is generally preferable to introducing new structures.
---
# Scope Verification
Before implementation, confirm:
* what is included;
* what is intentionally excluded;
* what should remain unchanged.
Scope discipline protects architectural consistency.
---
# Risk Assessment
Before implementation, identify potential risks.
Examples:
* architectural conflicts;
* business inconsistencies;
* duplicated responsibilities;
* security implications;
* synchronisation impact;
* documentation impact.
Significant risks should be communicated before coding begins.
---
# Startup Checklist
Before implementation, mentally verify:
□ Blueprint understood.
□ Business Discovery understood.
□ Current Use Case reviewed.
□ Existing implementation analysed.
□ Current task understood.
□ Scope confirmed.
□ Risks identified.
□ Clarifications requested if necessary.
Only after completing this checklist should implementation begin.
---
# Expected Behaviour
AI assistants should begin development by demonstrating understanding rather than immediately proposing code.
Examples include:
* summarising the business objective;
* identifying affected modules;
* confirming architectural constraints;
* confirming implementation scope.
This provides an opportunity to detect misunderstandings before implementation begins.
---
# Engineering Principle
Good software begins with understanding.
Every implementation should start by rebuilding business context, architectural context and project context.
The objective is not to begin coding quickly.
The objective is to begin coding correctly.
---
# 29. AI Session Closing Protocol
## Purpose
This chapter defines the mandatory closing procedure that every AI assistant should follow before ending a development session.
Software development is a continuous process.
Each session should leave the project in a state that allows another contributor to continue immediately without losing context.
The objective is to preserve continuity, transparency and engineering quality across independent AI sessions.
---
# General Principle
An implementation is not complete simply because the code has been written.
A development session is complete only when:
* the implementation has been reviewed;
* its impact has been evaluated;
* the remaining work has been identified;
* the next contributor has enough information to continue.
Every session should reduce uncertainty for the next one.
---
# Session Summary
Every completed development session should conclude with a concise summary.
The summary should describe:
* what was completed;
* what was intentionally not completed;
* which architectural decisions were made;
* which risks remain.
The objective is to communicate engineering progress rather than implementation details.
---
# Completed Work
Clearly identify every completed capability.
Example:
```text

Completed

✔ UC-003 Backend implementation

✔ Unit Tests

✔ REST API

✔ DTOs

✔ Validation

✔ Documentation updated

```
Completed work should correspond to the agreed implementation scope.
---
# Modified Components
List the project areas affected during the session.
Example:
```text

Modified Components

Backend

- inspection
- workorder

Frontend

- none

Android

- none

Documentation

- UC-003
- API documentation

```
This allows future contributors to quickly identify affected areas.
---
# Architectural Decisions
Summarise important engineering decisions made during the session.
Examples:
* new business service introduced;
* entity relationship modified;
* API structure updated;
* synchronisation strategy adjusted.
Routine implementation details should not appear here.
Only decisions with long-term impact should be recorded.
---
# Documentation Impact
Determine whether project documentation requires updates.
Possible outcomes:
```text

Documentation Impact

✔ No documentation changes required.

```
or
```text

Documentation Impact

Blueprint updated.

Business Discovery updated.

Functional Analysis updated.

```
Documentation should remain aligned with implementation.
---
# Outstanding Work
Identify work that remains incomplete.
Examples:
```text

Outstanding Work

- React implementation
- Android implementation
- Integration Tests

```
Avoid leaving unfinished work undocumented.
---
# Known Risks
Communicate any identified concerns.
Examples include:
* technical debt;
* temporary workaround;
* architectural limitation;
* dependency awaiting clarification;
* performance concern.
If no concerns exist, explicitly state so.
---
# Validation Status
Summarise implementation quality.
Example:
```text

Validation

✔ Blueprint respected

✔ Business Discovery respected

✔ Tests added

✔ Project builds successfully

```
Validation communicates confidence rather than perfection.
---
# Recommended Next Step
Suggest the most logical continuation.
Recommendations should follow project priorities.
Examples:
* implement React interface;
* implement Android workflow;
* write integration tests;
* document new business capability.
Avoid suggesting speculative future work.
---
# Session Handover
The closing report should allow another contributor to continue without reanalysing the completed implementation.
A good handover answers:
* What changed?
* Why?
* What remains?
* Where should work continue?
---
# AI Closing Checklist
Before ending a session, verify:
□ Implementation completed.
□ Self Review performed.
□ Tests completed or identified.
□ Documentation impact analysed.
□ Outstanding work identified.
□ Risks communicated.
□ Next recommended task identified.
A session should never end without completing this checklist.
---
# Standard Closing Template
Every implementation session should finish with a structure similar to:
```text

## Session Summary

Completed

...

Modified Components

...

Architecture Decisions

...

Documentation Impact

...

Outstanding Work

...

Known Risks

...

Validation

...

Recommended Next Task

...

```
The exact wording may vary.
The structure should remain consistent.
---
# Engineering Principle
Engineering knowledge should never disappear when a session ends.
Every completed session should leave the project:
* easier to understand;
* easier to continue;
* easier to review;
* easier to maintain.
A successful handover is considered part of the implementation itself.
---
# 30. Git Workflow
## Purpose
This chapter defines the Git workflow used throughout InfraTrack.
Version control is not only a mechanism for storing code.
It is also the historical record of engineering decisions made during the evolution of the project.
Every commit should improve the project while preserving its stability and traceability.
---
# General Philosophy
Git should reflect the evolution of business capabilities.
Commits should represent meaningful engineering progress rather than arbitrary snapshots of work.
The history should tell the story of the project.
A contributor reading the Git history should understand how InfraTrack evolved over time.
---
# Vertical Slice Development
Each Git contribution should correspond to a complete vertical slice whenever practical.
Preferred progression:
```text

Business Capability

↓

Implementation

↓

Tests

↓

Documentation

↓

Commit

```
Avoid committing partially implemented business capabilities unless absolutely necessary.
---
# Small, Meaningful Commits
Commits should remain focused.
Each commit should solve one problem.
Examples:
✔ Implement Asset registration service
✔ Add inspection validation rules
✔ Introduce Work Order REST API
Avoid combining unrelated changes in the same commit.
Poor examples:
❌ Fix bugs + Update UI + Rename classes + Add Docker changes
Small commits improve review quality and simplify troubleshooting.
---
# Atomic Changes
A commit should leave the project in a working state.
Every commit should:
* compile successfully;
* preserve existing functionality;
* avoid breaking unrelated modules.
Broken intermediate commits should never become part of the permanent history.
---
# Business-Oriented History
Commit history should reflect business evolution rather than implementation details.
Preferred:
```text

Implement Asset Registration

Complete Inspection Workflow

Introduce Operational Decision entity

```
Avoid:
```text

Update service

Fix code

Changes

Misc improvements

```
The history should remain understandable without reading the code.
---
# Commit Frequency
Commit regularly.
Avoid extremely large commits produced after several days of work.
Frequent commits improve:
* traceability;
* rollback capability;
* review quality;
* collaboration.
However, avoid committing unfinished experiments.
---
# Refactoring
Refactoring should generally be committed separately from new functionality.
Preferred sequence:
```text

Refactor Inspection module

↓

Implement Inspection validation

```
Separating refactoring from new features simplifies code reviews.
---
# Generated Files
Generated files should only be committed when they are part of the project's deliverables.
Avoid committing:
* temporary files;
* IDE configuration;
* local caches;
* build artefacts.
The repository should contain only reproducible project assets.
---
# Documentation
Whenever documentation changes are required, they should be committed alongside the implementation that introduced the change.
Business knowledge and implementation should evolve together.
---
# AI Workflow
Before creating a commit, AI assistants should verify:
* Is this capability complete?
* Does the project still build?
* Have tests been updated?
* Has documentation impact been evaluated?
* Does this commit represent one logical engineering change?
If the answer to any question is "no", reconsider committing.
---
# Review Before Commit
Every commit should follow this sequence:
```text

Implement

↓

Self Review

↓

Testing

↓

Documentation Review

↓

Commit

```
Git should never replace engineering discipline.
---
# Repository Integrity
The main development branch should always remain in a stable state.
Every contribution should improve the project rather than introducing uncertainty.
Project stability has higher priority than development speed.
---
# Engineering Principle
Git is the engineering journal of InfraTrack.
Every commit should document a meaningful step in the evolution of the product.
The objective is not simply to save work.
The objective is to preserve a clear, reliable and understandable engineering history.
---
# 31. Branch Strategy
## Purpose
This chapter defines the Git branch strategy used throughout InfraTrack.
The objective is to provide a simple, predictable and maintainable branching model that supports incremental development while preserving repository stability.
Branches exist to isolate engineering work.
They should never become long-lived parallel versions of the project.
---
# General Philosophy
InfraTrack favours a lightweight branching strategy.
The project is developed continuously through small, complete business capabilities.
Branches should support engineering discipline rather than introduce unnecessary process.
Simplicity is preferred over sophisticated branching models.
---
# Main Branch
The `main` branch always represents the latest stable version of the project.
It should always:
* compile successfully;
* pass all automated tests;
* respect the Blueprint;
* remain deployable.
The `main` branch is the reference point for the entire project.
---
# Feature Branches
Every business capability should be developed in its own feature branch.
Branch names should describe the capability being implemented.
Preferred examples:
```text

feature/register-asset

feature/business-trigger

feature/perform-inspection

feature/create-work-order

feature/maintenance-report

```
Avoid technical branch names.
Examples:
```text

feature/backend

feature/new-api

feature/update

feature/test

```
Branch names should communicate business intent.
---
# Branch Lifetime
Feature branches should remain short-lived.
A branch should exist only for the duration of one business capability.
Long-lived branches increase:
* merge conflicts;
* architectural divergence;
* review complexity.
Small branches encourage continuous integration.
---
# Scope Discipline
A feature branch should implement one business capability.
Avoid mixing:
* unrelated bug fixes;
* refactoring;
* infrastructure changes;
* UI redesigns.
Each branch should correspond to a single logical objective.
---
# Bug Fix Branches
Bug fixes should also use dedicated branches.
Preferred examples:
```text

fix/inspection-validation

fix/work-order-permission

fix/android-sync-conflict

```
Bug fixes should remain focused.
Avoid combining unrelated corrections.
---
# Refactoring Branches
Significant refactoring should use dedicated branches.
Examples:
```text

refactor/inspection-module

refactor/security-layer

refactor/api-responses

```
Refactoring should improve maintainability without changing business behaviour.
Whenever possible, refactoring should be completed independently of new feature development.
---
# Documentation Branches
Large documentation improvements may use dedicated branches.
Example:
```text

docs/business-discovery-update

docs/api-guidelines

```
Minor documentation updates should generally remain part of the feature branch that introduced the change.
---
# Branch Review
Before merging a branch, verify:
* business capability completed;
* Blueprint respected;
* tests completed;
* documentation reviewed;
* AI Self Review completed.
Branches should be merged only after satisfying the Definition of Done.
---
# Merge Strategy
Feature branches should produce a clean and understandable project history.
The preferred merge strategy should preserve meaningful commits while avoiding unnecessary complexity.
Merge decisions should prioritise repository readability over preserving every intermediate step.
---
# Delete Completed Branches
Once a branch has been successfully merged:
* delete the feature branch;
* avoid keeping obsolete branches;
* keep the repository organised.
Completed work belongs in the project history.
Not in long-lived branches.
---
# AI Branch Checklist
Before creating a branch, verify:
□ Does this branch represent one business capability?
□ Is the scope clearly defined?
□ Will this branch remain short-lived?
Before merging:
□ Capability complete.
□ Tests passing.
□ Documentation reviewed.
□ Blueprint respected.
□ Repository remains stable.
---
# Engineering Principle
Branches isolate work.
They should never isolate knowledge.
Every merged branch should represent one complete engineering contribution that improves the product while preserving the long-term clarity of the repository.
---
# 32. Commit Convention
## Purpose
This chapter defines the commit message conventions used throughout InfraTrack.
A commit message is more than a description of code changes.
It is a permanent engineering record explaining why the project evolved.
Every commit should communicate business intent clearly, consistently and concisely.
---
# General Philosophy
Commit messages should describe the business capability or engineering improvement introduced by the commit.
A reader should understand **what changed and why** without reading the implementation.
Commit messages should remain meaningful years after they were written.
---
# Commit Structure
InfraTrack follows the Conventional Commits specification with additional business-oriented guidance.
General format:
```text

<type>(<scope>): <short summary>

```
Example:
```text

feat(asset): implement asset registration workflow

```
The summary should describe the delivered capability, not the implementation mechanics.
---
# Supported Commit Types
Use the following commit types consistently.
| Type         | Purpose                                            |
| ------------ | -------------------------------------------------- |
| **feat**     | Introduces a new business capability               |
| **fix**      | Corrects incorrect behaviour                       |
| **refactor** | Improves implementation without changing behaviour |
| **test**     | Adds or improves automated tests                   |
| **docs**     | Updates project documentation                      |
| **perf**     | Improves measurable performance                    |
| **build**    | Build system or dependency changes                 |
| **ci**       | Continuous Integration changes                     |
| **chore**    | Repository maintenance with no business impact     |
Avoid inventing new commit types unless they become part of the official project standard.
---
# Scope
The scope identifies the affected business capability.
Preferred examples:
```text

asset

inspection

business-trigger

work-order

maintenance

security

sync

documentation

```
Avoid technical scopes that provide little business value.
Poor examples:
```text

controller

service

dto

bug

misc

backend

```
The scope should help contributors understand **which business area evolved**.
---
# Summary
The summary should:
* begin with an imperative verb;
* remain concise;
* describe business value.
Preferred examples:
```text

implement asset registration workflow

add inspection validation rules

prevent duplicate work order creation

introduce contractor assignment support

```
Avoid vague summaries.
Poor examples:
```text

update code

fix stuff

changes

cleanup

```
---
# Body
Use the commit body when additional context is valuable.
The body should explain:
* why the change was necessary;
* important architectural decisions;
* business implications;
* migration considerations if applicable.
Do not repeat implementation details already visible in the diff.
---
# Footer
Use the footer for structured metadata when required.
Examples include:
```text

Related: UC-003

ADR: ADR-005

Breaking Change: Asset identifiers are now immutable.

```
Footers improve long-term traceability.
---
# Business-Oriented Examples
Good:
```text

feat(inspection): implement inspection completion workflow

```
Good:
```text

fix(work-order): prevent duplicate maintenance assignment

```
Good:
```text

docs(blueprint): clarify offline-first architecture

```
Poor:
```text

feat: update service

```
Poor:
```text

fix: bug

```
Poor:
```text

misc changes

```
The commit history should tell the story of the product.
---
# Commit Frequency
Prefer several focused commits over one large commit.
Each commit should remain:
* understandable;
* reviewable;
* reversible;
* independently valuable.
Do not commit incomplete work simply to increase commit frequency.
---
# Business Traceability
Whenever possible, commits should reference the corresponding Use Case.
Example:
```text

feat(asset): implement asset registration workflow

Related: UC-001 Register Asset

```
This creates traceability between:
```text

Business Discovery

↓

Use Case

↓

Implementation

↓

Git History

```
---
# AI Commit Checklist
Before proposing a commit message, verify:
□ Does the type accurately describe the change?
□ Does the scope represent the business capability?
□ Does the summary describe business value?
□ Could another developer understand this commit without reading the code?
□ Does the commit contribute to a meaningful project history?
If the answer is "no", rewrite the message.
---
# Engineering Principle
A commit message should explain why the project evolved.
The Git history should read like the evolution of InfraTrack itself rather than a collection of technical modifications.
Every commit should preserve engineering knowledge for future contributors.
---
# 33. Configuration Management
## Purpose
This chapter defines the configuration management principles used throughout InfraTrack.
Configuration allows the same application to operate correctly in different environments without modifying business behaviour.
Configuration should adapt the application to its environment.
It should never redefine how the business works.
---
# Configuration Philosophy
Business logic belongs in the application.
Configuration belongs in the environment.
These responsibilities should never overlap.
Changing an environment should not require changing business code.
Changing business rules should not require changing configuration.
---
# Environment Independence
InfraTrack should operate consistently across multiple environments.
Typical environments include:
* Local Development
* Testing
* Staging
* Production
Each environment may provide different configuration values while preserving identical business behaviour.
---
# Configuration Hierarchy
Configuration should follow a predictable hierarchy.
```text id="cfg1"

Application Defaults

        │

        ▼

Environment Configuration

        │

        ▼

Environment Variables

        │

        ▼

Runtime Secrets

```
Higher-priority configuration overrides lower-priority configuration.
Business behaviour should remain unchanged.
---
# Environment Variables
Sensitive or environment-specific values should be provided externally.
Typical examples include:
* database connection;
* API URLs;
* authentication secrets;
* storage locations;
* SMTP credentials.
Avoid hardcoding environment-specific values in source code.
---
# Secrets Management
Secrets are never source code.
Examples include:
* JWT signing keys;
* database passwords;
* API credentials;
* cloud storage credentials.
Secrets must never be:
* committed to Git;
* hardcoded in Java;
* embedded in React;
* stored inside Android source code.
Secrets belong to the deployment environment.
---
# Business Configuration
Business rules should not become configuration unless councils genuinely require customisation.
Examples of acceptable business configuration:
* organisation name;
* branding;
* notification email;
* supported asset categories (future capability).
Examples of unacceptable business configuration:
* inspection workflow;
* approval rules;
* business lifecycle;
* security responsibilities.
Business rules belong in the domain model.
---
# Application Profiles
Application profiles exist to support different execution environments.
Typical examples:
```text id="cfg2"

development

test

staging

production

```
Profiles should configure infrastructure.
They should not change business behaviour.
---
# React Configuration
React configuration should include only client-specific values.
Examples:
* backend API URL;
* application version;
* feature flags (when approved);
* branding resources.
Business validation must never depend on React configuration.
---
# Android Configuration
Android configuration should remain minimal.
Typical examples:
* backend endpoint;
* synchronisation intervals;
* logging level;
* build configuration.
Operational workflows should remain independent of configuration.
---
# Infrastructure Configuration
Infrastructure should be configurable without modifying application code.
Examples include:
* database host;
* storage provider;
* reverse proxy;
* ports;
* container networking.
Infrastructure concerns should remain outside the business domain.
---
# Feature Flags
Feature flags should be introduced only when they provide measurable operational value.
Avoid using feature flags to permanently support unfinished implementations.
Feature flags should remain temporary engineering tools.
---
# Configuration Documentation
Every configuration option should document:
* purpose;
* default behaviour;
* expected values;
* affected environments.
Configuration should remain understandable without reading implementation code.
---
# AI Configuration Checklist
Before introducing new configuration, verify:
□ Is this truly environment-specific?
□ Does it avoid modifying business behaviour?
□ Could this remain constant instead?
□ Does it expose sensitive information?
□ Has the configuration been documented?
If the answer to the second question is **no**, reconsider the design.
---
# Engineering Principle
Configuration should adapt the application to its environment.
It should never redefine the business.
A correctly designed application behaves consistently regardless of where it is deployed.
Business consistency is an architectural requirement.
Environment flexibility is an operational requirement.
The two should remain clearly separated.
---
# 34. Docker & Deployment Philosophy
## Purpose
This chapter defines the deployment philosophy used throughout InfraTrack.
Deployment is not merely the act of running software.
It is the process of delivering a predictable, reproducible and maintainable operational environment.
Every deployment should produce the same behaviour regardless of where it is executed.
Infrastructure should support the application.
It should never redefine the application.
---
# Deployment Philosophy
InfraTrack is designed to be deployed consistently across all environments.
The deployment process should be:
* reproducible;
* deterministic;
* automated;
* environment-independent.
A deployment should never require manual modifications to the application itself.
---
# Container Philosophy
Every major system component should execute within its own isolated container whenever practical.
Typical components include:
* Spring Boot Backend
* React Frontend
* PostgreSQL Database
* Reverse Proxy
* Supporting Services (future)
Each container owns a single operational responsibility.
Containers should collaborate through well-defined interfaces.
---
# Reproducibility
A deployment performed today should behave identically tomorrow.
The same source code should produce the same application.
Environment differences should be introduced only through configuration.
Reproducibility is considered an engineering requirement rather than a deployment convenience.
---
# Immutable Deployments
Application containers should be treated as immutable.
Avoid modifying running containers manually.
Instead:
```text id="dep1"

Modify Source Code

↓

Build New Image

↓

Deploy New Container

↓

Retire Previous Version

```
Infrastructure should evolve through deployments rather than manual intervention.
---
# Environment Consistency
Development, Testing, Staging and Production should remain as similar as reasonably possible.
Differences should primarily involve:
* configuration;
* secrets;
* infrastructure scale;
* monitoring.
Business behaviour should remain identical across environments.
---
# Service Boundaries
Each deployed service should have a clearly defined responsibility.
Examples:
Backend
* Business logic
* REST API
* Authentication
* Validation
React
* Administrative interface
PostgreSQL
* Persistence
Reverse Proxy
* Routing
* TLS termination
* HTTP security
Services should communicate through explicit interfaces.
Internal implementation should remain encapsulated.
---
# Database Deployment
The database is a critical business asset.
Deployments must preserve:
* business data;
* operational history;
* audit information.
Application deployment should never risk unintended data loss.
Database migrations should be explicit, repeatable and version-controlled.
---
# Persistent Storage
Only persistent business data should survive container replacement.
Examples:
* PostgreSQL data
* Uploaded documents
* Inspection photographs
* Generated reports
Application binaries should remain disposable.
Business information should remain durable.
---
# Secrets During Deployment
Deployment secrets should remain external to the application.
Examples include:
* database credentials;
* JWT keys;
* cloud storage credentials;
* SMTP credentials.
Secrets should never be embedded inside container images.
---
# Logging
Containers should produce structured logs.
Logs should describe business events and operational health.
Avoid storing logs exclusively inside containers.
Logs should remain available even after container replacement.
---
# Health Monitoring
Every deployable service should expose meaningful health information.
Typical health indicators include:
* application availability;
* database connectivity;
* synchronisation status;
* storage availability.
Health monitoring supports operational reliability.
It should not expose sensitive implementation details.
---
# Deployment Automation
Deployments should be automated whenever practical.
The preferred deployment sequence is:
```text id="dep2"

Build

↓

Run Tests

↓

Build Container

↓

Deploy

↓

Health Verification

```
Automation reduces operational risk and improves repeatability.
---
# Rollback Strategy
Every deployment should support recovery.
If deployment fails:
* identify failure quickly;
* restore previous stable version;
* preserve business data;
* investigate root cause before redeployment.
Rollback capability is part of deployment quality.
---
# AI Deployment Checklist
Before introducing deployment changes, verify:
□ Does this improve reproducibility?
□ Does it preserve business data?
□ Does it separate configuration from application behaviour?
□ Does every container have a single responsibility?
□ Can the deployment be repeated without manual intervention?
□ Does the deployment remain understandable?
If one or more answers are uncertain, reconsider the deployment design.
---
# Engineering Principle
Deployment should be predictable.
Containers should be disposable.
Business data should be durable.
Infrastructure should make the application easier to operate without changing how the business behaves.
A successful deployment is one that is repeatable, observable and recoverable.
---
# 35. Architecture Decision Records (ADR)
## Purpose
This chapter defines how major architectural decisions are documented throughout the InfraTrack project.
Architecture is not only defined by the current implementation.
It is also defined by the reasoning behind the decisions that shaped it.
Architecture Decision Records preserve this reasoning.
Their objective is to ensure that future contributors understand **why** important decisions were made, not only **what** those decisions are.
---
# ADR Philosophy
Every significant architectural decision creates long-term consequences.
Without documentation, future contributors often revisit the same discussions repeatedly.
Architecture Decision Records preserve engineering knowledge.
They explain:
* the problem;
* the available options;
* the chosen solution;
* the reasoning behind the decision;
* the consequences of that decision.
An ADR captures engineering intent.
It is not a technical specification.
---
# When to Create an ADR
An ADR should be created when a decision:
* significantly influences the architecture;
* affects multiple modules;
* changes development practices;
* introduces long-term constraints;
* is likely to be questioned in the future.
Routine implementation decisions do not require ADRs.
---
# When Not to Create an ADR
Do **not** create an ADR for:
* bug fixes;
* naming decisions;
* code refactoring;
* library upgrades;
* small implementation details;
* temporary experiments.
ADRs should remain rare.
Their value comes from documenting only important decisions.
---
# ADR Lifecycle
Every ADR follows the same lifecycle.
```text id="adr1"

Problem

↓

Options

↓

Decision

↓

Consequences

↓

Status

```
The objective is to preserve reasoning rather than simply recording outcomes.
---
# ADR Structure
Each Architecture Decision Record should contain the following sections.
## Title
A concise description of the decision.
Example:
```text id="adr2"

ADR-003

Backend Is the Source of Truth

```
---
## Status
Possible values include:
* Proposed
* Accepted
* Superseded
* Deprecated
The current status should always be explicit.
---
## Context
Describe the engineering problem.
Example:
```text id="adr3"

Field employees may operate offline.

Business consistency must still be guaranteed.

The architecture requires a single authoritative source of business data.

```
The context explains **why** the decision was required.
---
## Options Considered
List the realistic alternatives.
Example:
```text id="adr4"

Option A

Backend validates all business operations.

Option B

Android validates offline operations independently.

Option C

Shared validation between backend and Android.

```
Documenting alternatives demonstrates that the decision was deliberate.
---
## Decision
Clearly state the chosen solution.
Example:
```text id="adr5"

The backend remains the single source of truth.

Android may temporarily store business events while offline.

Permanent validation occurs only during synchronisation.

```
The decision should be explicit and unambiguous.
---
## Consequences
Explain the impact of the decision.
Positive consequences:
* consistent business behaviour;
* simplified architecture;
* reliable audit history.
Negative consequences:
* synchronisation becomes more complex;
* backend validation becomes mandatory.
Every architectural decision introduces trade-offs.
Those trade-offs should be acknowledged.
---
# Existing ADR Candidates
InfraTrack already contains several decisions that deserve ADRs.
Examples include:
| ADR     | Decision                              |
| ------- | ------------------------------------- |
| ADR-001 | Feature-First Architecture            |
| ADR-002 | Vertical Slice Development            |
| ADR-003 | Backend Is the Source of Truth        |
| ADR-004 | Offline-First Android                 |
| ADR-005 | Business Discovery Before Development |
| ADR-006 | Asset History Is Never Deleted        |
| ADR-007 | Operational Workflow Is Event-Driven  |
| ADR-008 | Documentation Is Living Knowledge     |
These ADRs explain why InfraTrack looks the way it does.
---
# ADR Evolution
Architecture evolves.
Some decisions may eventually become obsolete.
When this occurs:
* create a new ADR;
* reference the previous ADR;
* explain why the decision changed.
Never silently rewrite architectural history.
Engineering history has value.
---
# ADR Ownership
Architecture Decisions belong to the project.
They do not belong to individual contributors.
Future contributors should feel free to challenge existing ADRs when justified.
However, changes should always be documented through new ADRs rather than modifying historical records.
---
# AI ADR Checklist
Before proposing a new ADR, verify:
□ Does this decision significantly influence the architecture?
□ Will future contributors likely ask why this decision exists?
□ Does the decision affect multiple modules?
□ Are meaningful alternatives available?
□ Does documenting this decision improve long-term understanding?
If the answer to several questions is "no", an ADR is probably unnecessary.
---
# Relationship With Other Documents
Business Discovery explains **how the business operates**.
The Blueprint explains **how the project should be developed**.
Use Cases explain **what a capability must do**.
Architecture Decision Records explain **why the architecture looks the way it does**.
Each document has a unique responsibility.
---
# Engineering Principle
Architecture is the accumulation of decisions.
Code expresses those decisions.
Architecture Decision Records preserve the reasoning behind them.
Future contributors should inherit not only the implementation, but also the engineering thinking that produced it.
A well-maintained ADR repository transforms architecture from undocumented intuition into shared engineering knowledge.
---
# 36. Quality Gates
## Purpose
This chapter defines the mandatory quality gates that every business capability must satisfy before being considered complete.
Quality is not evaluated after development.
Quality is verified continuously throughout the engineering process.
Every completed capability should strengthen the project rather than introducing hidden technical debt.
Quality Gates exist to ensure that every contribution meets the engineering standards defined by the Blueprint.
---
# Quality Philosophy
Quality is a property of the entire engineering process.
It cannot be added at the end of development.
Every implementation should progressively satisfy increasingly demanding quality criteria before reaching completion.
A feature is not considered complete simply because it works.
It is complete only when it satisfies the project's engineering standards.
---
# Quality Gate Overview
Every business capability must pass the following Quality Gates.
```text id="qg1"

Business Understanding

        │

        ▼

Architecture

        │

        ▼

Implementation

        │

        ▼

Testing

        │

        ▼

Documentation

        │

        ▼

AI Self Review

        │

        ▼

Ready for Merge

```
Skipping a Quality Gate is considered an incomplete implementation.
---
# Gate 1 — Business Understanding
Before implementation begins:
Verify:
* Business Discovery reviewed.
* Use Case approved.
* Business terminology understood.
* Scope confirmed.
* Constraints identified.
If business understanding is incomplete:
Implementation must not begin.
---
# Gate 2 — Architecture
Before writing code:
Verify:
* Blueprint respected.
* Existing architecture reviewed.
* Responsibilities correctly assigned.
* No unnecessary abstractions introduced.
* Existing components reused where appropriate.
Architecture should be intentional.
Not accidental.
---
# Gate 3 — Implementation
During implementation:
Verify:
* Business terminology preserved.
* Feature-first organisation respected.
* Code remains readable.
* Responsibilities remain clear.
* Complexity remains justified.
Implementation should improve the project.
Not merely satisfy the requirement.
---
# Gate 4 — Testing
Before completion:
Verify:
* Unit tests added where appropriate.
* Integration tests updated when required.
* Failure scenarios considered.
* Security behaviour validated.
* Existing tests continue passing.
Testing demonstrates confidence.
Not perfection.
---
# Gate 5 — Documentation
Before merge:
Determine whether project knowledge has changed.
If yes:
Update:
* Business Discovery;
* Blueprint;
* Functional Analysis;
* ADRs;
* Architecture documentation;
as appropriate.
If no:
No documentation changes are required.
Documentation should evolve only when knowledge evolves.
---
# Gate 6 — AI Self Review
Every implementation must undergo the self-review defined in:
> AI Code Review Standards.
The review should confirm:
* business consistency;
* architectural consistency;
* maintainability;
* security;
* testing impact;
* documentation impact.
No implementation should bypass this review.
---
# Gate 7 — Build Validation
Before completion:
Verify:
* project builds successfully;
* application starts correctly;
* no compilation errors;
* no failing automated tests;
* configuration remains valid.
A feature that cannot be built is not complete.
---
# Gate 8 — Repository Quality
Before merging:
Verify:
* no dead code;
* no commented-out code;
* no temporary debugging code;
* no unused imports;
* no unresolved TODOs unless explicitly approved.
The repository should remain clean after every contribution.
---
# Gate 9 — Engineering Consistency
Finally, ask:
* Does this implementation still feel like InfraTrack?
* Would another contributor immediately recognise the architecture?
* Does it strengthen long-term maintainability?
* Does it remain consistent with previous business capabilities?
Consistency is the final quality gate.
---
# Quality Gate Checklist
Before considering any business capability complete:
□ Business understood.
□ Blueprint respected.
□ Architecture reviewed.
□ Code implemented.
□ Tests completed.
□ Documentation reviewed.
□ AI Self Review completed.
□ Build successful.
□ Repository clean.
□ Ready for merge.
Every item is mandatory.
---
# AI Responsibilities
AI assistants should treat Quality Gates as mandatory engineering requirements rather than optional recommendations.
If a Quality Gate cannot be satisfied, the assistant should explicitly explain:
* which gate failed;
* why it failed;
* what remains to be completed.
Incomplete work should never be presented as complete.
---
# Relationship With Definition of Done
Quality Gates verify engineering quality.
The Definition of Done verifies project completion.
A capability must first pass every Quality Gate before it can satisfy the Definition of Done.
Quality Gates therefore operate continuously throughout development.
---
# Engineering Principle
Quality is achieved through discipline rather than inspection.
Every Quality Gate reduces uncertainty.
Every completed gate increases confidence.
The objective is not to deliver features quickly.
The objective is to deliver business capabilities that remain reliable, maintainable and understandable throughout the lifetime of the project.
Every contribution should leave InfraTrack in a better state than it was before.
---
# 37. Definition of Ready
## Purpose
This chapter defines the criteria that every business capability must satisfy before implementation begins.
Starting development without sufficient understanding increases the risk of incorrect assumptions, architectural inconsistencies and unnecessary rework.
The Definition of Ready ensures that every implementation starts with a shared understanding of the business objective, scope and constraints.
Development should begin only when the capability is ready.
---
# General Philosophy
Good software is prepared before it is implemented.
Implementation is the consequence of analysis.
It should never become the process used to discover business requirements.
The objective of the Definition of Ready is to replace uncertainty with understanding.
---
# Ready Means Understood
A capability is considered ready when contributors understand:
* why it exists;
* which business problem it solves;
* who will use it;
* how success will be measured;
* which constraints apply.
Understanding is more important than implementation planning.
---
# Business Discovery
Before implementation begins, verify that:
✔ Business terminology exists.
✔ Operational workflow is understood.
✔ Business rules have been identified.
✔ Actors are defined.
✔ Organisational responsibilities are clear.
If Business Discovery is incomplete, implementation must not begin.
---
# Functional Analysis
The Use Case must be approved.
It should clearly define:
* objective;
* actors;
* trigger;
* preconditions;
* main flow;
* alternative flows;
* postconditions;
* business rules.
The implementation team should not need to guess business behaviour.
---
# Scope Definition
The implementation scope must be explicit.
Contributors should know:
Included:
* what will be implemented.
Excluded:
* what will intentionally remain outside the current iteration.
Clear scope protects the project against uncontrolled feature expansion.
---
# Architecture Review
Before implementation:
Verify:
* affected modules identified;
* affected applications identified;
* architectural boundaries understood;
* Blueprint constraints reviewed.
Architecture should guide implementation rather than react to it.
---
# Dependencies
Every external dependency should be identified before development.
Examples include:
* existing services;
* APIs;
* database entities;
* authentication;
* synchronisation;
* documentation.
Unexpected dependencies discovered during implementation should trigger reassessment rather than immediate coding.
---
# Acceptance Criteria
Every capability should define measurable acceptance criteria.
Examples:
* Asset successfully registered.
* Inspection completed.
* Work Order created.
* Business validation enforced.
* Audit history updated.
Acceptance criteria describe observable behaviour.
They do not describe implementation.
---
# Testing Strategy
Before coding begins, identify:
* expected Unit Tests;
* expected Integration Tests;
* expected End-to-End scenarios;
* expected failure cases.
Testing should be planned before implementation.
Not afterwards.
---
# Documentation Impact
Determine whether implementation is expected to affect:
* Business Discovery;
* Blueprint;
* Functional Analysis;
* ADRs;
* Technical Documentation.
Documentation planning should occur before implementation.
---
# Risk Assessment
Before implementation begins, identify significant risks.
Examples:
* unclear business behaviour;
* architectural uncertainty;
* security implications;
* synchronisation complexity;
* integration risks.
Known risks should be discussed before coding starts.
---
# AI Readiness Checklist
Before implementing any capability, verify:
□ Business Discovery complete.
□ Use Case approved.
□ Scope clearly defined.
□ Acceptance criteria agreed.
□ Architecture understood.
□ Dependencies identified.
□ Testing strategy identified.
□ Documentation impact considered.
□ Significant risks identified.
If one or more answers are "no", implementation should be postponed until the missing information has been clarified.
---
# Relationship With Development Workflow
The Definition of Ready corresponds to the first stages of the Development Workflow.
It confirms that:
```text id="dor1"

Business Discovery

↓

Functional Analysis

↓

Use Case

↓

Definition of Ready

↓

Implementation

```
Only capabilities that satisfy the Definition of Ready should enter implementation.
---
# Engineering Principle
Preparation is part of software engineering.
Every hour invested in understanding the business reduces multiple hours of future rework.
InfraTrack values deliberate engineering over rapid implementation.
A capability should begin only when the team understands what must be built and why it matters.
---
# 38. Definition of Done
## Purpose
This chapter defines the mandatory completion criteria for every business capability developed within InfraTrack.
A capability is considered complete only when it satisfies both the business requirements and the engineering standards defined throughout this Blueprint.
Completing implementation does not mean completing the capability.
Completion is achieved only when the capability is ready to become a permanent part of the product.
---
# General Philosophy
Working software is necessary.
Reliable, maintainable and understandable software is the objective.
A capability should never be considered complete simply because it compiles or produces the expected output.
Completion represents confidence in the implementation.
---
# Business Completion
The implemented capability must satisfy the approved Use Case.
Verify:
* Business objective achieved.
* Main workflow implemented.
* Alternative flows implemented where required.
* Business rules enforced.
* Business terminology respected.
The implementation should accurately reflect the Business Discovery documentation.
---
# Architectural Completion
The implementation must respect the architectural principles defined in the Blueprint.
Verify:
* Feature-First Architecture respected.
* Backend remains the source of truth.
* Responsibilities correctly assigned.
* No architectural shortcuts introduced.
* Module boundaries preserved.
Architecture should remain stronger after the implementation than before it.
---
# Code Completion
Production code should satisfy the project's coding standards.
Verify:
* Readable.
* Maintainable.
* Explicit.
* Business-oriented.
* No unnecessary abstraction.
* No dead code.
* No commented-out code.
Code quality is considered part of feature completeness.
---
# Testing Completion
Testing must provide confidence that the capability behaves correctly.
Verify:
* Unit Tests completed.
* Integration Tests updated when required.
* Critical business rules verified.
* Failure scenarios covered.
* Existing automated tests continue passing.
Testing should demonstrate business correctness rather than implementation coverage.
---
# Security Completion
Every completed capability must preserve the project's security model.
Verify:
* Authentication respected.
* Authorization enforced.
* Business permissions verified.
* No sensitive information exposed.
* Audit requirements preserved.
Security is never considered optional.
---
# Documentation Completion
Documentation should accurately reflect project knowledge.
Verify whether updates were required for:
* Business Discovery;
* Functional Analysis;
* Blueprint;
* ADRs;
* Technical Documentation.
If documentation remains unchanged, confirm that no project knowledge evolved.
---
# AI Self Review Completion
Every implementation must pass the AI Self Review defined in:
> AI Code Review Standards.
The review should confirm:
* business consistency;
* architectural consistency;
* maintainability;
* documentation impact;
* testing impact.
The review is part of the Definition of Done.
---
# Build Completion
The project must remain operational.
Verify:
* successful compilation;
* successful application startup;
* dependency integrity;
* configuration validity.
A feature that prevents deployment is not complete.
---
# Repository Completion
The repository should remain clean.
Verify:
* no temporary debugging code;
* no unused imports;
* no obsolete TODOs;
* no experimental code;
* no broken modules.
Every contribution should leave the repository cleaner or at least no worse than before.
---
# Knowledge Completion
A completed capability should improve the project beyond the source code itself.
Future contributors should understand:
* why the capability exists;
* how it behaves;
* where it belongs;
* how it integrates with the rest of the system.
Engineering knowledge is considered part of the deliverable.
---
# Definition of Done Checklist
Before marking a capability as complete, verify:
□ Business Discovery respected.
□ Use Case satisfied.
□ Blueprint respected.
□ Architecture preserved.
□ Code reviewed.
□ Tests completed.
□ Security verified.
□ Documentation reviewed.
□ AI Self Review completed.
□ Build successful.
□ Repository clean.
□ Ready for merge.
Every criterion is mandatory.
Partial completion is not considered completion.
---
# Relationship With Quality Gates
Quality Gates validate engineering quality throughout implementation.
The Definition of Done confirms that every Quality Gate has been successfully passed.
A capability cannot satisfy the Definition of Done without first satisfying every Quality Gate.
---
# Ready for Merge
Only capabilities satisfying the Definition of Done may be merged into the main branch.
Merging incomplete work transfers technical debt to future contributors.
InfraTrack intentionally prevents this.
---
# Engineering Principle
Completion is not the moment when development stops.
Completion is the moment when the project owner, future contributors and AI assistants can trust that the capability has become a stable, maintainable and understandable part of the product.
Every completed capability should increase confidence in the project.
Never uncertainty.
---
# 39. Final Engineering Principles
## Purpose
This chapter concludes the InfraTrack Blueprint by defining the engineering principles that guide every architectural decision, implementation and collaboration throughout the project.
These principles are intentionally independent of programming languages, frameworks and technologies.
They represent the engineering values that every contributor—human or AI—is expected to uphold.
Whenever uncertainty exists, these principles take precedence over implementation preferences.
---
# Principle 1 — Business Before Technology
Technology exists to support the business.
Business requirements define the architecture.
Frameworks, libraries and implementation details are secondary.
Every technical decision should begin by understanding the operational problem it solves.
---
# Principle 2 — Understand Before Implementing
Implementation should never be used to discover requirements.
Every capability begins with:
* Business Discovery;
* Functional Analysis;
* an approved Use Case.
Understanding precedes implementation.
---
# Principle 3 — Architecture Before Code
Code is a consequence of architecture.
Architecture defines:
* responsibilities;
* boundaries;
* interactions;
* ownership.
Implementation should strengthen the architecture rather than reshape it.
---
# Principle 4 — Consistency Before Convenience
Consistency improves maintainability.
When several valid solutions exist, prefer the one that best preserves the project's consistency.
Predictable software is easier to understand, maintain and extend.
---
# Principle 5 — Vertical Slices Over Technical Layers
InfraTrack is developed one business capability at a time.
Business capabilities span:
* Backend;
* React;
* Android;
* Database;
* Documentation;
* Testing.
The project evolves through complete business outcomes rather than isolated technical changes.
---
# Principle 6 — The Backend Is the Source of Truth
Business rules belong to the backend.
Clients:
* present information;
* collect input;
* support operational work.
Business validation and business consistency remain server responsibilities.
---
# Principle 7 — Documentation Preserves Knowledge
Documentation captures engineering knowledge.
Source code explains implementation.
Documentation explains:
* why;
* intent;
* architecture;
* business reasoning.
Knowledge should never depend on individual memory.
---
# Principle 8 — Quality Is Continuous
Quality is not a final verification step.
It is built continuously through:
* disciplined architecture;
* readable code;
* testing;
* documentation;
* review.
Every contribution should improve the project.
---
# Principle 9 — AI Must Reason Before Coding
AI assistants are engineering collaborators.
They should:
* analyse;
* question;
* justify;
* review;
* improve.
Generating code is only one part of engineering.
Reasoning is equally important.
---
# Principle 10 — Simplicity Requires Discipline
Simple software is not created accidentally.
It is the result of deliberate engineering decisions.
Avoid:
* unnecessary abstractions;
* speculative design;
* hidden complexity.
Every additional layer should provide measurable value.
---
# Principle 11 — Preserve the Language of the Business
Business terminology is part of the architecture.
The same concepts should use the same names across:
* documentation;
* database;
* backend;
* frontend;
* Android;
* APIs.
A shared vocabulary reduces ambiguity and improves collaboration.
---
# Principle 12 — Every Contribution Leaves the Project Better
Every contribution should improve at least one aspect of the project.
Examples include:
* clearer code;
* stronger architecture;
* improved documentation;
* additional tests;
* reduced complexity;
* better consistency.
Software quality is the result of continuous improvement.
---
# Engineering Mindset
InfraTrack is not built through isolated coding tasks.
It is built through thousands of consistent engineering decisions.
Every contributor is responsible for protecting:
* business integrity;
* architectural consistency;
* engineering quality;
* long-term maintainability.
Engineering excellence is achieved through discipline rather than individual brilliance.
---
# Decision Hierarchy
Whenever uncertainty exists, decisions should follow this order:
```text

Business Discovery

        │

        ▼

Blueprint

        │

        ▼

Architecture Decision Records

        │

        ▼

Functional Analysis

        │

        ▼

Use Cases

        │

        ▼

Implementation

```

Higher-level knowledge always takes precedence over lower-level implementation.

---

# Engineering Conventions (V1.0.1)

## Purpose

This chapter defines the engineering conventions established during the implementation and hardening of InfraTrack Version 1.

Unlike the architectural principles described elsewhere in this Blueprint, these conventions define **how contributors are expected to implement new features and refactor existing code** so that the codebase remains consistent, maintainable, and scalable over time.

Unless an Architecture Decision Record (ADR) explicitly states otherwise, every contribution should follow these conventions.

---

## Service Architecture

Application Services are **orchestrators**.

Their responsibility is to coordinate a complete business use case.

Application Services should:

- Coordinate business operations.
- Manage transactions.
- Invoke repositories.
- Delegate specialised responsibilities to dedicated collaborators.
- Return business results.

Application Services should **not** accumulate unrelated responsibilities.

When a service becomes too large, extract focused collaborators instead of introducing generic abstractions.

---

## Authorization Services

Role-based authorization must be implemented inside dedicated Authorization Services.

Examples include:

- InspectionAuthorizationService
- WorkOrderAuthorizationService
- OperationalDocumentAuthorizationService

Authorization logic should never be duplicated across controllers or helper classes.

Business permissions should remain explicit and easy to locate.

---

## History Recorders

Operational history must always be written through dedicated History Recorder components.

Examples include:

- InspectionHistoryRecorder
- WorkOrderHistoryRecorder
- OperationalDocumentHistoryRecorder

History recording should remain centralised in order to:

- guarantee consistency;
- simplify auditing;
- reduce duplicated history logic;
- preserve traceability.

Application Services should never write history directly if a dedicated recorder already exists.

---

## Resolvers

Whenever a business operation requires resolving multiple related entities, prefer dedicated Resolver classes.

Typical responsibilities include:

- loading business entities;
- validating ownership;
- validating relationships;
- resolving business context.

Resolvers should **never** perform permission checks.

Authorization belongs exclusively to Authorization Services.

---

## Validation Strategy

InfraTrack distinguishes two categories of validation.

### Request Validation

Structural validation is performed using Jakarta Bean Validation.

Typical annotations include:

- `@NotBlank`
- `@NotNull`
- `@Positive`
- `@Email`
- `@Size`

Controllers should validate request structure before business logic is executed.

---

### Business Validation

Business validation belongs exclusively inside Application Services.

Examples include:

- workflow rules;
- permission checks;
- state transitions;
- operational constraints;
- business invariants;
- cross-entity consistency.

Business validation must never rely solely on Bean Validation annotations.

---

## Exception Strategy

Business errors should be represented using dedicated Business Exceptions.

Examples include:

- `NotFoundException`
- `ConflictException`
- `BusinessValidationException`
- `ForbiddenOperationException`

Application Services should throw business exceptions.

`GlobalExceptionHandler` is responsible for translating business exceptions into HTTP responses.

Controllers should never construct business error responses manually.

---

## Controller Responsibilities

Controllers should remain intentionally small.

Controllers should:

- validate request structure;
- invoke Application Services;
- return responses.

Controllers should never:

- implement workflows;
- access repositories directly;
- contain business rules;
- duplicate authorization logic.

---

## Repository Responsibilities

Repositories are responsible only for persistence.

Repositories should:

- load entities;
- persist entities;
- expose query methods.

Repositories should never:

- implement workflows;
- perform authorization;
- enforce business rules.

---

## Frontend Architecture

React Pages are orchestration components.

Pages should:

- load data;
- own page state;
- invoke backend APIs;
- coordinate reusable UI components.

Presentation logic belongs inside reusable components.

Business logic always remains in the backend.

Global state should not be introduced unless clearly justified.

---

## API Error Handling

Frontend API errors should use the shared error helper utilities.

Avoid duplicating HTTP error parsing across pages.

Error handling should remain consistent throughout the application.

---

## Database Evolution

Every database schema modification must use Flyway.

Direct schema changes are prohibited.

Every migration must be:

- versioned;
- reproducible;
- backward compatible whenever possible.

Hibernate automatic schema updates must never be relied upon in production.

---

## Testing Requirements

A feature is not considered complete until the following commands succeed.

Backend:

```bash
mvn clean test
```

Packaging:

```bash
mvn clean package -DskipTests
```

Frontend:

```bash
npm run build
```

Docker:

```bash
docker compose up --build
```

All commands must complete successfully before requesting review.

---

## Smoke Testing

Every completed business capability must be manually validated through smoke testing.

Smoke tests verify:

- business behaviour;
- permissions;
- workflow integrity;
- notifications;
- asset history;
- operational documents;
- edge cases.

No feature is considered complete before successful smoke testing.

---

## Documentation Rules

Documentation is part of the implementation.

Business behaviour changes must update:

- Business Discovery;
- Functional Analysis;
- Business Rules;
- Use Cases.

Architectural changes must update:

- this Blueprint;
- relevant ADRs.

Documentation should always remain synchronised with the implementation.

---

## AI Development Rules

AI assistants working on InfraTrack are expected to:

- analyse before implementing;
- preserve the documented architecture;
- preserve business terminology;
- minimise technical debt;
- favour explicit solutions over clever abstractions;
- challenge inconsistent requests when appropriate.

AI assistants should never:

- invent business rules;
- bypass documented architecture;
- introduce speculative abstractions;
- optimise prematurely;
- modify workflows without documentation updates.

---

## Release Checklist

Before considering any sprint complete, verify the following:

- [ ] Business behaviour unchanged (unless intentionally modified).
- [ ] Documentation updated.
- [ ] All backend tests pass.
- [ ] Backend package builds successfully.
- [ ] Frontend builds successfully.
- [ ] Docker images build successfully.
- [ ] Manual smoke tests completed.
- [ ] No unnecessary abstraction introduced.
- [ ] No duplicated business logic introduced.
- [ ] No API contract broken.
- [ ] No database inconsistency introduced.

---

## Engineering Principles

InfraTrack is developed as a long-term enterprise software product.

The following principles guide every implementation:

- Business correctness has priority over technical convenience.
- Explicit code is preferred over hidden behaviour.
- Consistency is preferred over cleverness.
- Readability is preferred over premature optimisation.
- Simplicity is preferred over unnecessary abstraction.
- Every implementation should leave the codebase in a better state than it was found.

---

# Final Commitment

Every contributor to InfraTrack commits to:

* understanding before implementing;
* preserving the business domain;
* respecting the architecture;
* maintaining consistency;
* documenting knowledge;
* testing behaviour;
* reviewing critically;
* collaborating professionally.

The objective is not simply to produce software.

The objective is to build software that remains understandable, maintainable and valuable for many years.

---

# Closing Statement

InfraTrack is more than an application.

It is a long-term engineering project built around business understanding, architectural discipline and collaborative software development.

Technology will evolve.

Frameworks will change.

Programming languages will change.

The engineering principles defined in this Blueprint are intended to endure.

Every decision should strengthen the product.

Every implementation should respect the business.

Every contribution should leave InfraTrack better than it was before.
