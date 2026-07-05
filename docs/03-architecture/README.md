# Architecture

Architecture Decision Records (ADRs) and Business Decision Records (BDRs) for InfraTrack.

| Document | Description |
| -------- | ----------- |
| [**ADR Index**](ADR-INDEX.md) | Entry point — all ADRs and BDRs with summaries and reading guidance |
| [ADR-001 — Asset History Starts with Registration](ADR-001-asset-history-starts-with-registration.md) | First history event at asset registration |
| [ADR-002 — Inspection Produces At Most One Issue (V1)](ADR-002-inspection-produces-at-most-one-issue-v1.md) | Zero or one Issue per Inspection in V1 |
| [ADR-003 — V2 Domain-Driven Workflow](adr-003-v2-domain-driven-workflow.md) | V2 layer model and human validation |
| [ADR-004 — Platform Versioning Strategy](adr-004-platform-versioning-strategy.md) | Product semantic versioning rules |
| [BDR-001 — Human-in-the-Loop Decision Engine](bdr-001-human-in-the-loop-decision-engine.md) | Rules suggest; managers decide |
| [BDR-002 — Preventive Candidates Before Automation](bdr-002-preventive-candidates-before-automation.md) | Scheduler generates candidates only |
| [BDR-003 — Bearer Token Architecture](bdr-003-bearer-token-architecture.md) | JWT Bearer tokens for web and mobile |
| [BDR-004 — Configurable Organizational Policies](bdr-004-configurable-organizational-policies.md) | Business rules are stable; organizational policies are configurable |
| [BDR-005 — Offline & Synchronization Architecture](bdr-005-offline-synchronization-architecture.md) | Offline field work; backend source of truth; deterministic sync |

**Start here:** [ADR Index](ADR-INDEX.md)
