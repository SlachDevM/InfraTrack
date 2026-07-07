import { getAssetStatusLabel } from '../../constants/assetStatuses';
import { getAssetHistoryEventTypeLabel } from '../../constants/assetHistoryEventTypes';
import PaginationControls from '../PaginationControls';

export default function AssetHistoryPanel({
  assets,
  selectedAssetId,
  selectedAsset,
  assetHistory,
  historyLoading,
  historyPage,
  historyTotalPages,
  onAssetChange,
  onHistoryPrevious,
  onHistoryNext,
}) {
  return (
    <section className="asset-history-section" aria-labelledby="asset-history-heading">
      <h2 id="asset-history-heading">Asset History</h2>
      <div className="form-row">
        <label htmlFor="historyAssetId">Asset</label>
        <select
          id="historyAssetId"
          name="historyAssetId"
          value={selectedAssetId}
          onChange={onAssetChange}
          disabled={historyLoading || assets.length === 0}
          aria-describedby={assets.length === 0 ? 'asset-history-empty-hint' : undefined}
        >
          <option value="">Select asset</option>
          {assets.map((asset) => (
            <option key={asset.id} value={asset.id}>
              {asset.name} — {asset.location}
            </option>
          ))}
        </select>
      </div>

      {assets.length === 0 && (
        <p id="asset-history-empty-hint" className="read-only-note" role="status">
          Register an asset to review its operational history.
        </p>
      )}

      {selectedAsset && (
        <dl className="asset-context-summary">
          <div className="asset-context-summary-item">
            <dt>Department</dt>
            <dd>{selectedAsset.departmentName}</dd>
          </div>
          <div className="asset-context-summary-item">
            <dt>Category</dt>
            <dd>{selectedAsset.assetCategoryName}</dd>
          </div>
          <div className="asset-context-summary-item">
            <dt>Status</dt>
            <dd>{getAssetStatusLabel(selectedAsset.status)}</dd>
          </div>
          <div className="asset-context-summary-item">
            <dt>Location</dt>
            <dd>{selectedAsset.location || '—'}</dd>
          </div>
        </dl>
      )}

      {historyLoading && (
        <p className="read-only-note" role="status" aria-live="polite">
          Loading asset history...
        </p>
      )}

      {!historyLoading && selectedAssetId && assetHistory.length === 0 && (
        <p className="empty-state no-items" role="status">
          No history entries recorded for this asset.
        </p>
      )}

      {!historyLoading && assetHistory.length > 0 && (
        <table className="reference-table assets-table" aria-label="Asset history events">
          <thead>
            <tr>
              <th scope="col">Event Date</th>
              <th scope="col">Event Type</th>
              <th scope="col">Details</th>
              <th scope="col">Responsible User</th>
            </tr>
          </thead>
          <tbody>
            {assetHistory.map((entry, index) => (
              <tr key={`${entry.eventType}-${entry.eventDate}-${index}`}>
                <td>{entry.eventDate}</td>
                <td>{getAssetHistoryEventTypeLabel(entry.eventType)}</td>
                <td>{entry.details || '—'}</td>
                <td>{entry.responsibleUserName || `#${entry.responsibleUserId}`}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {selectedAssetId && (
        <PaginationControls
          page={historyPage}
          totalPages={historyTotalPages}
          loading={historyLoading}
          onPrevious={onHistoryPrevious}
          onNext={onHistoryNext}
        />
      )}
    </section>
  );
}
