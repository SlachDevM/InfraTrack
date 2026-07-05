# InfraTrack Domain Engine

## Purpose

The Domain Engine holds reusable operational knowledge that applies across assets of the same type. It separates **what should be inspected** from individual **Inspection** executions.

Knowledge belongs to the **Asset Category**, not to individual Assets.

### Related documentation

| Document | Purpose |
|----------|---------|
| [Business Glossary](../01-business-architecture/glossary.md) | Terminology for stakeholders |
| [ADR-003 — V2 domain-driven workflow](../03-architecture/adr-003-v2-domain-driven-workflow.md) | How V2 domains interact |
| [Platform Version History](../06-release-notes/platform-version-history.md) | Product versions |
| [ADR-004 — Platform versioning](../03-architecture/adr-004-platform-versioning-strategy.md) | Version numbering rules |
| [V2 Roadmap](../06-release-notes/v2-roadmap.md) | Planned versions |
| [Version 2.0.0 sprint report](../06-release-notes/v2-phase-a-b.md) | Historical delivery record |

## Version 2.0.0 — Current State

Version 2.0.0 delivers two complementary engines on top of the V1 operational workflow:

| Engine | Logical version | Purpose |
|--------|-----------------|--------|
| **Decision Engine** | 1.0 | Structured inspection knowledge, rule evaluation, suggested actions, and manager review |
| **Preventive Maintenance Engine** | 1.0 | Plans, trigger evaluation, execution candidates, manager decisions, audit reports, and controlled scheduling |
| **Controlled Scheduler** | 1.0 | Scheduled and manual execution-candidate discovery (disabled by default) |

Logical engine versions are independent of the platform semver — see [Platform Version History](../06-release-notes/platform-version-history.md).

### Human-in-the-loop principle

Both engines follow the same rule:

```text
The system proposes.
The Manager decides.
```

Rules produce **Suggested Actions**, not automatic Issues. The scheduler generates **Execution Candidates**, not automatic Inspections. Automation of outcomes remains intentionally out of scope.

### Organizational policy foundation (BDR-004)

As of V2.3 foundation work, **Inspection Visibility Policy** is the first concrete implementation of the BDR-004 principle: business rules remain stable; organizational policies are configurable.

**Notification Policy** is the second foundation. Operational services consult `NotificationPolicyService.getPolicy()` before sending UC-013 notifications.

**Dashboard Policy** is the third foundation. `DashboardPreferencesService` consults `DashboardPolicyService.getPolicy()` when no saved user preferences exist, and when preferences are reset. User-scoped saved preferences always take precedence over the organizational dashboard policy.

Precedence:

```text
User Preferences
↓
Organization Dashboard Policy
↓
System Defaults (DefaultDashboardPolicy)
```

No configurable dashboard policy modes, properties, or admin UI exist yet. `DefaultDashboardPolicy` reproduces the original fixed dashboard presentation exactly.

Future dashboard policy modes may include `OPERATIONAL`, `FIELD`, `MANAGEMENT`, and `EXECUTIVE` — documentation only; not implemented.

**Reporting Policy** is the fourth foundation. `ReportingExportService` consults `ReportingPolicyService.getPolicy()` for organizational reporting defaults (default export format, enabled export formats, default reporting date range). No user reporting preferences exist yet.

Precedence:

```text
User Preference (future)
↓
Organization Reporting Policy
↓
System Defaults (DefaultReportingPolicy)
```

Current defaults: CSV export format; CSV, XLSX, and PDF enabled; `LAST_30_DAYS` default reporting period for frontend export UI. Export REST endpoints **require** explicit `from` and `to` parameters (epoch millis); unfiltered exports are rejected with HTTP `400`. The 365-day maximum window is enforced in `ReportingExportService.validateExportDateWindow()`.

No configurable reporting policy modes, properties, or admin UI exist yet. `DefaultReportingPolicy` reproduces existing behaviour exactly.

Future reporting policy modes may include `OPERATIONAL`, `MANAGEMENT`, `EXECUTIVE`, `CSV_ONLY`, and `FULL_EXPORT` — documentation only; not implemented.

**Approval Policy** is the fifth foundation. Operational services consult `ApprovalPolicyService.getPolicy()` to determine whether approval steps are required (completion review, manager operational decisions, suggested action approval, preventive candidate approval). `DefaultApprovalPolicy` reproduces today's fixed approval behaviour exactly.

No configurable approval policy modes, properties, or admin UI exist yet.

Future approval policy modes may include `STANDARD`, `SIMPLIFIED`, and `STRICT` — documentation only; not implemented. Potential future capabilities include always require review, contractor-only review, automatic approval, dual approval, and threshold-based approval.

| Mode | Purpose |
|------|---------|
| `DEFAULT` | Reproduces the original fixed notification behaviour exactly (default) |
| `QUIET` | Reduces non-critical notifications: preserves direct-action notifications (assignments, completion review, rework decision); suppresses informational maintenance-completed notifications to coordinators |

Configuration:

```properties
app.policies.notification.mode=DEFAULT
```

Environment override: `APP_POLICIES_NOTIFICATION_MODE=QUIET`

No user-level notification preferences, database settings, or push/email delivery changes exist yet.

Visibility is configured by property:

```properties
app.policies.inspection.visibility=DEPARTMENT
```

Supported modes:

- `DEPARTMENT` (default)
- `ORGANIZATION` (small-council view mode; view-only)

### Phase A — Decision Engine (implemented)

| Capability | Status |
|------------|--------|
| Inspection Templates, Questions, Business Codes | Implemented |
| Question Choices, Value Model, Units of Measure | Implemented |
| Inspection Answers (structured completion) | Implemented |
| Decision Rules | Implemented |
| Rule Evaluation Engine and Reports | Implemented |
| Suggested Actions | Implemented |
| Decision Assistant (approve / reject / dismiss → Issue) | Implemented |
| Rework workflow, IssueType NORMAL/REWORK, CAPA, Lessons Learned | Implemented (completion review cross-cutting) |

High-level flow:

```text
Asset Category
  ↓
Inspection Template
  ↓
Questions
  ↓
Answers
  ↓
Rules
  ↓
Evaluation Report
  ↓
Suggested Actions
  ↓
Decision Assistant
```

Authoritative detail: sprint sections A2.x and A3.x below.

### Phase B — Preventive Maintenance Engine (implemented)

| Capability | Status |
|------------|--------|
| Preventive Maintenance Plans and Plan Business Triggers | Implemented |
| Trigger Definitions and Trigger Evaluation | Implemented |
| Execution Candidates | Implemented |
| Preventive Decision Assistant | Implemented |
| Preventive Execution Reports | Implemented |
| Controlled Preventive Scheduler | Implemented (disabled by default) |

High-level flow:

```text
Preventive Plan
  ↓
Trigger Definition
  ↓
Trigger Evaluation
  ↓
Execution Candidate
  ↓
Preventive Decision Assistant
  ↓
Inspection
```

Authoritative detail: sprint sections B1.x–B5 below.

### What is not automated yet

- Automatic Issue creation from matched rules (without manager approval)
- Automatic Inspection creation from scheduler or trigger evaluation
- Work Order or Maintenance Activity creation from preventive plans (`CREATE_WORK_ORDER`, `CREATE_MAINTENANCE` target actions)
- METER and EVENT trigger evaluation (deferred)
- KPI dashboards and analytics over execution reports
- Distributed scheduler locking for multi-instance deployments

See [Version 2.0.0 sprint report](../06-release-notes/v2-phase-a-b.md) for the validation checklist. Terminology: [Business Glossary](../01-business-architecture/glossary.md).

---

An **Inspection Template** defines the reusable business structure for future Inspections within an Asset Category.

Examples:

- Pump Inspection Template
- Motor Inspection Template
- Building Safety Inspection Template
- Electrical Panel Inspection Template

### Relationship (implemented)

```text
AssetCategory
    ↓
InspectionTemplate
    ↓
InspectionQuestions
    ↓
InspectionAnswers
    ↓
DecisionRules
    ↓
RuleEvaluationReport
    ↓
SuggestedActions
    ↓
DecisionAssistant
```

### Why Asset Category?

Assets share category-level maintenance and inspection knowledge. Attaching templates to Asset Categories ensures:

- one definition serves all assets of that type;
- updates propagate consistently when versioning is introduced;
- reporting and analytics can compare inspections against the same template baseline.

## Sprint A2.1 — Inspection Templates Foundation

Sprint A2.1 introduces **metadata-only** Inspection Templates as a standalone capability.

