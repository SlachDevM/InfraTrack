export const DEFAULT_PAGE = 0;
export const DEFAULT_SIZE = 20;

export function paginatedQuery(page = DEFAULT_PAGE, size = DEFAULT_SIZE) {
  return `page=${page}&size=${size}`;
}

export function unwrapPageContent(pageResponse) {
  return pageResponse?.content ?? [];
}
