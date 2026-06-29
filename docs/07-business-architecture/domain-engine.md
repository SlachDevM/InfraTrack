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

## Future sprints

Planned extensions to the Domain Engine include:

- structured answers captured during Inspection completion;
- publish and archive workflow rules;
- template version cloning;
- decision rules derived from template outcomes;
- analytics and KPI dashboards powered by template-aligned inspection data.

## Authorization summary

| Role | Access |
|------|--------|
| Administrator | Create, update, archive templates; manage checklist questions on draft templates; view all |
| Manager | View templates and checklist questions |
| Operational Coordinator | View templates and checklist questions |
| Field Employee | No access |
| Contractor | No access |
