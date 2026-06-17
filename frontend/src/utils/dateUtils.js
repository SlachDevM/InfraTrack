export function timestampToDateInput(timestamp) {
  if (!timestamp) return '';
  const d = new Date(timestamp);
  const offset = d.getTimezoneOffset();
  const local = new Date(d.getTime() - offset * 60 * 1000);
  return local.toISOString().split('T')[0];
}

export function dateInputToTimestamp(dateStr) {
  if (!dateStr) return null;
  const [year, month, day] = dateStr.split('-').map(Number);
  const d = new Date(year, month - 1, day);
  return d.getTime();
}

export function startOfDay(date) {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d.getTime();
}

export function endOfDay(date) {
  const d = new Date(date);
  d.setHours(23, 59, 59, 999);
  return d.getTime();
}

export function isSameDay(ts, dayDate) {
  if (!ts) return false;
  return startOfDay(new Date(ts)) === startOfDay(dayDate);
}

export function formatJobTypeLabel(type) {
  return type.replace(/_/g, ' ');
}

export function getMonday(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  return new Date(d.setDate(diff));
}
