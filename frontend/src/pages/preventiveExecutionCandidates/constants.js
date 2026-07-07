export const EMPTY_APPROVE_FORM = {
  assigneeId: '',
  plannedAt: '',
  notes: '',
};

export { formatTimestamp } from '../../utils/dateTime';

export function dateInputToPlannedAt(value) {
  if (!value) {
    return undefined;
  }
  return new Date(`${value}T00:00:00`).getTime();
}

export function parseAssigneeId(value) {
  if (value === '' || value == null) {
    return null;
  }
  const parsed = Number(value);
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
}
