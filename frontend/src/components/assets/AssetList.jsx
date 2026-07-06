import { getAssetStatusLabel } from '../../constants/assetStatuses';

export default function AssetList({ assets }) {
  return (
    <section className="asset-list-section">
      <h2>Registered Assets</h2>
      {assets.length === 0 ? (
        <p className="empty-state no-items">No assets registered yet.</p>
      ) : (
        <div className="table-scroll">
          <table className="reference-table assets-table" aria-label="Registered assets">
            <thead>
              <tr>
                <th>Name</th>
                <th>Department</th>
                <th>Category</th>
                <th>Location</th>
                <th>Status</th>
                <th>Registered</th>
              </tr>
            </thead>
            <tbody>
              {assets.map((asset) => (
                <tr key={asset.id}>
                  <td>{asset.name}</td>
                  <td>{asset.departmentName}</td>
                  <td>{asset.assetCategoryName}</td>
                  <td>{asset.location}</td>
                  <td>{getAssetStatusLabel(asset.status)}</td>
                  <td>{asset.registrationDate}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
