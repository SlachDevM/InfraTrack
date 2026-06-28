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
    <section className="asset-history-section">
      <h2>Asset History</h2>
      <div className="form-row">
        <label htmlFor="historyAssetId">Asset</label>
        <select
          id="historyAssetId"
          name="historyAssetId"
          value={selectedAssetId}
          onChange={onAssetChange}
          disabled={historyLoading || assets.length === 0}
        >
          <option value="">Select asset</option>
          {assets.map((asset) => (
            <option key={asset.id} value={asset.id}>
              {asset.name} — {asset.location}
            </option>
          ))}
        </select>
      </div>

      {selectedAsset && (
        <div className="linked-decision-info">
          <strong>Department:</strong> {selectedAsset.departmentName}
          <br />
          <strong>Category:</strong> {selectedAsset.assetCategoryName}
          <br />
          <strong>Status:</strong> {getAssetStatusLabel(selectedAsset.status)}
        </div>
      )}

      {historyLoading && <p className="read-only-note">Loading asset history...</p>}

      {!historyLoading && selectedAssetId && assetHistory.length === 0 && (
        <p className="read-only-note">No history entries recorded for this asset.</p>
      )}

      {!historyLoading && assetHistory.length > 0 && (
        <table className="reference-table assets-table">
          <thead>
            <tr>
              <th>Event Date</th>
              <th>Event Type</th>
              <th>Details</th>
              <th>Responsible User</th>
            </tr>
          </thead>
          <tbody>
            {assetHistory.map((entry, index) => (
              <tr key={`${entry.eventType}-${entry.eventDate}-${index}`}>
                <td>{entry.eventDate}</td>
                <td>{getAssetHistoryEventTypeLabel(entry.eventType)}</td>
                <td>{entry.details || '-'}</td>
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
