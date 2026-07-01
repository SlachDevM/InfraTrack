# InfraTrack API Documentation

The authoritative REST API reference is the live OpenAPI documentation served by the backend.

| Resource | URL (local development) |
|----------|-------------------------|
| Swagger UI | http://localhost:4000/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:4000/v3/api-docs |

All public endpoints are grouped by business capability (Assets, Inspections, Work Orders, and so on). Authenticated endpoints require a JWT bearer token from `POST /api/auth/login`.

## Mobile API (V2.2.0)

Compact read/bundle endpoints for the future Android field client. See [mobile-api.md](mobile-api.md).

## Reporting Exports (V2.2.x)

Read-only CSV exports for Assets, Inspections, Issues, Work Orders, and Preventive Execution Candidates. See [reporting-api.md](reporting-api.md).

## V2 Domain Engine

**Version 2.0.0** introduced additional endpoint groups for the Decision Engine and Preventive Maintenance Engine. See [v2-domain-engine-api.md](v2-domain-engine-api.md) for a concise summary of major paths.

## V1 API

For authentication flow, pagination conventions, error format and versioning policy, see the [API Developer Guide](../../README.md#api-developer-guide) in the project README.
