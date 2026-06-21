# Maintenance Guide

## Introduction

This document provides guidance for developers responsible for maintaining and evolving the Business Platform Template.

Rather than describing implementation details, it explains the principles that should be followed when introducing new features or modifying existing ones.

The objective is to preserve consistency across the platform while allowing applications built from it to evolve with changing business requirements.

---

## Preserve Business Ownership

Business rules belong in the backend.

Whenever a new feature is introduced, validation, permissions and workflow decisions should be implemented within the Spring Boot application.

React and Android should remain responsible only for presenting information and collecting user input.

---

## Maintain Client Consistency

The platform supports multiple client applications serving different users.

Although their interfaces differ, all clients must produce identical business behaviour when accessing the same backend.

Any feature introduced in one client should be evaluated to determine whether equivalent platform support is required.

---

## Keep the Architecture Simple

Additional libraries, frameworks or architectural patterns should only be introduced when they solve an existing problem.

The Business Platform Template intentionally avoids unnecessary complexity.

Future contributors should preserve this philosophy unless the size and scope of the application clearly justifies a different approach.

---

## Extend Existing Workflows

New functionality should integrate with existing platform infrastructure whenever possible.

For example:

- reuse notification infrastructure rather than introducing additional messaging systems;
- extend existing user roles before creating new authorization models;
- integrate with existing backend patterns (Controller → Service → Repository).

Maintaining coherent architecture reduces long-term maintenance costs.

---

## Preserve Data Consistency

The backend remains the authoritative source of business data.

Local caches, notifications and client-side state should never become independent sources of truth.

Whenever data ownership becomes unclear, responsibility should remain with the backend.

---

## Removing Features

Removing obsolete functionality is considered a normal part of the project's evolution.

Unused code, abandoned workflows and unnecessary abstractions should be removed rather than preserved.

A smaller and more coherent codebase is easier to understand and maintain than one that attempts to support every historical implementation.

---

## Code Quality

When modifying the project:

- prefer readable code over clever implementations;
- follow existing naming conventions;
- keep responsibilities explicit;
- avoid duplicate business logic;
- remove dead code when it is discovered.

Consistency is generally more valuable than individual optimisation.

---

## Before Merging Changes

Before introducing significant changes, verify that:

- business rules remain enforced by the backend;
- both clients continue to behave consistently;
- existing workflows remain coherent;
- documentation is updated where necessary;
- obsolete code has been removed;
- new code follows the existing project philosophy;

Maintaining these practices helps ensure that the project remains understandable as it grows.