### Implemented in A2.1

- `InspectionTemplate` entity with name, description, asset category, version, and status
- Status lifecycle values: `DRAFT`, `PUBLISHED`, `ARCHIVED`
- Initial creation defaults: version `1`, status `DRAFT`
- Administrator create, update metadata, and archive (soft retire)
- Paginated list with filters by asset category and status
- Read-only access for Manager and Operational Coordinator
- Simple Inspection Templates management page in the frontend

### Not implemented in A2.1

- Checklist questions
- Inspection answers
- Integration with Inspection assignment or completion
- Publish workflow, version cloning, or automatic version publishing rules
- Decision rules or configurable workflows

The existing Inspection workflow is unchanged.

## Sprint A2.2 — Checklist Questions Foundation

Sprint A2.2 adds ordered **checklist questions** to Inspection Templates.

### Checklist question concept

Each question defines one item on a future inspection checklist. Questions belong to a single Inspection Template and are ordered by `displayOrder`.

Example for a Pump Inspection Template:

1. Is there any visible leak?
2. Is abnormal vibration present?
3. Is the pump temperature within normal range?

### Question types

| Type | Purpose |
|------|---------|
| `BOOLEAN` | Yes/no answer |
| `TEXT` | Free-text answer |
| `NUMBER` | Numeric measurement |
| `CHOICE` | Selection from predefined options (see Sprint A2.3.2) |
| `PHOTO` | Photo capture during inspection |

### DRAFT-only editing rule

Checklist questions can be **created, updated, deactivated, and reordered only while the template status is `DRAFT`**.

Published and archived templates remain viewable but cannot be mutated. This protects audit integrity until a future sprint introduces template version cloning.

### Implemented in A2.2

- `InspectionTemplateQuestion` entity with text, type, required flag, display order, and active flag
- Administrator create, update, deactivate, and bulk reorder on draft templates
- Read-only question list for Manager and Operational Coordinator
- Question management UI linked from Inspection Templates

### Not implemented in A2.2

- Inspection answer storage
- Checklist execution during Inspection completion
- Decision matrix or automatic issue creation
- Template publish/clone workflow
- Choice question options

The existing Inspection workflow is unchanged.

### Future relationship to Inspection Answers

When Inspection execution integrates with templates, each assigned Inspection will capture **Inspection Answers** aligned to the template's active questions at assignment time. Answers will reference the question definition for reporting and audit.

## Sprint A2.2.1 — Question Business Codes

Each checklist question owns a stable **business code** that survives display text changes.

### Business Code vs Display Text

| Property | Purpose |
|----------|---------|
| **Business Code** | Stable identifier for integrations (`VIBRATION`, `LEAK`, `SAFETY_GUARD_PRESENT`) |
| **Display Text** | Human-readable wording shown to inspectors; may evolve over time |

Display text must never become the business identifier.

### Code rules

- Required on creation
- Uppercase snake_case: `^[A-Z][A-Z0-9_]*$`
- Unique within an inspection template (duplicate returns HTTP 409)
- Immutable after creation
- Existing questions backfilled automatically from question text during migration

### Future consumers

Stable business codes prepare future integrations without changing current checklist behaviour:

- Android field inspection clients
- Decision Matrix rules
- Analytics and reporting
- AI-assisted knowledge retrieval
- Knowledge Base linking

## Sprint A2.3.1 — Inspection Answers Foundation

Inspections can now store structured **Inspection Answers** for basic checklist question types while preserving the existing UC-003/UC-004 completion fields.

### InspectionAnswer concept

Each answer belongs to one Inspection and one Inspection Template Question. Snapshots preserve the question code, text, and type at completion time so later template edits do not alter historical inspections.

### Snapshot strategy

When answers are recorded at completion, the system copies:

- `questionCodeSnapshot`
- `questionTextSnapshot`
- `questionTypeSnapshot`

These snapshots remain immutable even if the template question is later edited or deactivated.

### Transition mode: templates optional

- Existing inspections may have no template.
- New inspections may still be assigned without a template.
- When a **PUBLISHED** template matching the asset category is selected at assignment, structured answers become available at completion.
- Legacy free-text completion (`observedCondition`, `observations`, `issueIdentified`) remains unchanged.

### Supported answer types in A2.3.1

| Question type | Stored field |
|---------------|--------------|
| `BOOLEAN` | `booleanValue` |
| `TEXT` | `textValue` |
| `NUMBER` | `numberValue` |

### Not implemented in A2.3.1

- `PHOTO` capture and storage
- Automatic Issue creation from answers
- Decision Matrix rules
- Mandatory templates for all inspections

## Sprint V2.2.x — Progressive Inspection Completion

Assigned templated inspections can now persist structured answers **before** final completion. This supports field workflows where workers save partial progress, leave the site, and return later.

### Save vs complete

| | Save progress (draft) | Save answers (compat) | Final completion |
|--|-----------------------|----------------------|------------------|
| Endpoint | `PUT /api/inspections/{inspectionId}/progress` | `PUT /api/inspections/{inspectionId}/answers` | `POST /api/inspections/{inspectionId}/complete` |
| Persists | Summary fields + answers | Answers only | Summary fields + answers (final) |
| Inspection status | Remains `ASSIGNED` | Remains `ASSIGNED` | Becomes `COMPLETED` |
| Mandatory questions | Not required | Not required | Required |
| Upsert behaviour | Yes — partial payloads update only submitted fields/questions | Yes — partial payloads update only submitted questions | Merges any new answers with previously saved answers |
| Decision Engine | **Not executed** | **Not executed** | **Executed once** |
| Rule Evaluation Report | Not created | Not created | Created when applicable |
| Suggested Actions | Not created | Not created | Created when applicable |

### Side-effect guarantees

Progressive saves must never create Issues, Suggested Actions, Rule Evaluation Reports, Operational Decisions, Work Orders, or notifications. Only final completion may trigger those outcomes.

A draft inspection represents only the information explicitly entered by the field agent. Missing information is not considered invalid during draft persistence and is therefore neither replaced with placeholder values nor validated as final inspection data. Complete inspection validation occurs only during the inspection completion workflow.

Draft observation semantics on `PUT /progress`: omitted observations leave the stored value unchanged; empty or whitespace-only observations clear the stored value; non-empty observations are trimmed and saved. Checklist answer entries with no populated value are ignored (not persisted, not validated as errors).

### Authorization

The same inspection access rules apply: assigned Field Employee or Contractor, Administrator (support), and Manager / Operational Coordinator where existing view permissions allow. Completed inspections reject further saves with `409 Conflict`.

## Sprint V2.3.x — Server-Generated Workflow Audit Timestamps

Workflow endpoints no longer treat client-supplied audit timestamps as authoritative. The client submits the business action; the backend records when that action was processed.

| Workflow | Deprecated request fields (ignored) | Server-generated fields |
|----------|-------------------------------------|-------------------------|
| Inspection completion | `completedAt` | `Inspection.completedAt` |
| Maintenance completion | `completedAt` | `MaintenanceActivity.completedAt` |
| Completion review | `reviewedAt` | `CompletionReview.reviewedAt` |
| Suggested action approve/reject/dismiss | `recordedAt` on approve (issue path only) | `SuggestedAction.decidedAt`; issue `recordedAt` on approve |
| Preventive candidate approve/reject/dismiss | — | `PreventiveExecutionCandidate.decidedAt` |

User-entered business dates (`expectedCompletionDate`, preventive `plannedAt`, UC-005 issue `recordedAt` from field recording) are unchanged.

Implementation uses an injectable `WorkflowClock` (`java.time.Clock`) for deterministic tests. Workflow behaviour, status transitions, notifications, and Decision Engine timing are unchanged.

## Sprint A2.3.2 — Inspection Value Model

Template questions now define what constitutes a valid answer, not only what is asked.

### Value Model concept

A checklist question specifies both the prompt and the valid answer format. This supports consistent validation across web, Android, reporting, and future Decision Matrix rules.

### CHOICE values

`CHOICE` questions define allowed options through `InspectionTemplateQuestionChoice`:

- stable `code` (uppercase snake_case, unique per question);
- human-readable `label`;
- `displayOrder` and `active` flag.

Choices are editable only while the parent template is **DRAFT**. Inactive choices remain in the database for historical answers.

At completion, answers store:

- `choiceCodeValue`
- `choiceLabelSnapshot`

### NUMBER constraints

`NUMBER` questions may define:

