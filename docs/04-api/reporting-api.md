# Reporting API — CSV Exports (V2.2.x)

Read-only CSV export endpoints for operational reporting. Reporting **observes** data only — it does not create, update, approve workflows, run the scheduler, or send notifications.

PDF (`.pdf`) and Excel (`.xlsx`) exports remain **deferred**.

## Base path

All endpoints require JWT authentication (`Authorization: Bearer <token>`).

| Endpoint | Filename | Scope path |
|----------|----------|------------|
| `GET /api/reporting/exports/assets.csv` | `assets-export.csv` | `asset.department` |
| `GET /api/reporting/exports/inspections.csv` | `inspections-export.csv` | `inspection.asset.department` |
| `GET /api/reporting/exports/issues.csv` | `issues-export.csv` | `issue.asset.department` |
| `GET /api/reporting/exports/work-orders.csv` | `work-orders-export.csv` | `workOrder.asset.department` |
| `GET /api/reporting/exports/preventive-candidates.csv` | `preventive-candidates-export.csv` | `candidate.asset.department` |

## Response

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename="<export-filename>"`
- UTF-8 body with a header row and comma-separated values
- Values containing commas, quotes, or newlines are double-quoted; embedded quotes are escaped as `""`
- Null fields appear as blank cells

## Authorization

| Role | Access |
|------|--------|
| Administrator | Organisation-wide (global) exports |
| Manager | Own department only |
| Operational Coordinator | Own department only |
| Field Employee | Forbidden (`403`) |
| Contractor | Forbidden (`403`) |

Department scoping matches Operations Intelligence KPIs (Sprint C1).

## Optional filters

Query parameters (epoch millis, consistent with other InfraTrack APIs):

| Parameter | Description |
|-----------|-------------|
| `from` | Lower bound (inclusive where applicable) |
| `to` | Upper bound |

Date field used per export:

| Export | Filter field | Notes |
|--------|--------------|-------|
| Assets | `createdAt` | Registration timestamp on the asset |
| Inspections | `createdAt` | Assignment/creation timestamp |
| Issues | `recordedAt` | Business datetime when the issue was recorded |
| Work Orders | `createdAt` | Work order creation timestamp |
| Preventive Candidates | `evaluatedAt` | When the candidate was evaluated |

If `from` / `to` are omitted, all rows within the user's scope are exported.

## CSV columns

### Assets

`Asset ID`, `Asset Name`, `Category`, `Department`, `Location`, `Status`, `Created At`

### Inspections

`Inspection ID`, `Asset Name`, `Department`, `Status`, `Priority`, `Assigned To`, `Expected Completion Date`, `Completed At`, `Template`, `Issue Identified`

### Issues

`Issue ID`, `Asset Name`, `Department`, `Issue Type`, `Severity`, `Description`, `Recorded By`, `Recorded At`, `Resolved`

`Resolved` is `true` when an Operational Decision exists for the issue.

### Work Orders

`Work Order ID`, `Asset Name`, `Department`, `Status`, `Priority`, `Assigned To`, `Description`, `Created At`, `Updated At`

### Preventive Execution Candidates

`Candidate ID`, `Plan Code`, `Asset Name`, `Department`, `Status`, `Target Action`, `Trigger Type`, `Evaluated At`, `Decision Date`, `Created Inspection ID`

## Known limitations

- CSV only in this sprint; PDF and `.xlsx` are deferred.
- No scheduled or email reports.
- No streaming; suitable for typical council operational volumes.
- Work order date filtering uses `createdAt`, not completion time.
- Preventive candidate date filtering uses `evaluatedAt`, not `createdAt`.

## Frontend

List pages expose an **Export CSV** button for Administrator, Manager, and Operational Coordinator. Field Employee and Contractor do not see the button; direct API calls return `403`.

## Related documentation

- [Operations Intelligence KPI API](v2-domain-engine-api.md) — shared authorization model
- [Mobile API](mobile-api.md) — separate compact field client surface
