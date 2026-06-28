# Backup and Restore

InfraTrack persists data in Docker volumes. Back up before upgrades, migrations, or infrastructure changes.

---

## PostgreSQL (`pgdata` volume)

### Backup

With the stack running:

```bash
docker compose exec postgres pg_dump -U appuser -d infratrack -F c -f /tmp/infratrack.dump
docker compose cp postgres:/tmp/infratrack.dump ./backups/infratrack-$(date +%Y%m%d).dump
```

Production (adjust compose file and credentials):

```bash
docker compose -f docker-compose.prod.yml exec postgres pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -F c -f /tmp/infratrack.dump
```

Store backup files **outside the repository** with restricted permissions.

### Restore

Stop the backend to prevent writes, then restore into a fresh or existing database:

```bash
docker compose stop backend
docker compose exec postgres pg_restore -U appuser -d infratrack --clean --if-exists /tmp/infratrack.dump
docker compose start backend
```

For a full disaster recovery on an empty volume, recreate the database first if needed:

```bash
docker compose exec postgres psql -U appuser -c "DROP DATABASE IF EXISTS infratrack;"
docker compose exec postgres psql -U appuser -c "CREATE DATABASE infratrack;"
```

Then run Flyway migrations on next backend startup, or restore from backup as above.

---

## Operational documents (`operational-documents-data` volume)

UC-012 files are stored on the backend filesystem inside the `operational-documents-data` Docker volume.

### Backup

Copy the volume contents to a host directory:

```bash
docker run --rm \
  -v infratrack_operational-documents-data:/data:ro \
  -v "$(pwd)/backups:/backup" \
  alpine tar czf /backup/operational-documents-$(date +%Y%m%d).tar.gz -C /data .
```

Adjust the volume name with `docker volume ls | grep operational`.

### Restore

```bash
docker compose stop backend
docker run --rm \
  -v infratrack_operational-documents-data:/data \
  -v "$(pwd)/backups:/backup" \
  alpine sh -c "rm -rf /data/* && tar xzf /backup/operational-documents-YYYYMMDD.tar.gz -C /data"
docker compose start backend
```

---

## Backup schedule (recommended)

| Data | Suggested frequency |
|------|---------------------|
| PostgreSQL | Daily (automated) + before each release |
| Operational documents | Daily or weekly, depending on upload volume |
| `.env` and secrets files | On change (secure secret store, not Git) |

---

## Related

- [Production checklist](production-checklist.md)
- [Troubleshooting](troubleshooting.md)
