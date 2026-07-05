# Reporting API — CSV, XLSX, and PDF Exports (V2.2.x / V2.3.x)

Read-only CSV, XLSX, and PDF export endpoints for operational reporting. Reporting **observes** data only — it does not create, update, approve workflows, run the scheduler, or send notifications.

PDF exports are simple tabular reports (title, timestamp, optional date range, table). They are not branded report templates.

## Base path

All endpoints require JWT authentication (`Authorization: Bearer <token>`).

### CSV endpoints

| Endpoint | Filename | Scope path |
|----------|----------|------------|
| `GET /api/reporting/exports/assets.csv` | `assets-export.csv` | `asset.department` |
| `GET /api/reporting/exports/inspections.csv` | `inspections-export.csv` | `inspection.asset.department` |
| `GET /api/reporting/exports/issues.csv` | `issues-export.csv` | `issue.asset.department` |
| `GET /api/reporting/exports/work-orders.csv` | `work-orders-export.csv` | `workOrder.asset.department` |
| `GET /api/reporting/exports/preventive-candidates.csv` | `preventive-candidates-export.csv` | `candidate.asset.department` |

### XLSX endpoints (V2.3.x C1)

| Endpoint | Filename | Scope path |
|----------|----------|------------|
| `GET /api/reporting/exports/assets.xlsx` | `assets-export.xlsx` | `asset.department` |
| `GET /api/reporting/exports/inspections.xlsx` | `inspections-export.xlsx` | `inspection.asset.department` |
| `GET /api/reporting/exports/issues.xlsx` | `issues-export.xlsx` | `issue.asset.department` |
| `GET /api/reporting/exports/work-orders.xlsx` | `work-orders-export.xlsx` | `workOrder.asset.department` |
| `GET /api/reporting/exports/preventive-candidates.xlsx` | `preventive-candidates-export.xlsx` | `candidate.asset.department` |

### PDF endpoints (V2.3.x C2)

| Endpoint | Filename | Scope path |
|----------|----------|------------|
| `GET /api/reporting/exports/assets.pdf` | `assets-export.pdf` | `asset.department` |
| `GET /api/reporting/exports/inspections.pdf` | `inspections-export.pdf` | `inspection.asset.department` |
| `GET /api/reporting/exports/issues.pdf` | `issues-export.pdf` | `issue.asset.department` |
| `GET /api/reporting/exports/work-orders.pdf` | `work-orders-export.pdf` | `workOrder.asset.department` |
| `GET /api/reporting/exports/preventive-candidates.pdf` | `preventive-candidates-export.pdf` | `candidate.asset.department` |

CSV, XLSX, and PDF exports share the same authorization, filters, columns, and row data for a given request.

## Response

### CSV

- `Content-Type: text/csv;charset=UTF-8`
- `Content-Disposition: attachment; filename="<export-filename>"`
- UTF-8 body with a header row and comma-separated values
- Values containing commas, quotes, or newlines are double-quoted; embedded quotes are escaped as `""`
- Null fields appear as blank cells

### XLSX

- `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- `Content-Disposition: attachment; filename="<export-filename>"`
- Single-sheet workbook with bold header row, frozen top row, and auto-sized columns
- Null fields appear as blank cells
- Date and datetime values are written as readable ISO strings (same as CSV)

### PDF

- `Content-Type: application/pdf`
- `Content-Disposition: attachment; filename="<export-filename>"`
- Simple tabular layout: report title, server-generated timestamp, optional applied date range (`from` / `to`), column headers, and data rows
- Null fields appear as blank cells
- Wide tables may use landscape orientation, smaller font, or truncated cell text

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
| `from` | Lower bound (inclusive where applicable) — **required** |
| `to` | Upper bound — **required** |

Date field used per export:

| Export | Filter field | Notes |
|--------|--------------|-------|
| Assets | `createdAt` | Registration timestamp on the asset |
| Inspections | `createdAt` | Assignment/creation timestamp |
| Issues | `recordedAt` | Business datetime when the issue was recorded |
| Work Orders | `createdAt` | Work order creation timestamp |
| Preventive Candidates | `evaluatedAt` | When the candidate was evaluated |

Both `from` and `to` must be supplied on every export request. Unfiltered exports are not allowed.

### Export window limit (V2.3.x T4 / Security-2)

The export period cannot exceed **365 days** (inclusive calendar days in UTC). The `to` date must not be before `from`.

| Request | Result |
|---------|--------|
| `from=2026-01-01`, `to=2026-01-01` | Allowed (same day) |
| `from=2026-01-01`, `to=2026-12-31` | Allowed (365 days) |
| `from` or `to` omitted | Rejected (`400`) |
| `to` before `from` | Rejected (`400`) |
| `from=2023-01-01`, `to=2026-01-01` | Rejected (`400`) — exceeds 365 days |

Validation errors (plain text body):

```
Reporting exports require both from and to date filters.
```

```
Reporting export to date must not be before from date.
```

```
Reporting exports cannot span more than 365 days.
```

Applies equally to CSV, XLSX, and PDF exports.

## Export columns

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

- PDF exports are simple tabular reports — no logos, branding, charts, or custom templates.
- No scheduled or email reports.
- No streaming; suitable for typical council operational volumes.
- Work order date filtering uses `createdAt`, not completion time.
- Preventive candidate date filtering uses `evaluatedAt`, not `createdAt`.

## Frontend

List pages expose **Export CSV**, **Export XLSX**, and **Export PDF** buttons for Administrator, Manager, and Operational Coordinator. Field Employee and Contractor do not see the buttons; direct API calls return `403`.

## Related documentation

- [Operations Intelligence KPI API](v2-domain-engine-api.md) — shared authorization model
- [Mobile API](mobile-api.md) — separate compact field client surface
