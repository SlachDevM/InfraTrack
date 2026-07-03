import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import PaginationControls from '../components/PaginationControls';
import PreventivePlanFilters from '../components/preventiveMaintenancePlans/PreventivePlanFilters';
import PreventivePlanForm from '../components/preventiveMaintenancePlans/PreventivePlanForm';
import PreventivePlanList from '../components/preventiveMaintenancePlans/PreventivePlanList';
import { usePreventiveMaintenancePlansPage } from '../hooks/usePreventiveMaintenancePlansPage';

export default function PreventiveMaintenancePlansPage() {
  const page = usePreventiveMaintenancePlansPage();

  if (page.loading) {
    return <div className="loading">Loading preventive maintenance plans...</div>;
  }

  return (
    <ReferenceDataLayout title="Preventive Maintenance Plans">
      {page.error && <div className="error-message">{page.error}</div>}
      {page.success && <div className="success-message">{page.success}</div>}

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
