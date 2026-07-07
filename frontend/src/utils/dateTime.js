export function formatTimestamp(value) {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleString();
}

export function formatDateTime(value) {
  return value ? new Date(value).toLocaleString() : '—';
}

export function toDateTimeLocalValue(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
