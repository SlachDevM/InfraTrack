import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import ExportReportingMenu from '../components/ExportReportingMenu';
import PaginationControls from '../components/PaginationControls';
import PreventiveCandidateQueueSection from '../components/preventiveExecutionCandidates/PreventiveCandidateQueueSection';
import PreventiveCandidateDetailPanel from '../components/preventiveExecutionCandidates/PreventiveCandidateDetailPanel';
import PreventiveCandidateApproveDialog from '../components/preventiveExecutionCandidates/PreventiveCandidateApproveDialog';
import PreventiveCandidateRejectDialog from '../components/preventiveExecutionCandidates/PreventiveCandidateRejectDialog';
import PreventiveCandidateDismissDialog from '../components/preventiveExecutionCandidates/PreventiveCandidateDismissDialog';
import { canExportReporting } from '../constants/userRoles';
import { REPORTING_EXPORT_TYPES } from '../constants/reportingExports';
import { useAuth } from '../context/AuthContext';
import { usePreventiveExecutionCandidatesPage } from '../hooks/usePreventiveExecutionCandidatesPage';

export default function PreventiveExecutionCandidatesPage() {
  const { auth } = useAuth();
  const page = usePreventiveExecutionCandidatesPage();
  const canExport = canExportReporting(auth?.user?.role);

  if (page.loading) {
    return <div className="loading">Loading preventive execution candidates...</div>;
  }

  return (
    <ReferenceDataLayout
      title="Preventive Execution Candidates"
      headerActions={
        canExport ? (
          <ExportReportingMenu
            exportType={REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES}
            onError={page.setError}
          />
        ) : null
      }
    >
      {page.error && <div className="error-message">{page.error}</div>}
      {page.success && <div className="success-message">{page.success}</div>}

      <PreventiveCandidateQueueSection
        canGenerate={page.canGenerate}
        generating={page.generating}
        onGenerate={page.handleGenerate}
        filterStatus={page.filterStatus}
        filterAssetId={page.filterAssetId}
        filterPlanId={page.filterPlanId}
        assets={page.assets}
        plans={page.plans}
        onFilterChange={page.handleFilterChange}
        candidates={page.candidates}
        listLoading={page.listLoading}
        canReview={page.canReview}
        onViewDetail={page.handleViewDetail}
        onApprove={page.openApproveDialog}
        onReject={page.openRejectDialog}
        onDismiss={page.openDismissDialog}
      />

      <PaginationControls
        page={page.candidatesPage}
        totalPages={page.candidatesTotalPages}
        loading={page.listLoading}
        onPrevious={page.goToPreviousPage}
        onNext={page.goToNextPage}
      />

      {page.selectedCandidate && (
        <PreventiveCandidateDetailPanel
          selectedCandidate={page.selectedCandidate}
          selectedReport={page.selectedReport}
          detailTab={page.detailTab}
          detailLoading={page.detailLoading}
          canReview={page.canReview}
          onTabChange={page.setDetailTab}
          onClose={page.closeDetail}
          onApprove={page.openApproveDialog}
          onReject={page.openRejectDialog}
          onDismiss={page.openDismissDialog}
        />
      )}

      {page.approveCandidate && (
        <PreventiveCandidateApproveDialog
          approveCandidate={page.approveCandidate}
          approveForm={page.approveForm}
          workers={page.workers}
          reviewing={page.reviewing}
          selectedAssigneeId={page.selectedAssigneeId}
          createdInspectionId={page.createdInspectionId}
          onSubmit={page.handleApproveSubmit}
          onCancel={() => page.setApproveCandidate(null)}
          onFormChange={page.handleApproveFormChange}
        />
      )}

      {page.rejectCandidate && (
        <PreventiveCandidateRejectDialog
          rejectCandidate={page.rejectCandidate}
          rejectReason={page.rejectReason}
          reviewing={page.reviewing}
          onSubmit={page.handleRejectSubmit}
          onCancel={() => page.setRejectCandidate(null)}
          onReasonChange={page.setRejectReason}
        />
      )}

      {page.dismissCandidate && (
        <PreventiveCandidateDismissDialog
          dismissCandidate={page.dismissCandidate}
          dismissComment={page.dismissComment}
          reviewing={page.reviewing}
          onSubmit={page.handleDismissSubmit}
          onCancel={() => page.setDismissCandidate(null)}
          onCommentChange={page.setDismissComment}
        />
      )}
    </ReferenceDataLayout>
  );
}
