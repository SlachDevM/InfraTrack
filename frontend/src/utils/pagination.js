export const DEFAULT_PAGE = 0;
export const DEFAULT_SIZE = 20;
export const MAX_PAGE_SIZE = 100;

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
  return pageIndex <= 0;
}

export function isLastPage(pageIndex, totalPages) {
  return totalPages === 0 || pageIndex >= totalPages - 1;
}
