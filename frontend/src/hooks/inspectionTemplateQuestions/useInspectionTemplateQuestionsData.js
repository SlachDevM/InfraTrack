import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import apiClient from '../../services/apiClient';
import inspectionTemplateApi from '../../services/inspectionTemplateApi';
import inspectionTemplateQuestionApi from '../../services/inspectionTemplateQuestionApi';
import unitOfMeasureApi from '../../services/unitOfMeasureApi';
import {
  canManageInspectionTemplates,
  canViewInspectionTemplates,
} from '../../constants/userRoles';
import { ROUTES } from '../../constants/routes';
import { getApiErrorMessage } from '../../utils/apiError';
import { isDraftTemplate } from '../../pages/inspectionTemplateQuestions/constants';

export function useInspectionTemplateQuestionsData() {
  const { templateId } = useParams();
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [template, setTemplate] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [unitsOfMeasure, setUnitsOfMeasure] = useState([]);

  const canView = canViewInspectionTemplates(auth?.user?.role);
  const canManage = canManageInspectionTemplates(auth?.user?.role);
  const canMutate = canManage && isDraftTemplate(template);
  const activeQuestions = useMemo(
    () => questions.filter((question) => question.active),
    [questions]
  );

  useEffect(() => {
    if (!auth) {
      navigate(ROUTES.LOGIN);
      return;
    }
    if (!canView) {
      navigate(ROUTES.HOME);
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, canView, navigate, templateId]);

  const loadPageData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [templateData, questionList, unitList] = await Promise.all([
        inspectionTemplateApi.get(templateId),
        inspectionTemplateQuestionApi.list(templateId),
        unitOfMeasureApi.list({ active: true }),
      ]);
      setTemplate(templateData);
      setQuestions(questionList);
      setUnitsOfMeasure(unitList);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load checklist questions.'));
    } finally {
      setLoading(false);
    }
  };

  return {
    templateId,
    template,
    questions,
    setQuestions,
    loading,
    error,
    setError,
    success,
    setSuccess,
    unitsOfMeasure,
    loadPageData,
    canView,
    canManage,
    canMutate,
    activeQuestions,
    isDraft: isDraftTemplate(template),
  };
}
