# Troubleshooting

Common InfraTrack deployment and startup issues.

---

## Backend will not start

### Database connection refused

**Symptoms:** Backend exits or health check fails; logs mention connection refused to PostgreSQL.

**Checks:**

1. PostgreSQL container is running: `docker compose ps postgres`
2. Postgres health check passed before backend started
3. `SPRING_DATASOURCE_URL` points to the correct host (`postgres` inside Compose, not `localhost` from inside the backend container)

### Flyway migration failure

**Symptoms:** Backend stops during startup with Flyway errors.

**Checks:**

1. Database is reachable and credentials are correct
2. Do not set `SPRING_JPA_HIBERNATE_DDL_AUTO=create` in production — use `validate`
3. Review migration scripts in `backend/src/main/resources/db/migration`

### JWT secret missing in production

**Symptoms:** Backend fails to start with `prod` profile; JWT configuration error.

**Fix:** Set `JWT_SECRET` in `.env`. Production has no hardcoded fallback.

---

## Firebase / FCM

### Push notifications not sent (expected in dev)

**Symptoms:** In-app notifications work; no push delivery.

**Cause:** FCM is optional. Without `FIREBASE_SERVICE_ACCOUNT_PATH`, startup logs:

```text
Firebase credentials not configured. FCM push notifications are disabled.
```

**Fix (optional):** Follow [secrets.md](secrets.md) and use `docker-compose.override.yml`.

### Credentials file not found

**Symptoms:** Log warning about missing file at configured path.

**Fix:** Ensure the host file exists and the Docker volume mount is uncommented in compose, or remove `FIREBASE_SERVICE_ACCOUNT_PATH` to disable FCM.

---

## Swagger

### Swagger not accessible in development

**Checks:**

1. Backend running on port 4000
2. `SPRING_PROFILES_ACTIVE=dev` (default in local compose)
3. Open `http://localhost:4000/swagger-ui/index.html`

### Swagger accessible in production (should not be)

**Fix:** Confirm `SPRING_PROFILES_ACTIVE=prod` and `springdoc.api-docs.enabled=false` in `application-prod.properties`.

---

## Login rate limiting

### HTTP 429 on login

**Expected** after 10 failed attempts per minute per IP or email.

Response includes:

- Header: `Retry-After: <seconds>`
- Body: `{ "message": "...", "retryAfterSeconds": <seconds> }`

Wait for the indicated time or reduce test traffic.

---

## Frontend cannot reach API

**Symptoms:** Login fails with network error; browser console shows CORS or connection errors.

**Checks:**

1. Backend health: `curl http://localhost:4000/actuator/health`
2. Frontend nginx proxy configuration (production) or Vite dev proxy (local npm start)
3. `FRONTEND_ORIGIN` includes the frontend URL in production

---

## Docker Compose

### Bind mount fails for Firebase credentials

**Symptoms:** `docker compose up` fails when mounting a missing file.

**Fix:** Create `backend/firebase-service-account.local.json` or remove the override file. Default compose does not require Firebase.

### Port already in use

**Fix:** Stop conflicting services or change ports in `docker-compose.yml`.

---

## Logs

```bash
docker compose logs backend --tail 100
docker compose logs frontend --tail 50
docker compose logs postgres --tail 50
```

Look for the one-time startup diagnostic block (version, profile, Flyway, timezone).

---

## Related

- [Deployment overview](README.md)
- [Secrets management](secrets.md)
- [Backup and restore](backup-restore.md)
