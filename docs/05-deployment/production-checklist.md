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
cd frontend && npm ci --legacy-peer-deps && npm test -- --run && npm run build
docker compose up --build -d
curl http://localhost:4000/actuator/health
```

---

## Related

- [Deployment overview](README.md)
- [Secrets management](secrets.md)
- [Security hardening](security.md)
