# InfraTrack V2.0.1 — Security & Quality Hardening

**Status:** Completed  
**Scope:** Documentation consistency, security hardening, quality improvements, repository cleanup  
**Business impact:** None — no workflow, domain model, or API redesign changes

---

## Summary

Post **Version 2.0.0** hardening sprint. Strengthens authentication controls and deployment documentation without changing business capabilities.

---

## Changes

### Documentation

- Corrected frontend dev command: `npm start` (not `npm run dev`)
- Documented `JWT_EXPIRATION` → `jwt.expiration` (default 86400000 ms / 24 hours)
- Documented password minimum length (12 characters)
- Documented activation rate limiting, HSTS (production only), CORS tightening
- Documented trusted reverse proxy assumption for `X-Forwarded-For`
- Added [BDR-003 Bearer Token Architecture](../03-architecture/bdr-003-bearer-token-architecture.md)

### Security

| Control | Detail |
|---------|--------|
| Password validation | 12–128 characters on register and activate-account |
| Activation rate limit | 10 attempts/minute per IP; HTTP 429 + `Retry-After` |
| HSTS | `prod` profile only; `max-age=31536000; includeSubDomains` |
| CORS | Explicit allowed headers; no wildcard |
| Bearer tokens | Intentional architecture documented (no HttpOnly cookie migration) |

### Repository cleanup

Removed accidental committed files: `backend/test_*.txt`  
Updated `.gitignore` to prevent recurrence.

---

## Functional Validation

V2.0.1 functional validation campaign completed:

| Area | Result |
|------|--------|
| Decision Engine | PASS |
| Preventive Engine | PASS |
| V1 regression smoke | PASS |
| Docker stack | PASS |
| Backend tests | PASS |
| Frontend tests | PASS |

This version is an internally validated baseline for repository tracking — not a production release.

---

## Validation checklist

- [ ] Register rejects passwords shorter than 12 characters
- [ ] Activate-account rejects passwords shorter than 12 characters
- [ ] Repeated activation attempts return HTTP 429 with `Retry-After`
- [ ] Login rate limiting unchanged
- [ ] HSTS header present only with `prod` profile
- [ ] Frontend authentication works
- [ ] Swagger works in development
- [ ] Docker stack starts successfully

---

## Related

- [Security hardening](../05-deployment/security.md)
- [Deployment](../05-deployment/README.md)
- [Platform Version History](platform-version-history.md)
- [Version 2.0.0 sprint report](v2-phase-a-b.md)
