export function formatActivityTime(epochMillis) {
  if (!epochMillis) {
    return '';
  }
  return new Date(epochMillis).toLocaleString(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

export function formatActivityTypeLabel(type) {
  if (!type) {
    return '';
  }
  return type
    .split('_')
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join(' ');
}
