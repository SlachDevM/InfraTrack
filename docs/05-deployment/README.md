# Deployment

Operational deployment, security, and maintenance guides for InfraTrack.

## Documents

| Document | Description |
|----------|-------------|
| [Development deployment](README.md#development-docker-compose) | Local Docker Compose stack (this page) |
| [Production deployment](README.md#production-docker-compose) | Production-like Docker Compose |
| [Secrets management](secrets.md) | Firebase, JWT, `.env`, and credential rotation |
| [Security hardening](security.md) | Swagger lockdown, login rate limiting, JWT |
| [Backup and restore](backup-restore.md) | PostgreSQL and operational document volumes |
| [Production checklist](production-checklist.md) | Pre-release and go-live validation |
| [Troubleshooting](troubleshooting.md) | Common startup and runtime issues |

## Related configuration

- [`.env.example`](../../.env.example) — production environment template
- [`docker-compose.yml`](../../docker-compose.yml) — local development stack
- [`docker-compose.prod.yml`](../../docker-compose.prod.yml) — production Docker Compose stack
- [`docker-compose.firebase.example.yml`](../../docker-compose.firebase.example.yml) — optional local FCM overlay

---

## Development (Docker Compose)

The default local stack uses `docker-compose.yml` with the `dev` Spring profile.

### Start

```bash
git clone <repository-url>
cd InfraTrack
docker compose up --build -d
```

### Services

| Service | Port | Purpose |
|---------|------|---------|
| Frontend (nginx) | 3000 | React SPA |
| Backend (Spring Boot) | 4000 | REST API |
| PostgreSQL | 5432 | Database |
| Mailpit | 8025 (UI), 1025 (SMTP) | Dev email capture |

### Default administrator (development only)

- Email: `admin@infratrack.local`
- Password: `change-me`

### Optional Firebase (FCM)

FCM is **disabled by default**. To enable push notifications locally:

1. Save credentials to `backend/firebase-service-account.local.json` (gitignored).
2. `cp docker-compose.firebase.example.yml docker-compose.override.yml`
3. `docker compose up --build -d`

See [secrets.md](secrets.md) for details.

### Health checks

Docker Compose waits for PostgreSQL and backend health before starting dependent services.

- Backend: `curl http://localhost:4000/actuator/health`
- Frontend: `http://localhost:3000`

### Logs

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
```

---

## Production (Docker Compose)

Production uses `docker-compose.prod.yml` and a `.env` file (never committed).

### Prepare

```bash
cp .env.example .env
# Edit .env with production values — see secrets.md
docker compose -f docker-compose.prod.yml up -d --build
```

### Required environment variables

| Variable | Required | Notes |
|----------|----------|-------|
| `JWT_SECRET` | Yes | Strong random signing secret |
| `SPRING_DATASOURCE_URL` | Yes | JDBC URL to PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Yes | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Database password |
| `FRONTEND_ORIGIN` | Yes | CORS allowed origin (HTTPS) |
| `SPRING_MAIL_HOST` | Yes | SMTP server |
| `SPRING_MAIL_USERNAME` | Yes | SMTP user |
| `SPRING_MAIL_PASSWORD` | Yes | SMTP password |
| `SPRING_MAIL_FROM` | Yes | Sender address |
| `ACTIVATION_LINK_BASE_URL` | Yes | Account activation base URL |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | No | Enable FCM when credentials are mounted |
| `BOOTSTRAP_ADMIN_ENABLED` | No | Default `false` in production |

Full list: [`.env.example`](../../.env.example)

### Production security defaults

- `SPRING_PROFILES_ACTIVE=prod`
- Swagger UI and OpenAPI docs **disabled**
- Login rate limiting **active** on `POST /api/auth/login`
- Firebase credentials **optional** — FCM disabled when unset
- JWT secret **required** (no dev fallback)

See [security.md](security.md) and [secrets.md](secrets.md).

### Data persistence

Docker volumes:

| Volume | Content |
|--------|---------|
| `pgdata` | PostgreSQL database |
| `operational-documents-data` | UC-012 uploaded files |

Back up both before upgrades. See [backup-restore.md](backup-restore.md).

### Health checks

Both backend and frontend containers define Docker `HEALTHCHECK` instructions. Monitor:

```bash
curl http://localhost:4000/actuator/health
```

In production, expose the API through a reverse proxy (HTTPS) and keep PostgreSQL internal.

---

## Sprint 0 security summary

After V2 Sprint 0 hardening:

- No secrets committed to Git (Firebase, JWT, `.env`)
- Firebase optional via runtime mount or `docker-compose.override.yml`
- Swagger disabled in production profile
- Login rate limiting: 10 attempts/minute per IP and email; HTTP 429 with `Retry-After`
- Pagination invalid parameters return HTTP 400
