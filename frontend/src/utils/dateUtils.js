export function isSameDay(dateStr, dayDate) {
  if (!dateStr) return false;
  // dateStr is now a string in format "yyyy-MM-dd"
  const [year, month, day] = dateStr.split('-').map(Number);
  const dateToCompare = new Date(year, month - 1, day);
  return dateToCompare.toDateString() === dayDate.toDateString();
}

export function dateToISOString(date) {
  if (!date) return null;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
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
