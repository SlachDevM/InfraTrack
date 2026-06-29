package com.infratrack.config;

import com.infratrack.asset.AssetController;
import com.infratrack.asset.AssetService;
import com.infratrack.asset.dto.AssetSummaryResponse;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.inspection.InspectionController;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionSummaryResponse;
import com.infratrack.issue.IssueController;
import com.infratrack.issue.IssueService;
import com.infratrack.issue.dto.IssueResponse;
import com.infratrack.operationaldocument.OperationalDocumentController;
import com.infratrack.operationaldocument.OperationalDocumentService;
import com.infratrack.operationaldocument.dto.OperationalDocumentSummaryResponse;
import com.infratrack.workorder.WorkOrderController;
import com.infratrack.workorder.WorkOrderService;
import com.infratrack.workorder.dto.WorkOrderSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaginationControllerValidationTest {

    @Mock
    private AssetService assetService;

    @Mock
    private WorkOrderService workOrderService;

    @Mock
    private InspectionService inspectionService;

    @Mock
    private IssueService issueService;

    @Mock
    private OperationalDocumentService operationalDocumentService;

    @InjectMocks
    private AssetController assetController;

    @InjectMocks
    private WorkOrderController workOrderController;

    @InjectMocks
    private InspectionController inspectionController;

    @InjectMocks
    private IssueController issueController;

    @InjectMocks
    private OperationalDocumentController operationalDocumentController;

    static Stream<PaginatedListEndpoint> paginatedEndpoints() {
        return Stream.of(
                new PaginatedListEndpoint("Assets", (page, size) -> {
                    AssetController controller = new AssetController(null);
                    controller.listAssets(page, size, null, null);
                }),
                new PaginatedListEndpoint("Work Orders", (page, size) -> {
                    WorkOrderController controller = new WorkOrderController(null);
                    controller.listWorkOrders(page, size, null, null);
                }),
                new PaginatedListEndpoint("Inspections", (page, size) -> {
                    InspectionController controller = new InspectionController(null, null);
                    controller.listInspections(page, size, null, null);
                }),
                new PaginatedListEndpoint("Issues", (page, size) -> {
                    IssueController controller = new IssueController(null);
                    controller.listIssues(page, size, null, null);
                }),
                new PaginatedListEndpoint("Operational Documents", (page, size) -> {
                    OperationalDocumentController controller = new OperationalDocumentController(null);
                    controller.listDocuments(1L, page, size, null, null, null);
                })
        );
    }

    @ParameterizedTest
    @MethodSource("paginatedEndpoints")
    void paginatedEndpoints_shouldRejectNegativePage(PaginatedListEndpoint endpoint) {
        assertThatThrownBy(() -> endpoint.invoke(-1, 20))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page index must be greater than or equal to 0.");
    }

    @ParameterizedTest
    @MethodSource("paginatedEndpoints")
    void paginatedEndpoints_shouldRejectZeroSize(PaginatedListEndpoint endpoint) {
        assertThatThrownBy(() -> endpoint.invoke(0, 0))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page size must be greater than 0.");
    }

    @ParameterizedTest
    @MethodSource("paginatedEndpoints")
    void paginatedEndpoints_shouldRejectNegativeSize(PaginatedListEndpoint endpoint) {
        assertThatThrownBy(() -> endpoint.invoke(0, -5))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page size must be greater than 0.");
    }

    @Test
    void assetController_shouldReturnEmptyPageWhenPageIsBeyondTotal() {
        Page<AssetSummaryResponse> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(20), 0);
        when(assetService.listPage(any(Pageable.class))).thenReturn(emptyPage);

        ResponseEntity<Page<AssetSummaryResponse>> response = assetController.listAssets(999, 20, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        verify(assetService).listPage(any(Pageable.class));
    }

    @Test
    void workOrderController_shouldClampExcessiveSizeToOneHundred() {
        Page<WorkOrderSummaryResponse> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(100), 0);
        when(workOrderService.listPage(any(Pageable.class))).thenReturn(emptyPage);

        workOrderController.listWorkOrders(0, 999, null, null);

        verify(workOrderService).listPage(org.mockito.ArgumentMatchers.argThat(
                pageable -> pageable.getPageSize() == 100));
    }

    @Test
    void inspectionController_shouldAcceptValidPagination() {
        Page<InspectionSummaryResponse> page = new PageImpl<>(List.of(), Pageable.ofSize(20), 0);
        when(inspectionService.listPage(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<InspectionSummaryResponse>> response =
                inspectionController.listInspections(1, 20, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(inspectionService).listPage(any(Pageable.class));
    }

    @Test
    void issueController_shouldAcceptValidPagination() {
        Page<IssueResponse> page = new PageImpl<>(List.of(), Pageable.ofSize(20), 0);
        when(issueService.listPage(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<IssueResponse>> response = issueController.listIssues(0, 20, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(issueService).listPage(any(Pageable.class));
    }

    @Test
    void operationalDocumentController_shouldAcceptValidPagination() {
        Page<OperationalDocumentSummaryResponse> page = new PageImpl<>(List.of(), Pageable.ofSize(20), 0);
        when(operationalDocumentService.listDocuments(eq(1L), any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> response = operationalDocumentController.listDocuments(
                1L, 0, 20, null, null, null);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(operationalDocumentService).listDocuments(eq(1L), any(Pageable.class));
    }

    private record PaginatedListEndpoint(String name, PaginatedListInvoker invoker) {
        void invoke(Integer page, Integer size) {
            invoker.invoke(page, size);
        }
    }

    @FunctionalInterface
    private interface PaginatedListInvoker {
        void invoke(Integer page, Integer size);
    }
}
