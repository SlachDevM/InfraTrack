import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import assetApi from '../services/assetApi';
import operationalDocumentApi from '../services/operationalDocumentApi';
import departmentApi from '../services/departmentApi';
import assetCategoryApi from '../services/assetCategoryApi';
import NotificationButton from '../components/NotificationButton';
import { canRegisterAssets, canUploadOperationalDocuments } from '../constants/userRoles';
import { getAssetHistoryEventTypeLabel } from '../constants/assetHistoryEventTypes';
import {
  OPERATIONAL_DOCUMENT_TYPE_OPTIONS,
  OPERATIONAL_DOCUMENT_OWNER_TYPE_OPTIONS,
  OPERATIONAL_DOCUMENT_OWNER_TYPES,
  getOperationalDocumentTypeLabel,
  getOperationalDocumentOwnerTypeLabel,
} from '../constants/operationalDocumentTypes';
import {
  ASSET_STATUSES,
  ASSET_STATUS_OPTIONS,
  getAssetStatusLabel,
} from '../constants/assetStatuses';
import '../styles/ReferenceDataPage.css';
import '../styles/AssetsPage.css';

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}

export default function AssetsPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [assets, setAssets] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [selectedAssetId, setSelectedAssetId] = useState('');
  const [assetHistory, setAssetHistory] = useState([]);
  const [assetDocuments, setAssetDocuments] = useState([]);
  const [documentsLoading, setDocumentsLoading] = useState(false);
  const [documentUploading, setDocumentUploading] = useState(false);
  const [documentForm, setDocumentForm] = useState({
    documentType: '',
    ownerType: '',
    ownerId: '',
    documentDate: '',
    file: null,
  });
  const [formData, setFormData] = useState({
    name: '',
    departmentId: '',
    assetCategoryId: '',
    location: '',
    status: ASSET_STATUSES.ACTIVE,
    registrationDate: todayIsoDate(),
  });

  const canRegister = canRegisterAssets(auth?.user?.role);
  const canUploadDocuments = canUploadOperationalDocuments(auth?.user?.role);

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, navigate]);

  const loadPageData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [assetData, departmentData, categoryData] = await Promise.all([
        assetApi.list(),
        departmentApi.list(),
        assetCategoryApi.list(),
      ]);
      setAssets(assetData);
      setDepartments(departmentData);
      setCategories(categoryData);
    } catch (err) {
      setError(`Failed to load assets: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canRegister) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      await assetApi.register({
        name: formData.name,
        departmentId: Number(formData.departmentId),
        assetCategoryId: Number(formData.assetCategoryId),
        location: formData.location,
        status: formData.status,
        registrationDate: formData.registrationDate,
      });
      setSuccess('Asset registered successfully.');
      setFormData({
        name: '',
        departmentId: '',
        assetCategoryId: '',
        location: '',
        status: ASSET_STATUSES.ACTIVE,
        registrationDate: todayIsoDate(),
      });
      await loadPageData();
    } catch (err) {
      if (err.status === 409) {
        setError('A possible duplicate asset already exists with the same name, department and category.');
      } else if (err.status === 403) {
        setError('You do not have permission to register assets.');
      } else {
        setError(`Failed to register asset: ${err.message}`);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleAssetHistoryChange = async (e) => {
    const assetId = e.target.value;
    setSelectedAssetId(assetId);
    setAssetHistory([]);
    setAssetDocuments([]);

    if (!assetId) {
      return;
    }

    try {
      setHistoryLoading(true);
      setDocumentsLoading(true);
      setError(null);
      const [history, documents] = await Promise.all([
        assetApi.getHistory(Number(assetId)),
        operationalDocumentApi.list(Number(assetId)),
      ]);
      setAssetHistory(history);
      setAssetDocuments(documents);
    } catch (err) {
      if (err.status === 404) {
        setError('Asset not found.');
      } else {
        setError(`Failed to load asset details: ${err.message}`);
      }
    } finally {
      setHistoryLoading(false);
      setDocumentsLoading(false);
    }
  };

  const handleDocumentFormChange = (e) => {
    const { name, value } = e.target;
    setDocumentForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleDocumentFileChange = (e) => {
    const file = e.target.files?.[0] || null;
    setDocumentForm((prev) => ({ ...prev, file }));
  };

  const handleDocumentUpload = async (e) => {
    e.preventDefault();
    if (!canUploadDocuments || !selectedAssetId || !documentForm.file || !documentForm.documentType) {
      return;
    }

    try {
      setDocumentUploading(true);
      setError(null);
      setSuccess(null);
      const formData = new FormData();
      formData.append('file', documentForm.file);
      formData.append('documentType', documentForm.documentType);
      if (documentForm.ownerType) {
        formData.append('ownerType', documentForm.ownerType);
      }
      if (documentForm.ownerId) {
        formData.append('ownerId', documentForm.ownerId);
      }
      if (documentForm.documentDate) {
        formData.append('documentDate', documentForm.documentDate);
      }
      await operationalDocumentApi.upload(Number(selectedAssetId), formData);
      setSuccess('Operational document uploaded successfully.');
      setDocumentForm({
        documentType: '',
        ownerType: '',
        ownerId: '',
        documentDate: '',
        file: null,
      });
      const documents = await operationalDocumentApi.list(Number(selectedAssetId));
      setAssetDocuments(documents);
      const history = await assetApi.getHistory(Number(selectedAssetId));
      setAssetHistory(history);
    } catch (err) {
      if (err.status === 403) {
        setError('You do not have permission to upload documents for this context.');
      } else if (err.status === 404) {
        setError('Asset or operational owner not found.');
      } else {
        setError(`Failed to upload document: ${err.message}`);
      }
    } finally {
      setDocumentUploading(false);
    }
  };

  const handleDocumentDownload = async (documentId) => {
    try {
      setError(null);
      const { blob, filename } = await operationalDocumentApi.download(documentId, auth.token);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      link.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError(`Failed to download document: ${err.message}`);
    }
  };

  const selectedAsset = assets.find(
    (asset) => String(asset.id) === String(selectedAssetId)
  );

  if (loading) {
    return <div className="loading">Loading assets...</div>;
  }

  return (
    <div className="reference-data-page assets-page">
      <header
        className="reference-header"
        style={{
          background: 'linear-gradient(135deg, #1a472a 0%, #2d6b4d 100%)',
          color: 'white',
        }}
      >
        <button type="button" className="back-btn" onClick={() => navigate('/')}>
          ← Back
        </button>
        <h1>Assets</h1>
        <div className="user-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content assets-content">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {canRegister ? (
          <section className="asset-form-section">
            <h2>Register Asset</h2>
            <form className="asset-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <label htmlFor="name">Asset name</label>
                <input
                  id="name"
                  name="name"
                  type="text"
                  value={formData.name}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                />
              </div>

              <div className="form-row">
                <label htmlFor="departmentId">Department</label>
                <select
                  id="departmentId"
                  name="departmentId"
                  value={formData.departmentId}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  <option value="">Select department</option>
                  {departments.map((department) => (
                    <option key={department.id} value={department.id}>
                      {department.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="assetCategoryId">Asset Category</label>
                <select
                  id="assetCategoryId"
                  name="assetCategoryId"
                  value={formData.assetCategoryId}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  <option value="">Select category</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="location">Location</label>
                <input
                  id="location"
                  name="location"
                  type="text"
                  value={formData.location}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                />
              </div>

              <div className="form-row">
                <label htmlFor="status">Status</label>
                <select
                  id="status"
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  {ASSET_STATUS_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-row">
                <label htmlFor="registrationDate">Registration Date</label>
                <input
                  id="registrationDate"
                  name="registrationDate"
                  type="date"
                  value={formData.registrationDate}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                />
              </div>

              <button type="submit" className="btn-primary" disabled={submitting}>
                {submitting ? 'Registering...' : 'Register Asset'}
              </button>
            </form>
          </section>
        ) : (
          <p className="read-only-note">
            Asset registration is available to Managers and Operational Coordinators.
          </p>
        )}

        <section className="asset-list-section">
          <h2>Registered Assets</h2>
          {assets.length === 0 ? (
            <p className="no-items">No assets registered yet.</p>
          ) : (
            <table className="reference-table assets-table">
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
          )}
        </section>

        <section className="asset-history-section">
          <h2>Asset History</h2>
          <div className="form-row">
            <label htmlFor="historyAssetId">Asset</label>
            <select
              id="historyAssetId"
              name="historyAssetId"
              value={selectedAssetId}
              onChange={handleAssetHistoryChange}
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
                  <th>Responsible User</th>
                </tr>
              </thead>
              <tbody>
                {assetHistory.map((entry, index) => (
                  <tr key={`${entry.eventType}-${entry.eventDate}-${index}`}>
                    <td>{entry.eventDate}</td>
                    <td>{getAssetHistoryEventTypeLabel(entry.eventType)}</td>
                    <td>{entry.responsibleUserName || `#${entry.responsibleUserId}`}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <section className="asset-documents-section">
          <h2>Operational Documents</h2>
          <p className="read-only-note">
            Select an asset above to view or upload operational evidence.
          </p>

          {canUploadDocuments && selectedAssetId && (
            <form className="asset-form document-upload-form" onSubmit={handleDocumentUpload}>
              <div className="form-row">
                <label htmlFor="documentType">Document Type</label>
                <select
                  id="documentType"
                  name="documentType"
                  value={documentForm.documentType}
                  onChange={handleDocumentFormChange}
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
                  onChange={handleDocumentFormChange}
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

              <div className="form-row">
                <label htmlFor="ownerId">Owner ID (optional)</label>
                <input
                  id="ownerId"
                  name="ownerId"
                  type="number"
                  min="1"
                  value={documentForm.ownerId}
                  onChange={handleDocumentFormChange}
                  disabled={documentUploading || !documentForm.ownerType}
                />
              </div>

              <div className="form-row">
                <label htmlFor="documentDate">Document Date (optional)</label>
                <input
                  id="documentDate"
                  name="documentDate"
                  type="date"
                  value={documentForm.documentDate}
                  onChange={handleDocumentFormChange}
                  disabled={documentUploading}
                />
              </div>

              <div className="form-row">
                <label htmlFor="documentFile">File</label>
                <input
                  id="documentFile"
                  name="documentFile"
                  type="file"
                  onChange={handleDocumentFileChange}
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
                        onClick={() => handleDocumentDownload(document.id)}
                      >
                        Download
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </main>
    </div>
  );
}
