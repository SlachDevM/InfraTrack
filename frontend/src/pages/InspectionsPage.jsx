import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import inspectionApi from '../services/inspectionApi';
import businessTriggerApi from '../services/businessTriggerApi';
import assetApi from '../services/assetApi';
import userApi from '../services/userApi';
import inspectionTemplateApi from '../services/inspectionTemplateApi';
import inspectionTemplateQuestionApi from '../services/inspectionTemplateQuestionApi';
import NotificationButton from '../components/NotificationButton';
import PaginationControls from '../components/PaginationControls';
import AssignInspectionForm from '../components/inspections/AssignInspectionForm';
import CompleteInspectionForm from '../components/inspections/CompleteInspectionForm';
import InspectionList from '../components/inspections/InspectionList';
import ExportCsvButton from '../components/ExportCsvButton';
import { INSPECTION_STATUS } from '../constants/statuses';
import { REPORTING_EXPORT_TYPES } from '../constants/reportingExports';
import { ROUTES } from '../constants/routes';
import {
  canAssignInspections,
  canPerformInspections,
  canExportReporting,
} from '../constants/userRoles';
import { INSPECTION_PRIORITIES } from '../constants/inspectionPriorities';
import { PHYSICAL_CONDITIONS } from '../constants/physicalConditions';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import { filterInspectionAssignees } from '../utils/inspectionAssignees';
import {
  buildInspectionAnswerPayload,
  isSupportedInspectionAnswerType,
  validateRequiredTemplateAnswers,
  validateTemplateAnswerValues,
} from '../utils/inspectionAnswers';
import {
  DEFAULT_PAGE,
  MAX_PAGE_SIZE,
  getPageNumber,
  getTotalPages,
  unwrapPageContent,
} from '../utils/pagination';
import '../styles/ReferenceDataPage.css';
import '../styles/InspectionsPage.css';

