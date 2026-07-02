/**
 * Returns field employees eligible for inspection assignment in the UI.
 * Backend enforces the same rules on assign.
 */
export function filterInspectionAssignees(workers, departmentId) {
  if (!Array.isArray(workers)) {
    return [];
  }
  return workers.filter(
    (worker) =>
      worker.role === 'FIELD_EMPLOYEE' &&
      worker.status === 'ACTIVE' &&
      departmentId != null &&
      worker.departmentId === departmentId
  );
}
