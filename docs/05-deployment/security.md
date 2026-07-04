# Security Hardening

Production security controls applied in InfraTrack V2 Sprint 0 and extended in V2.0.1.

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

## JWT

| Property | Environment variable | Default | Purpose |
|----------|---------------------|---------|---------|
| `jwt.secret` | `JWT_SECRET` | Dev fallback only | HMAC signing key |
| `jwt.expiration` | `JWT_EXPIRATION` | `86400000` (24 hours) | Access token lifetime in milliseconds |

Production requires a strong `JWT_SECRET`. Token lifetime can be tuned per deployment without code changes.

Bearer token storage in clients is an intentional architecture decision — see [BDR-003](../03-architecture/bdr-003-bearer-token-architecture.md).

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

## Reverse proxy and client IP

Rate limiting uses the client IP from `X-Forwarded-For` when present (first address in the list), otherwise `request.getRemoteAddr()`.

**Important:** The backend treats `X-Forwarded-For` as trustworthy. Deployments must place the API **behind a trusted reverse proxy** that:

- Terminates TLS (HTTPS)
- Strips or overwrites untrusted `X-Forwarded-For` values from clients
- Sets the header from the actual client connection

Common proxies: **Nginx**, **Traefik**, **Cloudflare** (when configured to pass the connecting client IP).

Direct exposure of the Spring Boot port to the public internet without a proxy allows clients to spoof `X-Forwarded-For` and bypass IP-based rate limits.

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

## Authorization architecture guard

A backend architecture test (`AuthorizationArchitectureTest` in `backend/src/test/java/com/infratrack/architecture/`) scans all `@RestController` classes and verifies each one either:

1. depends directly on an `*AuthorizationService`;
2. delegates to an application service that depends on an `*AuthorizationService`; or
3. is explicitly allowlisted in the test with a documented reason.

The guard catches obvious regressions when a new protected controller is added without an authorization dependency path. It is **not** a replacement for endpoint-specific authorization tests or manual security review.

New protected controllers should follow the `Controller → Service → AuthorizationService` pattern or be added to the allowlist with a clear justification (public endpoints, reference data, or documented interim admin checks).

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

## Production secrets checklist

| Secret | Environment variable |
|--------|------------------------|
| Database password | `SPRING_DATASOURCE_PASSWORD` |
| JWT signing secret | `JWT_SECRET` |
| JWT expiration (optional) | `JWT_EXPIRATION` |
| SMTP password | `SPRING_MAIL_PASSWORD` |
| Firebase credentials (optional) | `FIREBASE_SERVICE_ACCOUNT_PATH` + mounted file |

Never commit real secrets to Git. See [secrets.md](secrets.md).
