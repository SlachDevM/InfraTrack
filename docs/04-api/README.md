# InfraTrack API Documentation

The authoritative V1 REST API reference is the live OpenAPI documentation served by the backend.

| Resource | URL (local development) |
|----------|-------------------------|
| Swagger UI | http://localhost:4000/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:4000/v3/api-docs |

All public endpoints are grouped by business capability (Assets, Inspections, Work Orders, and so on). Authenticated endpoints require a JWT bearer token from `POST /api/auth/login`.

For authentication flow, pagination conventions, error format and versioning policy, see the [API Developer Guide](../../README.md#api-developer-guide) in the project README.
