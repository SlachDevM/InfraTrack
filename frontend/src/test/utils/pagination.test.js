import { describe, it, expect } from 'vitest';
import {
  PAGINATION,
  DEFAULT_PAGE,
  DEFAULT_SIZE,
  paginatedQuery,
  unwrapPageContent,
  getPageNumber,
  getTotalPages,
  isFirstPage,
  isLastPage,
} from '../../utils/pagination';

describe('paginatedQuery', () => {
  it('exposes pagination configuration constants', () => {
    expect(PAGINATION.DEFAULT_PAGE).toBe(DEFAULT_PAGE);
    expect(PAGINATION.DEFAULT_PAGE_SIZE).toBe(DEFAULT_SIZE);
    expect(PAGINATION.MAX_PAGE_SIZE).toBe(100);
  });

  it('builds default page query', () => {
    expect(paginatedQuery()).toBe(`page=${DEFAULT_PAGE}&size=${DEFAULT_SIZE}`);
  });

  it('builds custom page query', () => {
    expect(paginatedQuery(2, 50)).toBe('page=2&size=50');
  });
});

describe('unwrapPageContent', () => {
  it('returns content from Page response', () => {
    const page = { content: [{ id: 1 }, { id: 2 }], totalElements: 2 };
    expect(unwrapPageContent(page)).toEqual([{ id: 1 }, { id: 2 }]);
  });

  it('returns empty array for empty content', () => {
    expect(unwrapPageContent({ content: [] })).toEqual([]);
  });

  it('returns empty array for null or malformed response', () => {
    expect(unwrapPageContent(null)).toEqual([]);
    expect(unwrapPageContent(undefined)).toEqual([]);
    expect(unwrapPageContent({})).toEqual([]);
  });
});

describe('page metadata helpers', () => {
  const page = { content: [{ id: 1 }], number: 2, totalPages: 5 };

  it('reads page number and total pages', () => {
    expect(getPageNumber(page)).toBe(2);
    expect(getTotalPages(page)).toBe(5);
  });

  it('detects first and last page', () => {
    expect(isFirstPage(0)).toBe(true);
    expect(isFirstPage(1)).toBe(false);
    expect(isLastPage(0, 1)).toBe(true);
    expect(isLastPage(0, 5)).toBe(false);
    expect(isLastPage(4, 5)).toBe(true);
  });
});
