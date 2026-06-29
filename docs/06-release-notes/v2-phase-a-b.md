# InfraTrack V2 — Phase A+B Milestone

**Version:** V2 Phase A+B (foundational)
**Status:** Completed
**Baseline:** V1.0.1 operational workflow + V2 Sprint 0 technical baseline

---

## Overview

Phase A and Phase B establish the **Domain Engine** for InfraTrack V2: reusable inspection intelligence and preventive maintenance orchestration on top of the existing V1 operational workflow.

Both phases follow a **human-in-the-loop** principle:

```text
The system proposes.
The Manager decides.
```

Neither phase introduces unattended workflow execution.

---

## Phase A — Inspection Intelligence / Decision Engine

### Delivered capabilities

| Area | Capability |
|------|------------|
| Knowledge model | Inspection Templates, Questions, Business Codes, Choices |
| Value model | NUMBER validation, Units of Measure, CHOICE options |
| Execution | Structured Inspection Answers at completion |
| Rules | Decision Rules on template questions |
| Evaluation | Rule Evaluation Engine and persisted Rule Evaluation Reports |
| Recommendations | Suggested Actions from matched rules |
| Review | Decision Assistant — approve, reject, dismiss; manual Issue creation on approval |
| Quality loop | Rework workflow, IssueType `NORMAL` / `REWORK`, CAPA fields, Lessons Learned |

### Decision Engine flow

```text
Asset Category → Inspection Template → Questions → Answers
  → Rules → Evaluation Report → Suggested Actions → Decision Assistant
```

### Intentionally not automated (Phase A)

- Automatic Issue creation when a rule matches (without manager approval)
- Automatic Operational Decisions or Work Orders from rules
- A3.6-style execution of accepted suggestions without human review
- AI-based confidence or explainability beyond deterministic rule snapshots

---

## Phase B — Preventive Maintenance Engine

### Delivered capabilities

| Area | Capability |
|------|------------|
| Plans | Preventive Maintenance Plans with plan code, version, target action |
| Triggers | Plan Business Triggers, Trigger Definitions, TIME evaluation |
| Candidates | Execution Candidates with duplicate prevention |
| Review | Preventive Decision Assistant — approve, reject, dismiss; Inspection on approval |
| Audit | Preventive Execution Reports (one per candidate) |
| Discovery | Controlled Preventive Scheduler (manual + optional cron; disabled by default) |

### Preventive Engine flow

```text
Preventive Plan → Trigger Definition → Trigger Evaluation
  → Execution Candidate → Preventive Decision Assistant → Inspection
```

### Intentionally not automated (Phase B)

- Automatic candidate approval
- Automatic Inspection creation by scheduler or trigger evaluation
- Work Order or Maintenance Activity creation from plans
- METER and EVENT trigger evaluation
- Scheduler notifications
- KPI dashboards over execution reports
- Cron configuration from the UI

---

## Configuration (Preventive Scheduler)

Disabled by default in all environments:

```properties
app.preventive.scheduler.enabled=false
app.preventive.scheduler.cron=0 0 6 * * *
```

Environment overrides:

| Variable | Purpose |
|----------|---------|
| `PREVENTIVE_SCHEDULER_ENABLED` | `true` to enable scheduled candidate generation |
| `PREVENTIVE_SCHEDULER_CRON` | Spring cron expression (default daily 06:00) |

Manual run (`POST /api/preventive-scheduler/run`) remains available to Administrator and Manager regardless of the scheduled flag.

See [Deployment — Preventive Scheduler](../05-deployment/README.md#preventive-scheduler).

---

## Validation commands

```bash
cd backend && mvn clean test && mvn clean package -DskipTests
cd frontend && npm ci --legacy-peer-deps && npm test -- --run && npm run build
docker compose up --build -d
```

Swagger UI (development): http://localhost:4000/swagger-ui/index.html

---

## Manual validation checklist

### Decision Engine

- [ ] Create an Inspection Template (Administrator) and add checklist questions on a DRAFT template
- [ ] Add Decision Rules to questions; publish template if required by your test data
- [ ] Assign an Inspection with the template to a field employee
- [ ] Complete the Inspection with structured answers
- [ ] Verify Inspection Answers are stored with snapshots
- [ ] Verify Rule Evaluation Report on the Inspection (`/rule-evaluation/reports/latest`)
- [ ] Verify Suggested Actions appear for matched rules
- [ ] As Manager, approve a suggestion and confirm Issue creation
- [ ] Reject or dismiss another suggestion; confirm no Issue created
- [ ] Confirm field-employee `issueIdentified` flow still works independently

### Preventive Engine

- [ ] Create a Preventive Maintenance Plan with TIME trigger (Administrator)
- [ ] Evaluate trigger eligibility for the plan
- [ ] Generate an Execution Candidate (manual or scheduler run)
- [ ] Verify Preventive Execution Report status `GENERATED`
- [ ] Verify Asset History shows `PREVENTIVE_CANDIDATE_GENERATED`
- [ ] As Manager, approve candidate; confirm Inspection created and report `INSPECTION_CREATED`
- [ ] Reject or dismiss a separate candidate; confirm no Inspection
- [ ] Run manual scheduler (`POST /api/preventive-scheduler/run` or UI **Run Scheduler**)
- [ ] Verify scheduler run history and counts; duplicate candidates skipped on repeat run
- [ ] With scheduler **disabled**, confirm no automatic runs after startup
- [ ] Confirm scheduler does not create Inspections without manager approval

### Regression (V1)

- [ ] Asset registration and operational workflow (UC-001–UC-013) unchanged
- [ ] Completion review and rework Issue flow unchanged
- [ ] No new notifications beyond existing Inspection assignment on preventive approval

---

## Documentation

| Document | Purpose |
|----------|---------|
| [Domain Engine](../07-business-architecture/domain-engine.md) | Authoritative V2 business architecture |
| [Business Glossary](../01-business-architecture/glossary.md) | Terminology |
| [ADR-003](../03-architecture/adr-003-v2-domain-driven-workflow.md) | V2 domain interaction |
| [V2 Roadmap](../06-release-notes/v2-roadmap.md) | Phase evolution |
| [V2 Domain Engine API](../04-api/v2-domain-engine-api.md) | Major endpoint groups |
| [BDR-001](../03-architecture/bdr-001-human-in-the-loop-decision-engine.md) | Why rules suggest, not execute |
| [BDR-002](../03-architecture/bdr-002-preventive-candidates-before-automation.md) | Why scheduler generates candidates only |

---

## Conclusion

Phase A+B delivers a complete **propose → review → decide** architecture for both inspection intelligence and preventive maintenance. Future work can extend automation, analytics, and additional trigger types without changing the human validation boundary established here.