function toDateTimeLocalValue(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export default function InspectionsPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [inspections, setInspections] = useState([]);
  const [inspectionsPage, setInspectionsPage] = useState(DEFAULT_PAGE);
  const [inspectionsTotalPages, setInspectionsTotalPages] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [triggers, setTriggers] = useState([]);
  const [assets, setAssets] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [publishedTemplates, setPublishedTemplates] = useState([]);
  const [templateQuestionsByTemplateId, setTemplateQuestionsByTemplateId] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [completingId, setCompletingId] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState({
    businessTriggerId: '',
    assignedToUserId: '',
    inspectionTemplateId: '',
    priority: INSPECTION_PRIORITIES.NORMAL,
    expectedCompletionDate: '',
  });
  const [completeFormData, setCompleteFormData] = useState({
    observedCondition: PHYSICAL_CONDITIONS.GOOD,
    observations: '',
    issueIdentified: false,
    completedAt: toDateTimeLocalValue(),
  });
  const [answersByInspectionId, setAnswersByInspectionId] = useState({});

  const canAssign = canAssignInspections(auth?.user?.role);
  const canPerform = canPerformInspections(auth?.user?.role);
  const canExport = canExportReporting(auth?.user?.role);
  const currentUserId = auth?.user?.userId;

  const myAssignedInspections = useMemo(
    () =>
      inspections.filter(
        (inspection) =>
          inspection.status === INSPECTION_STATUS.ASSIGNED &&
          String(inspection.assignedToUserId) === String(currentUserId)
      ),
    [inspections, currentUserId]
  );

  const selectedTrigger = useMemo(
    () => triggers.find((trigger) => String(trigger.id) === String(formData.businessTriggerId)),
    [triggers, formData.businessTriggerId]
  );

  const selectedAssetCategoryId = useMemo(() => {
    if (!selectedTrigger) {
      return null;
    }
    const asset = assets.find((item) => item.id === selectedTrigger.assetId);
    return asset?.assetCategoryId ?? null;
  }, [assets, selectedTrigger]);

  const eligiblePublishedTemplates = useMemo(
    () =>
      publishedTemplates.filter(
        (template) =>
          template.status === 'PUBLISHED' &&
          selectedAssetCategoryId != null &&
          template.assetCategoryId === selectedAssetCategoryId
      ),
    [publishedTemplates, selectedAssetCategoryId]
  );

  useEffect(() => {
    if (!auth) {
      navigate(ROUTES.LOGIN);
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, navigate]);

  useEffect(() => {
    if (!canAssign || selectedAssetCategoryId == null) {
      setPublishedTemplates([]);
      return;
    }
    inspectionTemplateApi
      .list(DEFAULT_PAGE, MAX_PAGE_SIZE, {
        assetCategoryId: selectedAssetCategoryId,
        status: 'PUBLISHED',
      })
      .then((page) => setPublishedTemplates(unwrapPageContent(page)))
      .catch(() => setPublishedTemplates([]));
  }, [canAssign, selectedAssetCategoryId]);

  useEffect(() => {
    myAssignedInspections
      .filter((inspection) => inspection.inspectionTemplateId)
      .forEach((inspection) => {
        const templateId = inspection.inspectionTemplateId;
        if (Object.prototype.hasOwnProperty.call(templateQuestionsByTemplateId, templateId)) {
          return;
        }
        inspectionTemplateQuestionApi
          .list(templateId)
          .then((questions) => {
            const questionList = Array.isArray(questions) ? questions : [];
            setTemplateQuestionsByTemplateId((prev) => ({
              ...prev,
              [templateId]: questionList.filter((question) => question.active),
            }));
          })
          .catch(() => {
            setTemplateQuestionsByTemplateId((prev) => ({ ...prev, [templateId]: [] }));
          });
      });
  }, [myAssignedInspections, templateQuestionsByTemplateId]);

  const loadInspections = async (page = inspectionsPage) => {
    try {
      setListLoading(true);
      const inspectionPage = await inspectionApi.list(page);
      setInspections(unwrapPageContent(inspectionPage));
      setInspectionsPage(getPageNumber(inspectionPage, page));
      setInspectionsTotalPages(getTotalPages(inspectionPage));
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load inspections.'));
    } finally {
      setListLoading(false);
    }
  };

  const loadPageData = async (page = inspectionsPage) => {
    try {
      setLoading(true);
      setError(null);
      const canAssignRole = canAssignInspections(auth?.user?.role);
      const [inspectionPage, triggerPage, assetPage] = await Promise.all([
        inspectionApi.list(page),
        businessTriggerApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE),
        canAssignRole ? assetApi.list(DEFAULT_PAGE, MAX_PAGE_SIZE) : Promise.resolve(null),
      ]);
      setInspections(unwrapPageContent(inspectionPage));
      setInspectionsPage(getPageNumber(inspectionPage, page));
      setInspectionsTotalPages(getTotalPages(inspectionPage));

      let loadedTriggers = unwrapPageContent(triggerPage);
      let profile = null;
      if (canAssignRole && assetPage) {
        const loadedAssets = unwrapPageContent(assetPage);
        setAssets(loadedAssets);
        profile = await userApi.getCurrentUser();
        if (profile?.departmentId != null) {
          const departmentAssetIds = new Set(
            loadedAssets
              .filter((asset) => asset.departmentId === profile.departmentId)
              .map((asset) => asset.id)
          );
          loadedTriggers = loadedTriggers.filter((trigger) =>
            departmentAssetIds.has(trigger.assetId)
          );
        }
      } else {
        setAssets([]);
      }
      setTriggers(loadedTriggers);

      if (canAssignRole) {
        const workerData = await inspectionApi.listWorkers();
        setWorkers(filterInspectionAssignees(workerData, profile?.departmentId));
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load inspections.'));
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const next = { ...prev, [name]: value };
      if (name === 'businessTriggerId') {
        const trigger = triggers.find((item) => String(item.id) === String(value));
        if (trigger?.urgent) {
          next.priority = INSPECTION_PRIORITIES.URGENT;
        }
        next.inspectionTemplateId = '';
      }
      return next;
    });
  };

  const handleCompleteChange = (e) => {
    const { name, value, type, checked } = e.target;
    setCompleteFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleAnswerChange = (inspectionId, questionId, value) => {
    setAnswersByInspectionId((prev) => ({
      ...prev,
      [inspectionId]: {
        ...(prev[inspectionId] || {}),
        [questionId]: value,
      },
    }));
  };

  const handleCompleteSubmit = async (e, inspection) => {
    e.preventDefault();
    if (!canPerform) return;

    const templateQuestions = inspection.inspectionTemplateId
      ? templateQuestionsByTemplateId[inspection.inspectionTemplateId] || []
      : [];
    const answerValues = answersByInspectionId[inspection.id] || {};
    const missingRequired = validateRequiredTemplateAnswers(templateQuestions, answerValues);
    if (missingRequired.length > 0) {
      setError('Please answer all required checklist questions before completing the inspection.');
      return;
    }

    const invalidValue = validateTemplateAnswerValues(templateQuestions, answerValues);
    if (invalidValue) {
      setError(`${invalidValue.question.code}: ${invalidValue.error}`);
      return;
    }

    try {
      setCompletingId(inspection.id);
      setError(null);
      setSuccess(null);
      const payload = {
        observedCondition: completeFormData.observedCondition,
        observations: completeFormData.observations,
        issueIdentified: completeFormData.issueIdentified,
        completedAt: `${completeFormData.completedAt}:00`,
      };
      if (inspection.inspectionTemplateId) {
        payload.answers = templateQuestions
          .filter((question) => isSupportedInspectionAnswerType(question.questionType))
          .filter((question) => {
            const value = answerValues[question.id];
            return value !== undefined && value !== null && value !== '';
          })
          .map((question) => buildInspectionAnswerPayload(question, answerValues[question.id]));
      }
      await inspectionApi.complete(inspection.id, payload);
      setSuccess('Inspection completed successfully.');
      setCompleteFormData({
        observedCondition: PHYSICAL_CONDITIONS.GOOD,
        observations: '',
        issueIdentified: false,
        completedAt: toDateTimeLocalValue(),
      });
      setAnswersByInspectionId((prev) => {
        const next = { ...prev };
        delete next[inspection.id];
        return next;
      });
      await loadPageData(inspectionsPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You are not allowed to complete this inspection.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to complete inspection.'));
      }
    } finally {
      setCompletingId(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canAssign) return;

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      const request = {
        businessTriggerId: Number(formData.businessTriggerId),
        assignedToUserId: Number(formData.assignedToUserId),
        priority: formData.priority,
      };
      if (formData.expectedCompletionDate) {
        request.expectedCompletionDate = formData.expectedCompletionDate;
      }
      if (formData.inspectionTemplateId) {
        request.inspectionTemplateId = Number(formData.inspectionTemplateId);
      }
      await inspectionApi.assign(request);
      setSuccess('Inspection assigned successfully.');
      setFormData({
        businessTriggerId: '',
        assignedToUserId: '',
        inspectionTemplateId: '',
        priority: INSPECTION_PRIORITIES.NORMAL,
        expectedCompletionDate: '',
      });
      await loadPageData(inspectionsPage);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to assign inspections.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to assign inspection.'));
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  if (loading) {
    return <div className="loading">Loading inspections...</div>;
  }

  return (
    <div className="reference-data-page inspections-page">
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
        <h1>Inspections</h1>
        <div className="user-header-actions">
          <NotificationButton />
          {canExport && (
            <ExportCsvButton exportType={REPORTING_EXPORT_TYPES.INSPECTIONS} onError={setError} />
          )}
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content inspections-content">
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {canAssign ? (
          <AssignInspectionForm
            formData={formData}
            triggers={triggers}
            workers={workers}
            selectedTrigger={selectedTrigger}
            publishedTemplates={eligiblePublishedTemplates}
            submitting={submitting}
            onChange={handleChange}
            onSubmit={handleSubmit}
          />
        ) : (
          <p className="read-only-note">
            Inspection assignment is available to Operational Coordinators.
          </p>
        )}

        {canPerform && (
          <section className="inspection-form-section">
            <h2>Perform Inspection</h2>
            {myAssignedInspections.length === 0 ? (
              <p className="read-only-note">You have no assigned inspections to complete.</p>
            ) : (
              myAssignedInspections.map((inspection) => (
                <CompleteInspectionForm
                  key={inspection.id}
                  inspection={inspection}
                  completeFormData={completeFormData}
                  templateQuestions={
                    inspection.inspectionTemplateId
                      ? templateQuestionsByTemplateId[inspection.inspectionTemplateId] || []
                      : []
                  }
                  answerValues={answersByInspectionId[inspection.id] || {}}
                  completingId={completingId}
                  onChange={handleCompleteChange}
                  onAnswerChange={(questionId, value) =>
                    handleAnswerChange(inspection.id, questionId, value)
                  }
                  onSubmit={(e) => handleCompleteSubmit(e, inspection)}
                />
              ))
            )}
          </section>
        )}

        <InspectionList inspections={inspections} />
        <PaginationControls
          page={inspectionsPage}
          totalPages={inspectionsTotalPages}
          loading={listLoading}
          onPrevious={() => loadInspections(inspectionsPage - 1)}
          onNext={() => loadInspections(inspectionsPage + 1)}
        />
      </main>
    </div>
  );
}
