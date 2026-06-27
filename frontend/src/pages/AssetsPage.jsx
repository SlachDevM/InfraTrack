import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import assetApi from '../services/assetApi';
import operationalDocumentApi from '../services/operationalDocumentApi';
import departmentApi from '../services/departmentApi';
import assetCategoryApi from '../services/assetCategoryApi';
import NotificationButton from '../components/NotificationButton';
import RegisterAssetForm from '../components/assets/RegisterAssetForm';
import AssetList from '../components/assets/AssetList';
import AssetHistoryPanel from '../components/assets/AssetHistoryPanel';
import OperationalDocumentsPanel from '../components/assets/OperationalDocumentsPanel';
import { canRegisterAssets, canUploadOperationalDocuments } from '../constants/userRoles';
import { ASSET_STATUSES } from '../constants/assetStatuses';
import {
  getApiErrorMessage,
  isConflict,
  isForbidden,
  isUploadAuthorizationError,
} from '../utils/apiError';
import '../styles/ReferenceDataPage.css';
import '../styles/AssetsPage.css';

function appendRequestPart(formData, name, value) {
  formData.append(
    name,
    new Blob([JSON.stringify(value)], { type: 'application/json' }),
  );
}

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
      setError(getApiErrorMessage(err, 'Failed to load assets.'));
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
      if (isConflict(err)) {
        setError('A possible duplicate asset already exists with the same name, department and category.');
      } else if (isForbidden(err)) {
        setError('You do not have permission to register assets.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to register asset.'));
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
        setError(getApiErrorMessage(err, 'Failed to load asset details.'));
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
      const uploadFormData = new FormData();
      uploadFormData.append('file', documentForm.file);
      appendRequestPart(uploadFormData, 'documentType', documentForm.documentType);
      if (documentForm.ownerType) {
        appendRequestPart(uploadFormData, 'ownerType', documentForm.ownerType);
      }
      if (documentForm.ownerId) {
        appendRequestPart(uploadFormData, 'ownerId', Number(documentForm.ownerId));
      }
      if (documentForm.documentDate) {
        appendRequestPart(uploadFormData, 'documentDate', documentForm.documentDate);
      }
      await operationalDocumentApi.upload(Number(selectedAssetId), uploadFormData);
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
      if (isUploadAuthorizationError(err)) {
        setError('You do not have permission to upload documents for this context.');
      } else if (err.status === 404) {
        setError('Asset or operational owner not found.');
      } else {
        setError(getApiErrorMessage(err, 'Document upload failed.'));
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
      setError(getApiErrorMessage(err, 'Failed to download document.'));
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
          <RegisterAssetForm
            formData={formData}
            departments={departments}
            categories={categories}
            submitting={submitting}
            onChange={handleChange}
            onSubmit={handleSubmit}
          />
        ) : (
          <p className="read-only-note">
            Asset registration is available to Managers and Operational Coordinators.
          </p>
        )}

        <AssetList assets={assets} />

        <AssetHistoryPanel
          assets={assets}
          selectedAssetId={selectedAssetId}
          selectedAsset={selectedAsset}
          assetHistory={assetHistory}
          historyLoading={historyLoading}
          onAssetChange={handleAssetHistoryChange}
        />

        <OperationalDocumentsPanel
          canUploadDocuments={canUploadDocuments}
          selectedAssetId={selectedAssetId}
          documentForm={documentForm}
          assetDocuments={assetDocuments}
          documentsLoading={documentsLoading}
          documentUploading={documentUploading}
          onDocumentFormChange={handleDocumentFormChange}
          onDocumentFileChange={handleDocumentFileChange}
          onUpload={handleDocumentUpload}
          onDownload={handleDocumentDownload}
        />
      </main>
    </div>
  );
}
