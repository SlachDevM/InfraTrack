# Mobile API (V2.2.0 Sprint M1)

Compact, read-only REST endpoints for the future Android field client. M1 is a **mobile API foundation only** — no Android app, offline sync, push notifications, or QR scanning in this sprint.

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

Operational Coordinators do not have mobile API access in M1.

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/mobile/me` | Identity summary for app startup |
| `GET` | `/api/mobile/dashboard` | Personal assignment counts |
| `GET` | `/api/mobile/my-inspections` | Assigned inspection summaries |
| `GET` | `/api/mobile/inspections/{inspectionId}/bundle` | Full inspection screen payload |
| `GET` | `/api/mobile/my-work-orders` | Assigned work order summaries |
| `GET` | `/api/mobile/work-orders/{workOrderId}/bundle` | Full work order screen payload |

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

## Future phases (deferred)

| Phase | Capability |
|-------|------------|
| M2+ | Mobile-optimised write endpoints if web APIs prove unsuitable |
| V2.3.0 | Android field application |
| V2.4.0 | Offline synchronisation |
| V2.2.0+ | Push notification integration beyond FCM token registration |

## Backend package

```
com.infratrack.mobile
├── MobileController
├── MobileService
├── MobileAuthorizationService
└── dto.*
```
