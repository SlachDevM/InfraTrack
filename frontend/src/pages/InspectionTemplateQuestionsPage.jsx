import { useNavigate } from 'react-router-dom';
import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import InspectionTemplateQuestionHeader from '../components/inspectionTemplateQuestions/InspectionTemplateQuestionHeader';
import InspectionTemplateQuestionForm from '../components/inspectionTemplateQuestions/InspectionTemplateQuestionForm';
import InspectionTemplateQuestionList from '../components/inspectionTemplateQuestions/InspectionTemplateQuestionList';
import InspectionTemplateChoicePanel from '../components/inspectionTemplateQuestions/InspectionTemplateChoicePanel';
import InspectionTemplateRulePanel from '../components/inspectionTemplateQuestions/InspectionTemplateRulePanel';
import { ROUTES } from '../constants/routes';
import { useInspectionTemplateQuestionsPage } from '../hooks/useInspectionTemplateQuestionsPage';
import { PageErrorMessage, PageSuccessMessage } from '../components/PageFeedback';

export default function InspectionTemplateQuestionsPage() {
  const navigate = useNavigate();
  const page = useInspectionTemplateQuestionsPage();

  if (page.loading) {
    return (
      <div className="loading" role="status">
        Loading checklist questions...
      </div>
    );
  }

  return (
    <ReferenceDataLayout
      title="Checklist Questions"
      backLabel="← Back to Templates"
      onBack={() => navigate(ROUTES.INSPECTION_TEMPLATES)}
    >
      <PageErrorMessage message={page.error} />
      <PageSuccessMessage message={page.success} />

      {page.template && (
        <InspectionTemplateQuestionHeader
          template={page.template}
          canManage={page.canManage}
          isDraft={page.isDraft}
        />
      )}

      {page.canMutate && (
        <InspectionTemplateQuestionForm
          editingId={page.editingId}
          formData={page.formData}
          submitting={page.submitting}
          unitsOfMeasure={page.unitsOfMeasure}
          onChange={page.handleFormChange}
          onSubmit={page.handleSubmit}
          onCancelEdit={page.resetForm}
        />
      )}

      <InspectionTemplateQuestionList
        questions={page.questions}
        activeQuestions={page.activeQuestions}
        canMutate={page.canMutate}
        canView={page.canView}
        reordering={page.reordering}
        onEdit={page.handleEdit}
        onDeactivate={page.handleDeactivate}
        onMove={page.handleMove}
        onManageChoices={page.handleManageChoices}
        onManageRules={page.handleManageRules}
      />

      {page.canMutate && page.managingChoicesQuestionId != null && (
        <InspectionTemplateChoicePanel
          questionCode={page.managingChoicesQuestion?.code}
          choices={page.managingChoicesQuestion?.choices || []}
          choiceForm={page.choiceForm}
          editingChoiceId={page.editingChoiceId}
          choiceSubmitting={page.choiceSubmitting}
          onClose={page.closeChoicePanel}
          onFormChange={page.handleChoiceFormChange}
          onSubmit={page.handleChoiceSubmit}
          onEditChoice={page.handleEditChoice}
          onDeactivateChoice={page.handleDeactivateChoice}
          onMoveChoice={page.handleMoveChoice}
          onCancelEdit={page.cancelChoiceEdit}
        />
      )}

      {page.managingRulesQuestionId != null && (
        <InspectionTemplateRulePanel
          questionCode={page.managingRulesQuestion?.code}
          canMutate={page.canMutate}
          ruleForm={page.ruleForm}
          editingRuleId={page.editingRuleId}
          ruleSubmitting={page.ruleSubmitting}
          rulePayloadError={page.rulePayloadError}
          ruleOperatorOptions={page.ruleOperatorOptions}
          showComparisonValue={page.showComparisonValue}
          ruleChoiceOptions={page.ruleChoiceOptions}
          rules={page.rules}
          onClose={page.handleCloseRules}
          onFormChange={page.handleRuleFormChange}
          onSubmit={page.handleRuleSubmit}
          onEditRule={page.handleEditRule}
          onDeactivateRule={page.handleDeactivateRule}
          onCancelEdit={page.cancelRuleEdit}
        />
      )}
    </ReferenceDataLayout>
  );
}