| Field | Purpose |
|-------|---------|
| `unit` | Display unit (e.g. °C, bar) |
| `minValue` / `maxValue` | Allowed numeric range |
| `decimalPlaces` | Maximum decimal precision (0–6) |

Constraints apply only to `NUMBER` questions. At answer time the system validates range and precision, and snapshots `numberUnitSnapshot`, `numberMinSnapshot`, `numberMaxSnapshot`, and `decimalPlacesSnapshot`.

### Historical snapshot strategy

Answer snapshots preserve question text, type, choice labels, and number constraints as they existed at completion. Later template edits do not alter completed inspections.

### Future Decision Matrix relationship

The Value Model prepares structured, validated answers for Decision Rule evaluation (A3.1+). A2.3.2 does not implement rules or automatic Issue creation.

### Supported answer types after A2.3.2

| Question type | Stored field(s) |
|---------------|-----------------|
| `BOOLEAN` | `booleanValue` |
| `TEXT` | `textValue` |
| `NUMBER` | `numberValue` + constraint snapshots |
| `CHOICE` | `choiceCodeValue` + `choiceLabelSnapshot` |

`PHOTO` remains deferred.

## Sprint A2.3.3 — Inspection Value Model Finalization

The Value Model is finalized with normalized units of measure and stronger answer snapshots before Decision Matrix work begins.

### Unit of Measure concept

`UnitOfMeasure` is reference data for NUMBER checklist questions. Each unit has a stable `code`, display `symbol`, human-readable `name`, and `quantityType` (TEMPERATURE, PRESSURE, ROTATION, etc.).

Free-text units are avoided because they break analytics consistency, cross-template reporting, and future Decision Matrix comparisons.

### Answer snapshot strategy (finalized)

When a NUMBER answer is recorded, the system snapshots:

- `unitCodeSnapshot`, `unitSymbolSnapshot`, `unitNameSnapshot`
- `numberMinSnapshot`, `numberMaxSnapshot`, `decimalPlacesSnapshot`
- `questionVersionSnapshot` (inspection template version at completion time)

Legacy `numberUnitSnapshot` is retained for backward compatibility. API responses prefer structured unit snapshots when present.

Historical answers remain readable even if a unit is renamed or deactivated later.

### Question version snapshot

`questionVersionSnapshot` stores the parent Inspection Template version at answer time. This prepares future per-question versioning and Decision Matrix evaluation without changing the current completion workflow.

### Why this matters for analytics and Decision Matrix

Normalized units and versioned snapshots ensure comparable numeric readings, auditable historical answers after template changes, and reliable inputs for future Decision Matrix rules. Decision Matrix rules are **not** implemented in A2.3.3.

## Sprint A3.1 — Decision Rule Engine Foundation

Sprint A3.1 introduces **Decision Rules** attached to Inspection Template Questions. A Decision Rule describes a condition on a question answer and the intended action when that condition is met (realised as Suggested Actions in A3.4).

Example:

```text
Question: TEMPERATURE
Condition: greater than 90
Future action: suggest HIGH severity Issue
```

### Decision Rule concept

Each `InspectionTemplateQuestionRule` belongs to one checklist question and stores:

- a stable business `ruleCode` (uppercase snake_case, unique per question, immutable after creation);
- human-readable `ruleName` and optional `description`;
- a `conditionType` matching the parent question type;
- an `operator` compatible with that condition type;
- an optional `comparisonValue` stored as text;
- an `actionType` describing the intended future outcome;
- an optional `actionPayload` stored as JSON text;
- an `active` flag for soft deactivation.

Rules follow the same template audit protection as checklist questions and choices: rules are editable only when the parent Inspection Template is `DRAFT`. Published and archived templates expose rules as read-only.

### Condition types

| Condition type | Meaning |
|----------------|---------|
| `BOOLEAN` | Yes/no question condition |
| `NUMBER` | Numeric measurement condition |
| `CHOICE` | Selected choice code condition |
| `TEXT` | Free-text answer condition |

The condition type must match the parent question type.

### Operators

| Operator | Supported condition types |
|----------|---------------------------|
| `IS_TRUE`, `IS_FALSE` | `BOOLEAN` |
| `GREATER_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN`, `LESS_THAN_OR_EQUAL`, `EQUALS`, `NOT_EQUALS` | `NUMBER` |
| `EQUALS`, `NOT_EQUALS` | `CHOICE` |
| `EQUALS`, `NOT_EQUALS`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH` | `TEXT` |

### Comparison value rules

- `BOOLEAN`: no comparison value required.
- `NUMBER`: comparison value must be numeric text (for example `"90"`).
- `CHOICE`: comparison value must reference an active choice code on the same question.
- `TEXT`: comparison value must be non-blank.

### Action types

| Action type | Purpose (future execution) |
|-------------|----------------------------|
| `SUGGEST_ISSUE` | Propose creating an Issue |
| `SUGGEST_SEVERITY` | Propose an Issue severity |
| `SUGGEST_OPERATIONAL_DECISION` | Propose an Operational Decision |
| `FLAG_FOR_REVIEW` | Flag the inspection outcome for review |

`actionPayload` holds structured JSON metadata for the future action (for example severity or message text). A3.1 validates JSON syntax only; it does not interpret payload contents.

### A3.1 scope limitation

**A3.1 stores rule definitions only.**

Rule definitions are persisted without evaluation at inspection completion time.

## Sprint A3.2 — Decision Rule Evaluation Engine

Sprint A3.2 evaluates stored Decision Rules against captured Inspection Answers.

### Rule priority

Each rule has a required positive integer `priority` (default `100`). Lower numbers indicate higher priority. Rules are evaluated in ascending priority order, then by `ruleCode` ascending, so output is deterministic.

Examples:

- `10` — critical rule
- `50` — warning rule
- `100` — default rule

### Disabled reason

When a rule is deactivated, an optional `disabledReason` may be recorded (for example `Replaced by HIGH_TEMPERATURE_V2.`). This is informational only and does not affect evaluation because inactive rules are excluded.

### Evaluation engine

`DecisionRuleEvaluationService` compares one Inspection Answer against the active rules for that question and returns in-memory `DecisionRuleEvaluationResult` objects containing:

- rule identity and metadata (`ruleId`, `ruleCode`, `ruleName`, `priority`);
- condition details (`conditionType`, `operator`, `comparisonValue`, `actualValue`);
- intended action (`actionType`, `actionPayload`);
- `matched` — whether the answer satisfies the rule condition.

Supported operators by condition type match the A3.1 definition. TEXT comparisons are case-insensitive. NUMBER comparisons use `BigDecimal`.

Inactive rules, rules for other questions, mismatched condition types, and missing answer values produce no match. The evaluator does not throw for safe non-match cases.

### Debug API

`GET /api/inspections/{inspectionId}/rule-evaluation` returns evaluation results for all answers on an inspection. Results are not persisted and no workflow side effects occur.

### A3.2 scope limitation

**A3.2 evaluates rules only.**

Results are returned in memory. They are **not persisted**, and no Issues, Operational Decisions, notifications, or review flags are created. Inspection completion behaviour is unchanged.

### Sprint A3.2.1 — Rule Evaluation Technical Finalization

Sprint A3.2.1 finalizes the evaluation engine before persisted reports in A3.3.

- `RuleEvaluationContext` carries inspection, asset, department, template, question, and answer for future context-aware rules; current logic remains answer-based.
- `DecisionRuleEvaluationResult` includes `evaluatedAt` and `evaluationDurationMs` metadata per answer evaluation batch.
- Rules for an inspection are loaded in a single batch query; evaluation runs in memory with no per-rule database access.

### Future execution roadmap

Later sprints will:

- surface persisted evaluation results as **Suggested Actions**;
- generate Issues, severities, or Operational Decisions from matching rules;
- integrate rule outcomes with operational workflows and analytics.

### Sprint A3.3 — Persisted Rule Evaluation Reports

Sprint A3.3 persists decision rule evaluation results when a templated Inspection is completed with structured answers.

#### Rule Evaluation Report

A **Rule Evaluation Report** records one evaluation run for an Inspection:

- which rules existed at evaluation time;
- which rules matched;
- compared values (actual vs comparison);
- configured future action types and payloads;
- when evaluation happened;
- which Rule Engine version was used (`A3.3-1.0`).

An Inspection may have **multiple reports** over time (for example, if re-evaluation is added later). The latest report is retrieved via `GET /api/inspections/{inspectionId}/rule-evaluation/reports/latest`.

#### Snapshot rationale

Each **Rule Evaluation Result** stores snapshots of the rule definition (`ruleCodeSnapshot`, `ruleNameSnapshot`, `actionTypeSnapshot`, `prioritySnapshot`, etc.). Historical reports must not depend on live rule records, which may change or be deactivated after evaluation.

#### A3.3 scope limitation

**A3.3 persists evaluation and enables A3.4 suggestion generation.**

Reports are created as part of the Inspection completion transaction. Issues and Operational Decisions are not created automatically. Inspection completion behaviour (including existing `issueIdentified` handling) is unchanged. Suggested Actions are generated in A3.4 from matched results.

When a templated Inspection is completed with structured answers but no active rules exist, a report is still created with `resultCount = 0` for audit clarity. Legacy inspections without a template do not receive a report.

#### Retrieval API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/inspections/{inspectionId}/rule-evaluation/reports` | List report summaries |
| `GET /api/inspections/{inspectionId}/rule-evaluation/reports/latest` | Latest report with results |
| `GET /api/inspections/{inspectionId}/rule-evaluation/reports/{reportId}` | Report detail |

