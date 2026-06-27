package com.infratrack.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

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
    void normalizeSize_shouldEnforceMinimumOfOne() {
        assertThat(PaginationSupport.normalizeSize(0)).isEqualTo(1);
    }
}
