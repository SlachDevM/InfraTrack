import { describe, it, expect } from 'vitest';
import {
  DEFAULT_PAGE,
  DEFAULT_SIZE,
  paginatedQuery,
  unwrapPageContent,
} from '../../utils/pagination';

describe('paginatedQuery', () => {
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