Access requires permission to view the Inspection; cross-department access is rejected.

#### Relationship to Suggested Actions

Persisted reports in A3.3 form the audit trail that A3.4 uses to generate **Suggested Actions** (implemented).

### Sprint A3.4 — Suggested Actions

Sprint A3.4 generates read-only **Suggested Actions** from matched Rule Evaluation Results.

#### Suggested Action concept

A **Suggested Action** is a recommendation produced when a Decision Rule matches during inspection completion. It surfaces what the Rule Engine would suggest — for example, a suggested Issue with severity and message — without creating business records.

Rule evaluation records **what happened**. Suggested actions record **what the system recommends**.

#### Report enhancements

`RuleEvaluationReport` now includes:

- `templateVersionSnapshot` — Inspection Template version at evaluation time;
- `evaluationStatus` — `SUCCESS`, `PARTIAL`, or `FAILED` (normal completion uses `SUCCESS`).

#### A3.4 scope limitation

**A3.4 generates suggestions only.**

Suggested Actions are created in the same transaction as the evaluation report when matched results exist. Initial status is `PENDING`. Manager review is provided by the Decision Assistant (A3.5).

One matched rule result produces one Suggested Action. Action payload JSON is interpreted tolerantly (`title`, `message`, `severity`); unknown fields are ignored and missing fields use readable fallbacks.

#### Retrieval API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/inspections/{inspectionId}/suggested-actions` | List suggestions (optional `status`, `actionType` filters) |
| `GET /api/inspections/{inspectionId}/suggested-actions/{suggestedActionId}` | Suggestion detail |

#### Later sprints

- **A3.6** — optional automation (execute accepted suggestions without manager step);
- template publish/clone workflow enhancements;
- analytics dashboards over template-aligned inspection data.

### Sprint A3.5 — Decision Assistant

Sprint A3.5 transforms **Suggested Actions** into a **Decision Assistant** for Managers.

#### Decision Assistant concept

The Rule Engine **proposes**; the Manager **decides**. Managers can review suggestions, read explainability (why panel), see confidence, and choose to approve, reject, or dismiss. Approval may create an Issue manually — never automatically.

Flow: Suggested Action → Decision Assistant → Manager Decision → (optional) Issue creation.

#### Explainability

Each suggestion exposes a **Why panel** built entirely from persisted Rule Evaluation Result snapshots (matched rule, condition, actual value, configured action). No re-evaluation occurs.

#### Confidence

`SuggestionConfidence` (`LOW`, `MEDIUM`, `HIGH`, `VERY_HIGH`) is calculated deterministically from `matchedRuleCount` at generation time. No AI.

#### Human validation

Managers may:

- **Approve** — edit Issue fields and create an Issue via existing `IssueService` (links to `SuggestedAction` and `RuleEvaluationReport`);
- **Reject** — business-oriented refusal with optional reason;
- **Dismiss** — informational dismissal with optional comment.

Only `PENDING` suggestions can be reviewed. Asset History records `SUGGESTED_ACTION_APPROVED` when an Issue is created from approval.

#### A3.5 scope limitation

**A3.5 adds human validation only.**

No automatic Issues, Operational Decisions, notifications, or workflow execution. Existing field-employee `issueIdentified` flow is unchanged.

#### API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/suggested-actions/{id}` | Detail with explanation and report context |
| `POST /api/suggested-actions/{id}/approve` | Approve and create Issue |
| `POST /api/suggested-actions/{id}/reject` | Reject suggestion |
| `POST /api/suggested-actions/{id}/dismiss` | Dismiss suggestion |

#### Difference between suggestion and automation

Suggestions in A3.4–A3.5 are **decision support**. Automation (A3.6) would execute accepted outcomes — out of scope until then.

## Sprint B1.1 — Preventive Maintenance Domain

Sprint B1.1 introduces the **Preventive Maintenance Plan** domain model. A plan defines **when** something should eventually happen and **what** should eventually happen. Execution is explicitly out of scope.

### Preventive Maintenance Plan

A **Preventive Maintenance Plan** belongs to a single **Asset**. One Asset may have many plans.

Each plan includes:

- name and description;
- status (`DRAFT`, `ACTIVE`, `PAUSED`, `ARCHIVED`);
- priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`);
- exactly one **target action** (`CREATE_INSPECTION`, `CREATE_WORK_ORDER`, `CREATE_MAINTENANCE`);
- optional **Inspection Template** reference (for inspection-oriented plans).

Only `ACTIVE` plans may generate execution candidates (B2+) or be evaluated by the scheduler (B5). Archived plans remain visible but cannot be modified.

### Business Trigger (plan configuration)

Each plan has exactly one **Business Trigger** configuration record (1:1). This is stored in `preventive_plan_business_triggers` and is distinct from the V1 operational `business_triggers` table used for UC-006 workflow events.

Fields:

- `triggerType` — `TIME`, `METER`, or `EVENT`;
- `configurationJson` — valid JSON only (interpretation deferred);
- `active` — whether the trigger configuration is enabled.

Example configurations:

| Type | Example JSON |
|------|----------------|
| TIME | `{"every":1,"unit":"MONTH"}` |
| METER | `{"meter":"OPERATING_HOURS","every":250}` |
| EVENT | `{"event":"COMPLETION_REVIEW"}` |

The Business Trigger model is intentionally generic. METER and EVENT evaluation remain deferred; TIME evaluation is implemented in B1.3.

### Target actions

Every plan defines exactly one target action. Only `CREATE_INSPECTION` approval is supported in B3; other target actions return *Target action not supported yet.*

### Execution pipeline (B2–B5, implemented)

The preventive **execution pipeline** (distinct from V1 operational `business_triggers`):

- **B1.3** — evaluates TIME triggers for eligibility;
- **B2** — generates PENDING execution candidates;
- **B3** — manager approve / reject / dismiss; optional Inspection on approval;
- **B4** — one audit report per candidate;
- **B5** — controlled scheduler (candidates only; disabled by default).

B1.1 alone does not schedule, evaluate triggers, or create workflow records.

### Sprint B1.2 — Trigger Definition

Sprint B1.2 finalizes the **Trigger Definition** for Preventive Maintenance Plans.

#### Trigger Definition concept

A Trigger Definition answers: **under which business condition should this plan become eligible for execution?** Execution remains out of scope.

Trigger configurations are **business-validated** before any execution engine exists so that stored plans are future-proof and cannot enter the system with ambiguous or unsupported definitions.

#### Plan identity

Each plan now includes:

- **planCode** — required, uppercase snake_case, unique, immutable after creation (for example `PUMP_MONTHLY`, `COMPRESSOR_500H`);
- **version** — positive integer, defaults to `1` on create; no publish workflow, cloning, or version history yet.

#### Trigger validation

`TriggerDefinitionValidator` replaces generic JSON-only checks:

| Type | Configuration | Rules |
|------|---------------|-------|
| TIME | `{"every":1,"unit":"MONTH"}` | `every` > 0; `unit` ∈ `DAY`, `WEEK`, `MONTH`, `YEAR` |
| METER | `{"meter":"OPERATING_HOURS","every":250}` | `meter` required; `every` > 0 |
| EVENT | `{"event":"COMPLETION_REVIEW"}` | `event` ∈ `COMPLETION_REVIEW`, `INSPECTION_COMPLETED`, `WORK_ORDER_COMPLETED`, `MAINTENANCE_COMPLETED` |

No dates, cron expressions, meter readings, or event evaluation.

#### Trigger summary

`TriggerDefinitionSummaryBuilder` generates a human-readable **triggerSummary** at read time (never stored). Examples:

- `Every month`
- `Every 250 operating hours`
- `After Completion Review`

The frontend uses structured trigger forms; JSON is generated internally for the API.

#### B1.2 scope limitation

**B1.2 validates trigger definitions only.**

No trigger evaluation, scheduling, Inspection or Work Order creation, reports, or notifications.

### Sprint B1.3 — Trigger Evaluation Engine

Sprint B1.3 introduces an in-memory **Trigger Evaluation Engine** that determines whether a plan is currently eligible for execution.

#### Trigger Evaluation concept

The engine answers **"Today, is this plan eligible?"** — never **"What should we execute?"**

#### Trigger Evaluation Context

`TriggerEvaluationContext` carries the plan, asset, business trigger, `currentDateTime`, and optional future meter values and business events. Only `currentDateTime` is used in B1.3.

#### Trigger Definition abstraction

`TriggerDefinition` is implemented by `TimeTriggerDefinition`, `MeterTriggerDefinition`, and `EventTriggerDefinition`. Each implementation parses configuration, builds a `TriggerSummary`, and evaluates eligibility without switch statements spread across the codebase.

#### Trigger Evaluation Result

`TriggerEvaluationResultResponse` includes `planId`, `planCode`, `triggerType`, `eligible`, `evaluationReason`, `evaluatedAt`, and `evaluationDurationMs`. Results are **not persisted**.

#### Evaluation rules (B1.3)

| Trigger | B1.3 behaviour |
|---------|----------------|
| TIME | Eligible when full intervals (day/week/month/year) have elapsed since plan creation |
| METER | Always not eligible — `Meter values are not available yet.` |
| EVENT | Always not eligible — `Business event evaluation is not implemented yet.` |

Plan status handling:

- `ACTIVE` — trigger evaluated
- `DRAFT` — not eligible (`Plan is still in draft.`)
- `PAUSED` / `ARCHIVED` — skipped in bulk evaluation; single-plan evaluation returns not eligible with status reason

#### Trigger Summary (structured)

`TriggerSummary` replaces the plain string with:

- `title` — e.g. `Every month`
- `description` — e.g. `Eligible once every full month from plan creation.`
- `triggerType`

Used by plan responses, evaluation API, and the frontend.

#### B1.3 API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/preventive-maintenance-plans/evaluation` | Evaluate all active plans in memory |
| `GET /api/preventive-maintenance-plans/{id}/evaluation` | Evaluate one plan in memory |

