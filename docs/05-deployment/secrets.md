# Secrets Management

InfraTrack never stores private keys or service account credentials in Git.

This document explains how to provide Firebase Cloud Messaging (FCM) credentials at runtime for local development and production.

---

## Firebase service account

Push notifications use the Firebase Admin SDK. The backend reads a Google service account JSON file from a path defined at runtime.

**Environment variable:** `FIREBASE_SERVICE_ACCOUNT_PATH`

| Value | Behaviour |
|-------|-----------|
| Unset or empty | FCM is disabled. In-app notifications still work; push delivery is skipped. |
| Valid file path | Firebase Admin SDK initialises and push notifications are sent when users have an FCM token. |

When credentials are missing or invalid, business workflows continue. Notification records are still created in the database.

Startup logging (see also [security.md](security.md)):

- Missing path: `Firebase credentials not configured. FCM push notifications are disabled.`
- Missing file: `Firebase credentials file not found at …`
- Success: `Firebase messaging enabled.`

---

## What is committed to Git

| File | Purpose |
|------|---------|
| `backend/firebase-service-account.example.json` | Safe placeholder structure only — **no real private key** |
| `docker-compose.firebase.example.yml` | Optional local Docker overlay template |

**Never commit:**

- `backend/firebase-service-account.json`
- `backend/firebase-service-account.local.json`
- Any file under `secrets/` containing real credentials

These paths are listed in `.gitignore`.

---

## Local development

### Without FCM (default)

```bash
docker compose up --build
```

The backend starts without Firebase credentials. Push notifications are skipped.

### With FCM

1. Download a service account key from the [Firebase Console](https://console.firebase.google.com/) → Project settings → Service accounts → Generate new private key.
2. Save it as `backend/firebase-service-account.local.json` (gitignored).
3. Copy the example overlay:

   ```bash
   cp docker-compose.firebase.example.yml docker-compose.override.yml
   ```

4. Start the stack:

   ```bash
   docker compose up --build
   ```

The overlay mounts the local file into the container and sets `FIREBASE_SERVICE_ACCOUNT_PATH=/app/firebase-service-account.json`.

### Running the backend outside Docker

Set the environment variable to your local file path:

```bash
export FIREBASE_SERVICE_ACCOUNT_PATH=/absolute/path/to/firebase-service-account.local.json
cd backend && mvn spring-boot:run
```

Use `backend/firebase-service-account.example.json` as a structural reference only.

---

## Docker / production

Production uses `docker-compose.prod.yml` and `.env` (not committed).

1. Store the real JSON file **outside the repository**, for example:

   ```
   /opt/infratrack/secrets/firebase-service-account.json
   ```

2. In `.env`, set:

   ```env
   FIREBASE_SERVICE_ACCOUNT_PATH=/app/firebase-service-account.json
   FIREBASE_CREDENTIALS_HOST_PATH=/opt/infratrack/secrets/firebase-service-account.json
   ```

3. Uncomment and configure the Firebase volume mount in `docker-compose.prod.yml`:

   ```yaml
   volumes:
     - ${FIREBASE_CREDENTIALS_HOST_PATH}:/app/firebase-service-account.json:ro
   ```

4. Restrict file permissions on the host (e.g. `chmod 600`).

Credentials are **not** copied into the Docker image. They are mounted read-only at container runtime.

To disable FCM in production, leave `FIREBASE_SERVICE_ACCOUNT_PATH` unset and omit the volume mount.

---

## Key rotation

If a service account key was ever committed to Git or shared:

1. **Revoke the exposed key immediately** in [Google Cloud Console](https://console.cloud.google.com/) → IAM & Admin → Service Accounts → select the account → Keys → delete the compromised key.
2. Generate a new key only if FCM is still required.
3. Store the new key outside Git and update the runtime mount path.
4. Restart the backend container.

Code cannot revoke keys — this step must be performed manually in Firebase / Google Cloud.

---

## Cleaning Git history

If a real private key was pushed to a shared remote, removing the file from the latest commit is **not sufficient**. The secret may remain in Git history.

Use one of these tools to rewrite history (only when explicitly approved by the repository owner):

- [git-filter-repo](https://github.com/newren/git-filter-repo)
- [BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/)

Example (git-filter-repo):

```bash
pip install git-filter-repo
git filter-repo --path backend/firebase-service-account.json --invert-paths
git push --force-with-lease origin main
```

Coordinate with all collaborators before rewriting history. After cleanup, **still rotate the key** — history may have been cloned or cached elsewhere.

---

## Verification

Confirm no credentials are tracked or present in source:

```bash
git ls-files | grep firebase
git grep "BEGIN PRIVATE KEY"
git grep "private_key"
```

Expected:

- `backend/firebase-service-account.example.json` may appear (placeholder values only).
- No real `BEGIN PRIVATE KEY` blocks in tracked files.
- `backend/firebase-service-account.json` is not tracked.
