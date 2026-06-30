# V2 Domain Engine API — Endpoint Groups

This document summarises major REST endpoint groups introduced in **Version 2.0.0** (Decision Engine and Preventive Maintenance Engine). It is **not** a substitute for Swagger.

**Authoritative interactive reference (development):**

- Swagger UI: http://localhost:4000/swagger-ui/index.html
- OpenAPI JSON: http://localhost:4000/v3/api-docs

All endpoints require JWT authentication unless noted. Pagination uses `page` (zero-based) and `size` (max 100) where applicable.

---

## Inspection Templates

Base path: `/api/inspection-templates`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/inspection-templates` | List templates (filters: category, status) |
| GET | `/api/inspection-templates/{id}` | Template detail |
| POST | `/api/inspection-templates` | Create template (Administrator) |
| PUT | `/api/inspection-templates/{id}` | Update metadata (Administrator, DRAFT) |
| POST | `/api/inspection-templates/{id}/archive` | Archive template (Administrator) |

---

## Template Questions

Base path: `/api/inspection-templates/{templateId}/questions`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `.../questions` | List questions |
| POST | `.../questions` | Create question (Administrator, DRAFT) |
| PUT | `.../questions/{questionId}` | Update question |
| POST | `.../questions/reorder` | Reorder questions |
| POST | `.../questions/{questionId}/deactivate` | Deactivate question |

Questions require a stable **business code** (uppercase snake_case, unique per template).

---

## Question Choices

Base path: `/api/inspection-templates/{templateId}/questions/{questionId}/choices`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `.../choices` | List choices for CHOICE questions |
| POST | `.../choices` | Create choice (DRAFT template) |
| PUT | `.../choices/{choiceId}` | Update choice |
| POST | `.../choices/{choiceId}/deactivate` | Deactivate choice |

---

## Decision Rules

Base path: `/api/inspection-templates/{templateId}/questions/{questionId}/rules`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `.../rules` | List rules for a question |
| POST | `.../rules` | Create rule (DRAFT template) |
| PUT | `.../rules/{ruleId}` | Update rule |
| POST | `.../rules/{ruleId}/deactivate` | Deactivate rule |

Rules are evaluated when a templated Inspection is completed with structured answers.

---

## Rule Evaluation Reports

Base path: `/api/inspections/{inspectionId}/rule-evaluation/reports`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `.../reports` | List evaluation reports |
| GET | `.../reports/latest` | Latest report with results |
| GET | `.../reports/{reportId}` | Report detail |

---

## Suggested Actions

### Per-inspection list

Base path: `/api/inspections/{inspectionId}/suggested-actions`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `.../suggested-actions` | List suggestions (optional status, actionType filters) |
| GET | `.../suggested-actions/{id}` | Suggestion detail |

### Decision Assistant

Base path: `/api/suggested-actions`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/suggested-actions/{id}` | Detail with explainability |
| POST | `/api/suggested-actions/{id}/approve` | Approve and create Issue (Manager) |
| POST | `/api/suggested-actions/{id}/reject` | Reject suggestion |
| POST | `/api/suggested-actions/{id}/dismiss` | Dismiss suggestion |

---

## Preventive Maintenance Plans

Base path: `/api/preventive-maintenance-plans`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/preventive-maintenance-plans` | List plans (filters: asset, status, trigger type) |
| GET | `/api/preventive-maintenance-plans/{id}` | Plan detail with trigger |
| POST | `/api/preventive-maintenance-plans` | Create plan (Administrator) |
| PUT | `/api/preventive-maintenance-plans/{id}` | Update plan (Administrator) |
| POST | `/api/preventive-maintenance-plans/{id}/archive` | Archive plan |
| GET | `/api/preventive-maintenance-plans/{id}/trigger-evaluation` | Evaluate trigger eligibility |
| POST | `/api/preventive-maintenance-plans/{id}/execution-candidate` | Generate candidate for one plan |

---

## Preventive Execution Candidates

Base path: `/api/preventive-execution-candidates`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/preventive-execution-candidates` | List candidates (filters: status, asset, plan) |
| GET | `/api/preventive-execution-candidates/{id}` | Candidate detail |
| POST | `/api/preventive-execution-candidates/generate` | Generate for all eligible ACTIVE plans |
| POST | `/api/preventive-execution-candidates/{id}/approve` | Approve; create Inspection (Manager) |
| POST | `/api/preventive-execution-candidates/{id}/reject` | Reject candidate |
| POST | `/api/preventive-execution-candidates/{id}/dismiss` | Dismiss candidate |
| GET | `/api/preventive-execution-candidates/{id}/report` | Audit report for candidate |

---

## Preventive Execution Reports

Base path: `/api/preventive-execution-reports`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/preventive-execution-reports` | List reports (filters: status, asset, plan, decision source) |
| GET | `/api/preventive-execution-reports/{id}` | Report detail |

Read-only. Reports are created automatically with candidates and updated on manager decisions.

---

## Preventive Scheduler

Base path: `/api/preventive-scheduler`

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/preventive-scheduler/status` | Whether scheduled execution is enabled |
| POST | `/api/preventive-scheduler/run` | Manual run (Administrator / Manager) |
| GET | `/api/preventive-scheduler/runs` | Run history |
| GET | `/api/preventive-scheduler/runs/{id}` | Run detail |

Scheduled execution is **disabled by default**. See [Deployment](../05-deployment/README.md#preventive-scheduler).

---

## Supporting reference data

| Base path | Purpose |
|-----------|---------|
| `/api/units-of-measure` | Units for NUMBER question validation |
| `/api/asset-categories` | Categories linked to templates |

---

## Authorization summary

| Role | Decision Engine | Preventive Engine |
|------|-----------------|-------------------|
| Administrator | Full template CRUD; view all; review suggestions and candidates globally | Full plan CRUD; generate candidates; run scheduler globally |
| Manager | View templates; review suggestions and candidates (own department) | View plans; review candidates; run scheduler (own department) |
| Operational Coordinator | View templates, suggestions, candidates, scheduler runs | View only |
| Field Employee / Contractor | No Domain Engine access | No access |

Field employees continue to complete Inspections through `/api/inspections` per V1.