#### B1.3 scope limitation

**B1.3 evaluates eligibility only.**

No scheduling, polling, background jobs, database writes, Inspections, Work Orders, Maintenance Activities, or notifications.

### Sprint B2 — Execution Candidate Engine

Sprint B2 transforms eligible preventive maintenance plans into reviewable **Execution Candidates**.

#### Execution Candidate concept

Trigger Evaluation answers: **"Is this plan eligible now?"**

The Execution Candidate Engine answers: **"This plan is eligible and should be reviewed for execution."**

Candidates are persisted for manager review. No workflow execution occurs in B2.

#### nextEligibleAt

`TriggerEvaluationResultResponse` now includes `nextEligibleAt`:

| Trigger | Behaviour |
|---------|-----------|
| TIME, not eligible | Timestamp when the next full interval from plan creation will be reached |
| TIME, eligible | `null` |
| METER / EVENT | `null` (deferred) |

#### Preventive Execution Candidate

`PreventiveExecutionCandidate` stores:

- plan and asset references;
- `candidateStatus` (`PENDING`, `APPROVED`, `REJECTED`, `DISMISSED`, `EXECUTED`);
- trigger type, eligibility reason, `evaluatedAt`, `nextEligibleAt`;
- snapshot fields (`planCodeSnapshot`, `planVersionSnapshot`, `planNameSnapshot`, `targetActionSnapshot`, trigger summary snapshots).

B2 introduced **PENDING** candidate generation only. Approve, reject, and dismiss were added in B3.

#### Duplicate prevention

For a given plan, if a **PENDING** candidate already exists, generation is skipped and the existing candidate is returned. A partial unique index enforces one pending candidate per plan at the database level.

#### Candidate generation

`PreventiveExecutionCandidateService`:

- evaluates **ACTIVE** plans;
- creates a **PENDING** candidate when eligible;
- creates nothing when not eligible;
- no scheduler, notifications, or workflow side effects.

#### B2 API

| Endpoint | Purpose |
|----------|---------|
| `POST /api/preventive-execution-candidates/generate` | Generate candidates for all active eligible plans |
| `POST /api/preventive-maintenance-plans/{planId}/execution-candidate` | Generate candidate for one plan |
| `GET /api/preventive-execution-candidates` | List candidates (filters: status, asset, plan) |
| `GET /api/preventive-execution-candidates/{id}` | Candidate detail |

#### B2 authorization

| Role | Access |
|------|--------|
| Administrator | Generate and view |
| Manager | Generate and view |
| Operational Coordinator | View only |
| Field Employee / Contractor | No access |

#### Relationship to Preventive Decision Assistant

Execution Candidates form the review queue for the **Preventive Decision Assistant** (B3, implemented) — analogous to Suggested Actions → Decision Assistant in Phase A.

#### B2 scope limitation

**B2 generates review candidates only.**

No Inspections, Work Orders, Maintenance Activities, notifications, scheduler, or background jobs.

### Sprint B3 — Preventive Decision Assistant

Sprint B3 allows Managers and Administrators to review **Execution Candidates** and decide what happens next.

#### Preventive Decision Assistant concept

Flow:

```text
Preventive Maintenance Plan → Trigger Evaluation → Execution Candidate
→ Preventive Decision Assistant → Manager Decision → Optional Inspection creation
```

The system proposes; the Manager decides. Automation remains intentionally absent.

#### Candidate lifecycle (B3)

Allowed transitions:

```text
PENDING → APPROVED
PENDING → REJECTED
PENDING → DISMISSED
```

`EXECUTED` remains reserved for future automation.

#### Approval behaviour

When a Manager approves a `CREATE_INSPECTION` candidate:

- an Inspection is created through the existing Inspection workflow;
- the candidate becomes `APPROVED` with `createdInspectionId`;
- the Inspection references the `PreventiveExecutionCandidate`;
- plan snapshots on the candidate are preserved;
- Asset History records `PREVENTIVE_INSPECTION_CREATED`;
- existing Inspection assignment notifications are preserved.

`CREATE_WORK_ORDER` and `CREATE_MAINTENANCE` return *Target action not supported yet.*

#### Reject and dismiss

Reject and dismiss update candidate status and decision metadata only. No Inspection, Work Order, Maintenance Activity, or Asset History event is created.

#### B3 API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/preventive-execution-candidates/{id}` | Candidate detail |
| `POST /api/preventive-execution-candidates/{id}/approve` | Approve and create Inspection |
| `POST /api/preventive-execution-candidates/{id}/reject` | Reject candidate |
| `POST /api/preventive-execution-candidates/{id}/dismiss` | Dismiss candidate |

#### B3 authorization

| Role | Access |
|------|--------|
| Administrator | Approve, reject, dismiss, view (any department) |
| Manager | Approve, reject, dismiss, view (own department assets) |
| Operational Coordinator | View only |
| Field Employee / Contractor | No access |

#### B3 scope limitation

**B3 adds manual decision-making only.**

No scheduler, background jobs, automatic candidate generation, Work Orders, Maintenance Activities, or automation beyond optional Inspection creation on approval.

### Sprint B4 — Preventive Execution Audit & Reports

Sprint B4 adds a persistent audit layer for the preventive candidate lifecycle.

#### Preventive Execution Report concept

Each `PreventiveExecutionCandidate` has exactly one `PreventiveExecutionReport`. The report captures snapshot fields and lifecycle timestamps for traceability, reporting, and future KPIs.

Lifecycle:

```text
Generated → Reviewed → Approved / Rejected / Dismissed → Inspection Created (if approved)
```

#### Report status

| Status | Meaning |
|--------|---------|
| `GENERATED` | Report created when candidate is generated |
| `UNDER_REVIEW` | Reserved (optional in B4) |
| `APPROVED` | Manager approved candidate (transitional) |
| `REJECTED` | Manager rejected candidate |
| `DISMISSED` | Manager dismissed candidate |
| `INSPECTION_CREATED` | Inspection created after approval |

