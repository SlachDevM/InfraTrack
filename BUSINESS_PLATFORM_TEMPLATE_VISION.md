# Business Platform Template Vision

## Purpose

The Business Platform Template is a reusable foundation for building professional business applications.

It is not a framework.

It is not a low-code platform.

It is not a generic CRUD generator.

Its purpose is simple:

> Provide a proven, production-ready foundation so that new projects can focus entirely on business requirements instead of rebuilding infrastructure.

The template is extracted from a real production-oriented application (MRRG) after its architecture, workflows and production configuration have been validated.

Only infrastructure that proved to be reusable is kept.

Everything else is intentionally removed.

---

# Design Philosophy

The platform follows exactly the same philosophy as MRRG.

## Simplicity before abstraction

The platform should always remain explicit.

It must not introduce abstractions that solve hypothetical problems.

No feature exists because it "might be useful later."

If a future project needs something, it should be added when the need actually exists.

---

## Business before technology

The template provides infrastructure.

It never provides business logic.

Every application built from this template must define its own domain model, workflows and business rules.

Examples:

* inspections
* fleet management
* inventory
* maintenance
* permits
* asset management

The template should know nothing about them.

---

## Backend owns the business

The backend always remains the source of truth.

The template provides the mechanisms.

Future projects provide the rules.

Business validation.

Permissions.

Workflow.

Notifications.

These belong to the application being built, never to the template itself.

---

## Remove instead of generalize

When extracting MRRG into this platform, the default decision is:

> Remove.

Not:

> Generalize.

If a feature is only useful for MRRG, it should disappear.

The template should never contain fake business examples simply to demonstrate patterns.

---

## Readability over cleverness

The platform must remain understandable by a developer joining the project years later.

Explicit code is preferred over clever abstractions.

The goal is maintainability, not sophistication.

---

# What the Platform Provides

The template provides only infrastructure that has demonstrated long-term value across business applications.

## Backend

* Spring Boot configuration
* JWT authentication
* Spring Security
* User management
* Role management
* Account activation
* Email infrastructure
* Notification infrastructure
* Firebase integration
* PostgreSQL configuration
* Docker configuration
* Production profiles
* Environment variable support
* Error handling
* OpenAPI configuration

No business entities are included.

---

## React

The template provides:

* authentication
* protected routing
* administration shell
* user management
* notification center
* API client
* environment configuration

No business pages are included.

---

## Docker

The platform provides:

* development environment
* production environment
* PostgreSQL
* Mailpit
* environment variable strategy
* production configuration

Infrastructure only.

---

## Documentation

The template documents:

* philosophy
* architecture
* installation
* maintenance
* security
* deployment

It intentionally contains no customer-specific documentation.

---

# What the Platform Does NOT Provide

The following must never become part of the platform.

Business entities.

Business workflows.

Business statuses.

Business dashboards.

Business terminology.

Customer branding.

Customer documentation.

Customer emails.

Every project must create these itself.

---

# No Fake Business Layer

The platform intentionally does not include example entities such as:

* Project
* Task
* Order
* Ticket

These examples rapidly become fake business logic that must be maintained forever.

Instead, the platform starts with no business module.

Each new project creates its own first entity.

---

# Notification Philosophy

The platform provides the notification infrastructure only.

It provides:

* Notification entity
* Notification repository
* Notification service
* Firebase integration

It does not provide business notification types.

Each project defines its own notification events.

Examples:

InspectionCompleted

PermitApproved

VehicleAssigned

InvoiceRejected

The platform should never attempt to generalize these.

---

# Architecture

The architecture intentionally remains simple.

Backend:

Controller

↓

Service

↓

Repository

Frontend:

UI

↓

API Client

↓

Backend

No additional architectural layers are introduced.

No framework-specific abstractions are added.

---

# Creating a New Application

Building a new application should follow this sequence.

1. Rename the project.

2. Rename packages.

3. Configure environment variables.

4. Replace branding.

5. Create the first business entity.

6. Create the corresponding repository.

7. Create the service.

8. Create the controller.

9. Create the React pages.

10. Implement the business workflow.

The platform itself should require very little modification.

---

# Evolution Rules

The template should evolve very carefully.

A feature should only become part of the platform if:

* it has already been implemented successfully;
* it has proven reusable;
* at least two projects benefit from it;
* it reduces duplicated infrastructure without introducing unnecessary complexity.

Otherwise it belongs to the application, not the platform.

---

# Long-Term Goal

The objective is not to build a framework.

The objective is to build a stable engineering foundation.

Every application created from this platform should share:

* the same architecture;
* the same production standards;
* the same security model;
* the same deployment strategy;
* the same development philosophy.

Only the business domain should change.

Everything else should remain consistent.

---

# Success Criteria

The platform is considered successful if:

* a new business application can be started in hours instead of weeks;
* no infrastructure needs to be redesigned;
* developers immediately understand the project structure;
* every application remains explicit and maintainable;
* the platform never becomes more complex than the applications built from it.

If a future modification makes the platform more impressive but less understandable, it should be rejected.

The platform exists to accelerate business software development, not to demonstrate technical sophistication.