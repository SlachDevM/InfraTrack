# BDR-003 — Bearer Token Architecture

**Status:** Accepted  
**Date:** 2026  
**Context:** V2.0.1 Security & Quality Hardening

---

## Decision

InfraTrack stores JWT access tokens in the **React SPA** (and future **Android** client) and sends them on each API request using the **`Authorization: Bearer`** header.

InfraTrack does **not** use HttpOnly cookies for JWT storage in the current architecture.

This is an **intentional** decision, not a temporary shortcut.

---

## Rationale

| Factor | Bearer token approach |
|--------|----------------------|
| REST API | Stateless JWT validation on every request; no server-side session store |
| React SPA | Token held in client memory/local storage pattern; simple axios/fetch integration |
| Future Android | Same header-based auth model as mobile HTTP clients expect |
| Mobile compatibility | Bearer tokens work consistently across web and native without cookie domain complexity |
| Cross-origin deployment | API and SPA are often on different origins; cookie policies add operational overhead |

HttpOnly cookies are valuable when the browser client and API share a tightly controlled same-site deployment. InfraTrack’s current model — separate React frontend, Spring Boot API, and planned Android client — favours explicit Bearer tokens.

---

## Security controls in place

- JWT signing secret externalised (`JWT_SECRET`)
- Configurable token lifetime (`JWT_EXPIRATION` → `jwt.expiration`, default 24 hours)
- Login and activation rate limiting
- HTTPS enforced at the reverse proxy in production; HSTS enabled on the API in the `prod` profile
- Production Swagger disabled

Clients must protect tokens at rest on the device (Android Keystore, secure browser storage practices).

---

## Future reconsideration

Future **multi-tenant SaaS** deployments with a same-origin BFF (Backend-for-Frontend) or unified domain architecture may revisit HttpOnly cookies or refresh-token rotation.

Any migration would be a deliberate architecture change — not an incremental tweak — and would require updated ADR/BDR documentation.

---

## Related

- [security.md](../05-deployment/security.md) — operational security controls
- [V2.0.1 release notes](../06-release-notes/v2-0-1-security-hardening.md)
