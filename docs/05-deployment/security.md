# Security Hardening

Production security controls applied in InfraTrack V2 Sprint 0.

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

Other auth endpoints (`/api/auth/activate-account`, `/api/auth/register`) are **not** rate limited by this control.

Configuration:

```properties
app.auth.login-rate-limit.max-attempts-per-minute=10
```

---

## JWT signing key

The JWT HMAC signing key is derived once at application startup and cached for the lifetime of the process. Token generation and validation use the same cached key, preserving compatibility with existing tokens.

Production requires a strong `JWT_SECRET` environment variable (see `.env.example`).

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
| SMTP password | `SPRING_MAIL_PASSWORD` |
| Firebase credentials (optional) | `FIREBASE_SERVICE_ACCOUNT_PATH` + mounted file |

Never commit real secrets to Git. See [secrets.md](secrets.md).
