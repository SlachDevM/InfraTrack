package com.infratrack.config;

import com.infratrack.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationSupportTest {

    @Test
    void pageable_shouldDefaultToPageZeroAndSizeTwenty() {
        Pageable pageable = PaginationSupport.pageable(null, null, Sort.unsorted());

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    void pageable_shouldApplyRequestedPageAndSize() {
        Pageable pageable = PaginationSupport.pageable(2, 10, Sort.by("createdAt").descending());

        assertThat(pageable).isEqualTo(PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Test
    void pageable_shouldCapSizeAtOneHundred() {
        Pageable pageable = PaginationSupport.pageable(0, 500, Sort.unsorted());

        assertThat(pageable.getPageSize()).isEqualTo(100);
    }

    @Test
    void normalizePage_shouldRejectNegativePageIndex() {
        assertThatThrownBy(() -> PaginationSupport.normalizePage(-1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page index must be greater than or equal to 0.");
    }

    @Test
    void normalizeSize_shouldRejectZeroSize() {
        assertThatThrownBy(() -> PaginationSupport.normalizeSize(0))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page size must be greater than 0.");
    }

    @Test
    void normalizeSize_shouldRejectNegativeSize() {
        assertThatThrownBy(() -> PaginationSupport.normalizeSize(-5))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page size must be greater than 0.");
    }

    @Test
    void pageable_shouldRejectNegativePageIndex() {
        assertThatThrownBy(() -> PaginationSupport.pageable(-1, 20, Sort.unsorted()))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page index must be greater than or equal to 0.");
    }

    @Test
    void pageable_shouldRejectZeroSize() {
        assertThatThrownBy(() -> PaginationSupport.pageable(0, 0, Sort.unsorted()))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page size must be greater than 0.");
    }

    @Test
    void pageable_shouldRejectNegativeSize() {
        assertThatThrownBy(() -> PaginationSupport.pageable(0, -5, Sort.unsorted()))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Page size must be greater than 0.");
    }
}
