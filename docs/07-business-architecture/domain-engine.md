# InfraTrack Domain Engine

## Purpose

The Domain Engine holds reusable operational knowledge that applies across assets of the same type. It separates **what should be inspected** from individual **Inspection** executions.

Knowledge belongs to the **Asset Category**, not to individual Assets.

## Inspection Template

An **Inspection Template** defines the reusable business structure for future Inspections within an Asset Category.

Examples:

- Pump Inspection Template
- Motor Inspection Template
- Building Safety Inspection Template
- Electrical Panel Inspection Template

### Relationship

```
AssetCategory
    ↓
InspectionTemplate
```

Future sprints will extend this chain:

```
InspectionTemplate
    ↓
InspectionQuestions
    ↓
InspectionAnswers
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
| `CHOICE` | Selection from options (options defined in a future sprint) |
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

The Value Model prepares structured, validated answers that future sprints will evaluate with Decision Matrix rules. A2.3.2 does **not** implement decision rules or automatic Issue creation.

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

Sprint A3.1 introduces **Decision Rules** attached to Inspection Template Questions. A Decision Rule describes a condition on a question answer and the intended future action when that condition is met.

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

**A3.3 persists evaluation only.**

Reports are created as part of the Inspection completion transaction. No Issues, Operational Decisions, Suggested Actions, notifications, or workflow side effects are triggered. Inspection completion behaviour (including existing `issueIdentified` handling) is unchanged.

When a templated Inspection is completed with structured answers but no active rules exist, a report is still created with `resultCount = 0` for audit clarity. Legacy inspections without a template do not receive a report.

#### Retrieval API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/inspections/{inspectionId}/rule-evaluation/reports` | List report summaries |
| `GET /api/inspections/{inspectionId}/rule-evaluation/reports/latest` | Latest report with results |
| `GET /api/inspections/{inspectionId}/rule-evaluation/reports/{reportId}` | Report detail |

Access requires permission to view the Inspection; cross-department access is rejected.

#### Relationship to future Suggested Actions

Persisted reports in A3.3 form the audit trail that A3.4 uses to generate **Suggested Actions**.

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

Suggested Actions are created in the same transaction as the evaluation report when matched results exist. Status is `PENDING`. No Issues, Operational Decisions, notifications, Asset History events, or workflow side effects occur. No accept/reject/dismiss endpoints yet.

One matched rule result produces one Suggested Action. Action payload JSON is interpreted tolerantly (`title`, `message`, `severity`); unknown fields are ignored and missing fields use readable fallbacks.

#### Retrieval API

| Endpoint | Purpose |
|----------|---------|
| `GET /api/inspections/{inspectionId}/suggested-actions` | List suggestions (optional `status`, `actionType` filters) |
| `GET /api/inspections/{inspectionId}/suggested-actions/{suggestedActionId}` | Suggestion detail |

#### Future sprints

- **A3.5** — Decision Assistant (manager review, explainability, manual Issue creation);
- **A3.6** — automation (optional execution of accepted suggestions into Operational Decisions).

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

## Future sprints

Planned extensions to the Domain Engine include:

- structured answers captured during Inspection completion;
- publish and archive workflow rules;
- template version cloning;
- **human validation and automation of suggested actions**;
- analytics and KPI dashboards powered by template-aligned inspection data.

## Authorization summary

| Role | Access |
|------|--------|
| Administrator | Create, update, archive templates; manage checklist questions and decision rules on draft templates; view all |
| Manager | View templates, checklist questions, and decision rules |
| Operational Coordinator | View templates, checklist questions, and decision rules |
| Field Employee | No access |
| Contractor | No access |
