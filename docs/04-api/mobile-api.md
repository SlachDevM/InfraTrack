# Mobile API (V2.2.0 Sprint M1, extended V2.4.0 Sprint M4-BE1 / M4-BE2)

Compact, read-only REST endpoints for the future Android field client. **M1** is the mobile API foundation. **M4-BE** (V2.4) is the current backend milestone — asset lookup, asset context, QR foundation, documents, and allowed actions. No Android scanning UI, offline sync, or push delivery in M4-BE (backend only).

## Purpose

Field users need one backend call per mobile screen instead of orchestrating multiple web APIs. The mobile layer:

- returns explicit, compact DTOs;
- bundles everything required for one inspection or work order screen;
- preserves backend authorization (Bearer JWT);
- does **not** change existing web APIs or business workflows.

Write operations (complete inspection, save inspection answers progressively, complete maintenance, upload documents) continue to use existing `/api/*` endpoints. Gaps for mobile-specific writes are deferred to M2.

## Authentication

All mobile endpoints require a valid JWT from `POST /api/auth/login`.

```
Authorization: Bearer <token>
```

Unauthenticated requests receive `401`. Forbidden scoping receives `403`.

## Primary users

| Role | List scope | Bundle access |
|------|------------|---------------|
| Field Employee | Inspections/work orders assigned to them | Assigned items only |
| Contractor | Same as field employee | Assigned items only |
| Manager | Department assigned inspections/work orders (active) | Department-scoped (existing inspection view rules) |
| Administrator | All active assigned inspections/work orders | All (support/debug) |

Operational Coordinators do not have mobile API access in M1. Starting with M4-BE1, Operational Coordinators may use the asset lookup endpoint only (see below); they still cannot use the M1 dashboard/bundle endpoints.

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/mobile/me` | Identity summary for app startup |
| `GET` | `/api/mobile/dashboard` | Personal assignment counts |
| `GET` | `/api/mobile/my-inspections` | Assigned inspection summaries |
| `GET` | `/api/mobile/inspections/{inspectionId}/bundle` | Full inspection screen payload |
| `GET` | `/api/mobile/my-work-orders` | Assigned work order summaries |
| `GET` | `/api/mobile/work-orders/{workOrderId}/bundle` | Full work order screen payload |
| `GET` | `/api/mobile/assets/lookup?code={assetCode}` | Asset operational context by scanned business code (M4-BE1, enriched M4-BE3/M4-BE4) |
| `POST` | `/api/mobile/sync` | Offline synchronization (M5.3 upload + M5.4 inspection delta download) |

Live OpenAPI documentation: [Swagger UI](http://localhost:4000/swagger-ui/index.html) (tag: **Mobile API**).

## Bundle concept

A **bundle** endpoint returns all data needed to render one mobile screen in a single response:

### Inspection bundle

- Inspection detail (status, priority, dates, observations)
- Asset summary (name, category, department, location)
- Template summary (when templated)
- Active checklist questions with choices
- Existing answers (including progressively saved answers while `ASSIGNED`)
- Allowed actions (`canComplete`, `canUploadDocument`, `canViewAsset`)

Progressive save workflow for field clients:

```text
GET  /api/mobile/inspections/{id}/bundle
PUT  /api/inspections/{id}/progress
GET  /api/mobile/inspections/{id}/bundle
POST /api/inspections/{id}/complete
```

`PUT /api/inspections/{id}/progress` saves draft inspection progress while status remains `ASSIGNED`:

- inspection summary fields (`observedCondition`, `observations`, `issueIdentified`)
- checklist answers (templated inspections only)

The inspection bundle reflects whatever is persisted in the backend — reload the bundle after each save. Completion still uses `POST /api/inspections/{id}/complete` and runs mandatory validation and the Decision Engine exactly once.

`PUT /api/inspections/{id}/answers` remains supported as a compatibility endpoint for saving answers only.

Legacy inspections without a template return empty `questions` and `answers`.

### Work order bundle

- Work order detail
- Asset summary
- Related issue and operational decision summary
- Maintenance activity (if completed)
- Allowed actions (`canCompleteMaintenance`, `canUploadDocument`, `canViewAsset`)

## Dashboard summary

`GET /api/mobile/dashboard` returns a compact personal summary:

- `assignedInspections`
- `assignedWorkOrders`
- `overdueInspections`
- `overdueWorkOrders` (always `0` — work orders have no due date in the current model)
- `completedToday` (inspections + maintenance completed today by the user)

This is **not** the Operations Intelligence web dashboard.

## Sorting

**My inspections:** overdue first → `expectedCompletionDate` ascending → priority (URGENT → LOW).

**My work orders:** active (`ASSIGNED`) first → `assignedAt` → priority.

## Reused write APIs (Android — future)

| Action | Existing endpoint |
|--------|-------------------|
| Complete inspection | `POST /api/inspections/{id}/complete` |
| Complete maintenance | `POST /api/work-orders/{id}/maintenance-activity` |
| Register FCM token | `PUT /api/users/me/fcm-token` |
| Upload operational document | Existing operational document endpoints |

**Workflow timestamps:** Mobile clients send workflow intent only. The backend sets authoritative `completedAt`, `reviewedAt`, and decision timestamps. Deprecated request fields such as `completedAt` on completion requests may still be accepted for compatibility but are ignored — read timestamps from API responses after each write.

## Asset lookup by QR / barcode (V2.4.0 Sprint M4-BE1, enriched M4-BE3/M4-BE4)

Android's future QR/barcode scanner resolves a physical asset tag to an `assetCode` string and calls this endpoint to open an "Asset Context" screen. QR code generation is provided separately by `GET /api/assets/{assetId}/qr` (M4-BE2); printable labels and Android scanning remain deferred.

```http
GET /api/mobile/assets/lookup?code=AST-1A2B3C4D
```

### Asset business code

Every asset now has a stable `code` (format `AST-XXXXXXXX`), independent from its internal database ID, generated automatically when the asset is registered. Existing assets were backfilled with a generated code by migration `V28__asset_business_code.sql`. This code — not `asset.id` — is the value encoded in the QR code returned by `GET /api/assets/{assetId}/qr`.

### End-to-end flow (when Android scanning ships)

```text
GET /api/assets/{assetId}/qr          → PNG QR encoding assetCode only
        ↓ (print label — future sprint)
