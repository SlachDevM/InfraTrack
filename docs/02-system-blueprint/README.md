# System Blueprint

Engineering standards, development workflow, and visual workflow guides for InfraTrack.

## Purpose

This folder holds the **engineering handbook** and **visual workflow references** that explain how InfraTrack is built — distinct from business discovery (domain language) and from ADRs/BDRs (decision records).

## Audience

- Backend and frontend developers
- Android contributors
- AI coding assistants
- Technical and architecture reviewers

## Recommended reading order

1. [Workflow Sequence Diagrams](workflow-sequence-diagrams.md) — visual overview of main business flows
2. [InfraTrack System Blueprint](INFRATRACK_SYSTEM_BLUEPRINT.md) — engineering principles, coding standards, AI collaboration rules
3. [ADR Index](../03-architecture/ADR-INDEX.md) — why key technical and product constraints exist
4. [Domain Engine](../07-business-architecture/domain-engine.md) — authoritative V2 business behaviour

## Normative documents

| Document | Role |
| -------- | ---- |
| [INFRATRACK_SYSTEM_BLUEPRINT.md](INFRATRACK_SYSTEM_BLUEPRINT.md) | Engineering handbook — how the product must be developed |
| [workflow-sequence-diagrams.md](workflow-sequence-diagrams.md) | Visual workflow guides (Living Document) |

## Reference documents

| Document | Role |
| -------- | ---- |
| [Product Vision](../00-product-vision.md) | Product purpose and principles |
| [Business Capability Map](../01-business-architecture/business-capability-map.md) | What the platform does today |
| [API Consumer Guide](../04-api/api-consumer-guide.md) | How clients consume the API |

## Historical documents

Business Discovery lifecycle chapters in [00-business-discovery/](../00-business-discovery/) predate V2 delivery detail. Use [Domain Engine](../07-business-architecture/domain-engine.md) for current V2 behaviour.

## Before modifying normative content

- **Blueprint chapters** — update when engineering workflow or quality standards change; keep aligned with [ADR Index](../03-architecture/ADR-INDEX.md).
- **Workflow diagrams** — update when a documented sequence changes materially; cross-check [Functional Use Cases](../01-business-architecture/functional-use-cases.md).
- **Offline/sync behaviour** — follow [BDR-005](../03-architecture/bdr-005-offline-synchronization-architecture.md) and [BDR-006](../03-architecture/bdr-006-conflict-resolution-strategy.md); do not redefine in this folder.
