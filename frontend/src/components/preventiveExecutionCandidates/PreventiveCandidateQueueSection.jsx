import PreventiveCandidateFilters from './PreventiveCandidateFilters';
import PreventiveCandidateList from './PreventiveCandidateList';

export default function PreventiveCandidateQueueSection({
  canGenerate,
  generating,
  onGenerate,
  filterStatus,
  filterAssetId,
  filterPlanId,
  assets,
  plans,
  onFilterChange,
  candidates,
  listLoading,
  canReview,
  onViewDetail,
  onApprove,
  onReject,
  onDismiss,
}) {
  return (
    <section className="reference-form-section">
      <div className="section-header">
        <h2>Candidate Queue</h2>
        {canGenerate && (
          <button type="button" className="btn-primary" onClick={onGenerate} disabled={generating}>
            {generating ? 'Generating...' : 'Generate Candidates'}
          </button>
        )}
      </div>

      <p className="section-description">
        Review eligible preventive maintenance plans and decide whether to create inspections.
        Automation and scheduling remain out of scope.
      </p>

      <PreventiveCandidateFilters
        filterStatus={filterStatus}
        filterAssetId={filterAssetId}
        filterPlanId={filterPlanId}
        assets={assets}
        plans={plans}
        onFilterChange={onFilterChange}
      />

      <PreventiveCandidateList
        candidates={candidates}
        listLoading={listLoading}
        canReview={canReview}
        onViewDetail={onViewDetail}
        onApprove={onApprove}
        onReject={onReject}
        onDismiss={onDismiss}
      />
    </section>
  );
}