#### Decision source

Reports created from preventive candidates use `PREVENTIVE_ENGINE`. Manual manager decisions are recorded via `decidedByUserId`. Other sources (`MANUAL`, `RULE_ENGINE`, `SYSTEM`, `EXTERNAL_API`) are reserved for future use.

#### Audit purpose

B4 makes the preventive decision process fully auditable without introducing automation. Reports are read-only through the API. One report per candidate; no event-sourcing table or KPI dashboards in B4.

#### Asset History (B4)

Concise lifecycle events:

| Event | When |
|-------|------|
| `PREVENTIVE_CANDIDATE_GENERATED` | Candidate created |
| `PREVENTIVE_CANDIDATE_APPROVED` | Candidate approved |
| `PREVENTIVE_CANDIDATE_REJECTED` | Candidate rejected |
| `PREVENTIVE_CANDIDATE_DISMISSED` | Candidate dismissed |
| `PREVENTIVE_INSPECTION_CREATED` | Inspection created on approval (existing) |

#### B4 API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/preventive-execution-candidates/{candidateId}/report` | Report for one candidate |
| `GET /api/preventive-execution-reports` | List reports (filters: status, asset, plan, decision source) |
| `GET /api/preventive-execution-reports/{reportId}` | Report detail |

#### B4 authorization

Same as candidate view: Administrator, Manager (own department), Operational Coordinator can view; Field Employee and Contractor forbidden.

#### Future KPI use cases

Execution reports provide the foundation for future preventive maintenance KPIs (approval rates, time-to-decision, inspection creation latency) without implementing analytics in B4.

#### B4 scope limitation

**B4 adds audit and reporting only.**

No automatic Inspection creation, new notifications, or KPI dashboards in B4.

### Sprint B5 — Controlled Preventive Scheduler

Sprint B5 introduces a controlled scheduler for preventive candidate discovery.

#### Controlled Preventive Scheduler concept

```text
Scheduler → Evaluate ACTIVE plans → Generate PENDING candidates
```

The scheduler **proposes**; the Manager **decides**. The scheduler never approves candidates or creates Inspections, Work Orders, Maintenance Activities, or notifications.

#### Disabled-by-default policy

```properties
app.preventive.scheduler.enabled=false
app.preventive.scheduler.cron=0 0 6 * * *
```

Scheduled execution is disabled by default. Manual run remains available to Administrators and Managers when policy allows.

#### Manual vs scheduled runs

| Trigger | `triggeredBy` | User ID | Scope |
|---------|---------------|---------|-------|
| Manual (Admin) | `MANUAL` | Yes | All departments |
| Manual (Manager) | `MANUAL` | Yes | Own department assets |
| Scheduled | `SCHEDULED` | No | All departments (when enabled) |

Each run creates a `PreventiveSchedulerRun` report with counts and status (`SUCCESS`, `PARTIAL`, `FAILED`).

#### Candidate-only rule

The scheduler reuses `PreventiveExecutionCandidateService` and existing duplicate prevention. Outcomes: created, skipped duplicate, not eligible, failed (per plan).

#### Multi-instance note

B5 does not implement distributed locking. Multi-instance deployments may require a database lock or ShedLock in a future sprint to prevent concurrent scheduled runs.

#### B5 API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/preventive-scheduler/status` | Scheduler enabled flag |
| `POST /api/preventive-scheduler/run` | Manual scheduler run |
| `GET /api/preventive-scheduler/runs` | Run history |
| `GET /api/preventive-scheduler/runs/{id}` | Run detail |

#### B5 authorization

| Role | Access |
|------|--------|
| Administrator | Manual run (global), view runs |
| Manager | Manual run (own department), view runs |
| Operational Coordinator | View runs only |
| Field Employee / Contractor | No access |

#### B5 scope limitation

**B5 automates candidate discovery only.**

No automatic approval, Inspection creation, Work Orders, Maintenance Activities, scheduler notifications, or cron editing from the UI.

### B1.1 API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/preventive-maintenance-plans` | Paginated list (filters: asset, status, trigger type) |
| `GET /api/preventive-maintenance-plans/{id}` | Plan detail with business trigger |
| `POST /api/preventive-maintenance-plans` | Create plan with trigger (Administrator) |
| `PUT /api/preventive-maintenance-plans/{id}` | Update plan and trigger (Administrator) |
| `POST /api/preventive-maintenance-plans/{id}/archive` | Archive plan (Administrator) |

### B1.1 authorization

| Role | Access |
|------|--------|
| Administrator | Full CRUD and archive |
| Manager | View only |
| Operational Coordinator | View only |
| Field Employee | No access |
| Contractor | No access |

## Future work

Planned extensions beyond the Version 2.0.0 baseline:

- **A3.6** — optional automation of accepted suggested actions;
- METER and EVENT trigger evaluation for preventive plans;
- `CREATE_WORK_ORDER` and `CREATE_MAINTENANCE` preventive target actions;
- template publish/clone workflow and per-question versioning;
- preventive and decision-engine KPI dashboards;
- distributed scheduler locking (ShedLock or equivalent) for multi-instance deployments;
- native Android field client consuming the same REST API.

## Operations Intelligence (Version 2.1.0 — Sprint C1)

Version 2.1.0 introduces the **Operations Intelligence KPI Engine** — a **read-only** aggregation layer over existing operational data.

### Responsibility

The KPI Engine aggregates counts and breakdowns from Assets, Inspections, Issues, Work Orders, Preventive Maintenance, and the Decision Engine. It does **not** create, update, approve, reject, execute, or notify.

### API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/operations-intelligence/kpis` | Structured operational KPI aggregates |
| `GET /api/operations-intelligence/trends` | Time-series trend aggregates (Sprint C3) |
| `GET /api/operations-intelligence/recent-activity` | Recent operational activity feed (Sprint C4) |

### Authorization

| Role | Scope |
|------|--------|
| Administrator | Organisation-wide KPIs |
| Manager | Own department only |
| Operational Coordinator | Own department only |
| Field Employee | Forbidden |
| Contractor | Forbidden |

### Deferred to later sprints

- Advanced analytics and forecasting remain deferred
- Scheduled and email reports remain deferred
- The same KPI and trend APIs are consumed by the React dashboard, the future Android application, and reporting exports

### Sprint R1 — CSV Reporting Foundation (V2.2.x)

Version 2.2.x introduces **read-only CSV exports** for operational review and spreadsheet analysis.

| Endpoint | Purpose |
|----------|---------|
| `GET /api/reporting/exports/assets.csv` | Asset register export |
| `GET /api/reporting/exports/inspections.csv` | Inspection list export |
| `GET /api/reporting/exports/issues.csv` | Issue register export |
| `GET /api/reporting/exports/work-orders.csv` | Work order list export |
| `GET /api/reporting/exports/preventive-candidates.csv` | Preventive execution candidate export |

Optional query parameters: `from` and `to` (epoch millis) where a reliable business or technical timestamp exists on the entity (see [reporting-api.md](../04-api/reporting-api.md)).

Authorization matches Sprint C1 (Administrator global; Manager and Operational Coordinator own department; Field Employee and Contractor forbidden).

Reporting **never** mutates operational data, runs the scheduler, or sends notifications.

The React web client adds an **Export** dropdown menu (`ExportReportingMenu`) on reporting list pages for authorized roles. The menu offers CSV, XLSX, and PDF format selection with **from** and **to** date inputs (default last 30 days). Client-side validation mirrors server rules before download.

### Sprint C1 — XLSX Export Foundation (V2.3.x)

Version 2.3.x adds **read-only XLSX exports** for the same entities as CSV, sharing authorization, filters, columns, and row data.

| Endpoint | Purpose |
|----------|---------|
| `GET /api/reporting/exports/assets.xlsx` | Asset register export |
| `GET /api/reporting/exports/inspections.xlsx` | Inspection list export |
| `GET /api/reporting/exports/issues.xlsx` | Issue register export |
| `GET /api/reporting/exports/work-orders.xlsx` | Work order list export |
| `GET /api/reporting/exports/preventive-candidates.xlsx` | Preventive execution candidate export |

CSV exports remain supported unchanged. Scheduled and email reports remain deferred. XLSX is available from the unified Export menu (see Sprint R1 frontend note above).

### Sprint C2 — PDF Export Foundation (V2.3.x)

Version 2.3.x adds **read-only PDF exports** for the same entities as CSV and XLSX, sharing authorization, filters, columns, and row data.

