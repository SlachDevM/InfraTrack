# Security Hardening

Production security controls applied in InfraTrack V2 Sprint 0, extended in V2.0.1, and consolidated through the **V2.4 platform baseline**. See [v2.4.md](../06-release-notes/v2.4.md) for the release summary.

| Section | Topics |
|---------|--------|
| [Authentication](#authentication) | JWT Bearer, disabled-account revocation, account-status cache |
| [Content Security Policy](#content-security-policy-csp) | Frontend nginx CSP, limitations, future tightening |
| [Reporting export security](#reporting-export-security) | Formula injection, required date filters, 365-day window |
| [Single application instance](#single-application-instance) | In-memory caches, rate limiting, scheduled jobs |
| [Authorization](#authorization) | Per-domain services, architecture guard (ArchUnit-style) |
| [Trusted reverse proxy](#trusted-reverse-proxy-and-client-ip-infra-sec-1) | X-Forwarded-For trust boundary, deployment assumptions |

---

## Swagger / OpenAPI

Interactive API documentation is enabled in development and disabled in production.

| Profile | Swagger UI | OpenAPI JSON |
|---------|------------|--------------|
| Development (`dev`) | Enabled | Enabled |
| Production (`prod`) | Disabled | Disabled |

Production settings (`application-prod.properties`):

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

`SecurityConfig` also denies unauthenticated access to Swagger paths when OpenAPI is disabled.

For local development, use:

- Swagger UI: `http://localhost:4000/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:4000/v3/api-docs`

---

## Authentication

InfraTrack uses **JWT Bearer tokens** for all authenticated API access. Clients send `Authorization: Bearer <token>` on every protected request. There are no refresh tokens, no HttpOnly cookies, and no server-side token blacklist — see [BDR-003](../03-architecture/bdr-003-bearer-token-architecture.md).

### JWT configuration

| Property | Environment variable | Default | Purpose |
|----------|---------------------|---------|---------|
| `jwt.secret` | `JWT_SECRET` | Dev fallback only | HMAC signing key |
| `jwt.expiration` | `JWT_EXPIRATION` | `86400000` (24 hours) | Access token lifetime in milliseconds |

Production requires a strong `JWT_SECRET`. Token lifetime can be tuned per deployment without code changes.

### Disabled account revocation (Security-3)

JWT Bearer authentication is unchanged — no refresh tokens, no HttpOnly cookies, no token blacklist.

After signature and expiry validation, `JwtAuthenticationFilter` calls `UserAccountStatusService` to confirm the account is still enabled. Disabled or missing users receive HTTP `401 Unauthorized`; the filter chain does not continue and controllers are not reached.

### Authentication metrics (V2.5-STAB-3)

Micrometer counters support production monitoring of JWT outcomes (no JWT content is logged):

| Metric | When incremented |
|--------|------------------|
| `mobile.auth.jwt.invalid` | Bearer token present but signature, format, or expiry validation fails |
| `mobile.auth.jwt.disabled_user` | Valid JWT rejected because the account is disabled |
| `mobile.auth.jwt.missing` | Protected endpoint accessed without authentication (`AuthenticationEntryPoint`) |

Tags use fixed metric names only — no `userId` or token identifiers.

| Control | Setting |
|---------|---------|
| Cache | Caffeine, per-user enabled flag |
| TTL | 30 seconds (`expireAfterWrite`) |
| Maximum size | 10,000 entries |
| Database check | `UserRepository.existsByIdAndEnabledTrue(userId)` |
| Cache eviction | On user deactivate, reactivate, and account activation |

Without eviction, disabled users lose API access within the 30-second TTL. With eviction (implemented), access stops on the next request after status change.

Login still rejects disabled accounts at `POST /api/auth/login`. This control closes the gap where an unexpired JWT continued to work after offboarding.

### Account-status validation flow

```text
Request with Authorization: Bearer <token>
        ↓
JwtAuthenticationFilter — signature + expiry check
        ↓
UserAccountStatusService.isEnabled(userId)
  ├── cache hit (≤30s) → return cached enabled flag
  └── cache miss → UserRepository.existsByIdAndEnabledTrue(userId)
        ↓
enabled=false or user missing → HTTP 401, chain stops
enabled=true → SecurityContext populated, request continues
```

**Immediate eviction:** `UserManagementService` (deactivate/reactivate) and `ActivationService` (activate account) call `evict(userId)` so the next request sees the updated status without waiting for TTL expiry.

Bearer token storage in clients is an intentional architecture decision — see [BDR-003](../03-architecture/bdr-003-bearer-token-architecture.md).

---

## Spring Boot platform (V2.4.x DT-3)

**V2.4.x DT-3** migrates the backend from Spring Boot **3.2.7** to **4.0.7** (Spring Framework **7.0.8**, embedded Tomcat **11.0.22**). No business logic, REST paths, DTOs, JWT model, frontend, Android, or Mobile API contracts were changed.

| Aspect | Status |
|--------|--------|
| JWT Bearer authentication | Unchanged |
| Login / logout behaviour | Unchanged |
| Role authorization | Unchanged |
| `SecurityConfig` rules | Unchanged |
| OpenAPI / Swagger paths | Unchanged (`springdoc-openapi` **3.0.3**) |
| Flyway migrations | Unchanged (Flyway **11.14.1** + `flyway-database-postgresql`) |
| Actuator `/actuator/health`, `/actuator/info` | Unchanged |

### Mobile sync operational limits (M5.4.1)

`POST /api/mobile/sync` is protected against oversized client batches:

| Limit | Value | Behaviour |
|-------|-------|-----------|
| Pending operations per request | 100 | HTTP `400` — no operation processed |
| Operation payload (UTF-8) | 256 KB | Operation `REJECTED` — other operations continue |

These limits reduce abuse and accidental overload before M5.5 conflict resolution.

### Mobile sync observability (M5.4.1, V2.5-STAB-3)

Micrometer counters, distribution summaries, timers, and structured INFO logging are emitted per successful sync handshake:

| Metric | Purpose |
|--------|---------|
| `mobile.sync.requests` | Sync handshakes completed |
| `mobile.sync.operations.accepted` | Accepted pending operations |
| `mobile.sync.operations.rejected` | Rejected pending operations |
| `mobile.sync.operations.ignored` | Ignored pending operations |
| `mobile.sync.operations.conflict` | Conflict pending operations |
| `mobile.sync.operations.duplicate` | Idempotent duplicate replays (DT-OFFLINE-1) |
| `mobile.sync.delta.inspections` | Inspection records in delta (counter) |
| `mobile.sync.delta.size` | Inspection delta size distribution (V2.5-STAB-3) |
| `mobile.sync.batch.size` | Pending operation batch size distribution (V2.5-STAB-3) |
| `mobile.sync.full_sync_required` | Responses that included `FULL_SYNC_REQUIRED` warning |
| `mobile.sync.invalid_token` | Requests with unparseable sync token |
| `mobile.sync.protocol_version` | Counter tagged `version=<n>` (low cardinality) |
| `mobile.sync.duration` | End-to-end sync duration |

Endpoint timers (V2.5-STAB-3): `mobile.endpoint.sync`, `mobile.endpoint.sync.conflicts.resolve`, `mobile.endpoint.dashboard`, `mobile.endpoint.my_inspections`, `mobile.endpoint.assets.lookup`.

Reporting export metrics (V2.5-STAB-3): `reporting.export.csv|xlsx|pdf`, `reporting.export.failure`, `reporting.export.duration` tagged by `entity` and `format` (enum names only).

Structured **INFO** logging records one line per sync with: `userId`, `protocolVersion`, `operationCount`, `accepted`, `rejected`, `conflicts`, `ignored`, `duplicateOperations`, `deltaInspectionCount`, `durationMs`, `requiresFullSync`. Logs exclude JWTs, operation payloads, inspection answers, document names, and user-entered text.

### Prometheus readiness (V2.5-STAB-3)

Metrics use dot-separated names and low-cardinality tags (`version`, `entity`, `format`). No `userId`, `assetId`, or `operationId` labels. Enable Prometheus scraping via `management.endpoints.web.exposure.include` when operational monitoring is deployed.

### Dependency changes (summary)

| Area | Before (3.2.7) | After (4.0.7) |
|------|----------------|---------------|
| OpenAPI UI | `springdoc-openapi-starter-webmvc-ui` 2.5.0 | 3.0.3 |
| Flyway | `flyway-core` only | `spring-boot-starter-flyway` + `flyway-database-postgresql` |
| Jackson (internal JSON helpers) | Jackson 2 via Boot 3 defaults | `spring-boot-jackson2` bridge retained for existing `com.fasterxml.jackson` code |
| Testcontainers | `junit-jupiter`, `postgresql` artifacts | `testcontainers-junit-jupiter`, `testcontainers-postgresql` (Testcontainers **2.0.5**) |
| WebMvc / Security slice tests | Covered by `spring-boot-starter-test` | Requires `spring-boot-starter-webmvc-test` and `spring-boot-starter-security-test` |

### Manual BOM overrides removed

Pre-migration CVE overrides for **Netty**, **Spring Framework**, and **embedded Tomcat** were removed. Spring Boot **4.0.7** manages patched versions (Netty **4.2.15.Final**, Spring Framework **7.0.8**, Tomcat **11.0.22**). Reintroduce explicit overrides only if a future OWASP Dependency-Check scan reports CVSS ≥ 9 against the managed tree.

### Actuator package move

Custom health contributor `OperationalDocumentStorageHealthIndicator` now uses `org.springframework.boot.health.contributor.*` (Boot 4 modular health API). Info contributor packages are unchanged (`org.springframework.boot.actuate.info.*`).

### Validation commands

```bash
cd backend && mvn clean test && mvn clean package -DskipTests
docker compose build backend
```

---

## Content Security Policy (CSP)

**V2.4.x DT-2A** adds a production Content Security Policy on the **frontend nginx** container (`frontend/nginx.conf`). **DT-2A.1** tightened the policy after review. The policy is **not** emitted by the Spring Boot API — the React SPA is served separately from the backend.

### Architecture unchanged

| Aspect | Status |
|--------|--------|
| JWT Bearer authentication | Unchanged |
| Token storage (`localStorage`) | Unchanged — see BDR-003 |
| Login / logout flows | Unchanged |
| Backend `SecurityConfig` | Unchanged (API CSP intentionally deferred) |
| HttpOnly cookies | Not introduced — possible future V3 evolution |

CSP **reduces XSS impact** by restricting script, connection, and embedding sources. It does **not** replace secure React coding practices (avoid `dangerouslySetInnerHTML`, validate user input, keep dependencies patched).

### Enforcement location

| Environment | CSP enforced |
|-------------|--------------|
| `npm run dev` (Vite) | No — local development unchanged |
| Frontend Docker image (nginx on port 8080) | Yes |
| Production external HTTPS reverse proxy | May add additional headers; frontend container CSP still applies to SPA responses |

### Policy (frontend nginx)

```http
Content-Security-Policy:
  default-src 'self';
  script-src 'self';
  style-src 'self';
  img-src 'self' data:;
  font-src 'self' data:;
  connect-src 'self' https: http://localhost:4000;
  object-src 'none';
  base-uri 'self';
  frame-ancestors 'none';
  form-action 'self';
```

**Directive notes:**

| Directive | Rationale |
|-----------|-----------|
| `script-src 'self'` | Vite production build serves bundled scripts from same origin only — no `'unsafe-eval'` or `'unsafe-inline'` |
| `style-src 'self'` | Stylesheets are bundled CSS from same origin. React `style={{…}}` props set styles via JavaScript at runtime and do **not** require `'unsafe-inline'` (DT-2A.1 audit: no `<style>` blocks, no HTML `style=""` attributes, no libraries injecting inline styles) |
| `connect-src https:` | Production `VITE_API_BASE_URL` is HTTPS (see `.env.example`) |
| `connect-src http://localhost:4000` | Local Docker Compose / dev API origin reachable from the browser |
| `upgrade-insecure-requests` | **Not included** (DT-2A.1). InfraTrack supports HTTP deployments (local Docker Compose, staging). Reintroduce only when HTTPS is mandatory for all supported deployments — typically at the external HTTPS reverse proxy once plain HTTP is no longer offered |

If your deployment uses another API origin (e.g. a dedicated API subdomain), ensure it is covered by `https:` in `connect-src`. Do not add wildcard script permissions or `'unsafe-eval'`.

### Verification

After deploying the frontend container:

1. Open the application in a browser.
2. Confirm login and authenticated API calls succeed.
3. Check the browser developer console for CSP violation reports.
4. Inspect response headers — `Content-Security-Policy` should be present on HTML and static asset responses from nginx.

See [production-checklist.md](production-checklist.md) and [troubleshooting.md](troubleshooting.md#content-security-policy-csp).

### Current limitations

| Limitation | Rationale |
|------------|-----------|
| No `upgrade-insecure-requests` | HTTP deployments (local Docker Compose, staging) remain supported |
| No API CSP | Spring Boot API serves JSON only; SPA CSP is on nginx |
| `connect-src http://localhost:4000` | Local dev API origin from browser |
| Vite dev server (`npm run dev`) | CSP not enforced — development unchanged |

### Future tightening

- Add `upgrade-insecure-requests` when all supported deployments mandate HTTPS (typically at the external reverse proxy).
- Evaluate API response CSP if SPA and API are served from a single hardened origin.
- Do not add `'unsafe-inline'`, `'unsafe-eval'`, or wildcard script sources without a documented security review.

---

## Password policy

Registration (`POST /api/auth/register`, development only) and account activation (`POST /api/auth/activate-account`) require passwords between **12 and 128 characters**.

No complexity rules (uppercase, symbols, etc.) — length only. The React activation page enforces the same minimum client-side.

---

## Login rate limiting

`POST /api/auth/login` is protected against brute-force attempts.

| Limit | Policy |
|-------|--------|
| Per IP address | 10 attempts per minute |
| Per email | 10 attempts per minute (normalized) |

When exceeded:

- HTTP `429 Too Many Requests`
- Header: `Retry-After: <remaining_seconds>`
- JSON body:

```json
{
  "message": "Too many login attempts. Please try again later.",
  "retryAfterSeconds": 60
}
```

No indication whether the email exists.

Configuration:

```properties
app.auth.login-rate-limit.max-attempts-per-minute=10
```

---

## Activation rate limiting

`POST /api/auth/activate-account` is rate limited by **client IP only** (not by token) to avoid leaking token validity.

| Limit | Policy |
|-------|--------|
| Per IP address | 10 attempts per minute |

When exceeded, the same response shape as login rate limiting:

- HTTP `429`
- Header: `Retry-After`
- JSON: `{ "message": "Too many activation attempts. Please try again later.", "retryAfterSeconds": <seconds> }`

Configuration:

```properties
app.auth.activate-account-rate-limit.max-attempts-per-minute=10
```

---

## HTTP Strict Transport Security (HSTS)

HSTS is enabled **only** when the active Spring profile includes `prod`:

- `max-age=31536000` (one year)
- `includeSubDomains=true`

Development profiles do **not** send the `Strict-Transport-Security` header.

HSTS assumes the API is served over HTTPS behind a reverse proxy in production.

---

## CORS

Allowed origins are configured via `FRONTEND_ORIGIN` / `app.cors.allowed-origins`.

Allowed request headers (explicit list — no wildcard):

- `Authorization` — JWT Bearer authentication
- `Content-Type` — JSON and multipart boundaries (browser sets boundary for `FormData`)
- `X-Requested-With`
- `Accept`
- `Origin`

No custom response headers are exposed to JavaScript; the frontend reads JWT from the login/activation response body.

Swagger UI in development is served from the API origin (`localhost:4000`) and is not affected by SPA CORS rules.

---

## Trusted reverse proxy and client IP (INFRA-SEC-1)

InfraTrack extracts the client IP from the `X-Forwarded-For` request header when present (first address in the comma-separated list), otherwise from `request.getRemoteAddr()`. This behaviour is intentional and unchanged — the application **assumes trusted infrastructure** rather than attempting to detect or validate proxy hops at runtime.

### Why the application trusts X-Forwarded-For

| Consumer | Purpose |
|----------|---------|
| Login rate limiting | Per-IP attempt budget on `POST /api/auth/login` |
| Activation rate limiting | Per-IP attempt budget on `POST /api/auth/activate-account` |
| Audit and operational logging | Client IP may be recorded for security-relevant events |
| Future IP-based controls | Additional infrastructure-layer protections may rely on the same client IP source |

The backend does **not** implement `ForwardedHeaderFilter`, proxy chain validation, or application-level proxy detection. Correctness depends on deployment: only a trusted reverse proxy should set or overwrite `X-Forwarded-For` before traffic reaches the API.

### Trusted reverse proxy requirement

Public traffic must **terminate on a trusted reverse proxy** (Nginx, Traefik, HAProxy, Caddy, or a cloud load balancer). The reverse proxy is the **trust boundary** for client identity at the HTTP layer.

| Requirement | Rationale |
|-------------|-----------|
| Backend containers must not be publicly exposed | Direct Internet access allows clients to forge `X-Forwarded-For` |
| HTTPS terminated at the reverse proxy or load balancer | TLS and HSTS assumptions in production |
| Only the reverse proxy forwards `X-Forwarded-For` | The proxy overwrites or appends from the actual client connection |
| Reverse proxy forwards `X-Forwarded-Proto` | Downstream services can distinguish HTTPS from HTTP when needed |
| Backend reachable only from internal network | Firewall, security group, or Docker network isolation |
| Docker service ports not published to the Internet | Bind backend/frontend ports to localhost or internal interfaces only |

Common proxies: **Nginx**, **Traefik**, **HAProxy**, **Caddy**, **AWS ALB/NLB**, **Azure Application Gateway**, **Google Cloud Load Balancing**. When using a CDN (e.g. Cloudflare), configure it to pass the connecting client IP and ensure the origin trusts only the CDN edge.

### Risk when misconfigured

If the Spring Boot port is exposed directly to the public Internet without a trusted proxy in front:

- Clients can send arbitrary `X-Forwarded-For` values
- IP-based rate limits may be weakened or bypassed
- Audit logs may record incorrect client addresses

This is an **infrastructure misconfiguration**, not an application vulnerability. Operators must enforce network placement and proxy configuration — see [production-checklist.md](production-checklist.md).

### Example reverse proxy headers (Nginx)

The proxy should set forwarding headers from the real client connection. Illustrative configuration only — adapt to your environment:

```nginx
proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
proxy_set_header Host              $host;
```

Do not forward client-supplied `X-Forwarded-For` unchanged from the public Internet to the backend without the proxy overwriting or validating the value.

### Local development note

`docker-compose.yml` publishes backend port `4000` and other service ports to the host for convenience. This layout is **development-only** and does not represent a production trust model. Local stacks typically have no reverse proxy; rate limiting falls back to `request.getRemoteAddr()` unless you manually send `X-Forwarded-For` (e.g. via curl). Production must use `docker-compose.prod.yml` behind an external reverse proxy with internal-only service exposure.

---

## Operational document download authorization

`GET /api/operational-documents/{id}/download` enforces the same ownership model as upload and delete.

| Role | Download access |
|------|-----------------|
| Administrator | Any operational document |
| Manager | Documents for assets in the manager's department (including delegated authority) |
| Operational Coordinator | Documents for assets in the coordinator's department |
| Field Employee | Documents linked to inspections, issues, work orders, or maintenance activities they performed or were assigned to |
| Contractor | Same assignment rules as Field Employee |

Authorization runs in `OperationalDocumentAuthorizationService.requireDownloadAuthorized` **before** the file is read from storage. Cross-department and unassigned access returns HTTP `403`. Unknown documents return HTTP `404` without touching storage.

Operational document metadata listing (`GET /api/assets/{assetId}/documents`) applies the same role-, department-, and ownership-based authorization rules as download. Cross-department and unassigned access returns HTTP `403`.

Upload and delete rules are unchanged. Administrators may download but cannot upload operational evidence.

---

## Asset history read authorization

`GET /api/assets/{assetId}/history` returns operational history only when the caller is authorized to view the asset.

| Role | History access |
|------|----------------|
| Administrator | Any asset history |
| Manager | History for assets in the manager's department (including delegated authority) |
| Operational Coordinator | History for assets in the coordinator's department |
| Field Employee | History for assets in the employee's department |
| Contractor | Same department rule as Field Employee |

Authorization runs in `AssetAuthorizationService.requireCanViewAsset` **before** history events are loaded. Cross-department access returns HTTP `403`. Unknown assets return HTTP `404`.

---

## Maintenance activity read authorization

`GET /api/maintenance-activities` returns maintenance activities scoped to the caller's authorized work order / asset context.

| Role | List access |
|------|-------------|
| Administrator | All maintenance activities |
| Manager | Activities for assets in the manager's department (including delegated authority) |
| Operational Coordinator | Activities for assets in the coordinator's department |
| Field Employee | Activities for work orders assigned to the employee or performed by them |
| Contractor | Same assignment rule as Field Employee |

The `eligibleForCompletionReview=true` query parameter continues to return only manager-eligible activities for completion review (UC-010), already scoped by department and delegation.

Authorization uses `MaintenanceActivityAuthorizationService` for single-record checks. Cross-department access returns HTTP `403`. Unknown activities return HTTP `404`.

---

## Authorization

Domain-specific `*AuthorizationService` classes enforce role and department rules before operational data is loaded or mutated. Controllers delegate to application services; services call authorization services at decision points.

### Authorization architecture guard

A backend architecture test (`AuthorizationArchitectureTest` in `backend/src/test/java/com/infratrack/architecture/`) scans all `@RestController` classes and verifies each one either:

1. depends directly on an `*AuthorizationService`;
2. delegates to an application service that depends on an `*AuthorizationService`; or
3. is explicitly allowlisted in the test with a documented reason.

The guard catches obvious regressions when a new protected controller is added without an authorization dependency path. It is **not** a replacement for endpoint-specific authorization tests or manual security review.

New protected controllers should follow the `Controller → Service → AuthorizationService` pattern or be added to the allowlist with a clear justification (public endpoints, reference data, or documented interim admin checks).

### ArchUnit-style protection

`AuthorizationArchitectureTest` in `backend/src/test/java/com/infratrack/architecture/` performs a compile-time dependency scan equivalent to an ArchUnit rule: every `@RestController` must have a verifiable path to an `*AuthorizationService`. This runs in CI on every build.

---

## Dependency and static analysis (CI)

| Check | Scope | Policy |
|-------|-------|--------|
| `npm audit --audit-level=high --omit=dev` | Frontend production dependencies | Fails CI on high or critical runtime findings |
| OWASP Dependency-Check | Backend Maven dependencies | Fails CI on CVSS ≥ 9 (see below) |
| GitHub CodeQL | Java backend and JavaScript frontend | Scheduled and on `main` pushes/PRs |

### Known npm audit findings (dev toolchain)

**Investigation performed (T2):** Full `npm audit` run without `--omit=dev` identified 5 vulnerabilities in development dependencies only:

| Package | Severity | Advisory ID | Affects |
|---------|----------|-------------|---------|
| vitest | critical | GHSA-5xrq-8626-4rwp | Test runner (UI server) |
| vite | high | GHSA-4w7w-66w2-5vf9, GHSA-fx2h-pf6j-xcff | Dev server, build tool |
| esbuild | moderate | GHSA-67mh-4wv8-2f99 | Bundler (via vite) |
| @vitest/mocker, vite-node | moderate | - | Test tooling (via vite) |

**Production audit status:**
- `npm audit --audit-level=high --omit=dev` reports **0 vulnerabilities**
- No production dependencies are affected

**Remediation status:**
- An `npm audit fix --force` attempt during T2 temporarily upgraded the dev toolchain to `vite@8.x` / `vitest@4.x`; that upgrade was reverted in T2.1 because `@vitejs/plugin-react@4.x` is incompatible with Vite 8.
- Fixes for the dev-only findings require a dedicated Vite/Vitest migration sprint (target: `vite@8.x`, `vitest@4.x` with a compatible `@vitejs/plugin-react` release).
- No runtime production dependency vulnerabilities are currently reported

**CI strategy:** Continue using `npm audit --audit-level=high --omit=dev` to protect production builds.

### OWASP Dependency-Check threshold

The build fails on CVSS **≥ 9**. The threshold remains at 9 because lowering to 7 has not been validated against the current dependency tree in CI; the existing suppression file documents one confirmed false positive (gRPC-Go CVE mapped incorrectly to gRPC-Java). Revisit when the NVD scan is run after the next dependency upgrade sprint.

---

## Firebase (optional)

FCM push notifications remain optional. See [secrets.md](secrets.md) for credential management.

Startup logs:

| Condition | Log message |
|-----------|-------------|
| Path not configured | `Firebase credentials not configured. FCM push notifications are disabled.` |
| File not found | `Firebase credentials file not found at …` |
| Loaded successfully | `Firebase messaging enabled.` |

The backend starts normally when Firebase is disabled.

---

## Reporting export security

Reporting exports can return user-controlled text and large datasets. V2.4 adds layered protections below.

### Spreadsheet formula injection protection

InfraTrack exports user-controlled text (asset names, issue descriptions, work order notes, and similar fields) as CSV and XLSX downloads. Spreadsheet applications may interpret cell values that begin with certain characters as formulas and execute them when the file is opened ([CWE-1236](https://cwe.mitre.org/data/definitions/1236.html) / CSV injection).

### Mitigation

Before any textual value is written to CSV or XLSX, the export layer prefixes values whose first significant character (after optional leading whitespace) is one of:

- `=` (equals)
- `+` (plus)
- `-` (minus)
- `@` (at)
- TAB

with a single apostrophe (`'`). Excel, LibreOffice Calc, and Google Sheets then display the literal text instead of evaluating a formula.

Example: `=HYPERLINK("https://evil.example")` is exported as `'=HYPERLINK("https://evil.example")`.

### Scope

| Export format | Protected |
|---------------|-----------|
| CSV | Yes |
| XLSX | Yes |
| PDF | No — PDF is not opened as a spreadsheet; export content is unchanged |

Implementation is centralized in `ExportCellFormatter.sanitizeSpreadsheetText()` and applied automatically by `CsvExportWriter` and `XlsxExportWriter`. Business services, REST endpoints, DTOs, and API consumers are unchanged — protection is transparent at download time only.

### Export volume guard

Reporting exports can return large datasets. To prevent unbounded resource use, every CSV, XLSX, and PDF export request must include explicit `from` and `to` date filters (epoch millis).

| Rule | Behaviour |
|------|-----------|
| `from` and `to` required | HTTP `400` if either is omitted |
| Maximum window | 365 inclusive calendar days (UTC) |
| `to` before `from` | HTTP `400` |
| No default range | The server does not default to "last 365 days" or truncate results |

Validation runs once in `ReportingExportService.validateExportDateWindow()` before any export data is loaded. Applies to all export domains (assets, inspections, issues, work orders, preventive candidates) and all formats.

**Memory and throughput (RC-FIX-BE-2 review):** Exports assemble one in-memory `TabularExport` for the validated date window, then write CSV, XLSX, or PDF bytes. This is acceptable for the current design because:

- Every request requires explicit `from` and `to` filters with a maximum 365-day inclusive window.
- Results are scoped to the caller's reporting department (or organization-wide for administrators).
- Validation runs before any repository query; there is no unbounded default range.

Streaming or chunked writers would require a reporting redesign. No API or export-format changes are planned for V2.5. Monitor export duration and row counts in production; tighten the window operationally if needed.

The React **Export** menu (`ExportReportingMenu`) defaults to the last 30 days and validates the date range client-side before calling the API. Server validation remains authoritative.

---

## Single application instance

InfraTrack V2.5 assumes **one active Spring Boot application instance** per environment. The backend does not use Redis, distributed locks, or a clustered cache manager.

| Component | Behaviour | Multi-instance risk |
|-----------|-----------|---------------------|
| Reference-data cache (`CacheConfig`) | Caffeine in-process cache for departments and asset categories (1-hour TTL) | Stale reads until TTL expiry after reference-data changes |
| Account-status cache (`UserAccountStatusService`) | Caffeine per-user enabled flag (30-second TTL) with explicit eviction on status change | Eviction is local to the instance that performed the change; other instances rely on TTL |
| Login rate limiter (`LoginRateLimiter`) | Caffeine token buckets per client IP and email | Limits are per instance, not global |
| Preventive scheduler (`PreventiveSchedulerJob`) | `@Scheduled` cron when `PREVENTIVE_SCHEDULER_ENABLED=true` | Duplicate candidate generation if multiple instances run the job concurrently |
| Mobile sync idempotency cleanup (`MobileSyncIdempotencyCleanupJob`) | `@Scheduled` purge of aged `mobile_sync_operation` rows | Duplicate cleanup work only; idempotent SQL |

**Deployment guidance:**

- Run a **single backend replica** in production unless you add external coordination (not part of V2.5).
- If you must scale horizontally before distributed locking is introduced, keep `PREVENTIVE_SCHEDULER_ENABLED=false` and run scheduled jobs on one designated instance only.
- Load-balancing multiple stateless replicas is not a supported production topology in V2.5 because of the in-memory controls above.

This is documentation only — no clustering changes are introduced in the V2.5 release family.

---

## Production secrets checklist

| Secret | Environment variable |
|--------|------------------------|
| Database password | `SPRING_DATASOURCE_PASSWORD` |
| JWT signing secret | `JWT_SECRET` |
| JWT expiration (optional) | `JWT_EXPIRATION` |
| SMTP password | `SPRING_MAIL_PASSWORD` |
| Firebase credentials (optional) | `FIREBASE_SERVICE_ACCOUNT_PATH` + mounted file |

Never commit real secrets to Git. See [secrets.md](secrets.md).
