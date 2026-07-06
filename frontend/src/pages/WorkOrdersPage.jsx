import { Link } from 'react-router-dom';
import PaginationControls from '../components/PaginationControls';
import CreateWorkOrderForm from '../components/workorders/CreateWorkOrderForm';
import AssignWorkOrderForm from '../components/workorders/AssignWorkOrderForm';
import CompleteMaintenanceForm from '../components/workorders/CompleteMaintenanceForm';
import WorkOrderList from '../components/workorders/WorkOrderList';
import WorkOrderPageHeader from '../components/workorders/WorkOrderPageHeader';
import CompletionReviewForm from '../components/workorders/CompletionReviewForm';
import {
  PageErrorMessage,
  PageSuccessMessage,
  ListLoadingIndicator,
} from '../components/PageFeedback';
import { canExportReporting } from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import { useAuth } from '../context/AuthContext';
import { useWorkOrdersPage } from '../hooks/useWorkOrdersPage';
import '../styles/ReferenceDataPage.css';
import '../styles/WorkOrdersPage.css';

export default function WorkOrdersPage() {
  const { auth } = useAuth();
  const page = useWorkOrdersPage();
  const canExport = canExportReporting(auth?.user?.role);

  if (page.loading) {
    return (
      <div className="loading" role="status">
        Loading work orders...
      </div>
    );
  }

  return (
    <div className="reference-data-page work-orders-page">
      <WorkOrderPageHeader
        canExport={canExport}
        onExportError={page.setError}
        onExportSuccess={page.setSuccess}
        onNavigateHome={() => page.navigate(ROUTES.HOME)}
        onLogout={page.handleLogout}
      />

      <main className="reference-content work-orders-content">
        <PageErrorMessage message={page.error} />
        <PageSuccessMessage message={page.success}>
          {page.showReworkDecisionLink && (
            <>
              {' '}
              <Link to="/operational-decisions">Go to Operational Decisions</Link>
            </>
          )}
        </PageSuccessMessage>

        {page.canCreate ? (
          <CreateWorkOrderForm
            formData={page.formData}
            eligibleDecisions={page.decisions}
            selectedDecision={page.selectedDecision}
            submitting={page.submitting}
            onChange={page.handleChange}
            onSubmit={page.handleSubmit}
          />
        ) : (
          <p className="read-only-note">
            Work order creation is available to Operational Coordinators.
          </p>
        )}

        {page.canAssign ? (
          <AssignWorkOrderForm
            assignFormData={page.assignFormData}
            createdWorkOrders={page.assignableWorkOrders}
            selectedAssignWorkOrder={page.selectedAssignWorkOrder}
            eligibleAssignees={page.eligibleAssignees}
            assigning={page.assigning}
            onChange={page.handleAssignChange}
            onSubmit={page.handleAssignSubmit}
          />
        ) : (
          <p className="read-only-note">
            Work order assignment is available to Operational Coordinators.
          </p>
        )}

        {page.canComplete ? (
          <CompleteMaintenanceForm
            completeFormData={page.completeFormData}
            assignedWorkOrdersForCurrentUser={page.assignedWorkOrdersForCurrentUser}
            selectedCompleteWorkOrder={page.selectedCompleteWorkOrder}
            completing={page.completing}
            onChange={page.handleCompleteChange}
            onSubmit={page.handleCompleteSubmit}
          />
        ) : (
          <p className="read-only-note">
            Maintenance completion is available to assigned Field Employees and Contractors.
          </p>
        )}

        {page.canReview ? (
          <CompletionReviewForm
            reviewFormData={page.reviewFormData}
            reviewableMaintenanceActivities={page.reviewableMaintenanceActivities}
            selectedReviewActivity={page.selectedReviewActivity}
            isReworkRequired={page.isReworkRequired}
            reviewing={page.reviewing}
            onChange={page.handleReviewChange}
            onSubmit={page.handleReviewSubmit}
          />
        ) : (
          <p className="read-only-note">Completion review is available to Managers.</p>
        )}

        <WorkOrderList
          workOrders={page.workOrders}
          maintenanceActivities={page.maintenanceActivities}
        />
        {page.listLoading && <ListLoadingIndicator />}
        <PaginationControls
          page={page.workOrdersPage}
          totalPages={page.workOrdersTotalPages}
          loading={page.listLoading}
          onPrevious={page.goToPreviousPage}
          onNext={page.goToNextPage}
        />
      </main>
    </div>
  );
}
