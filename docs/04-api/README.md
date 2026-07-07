# InfraTrack API Documentation

API contracts, consumer guidance, and capability-specific references for InfraTrack.

## Purpose

This folder explains **how clients integrate** with the platform. The live OpenAPI document remains the field-level authority for request/response shapes.

## Audience

- React frontend contributors
- Android field client developers
- Integration engineers and API consumers
- Technical reviewers validating client behaviour

## Recommended reading order

1. [API Consumer Guide](api-consumer-guide.md) — authentication, bundles, permissions, sync principles
2. Live **Swagger UI** (local: http://localhost:4000/swagger-ui/index.html) — endpoint schemas
3. [Mobile API](mobile-api.md) — Android bundles and `POST /api/mobile/sync`
4. [V2 Domain Engine API](v2-domain-engine-api.md) — Decision Engine and Preventive Maintenance paths
5. [Reporting API](reporting-api.md) — read-only CSV exports

## Normative documents

| Document | Role |
| -------- | ---- |
| [api-consumer-guide.md](api-consumer-guide.md) | How clients must consume the API (Living Document) |
| [mobile-api.md](mobile-api.md) | Mobile bundle and sync contract |
| Live OpenAPI (`/v3/api-docs`) | Authoritative REST schemas |

## Reference documents

| Document | Role |
| -------- | ---- |
| [v2-domain-engine-api.md](v2-domain-engine-api.md) | V2 endpoint group summary |
| [reporting-api.md](reporting-api.md) | Reporting export contract |
| [Project README — API Developer Guide](../../README.md#api-developer-guide) | Auth, pagination, errors |

## Historical documents

V1-only patterns are documented in the project README and release notes. V2 behaviour is authoritative in [Domain Engine](../07-business-architecture/domain-engine.md).

## Before modifying API documentation

- **Behaviour change** → update OpenAPI (code), then [Domain Engine](../07-business-architecture/domain-engine.md) and relevant guide here
- **Mobile sync** → follow [BDR-005](../03-architecture/bdr-005-offline-synchronization-architecture.md) and [BDR-006](../03-architecture/bdr-006-conflict-resolution-strategy.md); update [mobile-api.md](mobile-api.md) and [api-consumer-guide.md](api-consumer-guide.md) together
- **New ADR/BDR** → required when the change alters a documented architectural or product constraint (see [ADR Index](../03-architecture/ADR-INDEX.md))

## Live OpenAPI

| Resource | URL (local development) |
|----------|-------------------------|
| Swagger UI | http://localhost:4000/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:4000/v3/api-docs |

Authenticated endpoints require a JWT bearer token from `POST /api/auth/login`.
