package com.infratrack.config;

import com.infratrack.exception.BusinessValidationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationSupport {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private PaginationSupport() {
    }

    public static int normalizePage(Integer page) {
        if (page == null) {
            return 0;
        }
        if (page < 0) {
            throw new BusinessValidationException("Page index must be greater than or equal to 0.");
        }
        return page;
    }

    public static int normalizeSize(Integer size) {
        int resolvedSize = size != null ? size : DEFAULT_PAGE_SIZE;
        if (resolvedSize <= 0) {
            throw new BusinessValidationException("Page size must be greater than 0.");
        }
        return Math.min(resolvedSize, MAX_PAGE_SIZE);
    }

    public static Pageable pageable(Integer page, Integer size, Sort defaultSort) {
        int pageNumber = normalizePage(page);
        int pageSize = normalizeSize(size);
        Sort sort = defaultSort != null ? defaultSort : Sort.unsorted();
        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
