import { describe, it, expect } from 'vitest';
import {
  buildTriggerConfiguration,
  buildTriggerSummaryPreview,
  isValidPlanCode,
  normalizePlanCode,
  parseTriggerConfiguration,
} from '../../utils/planTriggerConfiguration';

describe('planTriggerConfiguration', () => {
  it('normalizes and validates plan codes', () => {
    expect(normalizePlanCode('pump-monthly')).toBe('PUMP_MONTHLY');
    expect(isValidPlanCode('PUMP_MONTHLY')).toBe(true);
    expect(isValidPlanCode('1PUMP')).toBe(false);
  });

  it('builds TIME trigger configuration', () => {
    const json = buildTriggerConfiguration('TIME', {
      timeEvery: '1',
      timeUnit: 'MONTH',
    });

    expect(JSON.parse(json)).toEqual({ every: 1, unit: 'MONTH' });
    expect(buildTriggerSummaryPreview('TIME', { timeEvery: '1', timeUnit: 'MONTH' })).toBe(
      'Every month'
    );
  });

  it('builds METER trigger configuration', () => {
    const json = buildTriggerConfiguration('METER', {
      meterType: 'OPERATING_HOURS',
      meterEvery: '250',
    });

    expect(JSON.parse(json)).toEqual({ meter: 'OPERATING_HOURS', every: 250 });
    expect(
      buildTriggerSummaryPreview('METER', {
        meterType: 'OPERATING_HOURS',
        meterEvery: '250',
      })
    ).toBe('Every 250 operating hours');
  });

  it('builds EVENT trigger configuration', () => {
    const json = buildTriggerConfiguration('EVENT', {
      eventType: 'COMPLETION_REVIEW',
    });

    expect(JSON.parse(json)).toEqual({ event: 'COMPLETION_REVIEW' });
    expect(buildTriggerSummaryPreview('EVENT', { eventType: 'COMPLETION_REVIEW' })).toBe(
      'After Completion Review'
    );
  });

  it('parses trigger configuration into form fields', () => {
    const fields = parseTriggerConfiguration('METER', '{"meter":"OPERATING_HOURS","every":250}');

    expect(fields.meterType).toBe('OPERATING_HOURS');
    expect(fields.meterEvery).toBe('250');
  });
});
