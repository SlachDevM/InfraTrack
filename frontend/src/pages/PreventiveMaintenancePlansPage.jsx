import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import PaginationControls from '../components/PaginationControls';
import PreventivePlanFilters from '../components/preventiveMaintenancePlans/PreventivePlanFilters';
import PreventivePlanForm from '../components/preventiveMaintenancePlans/PreventivePlanForm';
import PreventivePlanList from '../components/preventiveMaintenancePlans/PreventivePlanList';
import { usePreventiveMaintenancePlansPage } from '../hooks/usePreventiveMaintenancePlansPage';
import {
  PageErrorMessage,
  PageSuccessMessage,
  ListLoadingIndicator,
} from '../components/PageFeedback';

export default function PreventiveMaintenancePlansPage() {
  const page = usePreventiveMaintenancePlansPage();

  if (page.loading) {
    return (
      <div className="loading" role="status">
        Loading preventive maintenance plans...
      </div>
    );
  }

  return (
    <ReferenceDataLayout title="Preventive Maintenance Plans">
      <PageErrorMessage message={page.error} />
      <PageSuccessMessage message={page.success} />

      {!page.canManage && (
        <p className="read-only-note">
          Preventive maintenance plans are read-only. Administrators can create, edit, and archive
          plans.
        </p>
      )}

      <PreventivePlanFilters
        filterAssetId={page.filterAssetId}
        filterStatus={page.filterStatus}
        filterTriggerType={page.filterTriggerType}
        assets={page.assets}
        listLoading={page.listLoading}
        onFilterChange={page.handleFilterChange}
      />

      {page.canManage && (
        <PreventivePlanForm
          editingId={page.editingId}
          formData={page.formData}
          submitting={page.submitting}
          assets={page.assets}
          templates={page.templates}
          triggerSummaryPreview={page.triggerSummaryPreview}
          onChange={page.handleFormChange}
          onSubmit={page.handleSubmit}
          onCancelEdit={page.resetForm}
        />
      )}

      <PreventivePlanList
        plans={page.plans}
        canManage={page.canManage}
        evaluationResults={page.evaluationResults}
        evaluatingId={page.evaluatingId}
        onEvaluate={page.handleEvaluate}
        onEdit={page.handleEdit}
        onArchive={page.handleArchive}
      />

      {page.listLoading && <ListLoadingIndicator />}
      <PaginationControls
        page={page.plansPage}
        totalPages={page.plansTotalPages}
        loading={page.listLoading}
        onPrevious={page.goToPreviousPage}
        onNext={page.goToNextPage}
      />
    </ReferenceDataLayout>
  );
}
