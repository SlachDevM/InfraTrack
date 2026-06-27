package com.infratrack.config;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationSupport {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private PaginationSupport() {
    }

    public static int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    public static Pageable pageable(Integer page, Integer size, Sort defaultSort) {
        int pageNumber = page != null ? Math.max(page, 0) : 0;
        int pageSize = normalizeSize(size != null ? size : DEFAULT_PAGE_SIZE);
        Sort sort = defaultSort != null ? defaultSort : Sort.unsorted();
        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
