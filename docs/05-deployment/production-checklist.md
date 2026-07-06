# Production Checklist

Use this checklist before go-live and before each production release.

---

## Secrets and configuration

- [ ] `.env` created from `.env.example` with production values
- [ ] `.env` is **not** committed to Git
- [ ] `JWT_SECRET` is a strong random value (not the dev default)
- [ ] `SPRING_DATASOURCE_PASSWORD` is strong and unique
- [ ] `FRONTEND_ORIGIN` matches the public HTTPS frontend URL
- [ ] `ACTIVATION_LINK_BASE_URL` matches the public activation route
- [ ] SMTP credentials tested (send a test activation email)
- [ ] Firebase credentials mounted outside Git if FCM is required (optional)
- [ ] `BOOTSTRAP_ADMIN_ENABLED=false` unless performing controlled first-time setup

---

## Security (Sprint 0)

- [ ] No Firebase service account JSON in the repository
- [ ] Swagger UI returns 403/404 on production (`SPRING_PROFILES_ACTIVE=prod`)
- [ ] Login rate limiting returns HTTP 429 with `Retry-After` when exceeded
- [ ] PostgreSQL port not exposed publicly in production compose
- [ ] HTTPS terminated at reverse proxy
- [ ] Frontend nginx sends `Content-Security-Policy` (V2.4.x DT-2A) — login and API calls work; no CSP violations in browser console
- [ ] Backend built on Spring Boot **4.0.7** (V2.4.x DT-3) — `mvn clean test` passes; Docker backend image builds

---

## Trusted reverse proxy and network exposure (INFRA-SEC-1)

The backend uses `X-Forwarded-For` for client IP extraction (login and activation rate limiting). This is correct **only** when the API sits behind a trusted reverse proxy. Verify before go-live:

- [ ] Backend API is **not** publicly reachable on its container port (e.g. `4000`) from the Internet
- [ ] Public HTTPS terminates at the reverse proxy or cloud load balancer
- [ ] Reverse proxy forwards **`X-Forwarded-For`** (set from the real client connection, not passed through unchanged from untrusted clients)
- [ ] Reverse proxy forwards **`X-Forwarded-Proto`** (`https` for TLS-terminated traffic)
- [ ] Backend is reachable only from the internal network (Docker bridge, VPC private subnet, or equivalent)
- [ ] Docker Compose production ports are bound to localhost or removed — not published `0.0.0.0` on the public host
- [ ] Firewall or security group restricts inbound access to the backend; only the reverse proxy subnet or security group can reach the API
- [ ] Frontend container port is internal or localhost-bound; public traffic enters through the reverse proxy on `443`
- [ ] PostgreSQL and other data services remain on internal networks only (no public `5432`)

See [security.md — Trusted reverse proxy](security.md#trusted-reverse-proxy-and-client-ip-infra-sec-1) for architecture rationale and example proxy headers.

---

## Data and persistence

- [ ] PostgreSQL volume (`pgdata`) on durable storage
- [ ] Operational documents volume backed up (see [backup-restore.md](backup-restore.md))
- [ ] Backup restore procedure tested in staging

---

## Health and observability

- [ ] `GET /actuator/health` returns `{"status":"UP"}`
- [ ] Backend and frontend Docker health checks passing
- [ ] Startup logs show expected Flyway migration version
- [ ] Firebase log shows expected state (enabled or disabled)

---

## Functional smoke test

- [ ] Administrator can log in
- [ ] Field employee can log in and access operational pages
- [ ] Asset list loads with pagination
- [ ] Invalid pagination parameters return HTTP 400 (not silent empty results)
- [ ] Account activation email delivers (or Mailpit in staging)

---

## Release validation commands

Run locally or in CI before tagging a release:

```bash
cd backend && mvn clean test && mvn clean package -DskipTests
cd frontend && npm ci && npm test -- --run && npm run build
docker compose up --build -d
curl http://localhost:4000/actuator/health
```

**DT-3 note:** Backend slice tests (`@WebMvcTest`) require `spring-boot-starter-webmvc-test` and `spring-boot-starter-security-test` on the test classpath (modularized in Spring Boot 4). CI uses the same `mvn clean test` command.

---

## Related

- [Deployment overview](README.md)
- [Secrets management](secrets.md)
- [Security hardening](security.md)
