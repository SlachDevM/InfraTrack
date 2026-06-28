# InfraTrack V2 — Sprint 0 Technical Baseline

**Version:** V2 Sprint 0
**Git Tag:** `v2.0.0-sprint0`
**Status:** Completed

---

# Overview

Sprint 0 marks the transition between the completed V1.0.1 release and the beginning of V2 feature development.

Unlike previous sprints, Sprint 0 introduced no new business functionality.

Its objective was to strengthen the project's technical foundations by improving security, maintainability, documentation, developer experience and build quality.

The outcome is a significantly more robust development baseline for all future V2 work.

---

# Objectives

Sprint 0 focused on five major areas:

* Security hardening
* Infrastructure and secret management
* Documentation and deployment
* Continuous Integration and quality gates
* Code quality and maintainability

No business workflows were modified.

---

# Security Improvements

## Secret management

Firebase service account credentials are no longer stored inside the repository.

The application now supports external credential injection through environment configuration.

Firebase becomes an optional component:

* available when credentials are provided;
* automatically disabled otherwise.

---

## Swagger production lockdown

Swagger and OpenAPI documentation are now enabled only for development environments.

Production deployments no longer expose API documentation.

---

## Login protection

Authentication endpoints now include brute-force protection using rate limiting.

When the limit is exceeded:

* HTTP 429 is returned;
* Retry-After header is included;
* frontend displays a user-friendly retry message.

---

## JWT optimisation

The JWT signing key is now computed once during application startup and reused throughout the application lifecycle.

This removes unnecessary cryptographic recomputation while preserving full token compatibility.

---

## Startup diagnostics

Application startup now reports the Firebase state clearly:

* credentials loaded;
* credentials missing;
* FCM disabled.

---

# Documentation

Documentation has been significantly expanded.

New documentation includes:

* deployment guide;
* production checklist;
* backup and restore procedures;
* troubleshooting guide;
* secrets management;
* updated project README.

The project can now be deployed by following documented procedures rather than relying on tribal knowledge.

---

# Continuous Integration

CI has been redesigned into a unified pipeline.

The pipeline now validates:

* backend build;
* backend tests;
* frontend tests;
* frontend build;
* Docker configuration;
* dependency vulnerability scanning;
* JaCoCo coverage generation.

Coverage reports are published as build artifacts.

Coverage thresholds are intentionally informational during Sprint 0 and may become enforced in a future release.

---

# Code Quality

Several maintainability improvements were completed.

These include:

* constructor injection replacing field injection;
* SQL logging isolated to the development profile;
* confirmed N+1 query removed from user listing;
* optional dependency handling improved.

No functional behaviour changed.

---

# Quality Metrics

Current automated validation:

Backend:

* 497 automated tests
* 0 failures

Frontend:

* 135 automated tests
* 0 failures

Code coverage (JaCoCo):

* Instruction: ~70%
* Branch: ~65%
* Line: ~72%

The project now generates HTML and XML coverage reports for every CI execution.

---

# Business Impact

Sprint 0 intentionally introduced no functional changes.

Existing workflows remain identical to V1.0.1.

All improvements are transparent to end users.

---

# Technical Outcome

InfraTrack now provides:

* stronger security posture;
* cleaner dependency injection;
* improved deployment process;
* reproducible CI pipeline;
* improved documentation;
* measurable code coverage;
* better operational diagnostics.

The project is now ready to begin functional V2 development.

---

# Next Phase

Sprint 1 begins feature development.

The initial V2 backlog focuses on:

* Android application
* Workflow improvements
* Business enhancements
* Additional operational features

Future technical work (Spring Boot upgrades, additional performance optimisations and further code quality improvements) will be handled independently from business feature development.

---

# Conclusion

Sprint 0 successfully established the technical foundation for InfraTrack V2.

With a stable architecture, strengthened security, improved documentation and nearly 500 automated backend tests, future development can focus primarily on delivering business value rather than technical remediation.
