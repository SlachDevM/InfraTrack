import {
  OPERATIONAL_DOCUMENT_TYPE_OPTIONS,
  OPERATIONAL_DOCUMENT_OWNER_TYPE_OPTIONS,
  OPERATIONAL_DOCUMENT_OWNER_TYPES,
  getOperationalDocumentTypeLabel,
  getOperationalDocumentOwnerTypeLabel,
} from '../../constants/operationalDocumentTypes';
import PaginationControls from '../PaginationControls';

function formatOwnerOptionLabel(owner) {
  const datePart = owner.businessDate ? ` — ${owner.businessDate}` : '';
  const summaryPart = owner.contextSummary ? ` — ${owner.contextSummary}` : '';
  return `${owner.label}${datePart}${summaryPart}`;
}

export default function OperationalDocumentsPanel({
  canUploadDocuments,
  selectedAssetId,
  documentForm,
  eligibleOwners,
  eligibleOwnersLoading,
  assetDocuments,
  documentsLoading,
  documentUploading,
  documentsPage,
  documentsTotalPages,
  onDocumentFormChange,
  onDocumentFileChange,
  onUpload,
  onDownload,
  onDocumentsPrevious,
  onDocumentsNext,
}) {
  return (
    <section className="asset-documents-section">
      <h2>Operational Documents</h2>
      <p className="read-only-note">
        Select an asset above to view or upload operational evidence.
      </p>

      {canUploadDocuments && selectedAssetId && (
        <form className="asset-form document-upload-form" onSubmit={onUpload}>
          <div className="form-row">
            <label htmlFor="documentType">Document Type</label>
            <select
              id="documentType"
              name="documentType"
              value={documentForm.documentType}
              onChange={onDocumentFormChange}
              required
              disabled={documentUploading}
            >
              <option value="">Select document type</option>
              {OPERATIONAL_DOCUMENT_TYPE_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-row">
            <label htmlFor="ownerType">Owner Type (optional)</label>
            <select
              id="ownerType"
              name="ownerType"
              value={documentForm.ownerType}
              onChange={onDocumentFormChange}
              disabled={documentUploading}
            >
              <option value="">Asset-level document</option>
              {OPERATIONAL_DOCUMENT_OWNER_TYPE_OPTIONS.filter(
                (option) => option.value !== OPERATIONAL_DOCUMENT_OWNER_TYPES.ASSET
              ).map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          {documentForm.ownerType && (
            <div className="form-row">
              <label htmlFor="ownerId">Owner</label>
              <select
                id="ownerId"
                name="ownerId"
                value={documentForm.ownerId}
                onChange={onDocumentFormChange}
                required
                disabled={documentUploading || eligibleOwnersLoading || eligibleOwners.length === 0}
              >
                <option value="">Select owner</option>
                {eligibleOwners.map((owner) => (
                  <option key={owner.id} value={owner.id}>
                    {formatOwnerOptionLabel(owner)}
                  </option>
                ))}
              </select>
              {eligibleOwnersLoading && (
                <p className="read-only-note">Loading eligible owners...</p>
              )}
              {!eligibleOwnersLoading && eligibleOwners.length === 0 && (
                <p className="read-only-note">
                  No eligible records found for this asset and owner type.
                </p>
              )}
            </div>
          )}

          <div className="form-row">
            <label htmlFor="documentDate">Document Date (optional)</label>
            <input
              id="documentDate"
              name="documentDate"
              type="date"
              value={documentForm.documentDate}
              onChange={onDocumentFormChange}
              disabled={documentUploading}
            />
          </div>

          <div className="form-row">
            <label htmlFor="documentFile">File</label>
            <input
              id="documentFile"
              name="documentFile"
              type="file"
              accept=".pdf,.png,.jpg,.jpeg,.docx,.xlsx,application/pdf,image/png,image/jpeg,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
              onChange={onDocumentFileChange}
              required
              disabled={documentUploading}
            />
          </div>

          <button type="submit" className="btn-primary" disabled={documentUploading}>
            {documentUploading ? 'Uploading...' : 'Upload Document'}
          </button>
        </form>
      )}

      {!canUploadDocuments && (
        <p className="read-only-note">
          Document upload is available to Managers, Operational Coordinators, Field Employees and Contractors.
        </p>
      )}

      {documentsLoading && <p className="read-only-note">Loading operational documents...</p>}

      {!documentsLoading && selectedAssetId && assetDocuments.length === 0 && (
        <p className="read-only-note">No operational documents uploaded for this asset.</p>
      )}

      {!documentsLoading && assetDocuments.length > 0 && (
        <table className="reference-table assets-table">
          <thead>
            <tr>
              <th>File Name</th>
              <th>Type</th>
              <th>Owner</th>
              <th>Uploaded</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {assetDocuments.map((document) => (
              <tr key={document.id}>
                <td>{document.originalFileName}</td>
                <td>{getOperationalDocumentTypeLabel(document.documentType)}</td>
                <td>
                  {getOperationalDocumentOwnerTypeLabel(document.ownerType)}
                  {document.ownerId ? ` #${document.ownerId}` : ''}
                </td>
                <td>{document.uploadedAt?.replace('T', ' ')}</td>
                <td>
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={() => onDownload(document.id)}
                  >
                    Download
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {selectedAssetId && (
        <PaginationControls
          page={documentsPage}
          totalPages={documentsTotalPages}
          loading={documentsLoading}
          onPrevious={onDocumentsPrevious}
          onNext={onDocumentsNext}
        />
      )}
    </section>
  );
}
