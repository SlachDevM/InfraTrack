import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import assetApi from '../services/assetApi';
import operationalDocumentApi from '../services/operationalDocumentApi';
import departmentApi from '../services/departmentApi';
import assetCategoryApi from '../services/assetCategoryApi';
import userApi from '../services/userApi';
import NotificationButton from '../components/NotificationButton';
import PaginationControls from '../components/PaginationControls';
import RegisterAssetForm from '../components/assets/RegisterAssetForm';
import AssetList from '../components/assets/AssetList';
import AssetHistoryPanel from '../components/assets/AssetHistoryPanel';
import OperationalDocumentsPanel from '../components/assets/OperationalDocumentsPanel';
import ConfirmDialog from '../components/ConfirmDialog';
import ExportCsvButton from '../components/ExportCsvButton';
import { ROUTES } from '../constants/routes';
import { HTTP_STATUS } from '../constants/httpStatus';
import { REPORTING_EXPORT_TYPES } from '../constants/reportingExports';
import { canRegisterAssets, canUploadOperationalDocuments, canExportReporting } from '../constants/userRoles';
import { ASSET_STATUSES } from '../constants/assetStatuses';
import {
  getApiErrorMessage,
  isConflict,
  isForbidden,
  isUploadAuthorizationError,
} from '../utils/apiError';
import {
  DEFAULT_PAGE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
import '../styles/ReferenceDataPage.css';
import '../styles/AssetsPage.css';
import '../styles/UserManagementPage.css';

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
  const [assetsPage, setAssetsPage] = useState(DEFAULT_PAGE);
  const [assetsTotalPages, setAssetsTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [departments, setDepartments] = useState([]);
  const [categories, setCategories] = useState([]);
  const [departmentLocked, setDepartmentLocked] = useState(false);
  const [lockedDepartmentId, setLockedDepartmentId] = useState('');
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [selectedAssetId, setSelectedAssetId] = useState('');
  const [assetHistory, setAssetHistory] = useState([]);
  const [historyPage, setHistoryPage] = useState(DEFAULT_PAGE);
  const [historyTotalPages, setHistoryTotalPages] = useState(0);
  const [assetDocuments, setAssetDocuments] = useState([]);
  const [documentsPage, setDocumentsPage] = useState(DEFAULT_PAGE);
  const [documentsTotalPages, setDocumentsTotalPages] = useState(0);
  const [documentsLoading, setDocumentsLoading] = useState(false);
  const [documentUploading, setDocumentUploading] = useState(false);
  const [documentToDelete, setDocumentToDelete] = useState(null);
  const [documentDeleting, setDocumentDeleting] = useState(false);
  const [documentForm, setDocumentForm] = useState({
    documentType: '',
    ownerType: '',
    documentDate: '',
    file: null,
  });
  const [selectedOwnerId, setSelectedOwnerId] = useState('');
  const [eligibleOwners, setEligibleOwners] = useState([]);
  const [eligibleOwnersLoading, setEligibleOwnersLoading] = useState(false);
  const [eligibleOwnersError, setEligibleOwnersError] = useState(null);
  const [selectableAssets, setSelectableAssets] = useState([]);
  const [formData, setFormData] = useState({
    name: '',
    departmentId: '',
    assetCategoryId: '',
    location: '',
    status: ASSET_STATUSES.ACTIVE,
    registrationDate: todayIsoDate(),
  });

  const canRegister = canRegisterAssets(auth?.user?.role);
  const canExport = canExportReporting(auth?.user?.role);
  const canUploadDocuments = canUploadOperationalDocuments(auth?.user?.role);

  useEffect(() => {
    if (!auth) {
      navigate(ROUTES.LOGIN);
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, navigate]);

  const loadAssets = async (page = assetsPage) => {
    try {
      setListLoading(true);
      setError(null);
      const assetPage = canUploadDocuments
        ? await assetApi.listEligibleForOperationalDocumentUpload(page)
        : await assetApi.list(page);
      const content = unwrapPageContent(assetPage);
      setAssets(content);
      setSelectableAssets(content);
      setAssetsPage(getPageNumber(assetPage, page));
      setAssetsTotalPages(getTotalPages(assetPage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load assets.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadPageData = async () => {
    try {
      setLoading(true);
      setError(null);
      const requests = [
        canUploadDocuments
          ? assetApi.listEligibleForOperationalDocumentUpload(DEFAULT_PAGE)
          : assetApi.list(DEFAULT_PAGE),
        departmentApi.list(),
        assetCategoryApi.list(),
      ];
      if (canRegister) {
        requests.push(userApi.getCurrentUser());
      }
      const [assetPage, departmentData, categoryData, profile] = await Promise.all(requests);
      const assetContent = unwrapPageContent(assetPage);
      setAssets(assetContent);
      setSelectableAssets(assetContent);
      setAssetsPage(getPageNumber(assetPage, DEFAULT_PAGE));
      setAssetsTotalPages(getTotalPages(assetPage));
      setDepartments(departmentData);
      setCategories(categoryData);
      if (profile?.departmentId) {
        const departmentId = String(profile.departmentId);
        setDepartmentLocked(true);
        setLockedDepartmentId(departmentId);
        setFormData((prev) => ({ ...prev, departmentId }));
      }
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
        departmentId: departmentLocked ? lockedDepartmentId : '',
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
        setError(getApiErrorMessage(err, 'You do not have permission to register this asset.'));
      } else {
        setError(getApiErrorMessage(err, 'Failed to register asset.'));
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  const loadAssetDetails = async (
    assetId,
    historyPageIndex = DEFAULT_PAGE,
    documentsPageIndex = DEFAULT_PAGE,
  ) => {
    try {
      setHistoryLoading(true);
      setDocumentsLoading(true);
      setError(null);
      const [historyPageResponse, documentsPageResponse] = await Promise.all([
        assetApi.getHistory(Number(assetId), historyPageIndex),
        operationalDocumentApi.list(Number(assetId), documentsPageIndex),
      ]);
      setAssetHistory(unwrapPageContent(historyPageResponse));
      setHistoryPage(getPageNumber(historyPageResponse, historyPageIndex));
      setHistoryTotalPages(getTotalPages(historyPageResponse));
      setAssetDocuments(unwrapPageContent(documentsPageResponse));
      setDocumentsPage(getPageNumber(documentsPageResponse, documentsPageIndex));
      setDocumentsTotalPages(getTotalPages(documentsPageResponse));
    } catch (err) {
      if (err.status === HTTP_STATUS.NOT_FOUND) {
        setError('Asset not found.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to load asset details.'));
      }
    } finally {
      setHistoryLoading(false);
      setDocumentsLoading(false);
    }
  };

  useEffect(() => {
    if (!selectedAssetId || !documentForm.ownerType || !canUploadDocuments) {
      setEligibleOwners([]);
      setEligibleOwnersError(null);
      return undefined;
    }

    let cancelled = false;
    setEligibleOwnersLoading(true);
    setEligibleOwnersError(null);
    operationalDocumentApi.listEligibleOwners(Number(selectedAssetId), documentForm.ownerType)
      .then((owners) => {
        if (!cancelled) {
          setEligibleOwners(owners);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setEligibleOwners([]);
          setEligibleOwnersError(getApiErrorMessage(err, 'Failed to load eligible owners.'));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setEligibleOwnersLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [selectedAssetId, documentForm.ownerType, canUploadDocuments]);

  const handleAssetHistoryChange = async (e) => {
    const assetId = e.target.value;
    setSelectedAssetId(assetId);
    setAssetHistory([]);
    setAssetDocuments([]);
    setHistoryPage(DEFAULT_PAGE);
    setHistoryTotalPages(0);
    setDocumentsPage(DEFAULT_PAGE);
    setDocumentsTotalPages(0);
    setDocumentForm({
      documentType: '',
      ownerType: '',
      documentDate: '',
      file: null,
    });
    setSelectedOwnerId('');
    setEligibleOwners([]);
    setEligibleOwnersError(null);

    if (!assetId) {
      return;
    }

    await loadAssetDetails(assetId);
  };

  const handleDocumentFormChange = (e) => {
    const { name, value } = e.target;
    setDocumentForm((prev) => ({
      ...prev,
      [name]: value,
    }));
    if (name === 'ownerType') {
      setSelectedOwnerId('');
      setEligibleOwners([]);
      setEligibleOwnersError(null);
    }
  };

  const handleOwnerSelect = (e) => {
    setSelectedOwnerId(e.target.value);
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
    if (documentForm.ownerType && !selectedOwnerId) {
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
        appendRequestPart(uploadFormData, 'ownerId', Number(selectedOwnerId));
      }
      if (documentForm.documentDate) {
        appendRequestPart(uploadFormData, 'documentDate', documentForm.documentDate);
      }
      await operationalDocumentApi.upload(Number(selectedAssetId), uploadFormData);
      setSuccess('Operational document uploaded successfully.');
      setDocumentForm({
        documentType: '',
        ownerType: '',
        documentDate: '',
        file: null,
      });
      setSelectedOwnerId('');
      setEligibleOwners([]);
      setEligibleOwnersError(null);
      const documentsPageResponse = await operationalDocumentApi.list(
        Number(selectedAssetId),
        documentsPage,
      );
      setAssetDocuments(unwrapPageContent(documentsPageResponse));
      setDocumentsPage(getPageNumber(documentsPageResponse, documentsPage));
      setDocumentsTotalPages(getTotalPages(documentsPageResponse));
      const historyPageResponse = await assetApi.getHistory(Number(selectedAssetId), historyPage);
      setAssetHistory(unwrapPageContent(historyPageResponse));
      setHistoryPage(getPageNumber(historyPageResponse, historyPage));
      setHistoryTotalPages(getTotalPages(historyPageResponse));
    } catch (err) {
      if (isUploadAuthorizationError(err)) {
        setError('You do not have permission to upload documents for this context.');
      } else if (err.status === HTTP_STATUS.NOT_FOUND) {
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

  const handleDocumentDeleteClick = (document) => {
    setDocumentToDelete(document);
  };

  const handleDocumentDeleteCancel = () => {
    if (!documentDeleting) {
      setDocumentToDelete(null);
    }
  };

  const handleDocumentDeleteConfirm = async () => {
    if (!documentToDelete || !selectedAssetId) {
      return;
    }

    try {
      setDocumentDeleting(true);
      setError(null);
      setSuccess(null);
      await operationalDocumentApi.delete(documentToDelete.id);

      let pageToLoad = documentsPage;
      if (assetDocuments.length === 1 && documentsPage > 0) {
        pageToLoad = documentsPage - 1;
      }

      const documentsPageResponse = await operationalDocumentApi.list(
        Number(selectedAssetId),
        pageToLoad,
      );
      setAssetDocuments(unwrapPageContent(documentsPageResponse));
      setDocumentsPage(getPageNumber(documentsPageResponse, pageToLoad));
      setDocumentsTotalPages(getTotalPages(documentsPageResponse));

      const historyPageResponse = await assetApi.getHistory(Number(selectedAssetId), historyPage);
      setAssetHistory(unwrapPageContent(historyPageResponse));
      setHistoryPage(getPageNumber(historyPageResponse, historyPage));
      setHistoryTotalPages(getTotalPages(historyPageResponse));

      setSuccess(`Operational document "${documentToDelete.originalFileName}" deleted.`);
      setDocumentToDelete(null);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to delete this document.');
      } else if (err.status === HTTP_STATUS.NOT_FOUND) {
        setError('Document not found.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to delete document.'));
      }
    } finally {
      setDocumentDeleting(false);
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
        <button type="button" className="back-btn" onClick={() => navigate(ROUTES.HOME)}>
          ← Back
        </button>
        <h1>Assets</h1>
        <div className="user-header-actions">
          <NotificationButton />
          {canExport && <ExportCsvButton exportType={REPORTING_EXPORT_TYPES.ASSETS} onError={setError} />}
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
            departmentLocked={departmentLocked}
            onChange={handleChange}
            onSubmit={handleSubmit}
          />
        ) : (
          <p className="read-only-note">
            Asset registration is available to Managers and Operational Coordinators.
          </p>
        )}

        <AssetList assets={assets} />
        <PaginationControls
          page={assetsPage}
          totalPages={assetsTotalPages}
          loading={listLoading}
          onPrevious={() => loadAssets(assetsPage - 1)}
          onNext={() => loadAssets(assetsPage + 1)}
        />

        <AssetHistoryPanel
          assets={canUploadDocuments ? selectableAssets : assets}
          selectedAssetId={selectedAssetId}
          selectedAsset={selectedAsset}
          assetHistory={assetHistory}
          historyLoading={historyLoading}
          historyPage={historyPage}
          historyTotalPages={historyTotalPages}
          onAssetChange={handleAssetHistoryChange}
          onHistoryPrevious={() => loadAssetDetails(selectedAssetId, historyPage - 1, documentsPage)}
          onHistoryNext={() => loadAssetDetails(selectedAssetId, historyPage + 1, documentsPage)}
        />

        <OperationalDocumentsPanel
          canUploadDocuments={canUploadDocuments}
          selectedAssetId={selectedAssetId}
          documentForm={documentForm}
          selectedOwnerId={selectedOwnerId}
          eligibleOwners={eligibleOwners}
          eligibleOwnersLoading={eligibleOwnersLoading}
          eligibleOwnersError={eligibleOwnersError}
          assetDocuments={assetDocuments}
          documentsLoading={documentsLoading}
          documentUploading={documentUploading}
          documentsPage={documentsPage}
          documentsTotalPages={documentsTotalPages}
          onDocumentFormChange={handleDocumentFormChange}
          onOwnerSelect={handleOwnerSelect}
          onDocumentFileChange={handleDocumentFileChange}
          onUpload={handleDocumentUpload}
          onDownload={handleDocumentDownload}
          onDeleteClick={handleDocumentDeleteClick}
          onDocumentsPrevious={() => loadAssetDetails(selectedAssetId, historyPage, documentsPage - 1)}
          onDocumentsNext={() => loadAssetDetails(selectedAssetId, historyPage, documentsPage + 1)}
        />
      </main>

      {documentToDelete && (
        <ConfirmDialog
          title="Delete Operational Document"
          message={`Delete document "${documentToDelete.originalFileName}"? This action cannot be undone.`}
          confirmLabel="Delete"
          onConfirm={handleDocumentDeleteConfirm}
          onCancel={handleDocumentDeleteCancel}
          isLoading={documentDeleting}
          isDangerous
        />
      )}
    </div>
  );
}
