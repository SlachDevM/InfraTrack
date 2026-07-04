import { useInspectionTemplateQuestionsData } from './inspectionTemplateQuestions/useInspectionTemplateQuestionsData';
import { useInspectionTemplateQuestionMutations } from './inspectionTemplateQuestions/useInspectionTemplateQuestionMutations';
import { useInspectionTemplateQuestionChoices } from './inspectionTemplateQuestions/useInspectionTemplateQuestionChoices';
import { useInspectionTemplateQuestionRules } from './inspectionTemplateQuestions/useInspectionTemplateQuestionRules';

export function useInspectionTemplateQuestionsPage() {
  const data = useInspectionTemplateQuestionsData();

  const questionMutations = useInspectionTemplateQuestionMutations({
    templateId: data.templateId,
    canMutate: data.canMutate,
    setQuestions: data.setQuestions,
    activeQuestions: data.activeQuestions,
    setError: data.setError,
    setSuccess: data.setSuccess,
    loadPageData: data.loadPageData,
  });

  const choices = useInspectionTemplateQuestionChoices({
    templateId: data.templateId,
    canMutate: data.canMutate,
    questions: data.questions,
    setError: data.setError,
    setSuccess: data.setSuccess,
    loadPageData: data.loadPageData,
  });

  const { clearManagingChoicesQuestionId, ...choiceState } = choices;

  const rules = useInspectionTemplateQuestionRules({
    templateId: data.templateId,
    canMutate: data.canMutate,
    canView: data.canView,
    questions: data.questions,
    setError: data.setError,
    setSuccess: data.setSuccess,
    clearManagingChoicesQuestionId,
  });

  return {
    template: data.template,
    questions: data.questions,
    loading: data.loading,
    error: data.error,
    success: data.success,
    canManage: data.canManage,
    canMutate: data.canMutate,
    canView: data.canView,
    unitsOfMeasure: data.unitsOfMeasure,
    activeQuestions: data.activeQuestions,
    isDraft: data.isDraft,
    ...questionMutations,
    ...choiceState,
    ...rules,
  };
}
