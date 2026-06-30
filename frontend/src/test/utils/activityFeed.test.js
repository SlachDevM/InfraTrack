import { describe, it, expect } from 'vitest';
import { formatActivityTime, formatActivityTypeLabel } from '../../utils/activityFeed';

describe('activityFeed utils', () => {
  it('formats activity type labels', () => {
    expect(formatActivityTypeLabel('INSPECTION_COMPLETED')).toBe('Inspection Completed');
    expect(formatActivityTypeLabel('SUGGESTED_ACTION_ACCEPTED')).toBe('Suggested Action Accepted');
  });

  it('formats activity timestamps', () => {
    expect(formatActivityTime(null)).toBe('');
    expect(formatActivityTime(1719792000000)).not.toBe('');
  });
});
