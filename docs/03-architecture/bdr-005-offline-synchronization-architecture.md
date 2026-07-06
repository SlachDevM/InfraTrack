# BDR-005 — Offline & Synchronization Architecture

**Status:** Accepted  
**Date:** July 2026  
**Context:** V2.4 Platform Baseline — Mobile API, Asset Context, QR Navigation, Security Hardening. Precedes M5 offline implementation.

---

## 1. Vision

InfraTrack field operations must continue when network connectivity is unreliable or absent. Offline capability is a **business requirement** for Australian Local Government field work — not a performance optimization.

### Core philosophy

| Principle | Meaning |
|-----------|---------|
| **Offline-capable, not offline-only** | The Android client works online by default and degrades gracefully when connectivity is lost. |
| **Thin client unchanged** | Android displays data and collects user actions. It does not own business rules. |
| **Backend is the source of truth** | PostgreSQL holds the official business record. Local storage holds **temporary working copies** and **pending operations** only. |
| **Deterministic synchronization** | Every upload is validated by the backend in a predictable order. Android never assumes local data is permanent until sync is confirmed. |
| **No business rules offline** | Decision Engine, Policy Engine, authorization, workflow transitions, and timestamp authority run on the server — never on the device. |

### Responsibility split

```text
Backend (Spring Boot)
        ↓
Business rules
Workflow state machines
Authorization & Policy Engine
Decision Engine evaluation
Business timestamps (LocalDate, recordedAt, completedAt)
Conflict detection & rejection
Audit trail
Permanent storage (PostgreSQL)

Android (Compose client)
        ↓
UI presentation
Temporary local cache (Room)
Pending mutation queue
Synchronization orchestration
Conflict presentation to the user
Connectivity state indicators
Never: business decisions, policy evaluation, or authorization logic
```

### Alignment with existing decisions