| Endpoint | Purpose |
|----------|---------|
| `GET /api/reporting/exports/assets.pdf` | Asset register export |
| `GET /api/reporting/exports/inspections.pdf` | Inspection list export |
| `GET /api/reporting/exports/issues.pdf` | Issue register export |
| `GET /api/reporting/exports/work-orders.pdf` | Work order list export |
| `GET /api/reporting/exports/preventive-candidates.pdf` | Preventive execution candidate export |

PDF exports are simple tabular reports (title, timestamp, optional date range, table) — not branded report templates. CSV and XLSX exports remain supported unchanged. Scheduled and email reports remain deferred. PDF is available from the unified Export menu.

### Sprint B4 — Reporting Policy Foundation (V2.3.x)

Version 2.3.x introduces the **Reporting Policy** organizational foundation (BDR-004). `DefaultReportingPolicy` defines CSV as the default export format, enables CSV/XLSX/PDF, and records `LAST_30_DAYS` as the default reporting period. `ReportingExportService` uses the policy for CSV export filenames. Enabled formats and default date range inform frontend defaults; server-side export window validation is enforced separately (Security-2).

### Sprint B5 — Approval Policy Foundation (V2.3.x)

Version 2.3.x introduces the **Approval Policy** organizational foundation (BDR-004). `DefaultApprovalPolicy` governs whether completion review, manager operational decisions, suggested action approval, and preventive candidate approval are required. Integrated services consult `ApprovalPolicyService.getPolicy()` at existing approval decision points. No configurable modes exist yet — workflow behaviour is unchanged.

### Sprint C2 — Dashboard UI

Version 2.1.0 Sprint C2 introduces the first **Operations Intelligence Dashboard** at `/dashboard`.

- Read-only cockpit for Administrator, Manager, and Operational Coordinator
- Consumes `GET /api/operations-intelligence/kpis` from Sprint C1
- Displays KPI cards, attention alerts, quick navigation links, and recent intelligence summaries
- Does **not** approve workflows, run the scheduler, or mutate operational data
- Field Employee and Contractor keep their existing landing experience

### Sprint C3 — Trend Intelligence

Version 2.1.0 Sprint C3 introduces **read-only trend time-series** aggregation.

| Endpoint | Purpose |
|----------|---------|
| `GET /api/operations-intelligence/trends` | Historical operational counts bucketed by DAY, WEEK, or MONTH |

Query parameters: `from` and `to` (epoch millis; default last 30 days), `bucket` (`DAY` \| `WEEK` \| `MONTH`; default `DAY`). Maximum range: 365 days. Missing buckets are zero-filled in chronological order.

Trend series: `inspectionsCompleted` (by `completedAt`), `issuesCreated` (by `recordedAt`), `workOrdersCompleted` (by `updatedAt` when status is COMPLETED — no dedicated completion timestamp exists), `preventiveCandidatesGenerated` (by candidate `createdAt`), `suggestedActionsAccepted` (by `decidedAt`).

Authorization matches Sprint C1 (Administrator global; Manager and Operational Coordinator own department; Field Employee and Contractor forbidden).

The dashboard adds lightweight CSS mini-bar trend widgets (no charting library). Scheduled and email reports remain deferred.

### Sprint C4 — Recent Activity & Dashboard Widgets

Version 2.1.0 Sprint C4 refactors the dashboard into reusable widgets and adds a **read-only recent activity feed**.

| Endpoint | Purpose |
|----------|---------|
| `GET /api/operations-intelligence/recent-activity` | Recent operational events aggregated from existing records |

Query parameter: `limit` (default 20; min 1; max 100). Items are sorted by `occurredAt` descending.

Activity types: `INSPECTION_COMPLETED`, `ISSUE_CREATED`, `WORK_ORDER_COMPLETED`, `PREVENTIVE_CANDIDATE_GENERATED`, `PREVENTIVE_CANDIDATE_APPROVED`, `SUGGESTED_ACTION_ACCEPTED`.

This is **not** event sourcing — it queries existing operational records with compact projections. Authorization matches Sprint C1.

### Sprint C5 — Dashboard Personalization

Version 2.1.0 Sprint C5 adds **user-scoped dashboard presentation preferences**. Personalization affects layout only — never permissions, data scope, or workflow behaviour.

| Endpoint | Purpose |
|----------|---------|
| `GET /api/dashboard/preferences` | Load preferences for the authenticated user |
| `PUT /api/dashboard/preferences` | Save widget visibility, order, and default trend range |
| `POST /api/dashboard/preferences/reset` | Restore organizational dashboard policy defaults (deletes saved user preferences) |

When no saved preferences exist, `GET` returns defaults from `DashboardPolicyService.getPolicy()`. Saved user preferences override the organizational policy.

Supported trend ranges: `LAST_7_DAYS`, `LAST_30_DAYS`, `LAST_90_DAYS`. At least one widget must remain visible. Preferences are stored per user in `dashboard_preferences`.

### KPI groups

Response shape: `assets`, `inspections`, `issues`, `workOrders`, `preventive`, `decisionEngine` — each with explicit DTO fields (no generic maps at the top level).

Work order overdue KPIs are always zero because work orders have no due date field in the current model.

### Sprint M1 — Mobile API Foundation

Version 2.2.0 Sprint M1 introduces **read-only mobile bundle endpoints** under `/api/mobile/*` for the future Android field client. M1 affects presentation and data bundling only — no new workflows, write endpoints, or authorization bypass.

| Endpoint | Purpose |
|----------|---------|
| `GET /api/mobile/me` | Authenticated user identity summary |
| `GET /api/mobile/dashboard` | Personal assignment counts |
| `GET /api/mobile/my-inspections` | Scoped inspection summaries |
| `GET /api/mobile/inspections/{id}/bundle` | Inspection screen bundle |
| `GET /api/mobile/my-work-orders` | Scoped work order summaries |
| `GET /api/mobile/work-orders/{id}/bundle` | Work order screen bundle |

Field employees and contractors access assigned items only. Managers access department-scoped items. Administrators have support access. Write operations reuse existing web APIs (`POST /api/inspections/{id}/complete`, `POST /api/work-orders/{id}/maintenance-activity`). Android client, offline sync, and push delivery remain deferred.

### Sprint M4-BE1 — Mobile Asset Lookup / Asset Context API

Version 2.4.0 Sprint M4-BE1 adds a backend endpoint for Android QR/barcode asset navigation: `GET /api/mobile/assets/lookup?code={assetCode}`. This sprint is **backend only** — no QR code generation, no Android scanner, and no new workflows. Android scans a physical tag, resolves the printed asset business code, and will call this endpoint in a later M4 sprint.

**Asset business code.** Every asset now has a stable `code` field (`assets.asset_code`, format `AST-XXXXXXXX`), generated automatically at registration and independent from the internal `asset.id` primary key. This is the identifier intended for QR/barcode encoding. Existing assets were backfilled by migration `V28__asset_business_code.sql`.

**Response.** A compact `AssetContextResponse` (package `com.infratrack.mobile.dto`) bundles:

- Asset summary (id, code, name, category, department, location, status)
- `openIssues` — issues on the asset with no linked operational decision (unresolved)
- `activeInspections` — inspections on the asset with status `ASSIGNED`
- `activeWorkOrders` — work orders on the asset with status `CREATED` or `ASSIGNED`
- `allowedActions` — backend-generated flags: `canViewAsset`, `canViewInspections`, `canViewIssues`, `canViewWorkOrders`, `canCreateInspection`, `canCreateIssue`

Sprint M4-BE3 adds optional nullable sections: `lastInspection`, `lastMaintenance`, and `preventivePlan` (see below). Sprint M4-BE4 adds a `documents` array of visible asset-owned operational document summaries (see below).

Completed/cancelled/resolved records are excluded. Documents and full asset history are deferred to a later M4 backend sprint.

**Authorization.** Reuses existing role and department rules — no Android-specific permission model:

| Role | Access |
|------|--------|
| Administrator | Any asset |
| Manager | Own department, or a department with an active delegated authority (reuses `DelegatedAuthorityService`) |
| Operational Coordinator | Own department only (this is the first mobile endpoint Operational Coordinators can call) |
| Field Employee / Contractor | Own department (existing conservative asset visibility rule; no more specific per-asset rule exists yet) |
| Any other case | `403 Forbidden`, with no asset context returned |

Blank code returns `400`; unknown code returns `404`. The nested lists are scoped to the same asset only after asset-level access is confirmed, so no lookup ever returns cross-asset or cross-department data.

