# ADR-001: Asset History Starts with Registration

## Status

Accepted

## Context

UC-001 Register Asset requires that:

- Asset registration starts the Asset History (Functional Analysis, UC-001 postconditions).
- Every operational event associated with an Asset contributes to its permanent operational history (BR-004).
- Asset History is permanent and must not be removed (BR-026).

At the same time, InfraTrack follows a vertical-slice delivery model. UC-011 View Asset History is not yet implemented, and the platform must avoid speculative abstractions such as a generic history framework or event sourcing.

## Decision

When an Asset is registered through UC-001, the backend creates exactly one `AssetHistoryEvent` with type `ASSET_REGISTERED` in the same database transaction as the Asset itself.

The event records:

- the Asset reference;
- the performing user;
- the business event date (`registrationDate` as `LocalDate`);
- a technical `createdAt` timestamp.

No separate history module, browsing API, or UI is introduced at this stage.

## Consequences

**Positive**

- Operational traceability begins at registration, satisfying UC-001 and BR-004 at minimal cost.
- The implementation stays explicit and feature-first under `com.infratrack.asset`.
- Future UC-011 can expose existing persisted events without redesigning the registration flow.

**Negative / deferred**

- Asset History cannot yet be viewed in the application (deferred to UC-011).
- Only registration events exist today; status changes and operational workflows will add further event types later.
- No document or photo linkage is stored with the registration event (out of UC-001 scope).

## Related use cases

- UC-001 Register Asset — implemented
- UC-011 View Asset History — deferred
