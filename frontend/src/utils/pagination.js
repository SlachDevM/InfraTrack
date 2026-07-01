export {
  PAGINATION,
  DEFAULT_PAGE,
  DEFAULT_PAGE_SIZE,
  DEFAULT_SIZE,
  MAX_PAGE_SIZE,
} from '../constants/pagination';

import { DEFAULT_PAGE, DEFAULT_SIZE } from '../constants/pagination';

export function paginatedQuery(page = DEFAULT_PAGE, size = DEFAULT_SIZE) {
  return `page=${page}&size=${size}`;
}

export function unwrapPageContent(pageResponse) {
  return pageResponse?.content ?? [];
}

export function createEmptyPageResponse() {
  return {
    content: [],
    number: DEFAULT_PAGE,
    totalPages: 0,
    first: true,
    last: true,
  };
}

export function getPageNumber(pageResponse, fallback = DEFAULT_PAGE) {
  return pageResponse?.number ?? fallback;
}

export function getTotalPages(pageResponse) {
  return pageResponse?.totalPages ?? 0;
}

export function isFirstPage(pageIndex) {
  return pageIndex <= DEFAULT_PAGE;
}

export function isLastPage(pageIndex, totalPages) {
  return totalPages === 0 || pageIndex >= totalPages - 1;
}
