# Mobile API (V2.2.0 Sprint M1, extended V2.4.0 Sprint M4-BE1)

Compact, read-only REST endpoints for the future Android field client. M1 is a **mobile API foundation only** — no Android app, offline sync, push notifications, or QR scanning in this sprint. V2.4.0 Sprint M4-BE1 adds the backend asset lookup endpoint that a future Android QR/barcode scanner will call; it does **not** add QR generation, an Android client, or any workflow change.

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
| `GET` | `/api/mobile/assets/lookup?code={assetCode}` | Asset operational context by scanned business code (M4-BE1) |

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

## Asset lookup by QR / barcode (V2.4.0 Sprint M4-BE1)

Android's future QR/barcode scanner resolves a physical asset tag to an `assetCode` string and calls this endpoint to open an "Asset Context" screen. This sprint adds the backend endpoint only — no QR code generation, printing, or Android scanning happens yet.

```http
GET /api/mobile/assets/lookup?code=AST-1A2B3C4D
```

### Asset business code

Every asset now has a stable `code` (format `AST-XXXXXXXX`), independent from its internal database ID, generated automatically when the asset is registered. Existing assets were backfilled with a generated code by migration `V28__asset_business_code.sql`. This code — not `asset.id` — is the value intended to be encoded in a QR code or barcode in a later sprint.

### Response shape

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

- `openIssues` — issues on the asset without a linked operational decision (unresolved), most recent first.
- `activeInspections` — inspections on the asset with status `ASSIGNED`.
- `activeWorkOrders` — work orders on the asset with status `CREATED` or `ASSIGNED`.
- Completed/cancelled/resolved records are excluded. Documents and full asset history are **not** included (deferred to a later M4 backend sprint).
- `allowedActions` is generated entirely by the backend; Android must not infer these flags itself.

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
- The nested lists (`openIssues`, `activeInspections`, `activeWorkOrders`) are scoped to the same asset only once asset-level access is confirmed; no broader cross-asset data is ever returned.

`canCreateInspection` is `true` only for Operational Coordinators in the asset's own department (matching the existing "assign inspection" rule). `canCreateIssue` is `true` only for Field Employees/Contractors in the asset's own department (matching the existing "record issue" role rule). Neither flag creates any new record — creation still goes through the existing `/api/inspections` and `/api/issues` endpoints, which enforce their own full preconditions.

## Future phases (deferred)

| Phase | Capability |
|-------|------------|
| M2+ | Mobile-optimised write endpoints if web APIs prove unsuitable |
| V2.3.0 | Android field application |
| V2.4.0 | Offline synchronisation |
| V2.2.0+ | Push notification integration beyond FCM token registration |
| M4 (later sprints) | QR code generation/printing, Android QR scanning UI, asset documents/full history on the context screen |

## Backend package

```
com.infratrack.mobile
├── MobileController
├── MobileService
├── MobileAuthorizationService
└── dto.*
    ├── AssetContextResponse
    ├── AssetContextSummaryResponse
    ├── AssetContextAllowedActionsResponse
    └── ...
```
