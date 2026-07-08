# Architecture

Architecture Decision Records (ADRs) and Business Decision Records (BDRs) for InfraTrack.

## Current Documentation Baseline

| Item | Value |
|------|-------|
| **Backend platform** | V2.6.x |
| **React web** | V2.6.x |
| **Android application** | v1.3.0 |
| **Documentation status** | Living Documentation |
| **Platform status** | Internally validated |
| **Delivered platform history** | [Platform Version History](../06-release-notes/platform-version-history.md) |
| **Active development** | M6.6 — see [Product Roadmap](../06-release-notes/v2-roadmap.md) |

Platform versions (V2.x) and Android application versions (v1.x) are independent numbering schemes.

## Purpose

This folder records **why** important technical and product constraints exist. ADRs capture implementation structure; BDRs capture workflow governance and product promises.

## Audience

- Developers implementing features
- Architects reviewing changes
- Product owners validating scope
- Contributors preparing ADRs or BDRs

## Recommended reading order

1. [**ADR Index**](ADR-INDEX.md) — complete list, summaries, and when to create new records
2. [ADR-003 — V2 Domain-Driven Workflow](adr-003-v2-domain-driven-workflow.md) — V2 layer model
3. [BDR-001](bdr-001-human-in-the-loop-decision-engine.md) and [BDR-002](bdr-002-preventive-candidates-before-automation.md) — human validation philosophy
4. [BDR-005 — Offline & Synchronization](bdr-005-offline-synchronization-architecture.md) with [BDR-006 — Conflict Resolution](bdr-006-conflict-resolution-strategy.md) — mobile offline platform
5. [Domain Engine](../07-business-architecture/domain-engine.md) — authoritative behaviour (read alongside ADRs/BDRs)

## Normative documents (Accepted)

| ID | Document |
|----|----------|
| ADR-001 | [Asset History Starts with Registration](ADR-001-asset-history-starts-with-registration.md) |
| ADR-002 | [Inspection Produces At Most One Issue (V1)](ADR-002-inspection-produces-at-most-one-issue-v1.md) |
| ADR-003 | [V2 Domain-Driven Workflow](adr-003-v2-domain-driven-workflow.md) |
| ADR-004 | [Platform Versioning Strategy](adr-004-platform-versioning-strategy.md) |
| BDR-001 | [Human-in-the-Loop Decision Engine](bdr-001-human-in-the-loop-decision-engine.md) |
| BDR-002 | [Preventive Candidates Before Automation](bdr-002-preventive-candidates-before-automation.md) |
| BDR-003 | [Bearer Token Architecture](bdr-003-bearer-token-architecture.md) |
| BDR-004 | [Configurable Organizational Policies](bdr-004-configurable-organizational-policies.md) |
| BDR-005 | [Offline & Synchronization Architecture](bdr-005-offline-synchronization-architecture.md) |
| BDR-006 | [Conflict Resolution Strategy](bdr-006-conflict-resolution-strategy.md) — companion to BDR-005 |

## Reference documents

| Document | Role |
| -------- | ---- |
| [ADR Index](ADR-INDEX.md) | Navigation and evolution policy (Living Document) |
| [Workflow Sequence Diagrams](../02-system-blueprint/workflow-sequence-diagrams.md) | Visual workflow context |

## Historical documents

Superseded records (when marked) remain for audit trail. Do not delete Accepted ADRs/BDRs — create successors per [ADR Index — Evolution](ADR-INDEX.md#evolution).

## Before modifying or adding records

- **New technical constraint** → create an ADR (see [When to Create a New ADR](ADR-INDEX.md#when-to-create-a-new-adr))
- **New product/workflow constraint** → create a BDR (see [When to Create a New BDR](ADR-INDEX.md#when-to-create-a-new-bdr))
- **Offline sync or conflict semantics** → amend BDR-005 / BDR-006 together; update [Mobile API](../04-api/mobile-api.md) and [Domain Engine](../07-business-architecture/domain-engine.md) delivery notes
- **Do not** change Accepted ADR/BDR substance in place without a successor record

**Start here:** [ADR Index](ADR-INDEX.md)
