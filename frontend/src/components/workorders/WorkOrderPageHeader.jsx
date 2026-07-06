import NotificationButton from '../NotificationButton';
import ExportReportingMenu from '../ExportReportingMenu';
import { REPORTING_EXPORT_TYPES } from '../../constants/reportingExports';

export default function WorkOrderPageHeader({
  canExport,
  onExportError,
  onExportSuccess,
  onNavigateHome,
  onLogout,
}) {
  return (
    <header
      className="reference-header"
      style={{
        background: 'linear-gradient(135deg, #1a472a 0%, #2d6b4d 100%)',
        color: 'white',
      }}
    >
      <button type="button" className="back-btn" onClick={onNavigateHome} aria-label="Back to home">
        ← Back
      </button>
      <h1>Work Orders</h1>
      <div className="user-header-actions">
        <NotificationButton />
        {canExport && (
          <ExportReportingMenu
            exportType={REPORTING_EXPORT_TYPES.WORK_ORDERS}
            onError={onExportError}
            onSuccess={onExportSuccess}
          />
        )}
        <button type="button" className="logout-btn" onClick={onLogout} aria-label="Log out">
          Logout
        </button>
      </div>
    </header>
  );
}