Android scans QR                      → reads assetCode (e.g. AST-1A2B3C4D)
        ↓
GET /api/mobile/assets/lookup?code=…  → AssetContextResponse (all sections below)
        ↓
GET /api/operational-documents/{id}/download  → authenticated document download
```

### Response shape (`AssetContextResponse`)

All nine top-level fields are always present. Optional sections use `null`; `documents` is always an array (possibly empty).

```json
{
  "asset": {
    "id": 1,
    "code": "AST-1A2B3C4D",
    "name": "Street Light 001",
    "category": "Street Lighting",
    "department": "Infrastructure",
    "location": "Main Street",
    "status": "ACTIVE"
  },
  "lastInspection": {
    "id": 123,
    "status": "COMPLETED",
    "completedAt": "2026-07-04T10:15:00",
    "observedCondition": "GOOD",
    "issueIdentified": false
  },
  "lastMaintenance": {
    "id": 456,
    "workOrderId": 77,
    "completedAt": "2026-07-03T15:30:00",
    "performedBy": "Field User"
  },
  "preventivePlan": {
    "exists": true,
    "id": 88,
    "name": "Monthly lighting inspection",
    "triggerType": "TIME",
    "active": true
  },
  "documents": [
    {
      "id": 101,
      "filename": "street-light-manual.pdf",
      "contentType": "application/pdf",
      "ownerType": "ASSET",
      "uploadedAt": "2026-07-04T09:30:00",
      "uploadedBy": "Maintenance Admin",
      "downloadUrl": "/api/operational-documents/101/download"
    }
  ],
  "openIssues": [],
  "activeInspections": [],
  "activeWorkOrders": [],
  "allowedActions": {
    "canViewAsset": true,
    "canViewInspections": true,
    "canViewIssues": true,
    "canViewWorkOrders": true,
    "canCreateInspection": false,
    "canCreateIssue": false
  }
}
```

- `lastInspection` — most recently **completed** inspection for the asset (compact summary), or `null` when none exists. Android should render `null` as “No recent inspection”.
- `lastMaintenance` — most recent completed maintenance activity for the asset, or `null` when none exists. Android should render `null` as “No recent maintenance”. `performedBy` is the performer display name when available.
- `preventivePlan` — compact summary of the active preventive maintenance plan for the asset (most recently created when multiple exist), or `null` when none exists. Android should render `null` as “No preventive plan”.
- `documents` — asset-owned operational documents the caller is allowed to view (metadata only), most recently uploaded first. Returns an **empty array** when none are visible — never `null`. Download uses the existing `GET /api/operational-documents/{id}/download` endpoint via `downloadUrl`. Android must not infer document permissions locally. Inspection/work-order/issue-linked documents are **not** included in this sprint.
- `openIssues` — issues on the asset without a linked operational decision (unresolved), most recent first.
- `activeInspections` — inspections on the asset with status `ASSIGNED`.
- `activeWorkOrders` — work orders on the asset with status `CREATED` or `ASSIGNED`.
- Completed/cancelled/resolved records are excluded from the active lists. Full asset history remains deferred.
- Optional context sections (`lastInspection`, `lastMaintenance`, `preventivePlan`) are `null` when no matching record exists — do not send empty placeholder objects. `documents` is always an array (possibly empty).
- `allowedActions` is generated entirely by the backend; Android must not infer these flags itself.
- This endpoint is **read-only**; it does not create or change workflow state.

### Authorization and scoping

Reuses existing role and department rules; no new Android-specific permissions were introduced:

| Role | Can look up |
|------|-------------|
| Administrator | Any asset |
| Manager | Assets in own department, or a department they hold an active delegated authority for |
| Operational Coordinator | Assets in own department |
| Field Employee / Contractor | Assets in own department (existing conservative asset visibility rule; no more specific per-asset rule exists yet) |
| Any other role | `403 Forbidden` |

- Blank/missing `code` → `400 Bad Request`.
- Unknown `code` → `404 Not Found`.
- Asset exists but the caller is not authorized → `403 Forbidden` with no asset context in the response body.
- The nested lists, optional context sections, and `documents` array are scoped to the same asset only once asset-level access is confirmed; no broader cross-asset data is ever returned. Forbidden lookups return `403` before any nested context is queried. **M4-BE4.1:** asset-owned `documents` are visible to any caller who can view the asset context (same department for field employees and contractors). Web asset document listing and upload/delete rules are unchanged. Download uses a mobile-aligned rule for asset-owned documents only (`GET /api/operational-documents/{id}/download`).

`canCreateInspection` is `true` only for Operational Coordinators in the asset's own department (matching the existing "assign inspection" rule). `canCreateIssue` is `true` only for Field Employees/Contractors in the asset's own department (matching the existing "record issue" role rule). Neither flag creates any new record — creation still goes through the existing `/api/inspections` and `/api/issues` endpoints, which enforce their own full preconditions.

## Offline synchronization protocol (M5.2-BE1 / M5.2-BE2 / M5.3-BE / M5.4-BE / M5.5-BE1 / M5.5-BE1.1)

`POST /api/mobile/sync` is the backend synchronization protocol foundation ([BDR-005](../03-architecture/bdr-005-offline-synchronization-architecture.md)). **M5.2-BE1** introduced the request/response envelope. **M5.2-BE2** added opaque sync tokens, protocol versioning, typed operation/conflict/warning codes, and the delta container. **M5.3-BE** processes `SAVE_INSPECTION_PROGRESS` uploads. **M5.4-BE** populates `delta.inspections` with scoped inspection sync records. **M5.4.2-BE** embeds checklist template/question/choice definitions in each inspection delta so assigned inspections are renderable offline without a prior bundle fetch. **M5.5-BE1** detects synchronization conflicts for `SAVE_INSPECTION_PROGRESS`. **M5.5-BE1.1** enriches `conflicts[]` with structured `serverState`, `clientState`, and `resolutionHint` (detection only — no automatic resolution). **M6.1-BE1** (V2.6) adds `SAVE_WORK_ORDER_PROGRESS` upload processing for draft maintenance notes. **M6.1-BE2** populates `delta.workOrders` with scoped work order sync records. Other delta sections remain empty; tombstones are not produced yet. `COMPLETE_MAINTENANCE` remains future work.

### Authorization

Same mobile role policy as M1 list/bundle endpoints (not asset lookup): Administrator, Manager, Field Employee, Contractor. Operational Coordinators receive `403`.

### Request (`SyncRequest`)

Required: `clientId`, `clientVersion`. Optional: `appVersion`, `syncToken` (opaque value from the previous `nextSyncToken`), `deviceTime`, `pendingOperations` (defaults to `[]`).

Each `pendingOperations[]` entry requires `operationId`, `entityType`, and `operationType`. Optional: `payload` (JSON string matching the target write DTO), `entityId`, `createdAt`.

#### Supported operations

| `operationType` | `entityType` | `entityId` | `payload` |
|-----------------|--------------|------------|-----------|
| `SAVE_INSPECTION_PROGRESS` | `INSPECTION` | Inspection id | `SaveInspectionProgressRequest` JSON |
| `SAVE_WORK_ORDER_PROGRESS` | `WORK_ORDER` | Work order id | `SaveWorkOrderProgressRequest` JSON (**M6.1-BE1**) |

Example inspection `payload` fields: `observedCondition`, `observations`, `issueIdentified`, `answers[]`.

Example work order `payload` fields: `completionNotes` (draft maintenance notes only; max 4000 characters; does not complete the work order).

### Response (`SyncResponse`)

| Field | M5.5-BE1.1 |
|-------|------------|
| `protocolVersion` | `1` — clients must ignore unknown fields on newer versions |
| `serverTime` | Backend instant |
| `nextSyncToken` | Opaque cursor issued on every successful sync; store and resubmit as `syncToken` on the next call. Android must not parse it. |
| `delta.inspections` | Scoped inspection sync records (`SyncInspectionDeltaResponse`) — see below |
| `delta.workOrders` | Scoped work order sync records (`SyncWorkOrderDeltaResponse`) — see below (**M6.1-BE2**) |
| `delta` (other sections) | `assets`, `documents`, `users`, `referenceData` remain `[]` |
| `operations` | One `SyncOperationResponse` per pending operation (in request order) |
| `conflicts` | Populated when an operation returns `CONFLICT` — see below |
| `warnings` | May include `FULL_SYNC_REQUIRED` when `syncToken` is invalid; sync still succeeds |
| `requiresFullSync` | `true` when `warnings` contains `FULL_SYNC_REQUIRED`; otherwise `false` (**RC-FIX-BE-1**) |

### Inspection delta (`delta.inspections`)

Each `SyncInspectionDeltaResponse` includes: `id`, `status`, `priority`, `assignedToUserId`, `assignedToName`, compact `asset` summary, draft `observedCondition` / `observations` / `issueIdentified`, `expectedCompletionDate`, `completedAt`, `updatedAt` (epoch millis), `answers[]` (`questionId`, value fields, optional additive `choiceId`), and — when the inspection uses a template — embedded checklist definitions:

| Field | Type | Notes |
|-------|------|-------|
| `template` | `SyncInspectionTemplateDeltaResponse` | `templateId`, `templateName`, `templateVersion` |
| `questions` | `SyncInspectionQuestionDeltaResponse[]` | Active questions only; stable `displayOrder`; same filtering as bundle endpoint |
| `questions[].choices` | `SyncInspectionChoiceDeltaResponse[]` | Active choices for `CHOICE` questions; empty for other types |

**M5.4.2-BE:** `delta.inspections` is self-contained for offline inspection rendering. Checklist definitions match `GET /api/mobile/inspections/{id}/bundle` mapping rules (via shared `MobileInspectionChecklistLoader`). Android does not fabricate checklist definitions; a prior online bundle fetch is not required to render an assigned inspection offline. Definitions are included only for inspections in the scoped delta — no separate template-wide sync section. Existing answer fields are unchanged; `choiceCodeValue` is retained; `choiceId` is additive.

**Download strategy (M5.4):**

| `syncToken` | Behaviour |
|-------------|-----------|
| `null` / omitted | Full inspection delta for the authenticated mobile user |
| Valid opaque token | Inspections with `updatedAt >= token.issuedAt` among scoped records |
| Invalid token | `FULL_SYNC_REQUIRED` warning + full inspection delta (sync does not fail) |

Inspections are scoped identically to `GET /api/mobile/my-inspections` (administrator / manager department / assigned field user). **Removals and tombstones are not synchronized yet** — records the user loses access to may remain in local cache until a later sprint.

**Synchronization watermark (RC-FIX-BE-1):** After pending operations are processed, the backend captures a single server `watermark` instant. The inspection and work order deltas include only records with `updatedAt <= watermark`. `nextSyncToken` is issued with `issuedAt = watermark`, so the token exactly represents the snapshot returned. Updates committed after the watermark appear on the next sync. Token encoding remains opaque; no API contract change.

### Work order delta (`delta.workOrders`) — M6.1-BE2

Each `SyncWorkOrderDeltaResponse` includes compact fields required for offline maintenance work:

| Field | Notes |
|-------|-------|
| `workOrderId` | Work order primary key |
| `status`, `priority`, `workType`, `description` | Current server state |
| `assetId`, `assetName`, `assetCategoryName` | Compact asset context |
| `assignedTo`, `assignedToName`, `assignedAt` | Assignee summary |
| `createdAt`, `updatedAt` | Epoch millis |
| `draftCompletionNotes` | Offline draft from `SAVE_WORK_ORDER_PROGRESS` |
| `completionEligible` | Whether the caller may complete maintenance (same rule as bundle endpoint) |
| `operationalDecisionId` | Linked operational decision |

**Download strategy:** Identical token semantics to `delta.inspections` — null/invalid token → full scoped delta (single `FULL_SYNC_REQUIRED` warning when token is invalid); valid token → `updatedAt >= token.issuedAt` among scoped records, bounded by watermark.

**Scoping:** Mirrors `GET /api/mobile/my-work-orders` — Administrator: assigned work orders organisation-wide; Manager: assigned work orders in department; Field Employee / Contractor: assigned to current user. Unauthorized work orders are never included.

**Not yet synchronized:** Offline maintenance completion (`COMPLETE_MAINTENANCE`), work order removals/tombstones, and document payloads.

### Per-operation outcomes (`SyncOperationResponse`)

| `status` | Meaning |
|----------|---------|
| `ACCEPTED` | Operation applied through existing backend service |
| `REJECTED` | Malformed payload, validation failure, or oversize payload — safe `message`; sync continues |
| `CONFLICT` | Server state changed since the operation was queued — see `conflicts[]` |
| `IGNORED` | Unsupported `operationType` / handler combination |
| `RETRY` | Reserved for future retry handling |

One rejected or conflicting operation does **not** fail the whole sync request.

### Conflict entries (`SyncConflictResponse`) — M5.5-BE1 / M5.5-BE1.1

When `SAVE_INSPECTION_PROGRESS` or `SAVE_WORK_ORDER_PROGRESS` cannot be applied safely because server state changed, the matching operation has `status: CONFLICT` and `conflicts[]` includes:

| Field | Notes |
|-------|-------|
| `operationId` | Client idempotency key |
| `entityType` | `INSPECTION` |
| `entityId` | Inspection id |
| `conflictType` | `WORKFLOW_COMPLETED`, `ENTITY_DELETED`, `PERMISSION_DENIED`, `VERSION_MISMATCH`, or `UNKNOWN` |
| `resolutionHint` | Informational guidance only — `SERVER_WINS`, `CLIENT_RETRY`, `MANUAL_REVIEW`, or `UNKNOWN` |
| `message` | Safe user-facing summary |
| `clientState` | `SyncConflictClientState`: `operationType`, `createdAt`, original `payload` JSON (not transformed to write DTOs) |
| `serverState` | `SyncConflictServerState`: compact inspection snapshot when the entity still exists; `null` for `ENTITY_DELETED` |

**`resolutionHint` mapping (conservative, informational only):**

| `conflictType` | `resolutionHint` |
|----------------|------------------|
| `WORKFLOW_COMPLETED` | `SERVER_WINS` |
| `ENTITY_DELETED` | `SERVER_WINS` |
| `PERMISSION_DENIED` | `MANUAL_REVIEW` |
| `VERSION_MISMATCH` | `CLIENT_RETRY` |
| `UNKNOWN` | `UNKNOWN` |

**`serverState` fields (populated when available):** `entityId`, `entityType`, `status`, `updatedAt`, `completedAt`, `assignedTo`, `assignedToName`, `version` (nullable — inspection has no version field yet).

Conflicts are **detected** on sync; **explicit resolution** is a separate endpoint (M5.5-BE2). The backend does not merge or auto-apply client payloads during resolution.

### Conflict resolution (`POST /api/mobile/sync/conflicts/resolve`) — M5.5-BE2

Dedicated endpoint for explicit resolution decisions. Does **not** overload `POST /api/mobile/sync`. JWT required; same mobile role policy as sync (Administrator, Manager, Field Employee, Contractor).

**Scope (M5.5-BE2):** `SAVE_INSPECTION_PROGRESS` on `INSPECTION` only.

**Request (`SyncConflictResolutionRequest`):** `operationId`, `entityType`, `entityId`, `operationType`, `conflictType`, `resolution`, optional `clientState`.

**`resolution` values:** `SERVER_WINS`, `CLIENT_RETRY`, `MANUAL_REVIEW`, `DISCARD_CLIENT` (explicit discard — same server behaviour as `SERVER_WINS`).

**Response (`SyncConflictResolutionResponse`):** `operationId`, `entityType`, `entityId`, `operationType`, `resolution`, `status`, `message`, `serverTime`.

| `resolution` | `status` | Server behaviour |
|--------------|----------|------------------|
| `SERVER_WINS` | `RESOLVED` | No mutation; client may discard pending op |
| `DISCARD_CLIENT` | `RESOLVED` | No mutation; explicit discard acknowledgement |
| `CLIENT_RETRY` | `RETRY_REQUIRED` | No mutation; client refreshes delta and may requeue |
| `MANUAL_REVIEW` | `MANUAL_REVIEW_REQUIRED` | No mutation; keep conflict visible |

Unsupported operation/entity scope returns `status: REJECTED` (HTTP 200). Loss of inspection view permission returns `MANUAL_REVIEW_REQUIRED`. Operational Coordinator receives `403`.

Resolution is **stateless** — no durable conflict history table. Android retains the pending queue. No client payload is applied during resolution.

See [BDR-005 Conflict Resolution Strategy](../03-architecture/bdr-005-conflict-resolution-strategy.md).

### Client queue guidance (Android)

- Remove local pending operations only when `status` is `ACCEPTED`.
- Keep `CONFLICT` operations locally until explicit resolution via `POST /api/mobile/sync/conflicts/resolve` (M5.5-BE2).
- On `RESOLVED` — remove or archive pending operation per UX.
- On `RETRY_REQUIRED` — refresh `delta.inspections`, let user requeue if still applicable.
- On `MANUAL_REVIEW_REQUIRED` — keep conflict visible; no silent discard.
- Keep or surface `REJECTED` operations according to UX rules (malformed or invalid payload).
- `IGNORED` applies only when the operation type is obsolete or unsupported.

### Idempotency (M5.3 / DT-OFFLINE-1)

`operationId` is the client idempotency key. **DT-OFFLINE-1 / RC-FIX-BE-1:** the backend atomically reserves `operationId` in `mobile_sync_operation` (`PROCESSING` → `RECORDED`) before handler execution. Concurrent duplicates wait for or receive the recorded `SyncOperationResponse` without re-executing business services. Runtime handler failures release the reservation so the client may retry. Retention default **90 days** (`mobile.sync.idempotency.retention-days`). Duplicate metric: `mobile.sync.operations.duplicate`.

### Typed envelopes

| Enum | Values |
|------|--------|
| `SyncOperationStatus` | `ACCEPTED`, `REJECTED`, `CONFLICT`, `RETRY`, `IGNORED` |
| `SyncConflictType` | `ENTITY_MODIFIED`, `ENTITY_DELETED`, `WORKFLOW_COMPLETED`, `VERSION_MISMATCH`, `PERMISSION_DENIED`, `UNKNOWN` |
| `SyncResolutionHint` | `SERVER_WINS`, `CLIENT_RETRY`, `MANUAL_REVIEW`, `UNKNOWN` (informational only — M5.5-BE1.1) |
| `SyncConflictResolutionAction` | `SERVER_WINS`, `CLIENT_RETRY`, `MANUAL_REVIEW`, `DISCARD_CLIENT` (explicit resolution request — M5.5-BE2) |
| `SyncConflictResolutionStatus` | `RESOLVED`, `RETRY_REQUIRED`, `MANUAL_REVIEW_REQUIRED`, `REJECTED` |
| `SyncWarningCode` | `FULL_SYNC_REQUIRED`, `SYNC_TOKEN_EXPIRED`, `CLIENT_OUTDATED`, `PARTIAL_SYNC`, `UNKNOWN_WARNING` |

### Protocol compatibility

- `protocolVersion` increments only when additive JSON fields are insufficient.
- Clients must tolerate unknown response fields and empty delta sections until download is implemented.
- Backend may replace sync token encoding without Android changes; clients store the opaque string only.

Android should not expect work order/asset/document deltas, maintenance completion upload (`COMPLETE_MAINTENANCE`), or automatic conflict resolution for work orders until later M6 phases.

## Future phases (deferred)

| Phase | Capability |
|-------|------------|
| M2+ | Mobile-optimised write endpoints if web APIs prove unsuitable |
| V2.3.0 | Android field application |
| V2.4.0 | Offline synchronisation (M5.2-BE1/BE2 sync protocol foundation delivered; upload/populated delta/conflicts deferred) |
| V2.2.0+ | Push notification integration beyond FCM token registration |
| M4 (later sprints) | Printable asset labels (PDF), Android QR scanning UI, inspection/work-order/issue-linked documents on the context screen, full asset history |

## Backend package

```
com.infratrack.mobile
├── MobileController
├── MobileService
├── MobileAuthorizationService
├── sync/
│   ├── MobileSyncController
│   ├── MobileSyncService
│   ├── SyncOperationProcessor
│   ├── SyncOperationHandler
│   ├── InspectionProgressSyncOperationHandler
│   ├── InspectionSyncDeltaService
│   ├── SyncTokenService (opaque cursor issuance)
│   ├── SyncToken / SyncProtocolVersion
│   ├── SyncConflictResolver (extension point)
│   └── dto.*
└── dto.*
    ├── AssetContextResponse
    ├── AssetContextSummaryResponse
    ├── AssetContextAllowedActionsResponse
    ├── MobileAssetLastInspectionResponse
    ├── MobileAssetLastMaintenanceResponse
    ├── MobileAssetPreventivePlanResponse
    ├── MobileAssetDocumentSummaryResponse
    └── ...
```
