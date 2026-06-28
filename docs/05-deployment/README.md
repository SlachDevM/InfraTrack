# Deployment

Operational deployment, security hardening, and secrets management for InfraTrack.

| Document | Description |
|----------|-------------|
| [Secrets management](secrets.md) | Firebase credentials, environment variables, rotation, and Git history cleanup |
| [Security hardening](security.md) | Swagger lockdown, login rate limiting, JWT and Firebase startup behaviour |

## Related configuration

- [`.env.example`](../../.env.example) — production environment template
- [`docker-compose.prod.yml`](../../docker-compose.prod.yml) — production Docker Compose stack
- [`docker-compose.firebase.example.yml`](../../docker-compose.firebase.example.yml) — optional local FCM overlay
