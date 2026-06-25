# ADR-002: Inspection Produces At Most One Issue in V1

## Status

Accepted

## Context

UC-004 Perform Inspection records a binary `issueIdentified` outcome on each Inspection.

UC-005 Record Issue creates an Issue when that flag is true.

The initial UC-005 implementation allowed multiple Issues per Inspection in the backend, while the frontend already treated each Inspection as recordable only once. UC-006 Make Operational Decision will evaluate operational responses one Issue at a time.

Business Discovery describes Issue identification as a binary inspection outcome, not as an open-ended collection of separate Issue records per Inspection.

## Decision

For InfraTrack V1, an Inspection may produce zero or one Issue.

The backend rejects a second Issue for the same Inspection with HTTP 409 Conflict.

Multiple defects observed during the same Inspection must be described within the single Issue description field for V1.

## Consequences

**Positive**

- Backend and frontend behaviour are aligned before UC-006.
- Operational Decision logic can assume at most one Issue per completed Inspection in V1.
- The model stays simple and matches the binary inspection outcome already implemented.

**Negative / deferred**

- Separate Issue records for multiple distinct defects from one Inspection are not supported in V1.
- A richer multi-issue model may be introduced later if councils require it.

## Related use cases

- UC-004 Perform Inspection — implemented
- UC-005 Record Issue — implemented
- UC-006 Make Operational Decision — not yet implemented