- [BDR-003](bdr-003-bearer-token-architecture.md) — JWT Bearer authentication applies online; offline session rules are defined in §7.
- [BDR-004](bdr-004-configurable-organizational-policies.md) — Policies are evaluated on the server during sync validation, not cached as rules on the device.
- [Mobile API](../04-api/mobile-api.md) — Bundles define the online contract; offline extends download and queues writes through future sync endpoints.
- [System Blueprint — Offline First Philosophy](../02-system-blueprint/INFRATRACK_SYSTEM_BLUEPRINT_V1_SKELETON.md#11-offline-first-philosophy) — Historical product intent; this BDR is the authoritative architecture reference for M5 implementation.

---

## 2. Offline Scope

### Included in offline operation (M5)

| Capability | Offline behaviour |
|------------|-----------------|
| **Assigned inspections** | Download and cache assigned inspection summaries and bundles |
| **Inspection progress** | Save template answers locally; queue for upload when online |
| **Assigned work orders** | Download and cache assigned work order summaries and bundles |
| **Work order execution** | Record maintenance activity notes locally; queue completion for server validation |
| **Asset Context** | Cache lookup response after QR scan or explicit navigation (read-only snapshot) |
| **Asset documents metadata** | Cache document list from asset context; download files on demand |
| **Cached documents** | Store downloaded file binaries locally for offline viewing (M5.6) |
| **User profile** | Cache identity from `GET /api/mobile/me` for offline UI context |

### Excluded initially

| Capability | Reason |
|------------|--------|
| **Reporting** | Read-only export; requires live server and date-window validation |
| **Administration** | User management, reference data, policy configuration |
| **Reference data editing** | Departments, categories, templates — web-only |
| **User management** | Web-only |
| **Policy management** | Server-side only; no policy admin on mobile |
| **Operations Intelligence / Dashboard** | Web-only analytics |
| **Preventive scheduler / candidate approval** | Manager workflows; web-only |
| **Issue creation from Decision Engine** | Human-in-the-loop manager actions; not field-offline scope in M5 |

Scope may expand in future versions only through explicit BDR or roadmap amendments.

---

## 3. Local Storage

Android uses **Room** as a local cache. Room is not a second source of truth — it mirrors server state and holds pending work until synchronization confirms.

### Storage model

```text
Room Database
        ↓
Cached entities (read mirrors)
  ├── UserProfile
  ├── AssetSummary / AssetContextSnapshot
  ├── InspectionSummary / InspectionBundle
  ├── InspectionAnswer (draft progress)
  ├── WorkOrderSummary / WorkOrderBundle
  └── DocumentMetadata (+ optional DocumentBlob in M5.6)
        ↓
Pending operations (write queue)
  ├── PendingInspectionProgress
  ├── PendingInspectionCompletion
  ├── PendingMaintenanceActivity
  ├── PendingDocumentUpload (future)
  └── PendingOperationMetadata (idempotency key, createdAt, retryCount)
        ↓
Sync metadata
  ├── SyncState (lastSuccessfulSyncAt, serverCursor, connectivityState)
  ├── EntityVersions (per-entity server version or updatedAt marker)
  └── DownloadManifest (assigned IDs, expiry, scope)
```

### Entity categories

| Category | Purpose | Authoritative source |
|----------|---------|---------------------|
| **Cached entities** | Read-only mirrors of server data for offline UI | Server — refreshed on sync |
| **Draft progress** | In-progress inspection answers not yet confirmed | Local until upload succeeds |
| **Pending mutations** | Completed actions awaiting server validation | Local until server accepts or rejects |
| **Sync metadata** | Cursors, timestamps, queue state | Device-local coordination only |

### Cache boundaries

- Cache only data the authenticated user is authorized to see (same scope as Mobile API bundles).
- Evict cache entries when assignment is revoked, entity is completed server-side, or user logs out.
- Never store policy configuration, decision rules, or cross-department data beyond authorized scope.

---

## 4. Synchronization Model

Synchronization is **eventually consistent**. Temporary divergence between device and server is expected and resolved through deterministic upload validation and incremental download.

### End-to-end flow

```text
Login (online)
        ↓
Initial download — assigned inspections, work orders, identity, optional asset context
        ↓
Local work — user reads cached bundles, saves draft progress, completes actions offline
        ↓
Queue mutations — each write becomes a PendingOperation with client idempotency key
        ↓
Reconnect detected
        ↓
Upload phase — push pending operations in dependency order (batch where supported)
        ↓
Server validation — authorization, workflow rules, conflict detection per entity
        ↓
Download phase — incremental delta of changed assignments and entity versions
        ↓
UI refresh — replace cached mirrors; surface conflicts or rejections to user
```

### Pull (download)

| Aspect | Rule |
|--------|------|
| **Initial sync** | On login or explicit refresh: download all assigned inspections, work orders, and identity |
| **Incremental sync** | After successful upload: fetch changes since `serverCursor` or per-entity `updatedAt` |
| **Asset context** | On-demand pull when user scans QR or opens asset; cache snapshot with version marker |
| **Documents** | Metadata with bundles; binary download separate (on demand or background in M5.6) |

### Push (upload)

| Aspect | Rule |
|--------|------|
| **Ordering** | Progress saves before completion for the same inspection; maintenance notes before work-order completion |
| **Batching** | Group independent operations; never batch conflicting mutations on the same entity |
| **Idempotency** | Every pending operation carries a `clientOperationId` (UUID); server deduplicates retries |
| **Retry** | Exponential backoff on transient failures; permanent rejection moves to conflict/rejection UI |
| **Partial success** | Accepted operations are removed from queue; rejected operations remain with server reason |

### Sync properties

| Property | Requirement |
|----------|-------------|
| **Resumable** | Interrupted sync resumes from last acknowledged operation |
| **Fault tolerant** | Network drop mid-upload does not corrupt server state |
| **Incremental** | Avoid full re-download when cursor/delta is available |
| **Repeatable** | Re-running sync with same `clientOperationId` produces same server outcome |
| **Idempotent** | Duplicate uploads must not create duplicate inspections, issues, or maintenance records |

---

## 5. Conflict Resolution

The **backend resolves all business conflicts**. Android detects sync failures and presents outcomes — it never auto-merges workflow state or overrides server decisions.

**Conflict resolution philosophy** — taxonomy, resolution hints (`SERVER_WINS`, `CLIENT_RETRY`, `MANUAL_REVIEW`), merge boundaries, and lifecycle — is defined in the companion document **[BDR-005 — Conflict Resolution Strategy](bdr-005-conflict-resolution-strategy.md)**. That document separates **detection** (M5.5-BE1 / M5.5-BE1.1), **explicit resolution** (M5.5-BE2), and **protocol idempotency** (DT-OFFLINE-1) from automatic merge (deferred). This BDR remains the primary offline architecture reference; the companion does not replace it.

### Strategy by entity

| Entity / operation | Strategy | Behaviour |
|--------------------|----------|-----------|
| **Inspection assignment / status** | Server authoritative | If inspection is no longer `ASSIGNED` (unassigned, completed, or cancelled server-side), local completion is **rejected**. User sees reason; local draft may be preserved for manual recovery. |
| **Inspection progress (draft answers)** | Server merge on upload | While inspection remains `ASSIGNED`, server accepts answer updates. If server has newer answers for the same question, **server value wins** per field; client is notified of overwritten fields. |
| **Inspection completion** | Server authoritative | Full workflow validation on upload. **Never merged.** Reject if rules fail (template, required answers, status). No partial completion. |
| **Work order assignment / status** | Server authoritative | If work order is `COMPLETED` or `CANCELLED` server-side, local completion is **rejected**. |
| **Maintenance activity / WO completion** | Server authoritative | **Never merged.** Server validates performer, assignment, and status. Reject if work order state changed. |
| **Asset metadata** | Server authoritative | Cached asset fields are read-only mirrors. Local edits to asset register are **not in M5 scope**. |
| **Asset context snapshot** | Refresh on sync | Stale context is replaced on next successful pull; no merge of context fields. |
| **Operational document metadata** | Server authoritative | List refreshed from server; deleted server documents removed from cache. |
| **Document binary cache** | Server version wins | Re-download when `contentVersion` or `uploadedAt` changes. |
| **Issue / decision workflows** | Server authoritative | Not created offline in M5 initial scope. |
| **User / role / department** | Server authoritative | Identity refresh on sync; disabled user triggers logout (see §7). |

### Conflict presentation (Android)

When the server rejects or overwrites local data:

1. Show a clear, non-technical message (e.g. “This inspection was completed by another user”).
2. Preserve rejected payload locally for support review (optional export/log) until user dismisses.
3. Refresh affected entity from server on next successful pull.
4. Never offer “force overwrite server” for workflow completions.

### Manager intervention

Manager actions (reassignment, cancellation, approval) always occur on the server. Field employees see the outcome on sync — offline devices do not participate in manager decisions.

---

## 6. Connectivity States

Android maintains an explicit connectivity state machine. Users always understand whether they are working offline, syncing, or fully online.

```text
                    ┌─────────────┐
         ┌─────────│   ONLINE    │◄────────┐
         │         └──────┬──────┘           │
         │                │ API failures /   │ sync complete
         │                │ slow network     │
         │                ▼                  │
         │         ┌─────────────┐           │
         │         │  DEGRADED   │───────────┤
         │         └──────┬──────┘           │
         │                │ no connectivity  │
         │                ▼                  │
         │         ┌─────────────┐     ┌─────┴──────┐
         └────────►│   OFFLINE   │────►│  SYNCING   │
                   └─────────────┘     └────────────┘
                         ▲                    │
                         │    sync failed     │
                         └────────────────────┘
                              (retry → SYNCING)
```

### State definitions

| State | Meaning | User experience |
|-------|---------|-----------------|
| **ONLINE** | API reachable; cache may be stale but sync is not required for reads | Normal operation; optional background sync |
| **DEGRADED** | Intermittent connectivity; some requests fail or time out | Show warning indicator; reads from cache; writes queue automatically |
| **OFFLINE** | No API connectivity confirmed | Show offline indicator; all reads from cache; writes queue locally |
| **SYNCING** | Upload and/or download in progress | Show progress indicator; block conflicting actions on entities being synced |

### Expected behaviour

- Transitions are automatic based on connectivity probes and sync lifecycle.
- Users can continue field work in **OFFLINE** and **DEGRADED** for in-scope capabilities.
- **SYNCING** should not block read-only navigation; block duplicate submission of the same pending operation.
- After sync completes with rejections, return to **ONLINE** or **DEGRADED** and show conflict notifications.

---

## 7. Security

Offline storage increases device-loss risk. Security controls complement [security.md](../05-deployment/security.md) and [BDR-003](bdr-003-bearer-token-architecture.md).

| Topic | Rule |
|-------|------|
| **Encrypted local storage** | Room database encrypted at rest (Android Keystore-backed SQLCipher or platform-approved equivalent). Document blobs stored in app-private storage. |
| **JWT handling** | Token stored in app-private secure storage (not plain SharedPreferences). Short-lived access token per BDR-003. |
| **Offline session** | Valid JWT required for last successful online auth. Offline work is permitted while token is locally valid; sync requires online re-validation. |
| **Token expiry offline** | When JWT expires offline, allow read-only cache access; **queue writes** but do not upload until user re-authenticates online. |
| **Disabled user** | On next online sync or identity check, disabled accounts receive `401` — clear token, purge pending mutations that cannot be attributed, wipe cache (see logout). |
| **Logout** | Purge JWT, pending queue (after user confirms or force on admin revoke), cached bundles, and document blobs. |
| **Device loss** | Treat as credential exposure risk; council should deactivate user account; JWT revocation (Security-3) limits window. Local encryption limits data extraction. |
| **Cache cleanup** | Periodic eviction of stale assignments, expired document blobs, and completed entity caches. |
| **Sensitive documents** | Operational documents may contain council-sensitive material; cache only documents user downloaded; respect same authorization as `GET /api/operational-documents/{id}/download`. |

---

## 8. API Evolution

M5 requires **new backend sync capabilities**. No implementation exists at V2.4 — this section defines future requirements only.

### Anticipated additions

| Capability | Purpose |
|------------|---------|
| **Sync cursor / token** | Opaque server-issued cursor (`nextSyncToken`) — **M5.2-BE2 delivered**; issued on every successful `POST /api/mobile/sync`. Android stores and resubmits; backend owns encoding. No business data in token. |
| **Batch upload endpoint** | `POST /api/mobile/sync` — **M5.3-BE delivered (first upload)**. **M5.4-BE delivered (first download)**. Returns per-operation outcomes plus `delta.inspections`. |
| **Delta download** | `SyncResponse.delta.inspections` — **M5.4-BE delivered**. Compact `SyncInspectionDeltaResponse` records scoped like mobile inspection lists. Null/invalid token → full delta; valid token → SQL filter `updatedAt >= issuedAt` (**V2.5-STAB-2**). Answers batch-loaded per sync (**V2.5-STAB-2**). **M5.4.2-BE:** checklist template/question/choice definitions embedded per inspection (batch-loaded; same mapping as bundle endpoint) so delta is self-contained for offline rendering — no prior bundle fetch required. Other delta sections remain empty. |
| **Delta download endpoints** | `GET /api/mobile/sync/changes?since={cursor}` — alternative/future path; primary delta container is `SyncResponse.delta` |
| **Operation result envelope** | Per-item `SyncOperationStatus`: `ACCEPTED`, `REJECTED`, `CONFLICT`, `RETRY`, `IGNORED` — **M5.3-BE:** `SAVE_INSPECTION_PROGRESS` returns `ACCEPTED` or `REJECTED`; unsupported types return `IGNORED`. **M5.5-BE1:** stale workflow, permission, or entity-state failures return `CONFLICT` plus a matching `conflicts[]` entry. Validation/malformed payloads remain `REJECTED`. One failure does not fail the whole sync. |
| **Conflict classification** | `SyncConflictType`: … **M5.5-BE1.1:** enriched `conflicts[]` payload … **M5.5-BE2:** explicit resolution via `POST /api/mobile/sync/conflicts/resolve` — stateless, no payload apply, no automatic merge. |
| **Sync warnings** | `SyncWarningCode`: `FULL_SYNC_REQUIRED`, `SYNC_TOKEN_EXPIRED`, `CLIENT_OUTDATED`, `PARTIAL_SYNC`, `UNKNOWN_WARNING` (types defined M5.2-BE2; list empty) |
| **Document caching support** | `ETag` or `contentVersion` on document metadata; conditional download |
| **Entity version numbers** | Monotonic `version` or `updatedAt` on bundles for incremental merge decisions |

### Contract principles

- Additive Mobile API extension under `/api/mobile/sync` — **M5.3-BE:** `POST /api/mobile/sync` returns protocol envelope with per-operation upload outcomes for supported operations. Existing bundle endpoints remain for online-first clients.
- **Operation independence:** pending operations are processed individually; rejected items do not abort the sync handshake.
- **Idempotency (DT-OFFLINE-1 delivered):** `operationId` is the client idempotency key. Processed operations are stored in `mobile_sync_operation` (outcome metadata only — no payload). Before handler execution, the backend looks up `operationId`; duplicates return the stored `SyncOperationResponse` without re-executing business services. Records are retained for **90 days** (configurable) with scheduled cleanup. Runtime handler failures roll back and do not reserve the `operationId`.
- **Download correctness (M5.4):** prefer full delta over incorrect incremental sync. Invalid `syncToken` returns `FULL_SYNC_REQUIRED` warning and full inspection delta; sync handshake still succeeds.
- **Deletion sync:** not implemented in M5.4 — loss of access to an inspection is not reflected as a tombstone in delta. **M5.5-BE1:** permission loss on upload is reported as `CONFLICT` / `PERMISSION_DENIED`, not a delta removal.
- **Synchronization limits (M5.4.1):** max **100** pending operations per request (HTTP 400 if exceeded, no processing). Max **256 KB** UTF-8 payload per operation (operation-level `REJECTED`, sync continues).
- **Observability (M5.4.1 / DT-OFFLINE-1):** Micrometer metrics (`mobile.sync.requests`, `mobile.sync.operations.accepted|rejected|ignored|conflict|duplicate`, `mobile.sync.delta.inspections`, `mobile.sync.duration`) and structured INFO logging per sync (user id and counts only — no JWT, payloads, or PII).
- **Sync scalability (V2.5-STAB-2):** Indexes on `inspections.assigned_to_user_id` and `inspections.updated_at` support mobile list and incremental delta queries. Incremental delta uses repository-level `updatedAt` filtering with unchanged administrator/manager/field scoping. Inspection answers are loaded in one batch query per sync handshake.
- **Offline checklist definitions (M5.4.2-BE):** Questions and choices for all templates referenced by scoped inspections are batch-loaded once per sync (`MobileInspectionChecklistLoader`). Active-question/active-choice filtering matches the bundle endpoint. Inactive choices remain available server-side for `choiceId` resolution on existing answers. No per-inspection bundle queries; no separate template delta section.
- **Protocol versioning:** `protocolVersion` starts at `1`. Clients must ignore unknown JSON fields. Backend increments version only when additive fields are insufficient; token encoding may change without client parsing.
- Every upload item includes `clientOperationId`, `entityType`, `entityId`, `operationType`, and `payload` matching existing write DTOs where possible.
- Server responses must be sufficient for Android to update Room without re-fetching full bundles (optimization) or trigger targeted bundle refresh (simplicity path).
- Authorization, Policy Engine, and Decision Engine run inside sync handlers — same rules as existing write endpoints.

### Non-goals for initial M5 API

- Generic graphQL or arbitrary entity sync.
- Offline reporting exports.
- Client-side conflict resolution endpoints — **M5.5-BE2 delivered:** `POST /api/mobile/sync/conflicts/resolve` for explicit `SAVE_INSPECTION_PROGRESS` decisions (stateless; no merge).

---

## 9. Android Responsibilities

| Responsibility | Owner | Notes |
|----------------|-------|-------|
| **UI** | Android | Compose screens; offline indicators; conflict dialogs |
| **Room database** | Android | Cache + pending queue only |
| **Sync scheduler** | Android | Trigger sync on connectivity restore, app foreground, and periodic background check |
| **WorkManager** | Android | Background sync tasks (connectivity constraints, retry policy) |
| **Conflict UI** | Android | Present server rejection reasons; never resolve business conflicts locally |
| **Offline indicators** | Android | Connectivity state machine (§6) |
| **Download manager** | Android | Document binary fetch, resume, and cache (M5.6) |
| **Idempotency keys** | Android | Generate and persist `clientOperationId` per pending operation |

### Android must never

- Execute Decision Engine rules or evaluate decision rules offline.
- Evaluate Policy Engine or infer authorization from role alone.
- Assign business timestamps that override server `recordedAt` / `completedAt`.
- Merge workflow completions locally.
- Call reporting or administration APIs from offline cache paths.

---

## 10. Backend Responsibilities

| Responsibility | Owner | Notes |
|----------------|-------|-------|
| **Validation** | Backend | Every synced operation passes same validation as direct REST writes |
| **Conflict detection** | Backend | Compare entity version/status before applying mutation |
| **Audit trail** | Backend | Accepted sync operations produce same history events as online writes |
| **Versioning** | Backend | Entity versions or timestamps for delta download |
| **Business timestamps** | Backend | `LocalDate`, `recordedAt`, `completedAt` assigned server-side |
| **Authorization** | Backend | `*AuthorizationService` on every sync handler |
| **Policy Engine** | Backend | `*PolicyService.getPolicy()` where applicable |
| **Decision Engine** | Backend | Suggested actions and rules evaluated only on server |
| **Synchronization responses** | Backend | Per-operation outcome envelope for Android queue management |

The backend does not trust Android workflow state — it re-validates from authoritative PostgreSQL records on every upload.

---

## 11. Future Roadmap — M5 Decomposition

Offline implementation is delivered in vertical slices. Each milestone is independently testable.

```text
M5.1 — Offline Foundations
        ↓ Room schema, connectivity state, secure token storage, cache of identity
M5.2 — Sync Engine
        ↓ Pending queue, idempotency, upload/download orchestration, sync API (backend)
M5.3 — Offline Inspection Bundles
        ↓ Download assigned inspections; offline read; draft answer persistence
M5.4 — Offline Work Orders
        ↓ Download assigned work orders; offline maintenance notes; queue completion
M5.5 — Conflict Detection (M5.5-BE1 delivered)
        ↓ Server classifies stale workflow / permission / entity-state failures as CONFLICT; Android retains conflicting operations for future UX
M5.5.1 — Conflict Payload Enrichment (M5.5-BE1.1 delivered)
        ↓ conflicts[] include serverState, clientState, resolutionHint for Android presentation; detection-only
M5.5.2 — Explicit Conflict Resolution (M5.5-BE2 delivered)
        ↓ POST /api/mobile/sync/conflicts/resolve records client resolution decisions; no server mutation
DT-OFFLINE-1 — Protocol Idempotency Store (delivered)
        ↓ mobile_sync_operation table; duplicate operationId returns stored outcome without handler execution
M5.5+ — Automatic Merge / Extended Sync History (deferred)
M5.6 — Cached Documents
        ↓ Document metadata cache, binary download manager, offline viewing
M5.7 — Offline UX Polish
        ↓ Indicators, sync progress, retry messaging, cache eviction, field testing
```

M5 aligns with **Version 2.4.0** remaining scope (offline sync) in [v2-roadmap.md](../06-release-notes/v2-roadmap.md). Android scanning UI and printable QR labels may proceed in parallel but are not prerequisites for M5.1–M5.2.

---

## Consequences

### Positive

- Field employees complete inspections and maintenance in low-connectivity areas without workflow interruption.
- Clear responsibility split preserves backend authority and auditability.
- Idempotent sync reduces duplicate operational records.
- M5 slices allow incremental delivery and validation.

### Negative / costs

- Additional backend sync API surface and test matrix.
- Room schema and sync engine maintenance on Android.
- Conflict UX complexity — users must understand rejected operations.
- Encrypted local storage and document cache increase Android implementation effort.

### Compliance

All M5 implementation sprints must reference this BDR. Changes to conflict strategy or offline scope require a BDR amendment or successor record.

---

## See also

- [BDR-005 — Conflict Resolution Strategy](bdr-005-conflict-resolution-strategy.md) — conflict taxonomy, resolution policies, merge boundaries (companion)
- [V2.4 release notes](../06-release-notes/v2.4.md) — current platform baseline
- [Mobile API](../04-api/mobile-api.md) — online bundle contracts
- [API Consumer Guide](../04-api/api-consumer-guide.md) — client integration principles
- [Domain Engine](../07-business-architecture/domain-engine.md) — business architecture authority
- [BDR-003 — Bearer Token Architecture](bdr-003-bearer-token-architecture.md)
- [BDR-004 — Configurable Organizational Policies](bdr-004-configurable-organizational-policies.md)
- [security.md](../05-deployment/security.md)
