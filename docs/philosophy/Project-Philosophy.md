# Project Philosophy

## Introduction

MRRG is a business management platform developed for Margaret River Re-Gutter, a roofing company based in Western Australia.

The project is designed to solve real operational needs while remaining straightforward to understand, maintain and evolve. Every technical decision is driven by current business requirements rather than anticipated future complexity.

This document describes the engineering principles that guide the evolution of the project.

---

## Simplicity Before Abstraction

MRRG favors simple and explicit solutions over architectural complexity.

Abstractions are introduced only when they solve an existing problem. If a design adds indirection without providing a measurable benefit, it is intentionally avoided.

This principle influences every part of the project, from package organization to dependency management and feature design.

---

## Business-Driven Development

Technical decisions are driven by business requirements rather than technology preferences.

Features are implemented only when they support a real operational workflow. Likewise, features that no longer provide business value are removed instead of being preserved for historical reasons.

The codebase should represent the current needs of the business, not every idea that has ever existed.

---

## The Backend Owns the Business Rules

Business rules are centralized within the Spring Boot backend.

The React web application and the Android application focus on presenting information and interacting with users, while the backend remains responsible for enforcing permissions, validation and workflow consistency.

This approach guarantees identical behavior across every client.

---

## Build Only What Is Needed

MRRG evolves incrementally.

New technologies, libraries and architectural patterns are introduced only when justified by the current size and complexity of the project.

This philosophy has led to deliberately simple choices throughout the codebase, reducing maintenance costs while keeping the project easy to understand.

---

## Maintainability Over Cleverness

Readable code is preferred over clever code.

The project prioritizes consistency, explicit responsibilities and predictable behavior. Every component should be understandable without requiring knowledge of unnecessary abstractions or design patterns.

Long-term maintainability is considered more valuable than short-term technical sophistication.

---

## Consistency Across the Ecosystem

MRRG consists of a shared backend, a React web application and an Android application.

Although each client serves a different audience, they are expected to behave consistently. Business workflows, permissions and data should produce the same results regardless of the client being used.

---

## Continuous Improvement

The project is continuously refined as new requirements emerge and better solutions are identified.

Improving existing code, simplifying implementations and removing unnecessary technical debt are considered part of normal development rather than separate maintenance activities.

The objective is not to build a perfect system from the beginning, but to build a system that remains understandable and maintainable as it grows.