# InfraTrack API Consumer Guide

## Document Information

| Field    | Value                                                          |
| -------- | -------------------------------------------------------------- |
| Project  | InfraTrack                                                     |
| Document | API Consumer Guide                                             |
| Version  | 1.0                                                            |
| Status   | Living Document                                                |
| Audience | Android, iOS, web, and third-party API consumers             |

This guide explains **how client applications should consume InfraTrack** — authentication, authorization patterns, bundles, permissions, errors, and best practices. It does not list every endpoint. For path-level reference, use [Swagger UI](http://localhost:4000/swagger-ui/index.html) and the specialised API documents linked at the end.

---

## 1. API Philosophy

InfraTrack exposes **business capabilities** through a single REST API. The backend is the **single source of truth** for:

- business rules and validation;
- workflow state transitions;
- role and assignment permissions;
- department scoping;
- intelligence evaluation (rules, candidates, KPIs).

Clients **present** data and **collect** user actions. They **never** implement business rules locally.

A mobile app must not decide whether an inspection can be completed. A web client must not infer export permission from role name alone. An integration must not assume an issue is resolved because a work order exists. The server decides; the client reflects the outcome.

> **Business decisions always belong to the backend.**

This philosophy aligns with [BDR-001 — Human-in-the-loop](../03-architecture/bdr-001-human-in-the-loop-decision-engine.md): intelligence may propose; managers and coordinators decide. Clients surface proposals and capture explicit human actions — they do not auto-execute workflows.

---

## 2. Authentication

InfraTrack uses **stateless JWT Bearer authentication**. There is no server-side session store for API consumers.

### Flow

```text
Login (POST /api/auth/login)
        ↓
JWT access token in response body
        ↓
Authenticated requests (Authorization: Bearer <token>)
```

### Request header

Every authenticated request must include:

```http
Authorization: Bearer <token>
```

Obtain the token from `POST /api/auth/login` with valid credentials. Account activation (`POST /api/auth/activate-account`) precedes first login for invited users.

### Token lifetime

Access token lifetime is configured server-side (`JWT_EXPIRATION`, default **24 hours**). When a token expires or is rejected, the client must **re-authenticate** — there is **no refresh-token flow** in the current implementation.

### Token storage

Clients are responsible for protecting tokens at rest:

- **Web** — storage pattern used by the React SPA (see [BDR-003](../03-architecture/bdr-003-bearer-token-architecture.md))
- **Mobile** — secure platform storage (for example Android Keystore)
- **Integrations** — secrets management appropriate to the deployment

Do not expose tokens in logs, analytics, or URL query parameters.

### Unauthenticated access

Public endpoints are limited (authentication, activation, health). All `/api/**` business endpoints require a valid JWT unless documented otherwise.

---

## 3. Startup Flow

Clients should validate stored credentials early and fail gracefully to login.

### Recommended sequence (mobile field client)

```text
App starts
        ↓
Read locally stored JWT (if any)
        ↓
GET /api/mobile/me
        ↓
200 OK → load dashboard (GET /api/mobile/dashboard)
```

### Recommended sequence (web or general client)

```text
App starts
        ↓
Read locally stored JWT (if any)
        ↓
GET /api/users/me
        ↓
200 OK → load home / role-appropriate landing
```

Use the identity endpoint appropriate to the client. Mobile field apps should prefer `/api/mobile/me` for compact identity aligned with mobile bundles.

### Invalid or expired session

When a protected request fails authentication or the identity check fails:

```text
401 Unauthorized  or  403 Forbidden
        ↓
Clear local session / token
        ↓
Return to login
```

InfraTrack may return **403** for missing or invalid Bearer tokens on protected routes, depending on the security filter chain. **Treat both 401 and 403 on identity or bootstrap calls as session failure** — do not attempt to continue with a stale token.

**Disabled accounts (V2.4 Security-3):** After an administrator deactivates a user, existing JWTs are rejected with HTTP `401` on the next request (account-status cache is evicted immediately on status change). Clients must clear the token and return to login — do not retry with the same token.

After re-login, reload identity and navigation state from the server. Do not assume cached role or department data remains valid.

---

## 4. API Design Principles

### REST resources

Endpoints are grouped by **business capability** (Assets, Inspections, Work Orders, Mobile, Reporting, Operations Intelligence). Paths are stable under `/api/...`.

### Read vs write

- **Read endpoints** return current server state (lists, details, bundles, KPIs, exports).
- **Write endpoints** perform explicit business actions (assign, complete, approve, record, delete where permitted).

Writes are intentional HTTP verbs (`POST`, `PUT`, `DELETE`) on explicit paths — not generic “save record” operations.

### Explicit DTOs

Responses use **DTO contracts** defined in OpenAPI — not internal database entities. Consumers must:

- parse documented JSON fields only;
- tolerate **additive** new fields in future releases;
- avoid coupling to undocumented properties.

### Stable contracts

V1 REST paths remain under `/api/...`. Additive DTO fields may appear without a path version change. Breaking changes require a new major platform version per [ADR-004](../03-architecture/adr-004-platform-versioning-strategy.md).

### Version compatibility

Check [Platform Version History](../06-release-notes/platform-version-history.md) when upgrading clients. Prefer feature detection via `allowedActions` and successful API responses rather than hard-coding server version assumptions.

---

## 5. Mobile Bundle Philosophy

List endpoints return **summaries** for navigation — enough to render a row in a list (identifier, title, status, key dates).

Bundle endpoints return a **complete screen payload** — everything needed to render one mobile screen in a single round trip.

```text
List endpoint          →  summary DTOs     →  “My Inspections” list
Bundle endpoint        →  screen payload   →  “Inspection Detail” screen
```

### One bundle, one screen

A bundle should normally populate **one screen**:

- `GET /api/mobile/inspections/{id}/bundle` — inspection detail, asset summary, template questions, answers, allowed actions
- `GET /api/mobile/work-orders/{id}/bundle` — work order detail, asset, related issue/decision, maintenance activity, allowed actions

Avoid chaining multiple bundle calls to reconstruct one screen. If a screen needs more data, that is a signal for a backend bundle extension — not client-side orchestration of ten list APIs.

### Asset context lookup (V2.4.0 Sprint M4-BE1, enriched M4-BE3/M4-BE4)

`GET /api/mobile/assets/lookup?code={assetCode}` is a bundle-style endpoint for a future "Asset Context" screen reached by scanning a QR code or barcode. It returns all sections in one call:

| Field | Nullable | Description |
|-------|----------|-------------|
| `asset` | No | Summary (id, code, name, category, department, location, status) |
| `lastInspection` | Yes | Most recent completed inspection |
| `lastMaintenance` | Yes | Most recent completed maintenance activity |
| `preventivePlan` | Yes | Active preventive plan summary |
| `documents` | No (array) | Visible asset-owned operational documents; empty when none |
| `openIssues` | No (array) | Unresolved issues on the asset |
| `activeInspections` | No (array) | Inspections with status `ASSIGNED` |
| `activeWorkOrders` | No (array) | Work orders with status `CREATED` or `ASSIGNED` |
| `allowedActions` | No | Backend-generated capability flags |

Android should render `null` optional sections as “No recent inspection”, “No recent maintenance”, or “No preventive plan”. Document download uses `downloadUrl` from the `documents` array:

```text
GET /api/operational-documents/{id}/download
```

Android scanning UI remains deferred. See [Mobile API — Asset lookup](mobile-api.md#asset-lookup-by-qr--barcode-v240-sprint-m4-be1-enriched-m4-be3m4-be4) for the full JSON contract.

### Asset QR code generation (V2.4.0 Sprint M4-BE2)

`GET /api/assets/{assetId}/qr` returns a `image/png` response encoding the asset business code only (e.g. `AST-1A2B3C4D`). The QR must never contain database ids, URLs, JSON, or other metadata. Authorization reuses existing asset view rules (`AssetAuthorizationService`). Printable labels, batch export, and frontend integration remain deferred.

```text
GET /api/assets/{assetId}/qr   → PNG QR (512×512, high error correction)
        ↓
Android scans QR             → reads assetCode
        ↓
GET /api/mobile/assets/lookup?code={assetCode}   → AssetContextResponse
        ↓
GET /api/operational-documents/{id}/download   → authenticated file download
```

### When to use lists

Use list endpoints (`/api/mobile/my-inspections`, `/api/mobile/my-work-orders`) for:

- home screen queues;
- pull-to-refresh of assignment lists;
- navigation into a bundle detail route.

After a write operation, refresh the relevant list **and** reload the bundle if the detail screen remains open.

See [Mobile API](mobile-api.md) for field definitions and sorting rules.

### Offline sync handshake (M5.2-BE1 / M5.2-BE2 / M5.3-BE / M5.4-BE / M5.5-BE1 / M5.5-BE1.1)

`POST /api/mobile/sync` is the protocol foundation for Android offline operation ([BDR-005](../03-architecture/bdr-005-offline-synchronization-architecture.md)). The client sends `clientId`, `clientVersion`, optionally the previous `syncToken`, and queued `pendingOperations`. **M5.3-BE** returns per-operation upload outcomes. **M5.4-BE** populates `delta.inspections` with scoped inspection records. **M5.4.2-BE** embeds checklist template/question/choice definitions in each inspection delta so assigned inspections are renderable offline without a prior bundle fetch. **M5.5-BE1** classifies stale workflow, permission, and entity-state failures as `CONFLICT`. **M5.5-BE1.1** enriches matching `conflicts[]` entries with `serverState`, `clientState`, and informational `resolutionHint` (detection only — no automatic resolution). **M6.1-BE1** (V2.6) processes `SAVE_WORK_ORDER_PROGRESS` for draft maintenance notes on assigned work orders. **M6.1-BE2** populates `delta.workOrders` with scoped work order sync records (including `draftCompletionNotes`). **M6.2-BE1** populates `delta.dashboard` with a server-computed dashboard snapshot on every sync. **M6.3-BE1** populates `delta.assets` with asset context for assets linked to scoped inspections/work orders. Other delta sections remain empty.

```text
Android (future M5.2+)
        ↓
POST /api/mobile/sync  { clientId, clientVersion, syncToken?, pendingOperations[] }
        ↓
M5.5-BE1.1 response    { protocolVersion: 1, serverTime, nextSyncToken, delta: { inspections: [...] }, operations: [...], conflicts: [{ serverState, clientState, resolutionHint, ... }], warnings: [] }
```

Store `nextSyncToken` opaquely; resubmit as `syncToken` on the next sync. Apply `delta.inspections`, `delta.workOrders`, `delta.dashboard`, and `delta.assets` to local cache after each successful sync — including embedded `template`, `questions`, and `choices` when present (**M5.4.2-BE**). Persist checklist definitions from the delta; do not synthesize them client-side. Replace the offline dashboard UI from `delta.dashboard` only; never recompute assignment/overdue/completed counters from local Room data (**M6.2-BE1**). Invalid `syncToken` yields a single `FULL_SYNC_REQUIRED` warning and full inspection/work order deltas — dashboard snapshot is still returned.

**Queue handling:** remove pending operations on `ACCEPTED`; resolve `CONFLICT` operations via `POST /api/mobile/sync/conflicts/resolve` (M5.5-BE2). `resolutionHint` from sync is informational; explicit `resolution` action is required for resolution outcome. Tombstones and delta removals are not synchronized yet.

### Conflict resolution (M5.5-BE2)

```text
Android displays conflict
        ↓
POST /api/mobile/sync/conflicts/resolve  { operationId, entityType, entityId, operationType, conflictType, resolution, clientState? }
        ↓
SyncConflictResolutionResponse  { status: RESOLVED | RETRY_REQUIRED | MANUAL_REVIEW_REQUIRED | REJECTED, message, serverTime }
```

No client payload is applied. No automatic merge. Durable conflict history deferred.

---

## 6. Permissions

### Backend evaluates; clients render

Permissions are **never** calculated on the client from role alone.

The web application uses role helpers for **UX only** (show or hide navigation). Authoritative checks always happen on the server. A hidden button does not grant access; a visible button does not guarantee success — the write endpoint may still return `403`.

### `allowedActions` (mobile bundles)

Mobile bundle responses include an **`allowedActions`** object computed by the backend for the authenticated user and the specific record.

Example pattern:

```text
allowedActions.canComplete === true
        ↓
Show “Complete Inspection” button
        ↓
POST /api/inspections/{id}/complete  (server validates again)
```

Typical flags include `canComplete`, `canCompleteMaintenance`, `canUploadDocument`, and `canViewAsset`. The asset context lookup response uses its own conservative set (`canViewAsset`, `canViewInspections`, `canViewIssues`, `canViewWorkOrders`, `canCreateInspection`, `canCreateIssue`). Flag names and availability are defined in the Mobile API contract.

**Never** infer `canComplete` from role `FIELD_EMPLOYEE` alone. **Never** show actions that the bundle did not explicitly allow.

### Progressive inspection answers

Inspections support **progress persistence** while status remains `ASSIGNED`:

```text
GET /api/mobile/inspections/{id}/bundle   (load questions + saved answers)
        ↓
PUT /api/inspections/{id}/progress      (save draft summary + checklist answers)
        ↓
GET /api/mobile/inspections/{id}/bundle   (reload saved answers)
        ↓
POST /api/inspections/{id}/complete       (mandatory validation + Decision Engine)
```

| Action | Endpoint | Mandatory questions | Decision Engine | Changes status |
|--------|----------|---------------------|-----------------|----------------|
| Save progress (summary + answers) | `PUT /api/inspections/{id}/progress` | No | No | No (`ASSIGNED`) |
| Save answers (compatibility) | `PUT /api/inspections/{id}/answers` | No | No | No (`ASSIGNED`) |
| Complete | `POST /api/inspections/{id}/complete` | Yes | Yes (once) | Yes (`COMPLETED`) |

`PUT /progress` and `PUT /answers` are idempotent and support partial payloads — omitted fields/questions are left unchanged. Completed inspections return `409 Conflict` on further saves. The Decision Engine runs **only** on final completion.

### Workflow audit timestamps

For workflow actions, clients submit **intent** (decision, observations, notes). The backend records **when the event occurred** using server-generated timestamps.

| Workflow action | Request field (deprecated, optional) | Authoritative response field |
|-----------------|--------------------------------------|------------------------------|
| Complete inspection | `completedAt` | Response `completedAt` |
| Complete maintenance | `completedAt` | Response `completedAt` |
| Record completion review | `reviewedAt` | Response `reviewedAt` |
| Approve suggested action | `recordedAt` (issue creation) | Issue `recordedAt`; suggestion `decidedAt` |

Request timestamp fields may still appear in OpenAPI for backward compatibility. They are **ignored** when persisting the official event time. Use response values as the source of truth.

User-entered **business dates** (for example `expectedCompletionDate`, preventive `plannedAt`) remain client-provided and are unchanged.

### Department and assignment scoping

Managers see department-scoped data. Field employees see assigned work only. Administrators have broader read access. Exact rules vary by endpoint group — when in doubt, handle `403` and display the server message.

---

## 7. Read vs Write APIs

### Read APIs (observe only)

| Area | Examples | Mutates data? |
|------|----------|---------------|
| Lists and details | Assets, inspections, issues, work orders | No |
| Mobile bundles | `/api/mobile/*` read paths | No |
| Operations Intelligence | KPIs, trends, recent activity | No |
| Dashboard preferences | GET preferences | No (PUT writes preferences only) |
| Reporting | CSV, XLSX, and PDF exports | No |
| Rule evaluation reports | Read evaluation history | No |

**Intelligence layers are read-only** from a workflow perspective: KPIs, trends, CSV/XLSX/PDF exports, and mobile dashboard counts do not create inspections, approve candidates, or assign work.

### Write APIs (explicit actions)

Examples:

- Save inspection answers (progressive) — `PUT /api/inspections/{id}/answers`
- Complete inspection — `POST /api/inspections/{id}/complete`
- Record issue — `POST /api/issues`
- Operational decision — `POST /api/operational-decisions`
- Create / assign work order
- Complete maintenance — `POST /api/work-orders/{id}/maintenance-activity`
- Approve suggested action / preventive candidate
- Upload operational document — multipart `POST`
- Delete operational document — `DELETE` (where authorised)

Mobile M1 **reuses existing write APIs** for field actions rather than duplicating write paths. See [Mobile API — Reused write APIs](mobile-api.md#reused-write-apis-android--future).

After every successful write, **refresh** affected read models from the server.

---

## 8. Error Handling

Responses for business failures are typically **plain-text bodies** with an HTTP status code. Parse the status first; display the message body when present.

| Status | Meaning | Client guidance |
|--------|---------|-----------------|
| **400** | Validation or business rule rejection | Show message; fix input; do not retry unchanged |
| **401** | Authentication required or failed | Clear session; return to login |
| **403** | Authenticated but not permitted | Show message; may also indicate invalid/expired token on some routes |
| **404** | Resource not found or not visible in scope | Remove stale UI reference; refresh list |
| **409** | Conflict (duplicate, invalid state transition) | Refresh record; show message; avoid blind retry |
| **429** | Rate limited (login, activation) | Respect `Retry-After` header; show friendly wait message |

### Practical rules

1. **Do not** parse error bodies as JSON unless documented for that endpoint.
2. **Do not** retry writes automatically on `409` or `400`.
3. **Do** refresh lists after `404` on detail views (item may have been deleted or scoped away).
4. **Do** implement exponential backoff only for transient network failures — not for application `4xx` responses.

Login rate limiting: `POST /api/auth/login` returns `429` with `Retry-After` when limits are exceeded. See [Security](../05-deployment/security.md).

---

## 9. Date and Time

InfraTrack uses **explicit conventions** — clients must not mix formats.

### Business dates

Fields representing council business dates (expected completion date, document date, registration date) use:

- **API JSON:** ISO-8601 date string `yyyy-MM-dd`
- **Example:** `"2026-06-15"`

Treat these as **local calendar dates** in the council's operational context, not as UTC instants.

### Business date-times

Fields such as `recordedAt`, `completedAt`, `assignedAt` use ISO-8601 date-time strings in API JSON (for example `2026-06-15T14:30:00`).

**Workflow audit timestamps** (`completedAt` on inspection/maintenance completion, `reviewedAt` on completion review, `decidedAt` on suggested-action and preventive-candidate decisions, and `recordedAt` when an issue is created from an approved suggestion) are generated by the server at the time the workflow action is processed. Clients must not treat request-body copies of these fields as authoritative; use the values returned in API responses.

**Field-recorded issues** (`POST /api/issues` from UC-005) still accept `recordedAt` from the field employee — that timestamp reflects when the worker recorded the issue on site and is distinct from workflow audit timestamps above.

### Technical timestamps (epoch millis)

Query parameters for filtering and trends use **epoch milliseconds** where documented:

- Operations Intelligence `from` / `to`
- Reporting export `from` / `to`
- Some analytics endpoints

Pass UTC epoch millis as query parameters; do not assume local timezone on the server.

### Created / updated metadata

Asset and work order `createdAt` / `updatedAt` in exports and some DTOs are epoch millis. Consult OpenAPI for each field's documented type.

**Rule:** Read the schema for each field. Do not convert blindly between formats.

---

## 10. Pagination

Paginated list endpoints accept:

| Parameter | Convention |
|-----------|------------|
| `page` | Zero-based index (default `0`) |
| `size` | Page size (default `20`, maximum `100`) |

Responses follow Spring Data `Page` JSON:

- `content` — array of items for this page
- `totalElements`, `totalPages`, `number`, `size`

### Client recommendations

- Preserve **server sort order** — do not re-sort pages client-side unless the API documents client-side sorting.
- Request reasonable `size` values; avoid `size=100` on every call if `20` suffices.
- Use `totalPages` for navigation UI; handle empty `content` on the last page.
- Some endpoints return a **plain JSON array** without pagination — check OpenAPI per path.

Non-paginated mobile list endpoints return bounded assignment lists for the current user.

---

## 11. Reporting APIs

Reporting endpoints under `/api/reporting/exports/*` are **strictly read-only**.

- They never create, update, or delete operational records.
- They do not run the scheduler or approve workflows.
- CSV responses use `text/csv` with `Content-Disposition: attachment`.
- XLSX responses use `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` with `Content-Disposition: attachment`.
- PDF responses use `application/pdf` with `Content-Disposition: attachment`.

**Today:** CSV, XLSX, and PDF exports for assets, inspections, issues, work orders, and preventive candidates. All formats share the same columns, filters, and authorization for a given request.

**Deferred:** Scheduled and email reports.

Authorisation: Administrator (global), Manager and Operational Coordinator (department-scoped). Field employees and contractors receive `403`.

Download via authenticated GET with Bearer token. See [Reporting API](reporting-api.md).

---

## 12. Mobile Development Guidelines

Recommended practices for Android and future native field clients:

1. **Cache presentation state only** — list scroll position, draft form input until submit. Do not cache permission outcomes or workflow state as truth.
2. **Never cache business rules** — no local copies of role matrices, rule engines, or candidate eligibility.
3. **Use bundle endpoints** for detail screens — one GET per screen.
4. **Use `allowedActions`** to drive action buttons — reload bundle after writes.
5. **Always handle 401 and 403** on bootstrap and write paths — clear session when identity fails.
6. **Avoid unnecessary API calls** — do not poll bundles every few seconds; refresh on navigation, pull-to-refresh, or after writes.
7. **Reuse write APIs** documented in Mobile API — do not invent parallel mobile-only write paths on the client.
8. **Register FCM token** via `PUT /api/users/me/fcm-token` when push is implemented — delivery remains server-driven.

Operational Coordinators do not have Mobile API access in M1 — design navigation for field roles accordingly. Since M4-BE1, Operational Coordinators may call the asset lookup endpoint only; the M1 dashboard/bundle/list endpoints remain unavailable to them.

---

## 13. Integration Best Practices

Concise guidance for third-party systems and new clients:

| Practice | Why |
|----------|-----|
| Avoid unnecessary polling | Reduces load; prefer webhooks or scheduled sync when Public API arrives |
| Prefer bundle or detail endpoints | Fewer round trips; consistent snapshots |
| Refresh lists after writes | Server state is authoritative after mutations |
| Never construct business state locally | Do not create “pending” issues or work orders only in client memory |
| Always trust backend validation | Submit user input; display server errors verbatim |
| Use idempotent user actions | Disable submit buttons while write in flight |
| Scope integrations per role credentials | Service accounts should use least-privilege council roles when introduced |
| Log correlation ids externally | Server logs use standard request handling; clients should log timestamp and path on failure |
| Plan for additive DTO fields | Forward-compatible parsers ignore unknown JSON properties |

Integrations must not bypass InfraTrack workflows — for example, creating a work order without an operational decision when the business path requires one.

---

## 14. Future Evolution

Planned capabilities will extend clients without invalidating the core pattern:

| Direction | Impact on consumers |
|-----------|---------------------|
| **Offline sync** | Local queue + reconciliation; server remains authority |
| **QR / Barcode** | M4-BE1: `GET /api/mobile/assets/lookup?code=` resolves operational context by asset business code. M4-BE2: `GET /api/assets/{assetId}/qr` generates a PNG QR encoding `assetCode` only. M4-BE3: lookup enriched with nullable `lastInspection`, `lastMaintenance`, and `preventivePlan`. M4-BE4/M4-BE4.1: lookup includes asset-owned `documents` for users who can view the asset context; download via existing `/api/operational-documents/{id}/download`. Android scanning UI and printable labels remain deferred. |
| **Push notifications** | FCM delivery of server events; no client-side workflow triggers |
| **Public API** | Documented integration authentication and stable integration endpoints |
| **Mobile-optimised writes** | Possible M2+ endpoints if web write shapes prove unsuitable |
| **Refresh tokens** | Would require explicit migration per BDR-003 reconsideration |

**Current API contracts remain valid** as these capabilities arrive. Clients should follow this guide's philosophy — backend truth, explicit writes, bundle reads — even as the platform grows.

---

## Related Documentation

| Document | Purpose |
|----------|---------|
| [Product Vision](../00-product-vision.md) | Why InfraTrack exists |
| [Business Capability Map](../01-business-architecture/business-capability-map.md) | What the platform does |
| [Mobile API](mobile-api.md) | Mobile endpoints, bundles, sorting |
| [Reporting API](reporting-api.md) | CSV, XLSX, and PDF export behaviour |
| [V2 Domain Engine API](v2-domain-engine-api.md) | Intelligence endpoint groups |
| [API documentation index](README.md) | Swagger links and index |
| [Domain Engine](../07-business-architecture/domain-engine.md) | Business rules behind APIs |
| [Security](../05-deployment/security.md) | Rate limits, document auth, CI |
| [ADR-003 — V2 workflow](../03-architecture/adr-003-v2-domain-driven-workflow.md) | Domain interaction model |
| [ADR-004 — Versioning](../03-architecture/adr-004-platform-versioning-strategy.md) | Version compatibility |
| [BDR-003 — Bearer tokens](../03-architecture/bdr-003-bearer-token-architecture.md) | Authentication architecture |
| [Project README — API Developer Guide](../../README.md#api-developer-guide) | Pagination, errors, activation |

---

*Consume the API; trust the backend; render what the server allows.*
