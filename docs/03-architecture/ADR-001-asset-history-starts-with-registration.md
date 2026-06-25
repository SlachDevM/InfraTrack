# ADR-001: Asset History Starts with Registration

## Status

Accepted

## Context

UC-001 Register Asset requires that:

- Asset registration starts the Asset History (Functional Analysis, UC-001 postconditions).
- Every operational event associated with an Asset contributes to its permanent operational history (BR-004).
- Asset History is permanent and must not be removed (BR-026).

InfraTrack follows a vertical-slice delivery model and avoids speculative abstractions such as a generic history framework or event sourcing.

## Decision

When an Asset is registered through UC-001, the backend creates exactly one `AssetHistoryEvent` with type `ASSET_REGISTERED` in the same database transaction as the Asset itself.

The event records:

- the Asset reference;
- the performing user;
- the business event date (`registrationDate` as `LocalDate`);
- a technical `createdAt` timestamp.

Registration remains the first persisted Asset History event. Subsequent operational use cases append further events to the same permanent history.

## Consequences

**Positive**

- Operational traceability begins at registration, satisfying UC-001 and BR-004 at minimal cost.
- The registration flow stays explicit and feature-first under `com.infratrack.asset`.
- UC-011 View Asset History exposes existing persisted events through `com.infratrack.assethistory` without redesigning the registration flow.
- Asset History browsing is available through `GET /api/assets/{id}/history` and a read-only slice on the Assets page.

**Current state**

Asset History now records the eleven event types implemented across UC-001 to UC-012:

- Asset Registered;
- Business Trigger Created;
- Inspection Assigned;
- Inspection Completed;
- Issue Recorded;
- Operational Decision Made;
- Work Order Created;
- Work Order Assigned;
- Maintenance Completed;
- Completion Review Recorded;
- Operational Document Uploaded.

Asset History remains read-only. Viewing history does not create, modify or delete history entries.

**Negative / deferred**

- History entries do not embed operational document files; UC-012 stores document evidence separately and appends one `OPERATIONAL_DOCUMENT_UPLOADED` event per upload action.

## Related use cases

- UC-001 Register Asset — implemented
- UC-011 View Asset History — implemented
- UC-012 Upload Operational Document — implemented