`canCreateInspection` mirrors the existing "assign inspection" rule (Operational Coordinator, own department). `canCreateIssue` mirrors the existing "record issue" role rule (Field Employee/Contractor, own department). Both flags are advisory only — actual creation still goes through the existing `/api/inspections` and `/api/issues` endpoints and their full validation.

### Sprint M4-BE2 — Asset QR Code Generation Foundation

Version 2.4.0 Sprint M4-BE2 adds `GET /api/assets/{assetId}/qr`, returning a `image/png` QR code that encodes the asset business `code` only (e.g. `AST-1A2B3C4D`). No database ids, URLs, JSON, or metadata are embedded. Generation is isolated in `QrCodeGenerator` (ZXing, 512×512 PNG, high error correction, black on white) and orchestrated by `AssetQrCodeService`, which reuses `AssetAuthorizationService.requireCanViewAsset` — the same rules as asset history view access.

**Deferred:** printable PDF labels, batch QR export, Android scanner UI, frontend download UI.

**Future compatibility:** `QrCodeGenerator` is reusable for printable asset label PDFs without changing encoding logic.

### Sprint M4-BE3 — Mobile Asset Context Enrichment

Version 2.4.0 Sprint M4-BE3 enriches the existing `GET /api/mobile/assets/lookup?code={assetCode}` response with compact, nullable operational context sections. No new endpoint, no workflow changes, and no Android or frontend changes in this sprint.

**Additional response sections** (all optional — `null` when no matching record exists):

- `lastInspection` — most recently completed inspection (id, status, completedAt, observedCondition, issueIdentified)
- `lastMaintenance` — most recent completed maintenance activity (id, workOrderId, completedAt, performedBy display name)
- `preventivePlan` — active preventive maintenance plan summary (exists, id, name, triggerType, active); when multiple active plans exist, the most recently created is returned

**Authorization.** Reuses the M4-BE1 outer asset authorization (`requireCanViewAssetContext`). If the caller can view the asset context, they can view these summaries. Forbidden access returns `403` before nested context is queried; unknown code returns `404`; blank code returns `400`.

**Read-only.** Context only — no inspections, work orders, issues, or plans are created or modified by this endpoint.

### Sprint M4-BE4 — Mobile Asset Documents Context

Version 2.4.0 Sprint M4-BE4 adds a `documents` array to the existing asset lookup response. Asset-owned operational documents only — documents linked to inspections, work orders, issues, or maintenance activities remain deferred.

**Response section:**

- `documents` — compact metadata for asset-owned operational documents the caller may view (`id`, `filename`, `contentType`, `ownerType`, `uploadedAt`, `uploadedBy`, `downloadUrl`). Empty array when none are visible. Never `null`. No storage paths or internal file keys are exposed.

**Authorization.** Outer M4-BE1 asset authorization runs first. **M4-BE4.1:** mobile asset context lists all ASSET-owned documents once department access is confirmed — field employees and contractors in the asset's department see reference documents alongside managers and coordinators. Web `GET /api/assets/{assetId}/documents` listing rules are unchanged. Forbidden asset lookup returns `403` before documents are queried.

**Download.** No new download endpoint. Android calls `downloadUrl` (`GET /api/operational-documents/{id}/download`). For field employees and contractors, asset-owned downloads use the same department gate as mobile asset context; context-linked documents (inspection, work order, issue, maintenance) still require assignment-based authorization.

---

## V2.4 Platform Baseline — Architecture Summary

The V2.4 documentation baseline (see [v2.4.md](../06-release-notes/v2.4.md)) reflects the cumulative architecture below.

### Policy Engine (BDR-004)

Five organizational policy foundations consult `*PolicyService.getPolicy()` before operational behaviour:

```text
InspectionVisibilityPolicyService
NotificationPolicyService
DashboardPolicyService
ReportingPolicyService
ApprovalPolicyService
        ↓
Default*Policy implementations (fixed behaviour today)
        ↓
Operational services (unchanged business rules)
```

Future configurable modes and admin UI remain deferred. Business rules (human-in-the-loop, scheduler candidates-only, immutable history) stay fixed.

### Mobile API architecture

```text
Android (future) / Web QR download
        ↓
GET /api/assets/{assetId}/qr  →  PNG encoding assetCode
        ↓
GET /api/mobile/assets/lookup?code=  →  AssetContextResponse
        ├── asset, lastInspection, lastMaintenance, preventivePlan
        ├── documents[], openIssues[], activeInspections[], activeWorkOrders[]
        └── allowedActions (MobileAuthorizationService)
        ↓
GET /api/operational-documents/{id}/download  (authenticated)
```

M1 bundle endpoints (`/api/mobile/me`, dashboard, my-inspections/work-orders, screen bundles) remain the foundation for assignment-based field workflows. M4-BE adds asset-centric navigation.

Write operations continue through existing web APIs — mobile bundles are read-only context.

### Reporting export pipeline

```text
ExportReportingMenu (React)  →  from/to date validation (client)
        ↓
GET /api/reporting/exports/*.{csv|xlsx|pdf}?from=&to=
        ↓
ReportingExportController
        ↓
ReportingExportService
  ├── ReportingAuthorizationService (role + department)
  ├── validateExportDateWindow() (required dates, 365-day max)
  ├── ReportingPolicyService.getPolicy() (filenames, future defaults)
  └── load*Export() (read-only repository queries)
        ↓
CsvExportWriter / XlsxExportWriter / PdfExportWriter
  └── ExportCellFormatter.sanitizeSpreadsheetText() (CSV/XLSX only)
```

### Security hardening (V2.4)

| Layer | Control |
|-------|---------|
| Authentication | JWT Bearer + `UserAccountStatusService` (30s cache, eviction on disable) |
| Transport | HTTPS recommended; CSP on frontend nginx |
| Authorization | Per-domain `*AuthorizationService`; architecture guard test |
| Reporting | Required date window; formula injection prefix on CSV/XLSX |

See [security.md](../05-deployment/security.md) for deployment detail.

### Offline & synchronization (BDR-005)

Version 2.4.0 offline scope is defined by [BDR-005 — Offline & Synchronization Architecture](../03-architecture/bdr-005-offline-synchronization-architecture.md). M4-BE delivered online mobile foundations (bundles, asset context, QR). M5 implements offline operation:

```text
Backend (source of truth)          Android (thin client)
        ↓                                    ↓
Validation, Policy Engine,          Room cache + pending queue
Conflict detection, Audit                  ↓
        ↓                            Sync on reconnect
POST /api/mobile/sync (M5.2-BE1/BE2)  ←── Protocol handshake (token + empty delta)
POST /api/mobile/sync (future)    ←── Idempotent upload processing
GET  /api/mobile/sync/* (future)  ──→ Incremental download
```

**Philosophy:** No business rules offline. Cached entities are temporary mirrors; pending mutations are validated server-side. Conflict resolution is server-authoritative.

**M5 roadmap:** M5.1 Offline Foundations → M5.2 Sync Engine → M5.3 Offline Inspections → M5.4 Offline Work Orders → M5.5 Conflict Resolution → M5.6 Cached Documents → M5.7 UX Polish (see BDR-005 §11).

**M5.2-BE1 (delivered):** `POST /api/mobile/sync` protocol foundation — accepts `SyncRequest` with optional `pendingOperations[]`, returns `SyncResponse`. No domain mutations, no delta download. Extension points: `SyncOperationProcessor`, `SyncTokenService`, `SyncConflictResolver`.

**M5.2-BE2 (delivered):** Opaque `nextSyncToken` on every successful sync (`SyncToken` value object; Android stores only). `protocolVersion: 1`. Empty `SyncDeltaResponse` (`assets`, `inspections`, `workOrders`, `documents`, `users`, `referenceData`). Typed enums for future outcomes: `SyncOperationStatus`, `SyncConflictType`, `SyncWarningCode`. Upload processing, populated deltas, and conflict resolution remain deferred.

---

## Authorization summary

| Role | Access |
|------|--------|
| Administrator | Create, update, archive templates and preventive maintenance plans; manage checklist questions and decision rules on draft templates; view all |
| Manager | View templates, checklist questions, decision rules, preventive maintenance plans, execution candidates, and scheduler runs; generate and review execution candidates; run preventive scheduler (own department) |
| Operational Coordinator | View templates, checklist questions, decision rules, preventive maintenance plans, execution candidates, and scheduler runs |
| Field Employee | No access |
| Contractor | No access |
