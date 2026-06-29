import { PLAN_TIME_UNITS } from '../constants/planTimeUnits';
import { PLAN_METER_TYPES } from '../constants/planMeterTypes';
import { PLAN_EVENT_TYPES } from '../constants/planEventTypes';

const PLAN_CODE_PATTERN = /^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$/;

export function normalizePlanCode(planCode) {
  return String(planCode || '').trim().toUpperCase().replace(/-/g, '_').replace(/\s+/g, '_');
}

export function isValidPlanCode(planCode) {
  const normalized = normalizePlanCode(planCode);
  return PLAN_CODE_PATTERN.test(normalized);
}

export function buildTriggerConfiguration(triggerType, formData) {
  if (triggerType === 'TIME') {
    return JSON.stringify({
      every: Number(formData.timeEvery),
      unit: formData.timeUnit,
    });
  }
  if (triggerType === 'METER') {
    return JSON.stringify({
      meter: formData.meterType,
      every: Number(formData.meterEvery),
    });
  }
  return JSON.stringify({
    event: formData.eventType,
  });
}

export function parseTriggerConfiguration(triggerType, configurationJson) {
  const defaults = {
    timeEvery: '1',
    timeUnit: PLAN_TIME_UNITS.MONTH,
    meterType: PLAN_METER_TYPES.OPERATING_HOURS,
    meterEvery: '250',
    eventType: PLAN_EVENT_TYPES.COMPLETION_REVIEW,
  };

  if (!configurationJson) {
    return defaults;
  }

  try {
    const parsed = JSON.parse(configurationJson);
    if (triggerType === 'TIME') {
      return {
        ...defaults,
        timeEvery: String(parsed.every ?? defaults.timeEvery),
        timeUnit: parsed.unit ?? defaults.timeUnit,
      };
    }
    if (triggerType === 'METER') {
      return {
        ...defaults,
        meterType: parsed.meter ?? defaults.meterType,
        meterEvery: String(parsed.every ?? defaults.meterEvery),
      };
    }
    return {
      ...defaults,
      eventType: parsed.event ?? defaults.eventType,
    };
  } catch {
    return defaults;
  }
}

export function buildTriggerSummaryPreview(triggerType, formData) {
  if (triggerType === 'TIME') {
    const every = Number(formData.timeEvery);
    const unit = formData.timeUnit;
    if (!every || every <= 0 || !unit) {
      return '';
    }
    if (every === 1) {
      const singular = {
        [PLAN_TIME_UNITS.DAY]: 'day',
        [PLAN_TIME_UNITS.WEEK]: 'week',
        [PLAN_TIME_UNITS.MONTH]: 'month',
        [PLAN_TIME_UNITS.YEAR]: 'year',
      }[unit];
      return singular ? `Every ${singular}` : '';
    }
    const plural = {
      [PLAN_TIME_UNITS.DAY]: 'days',
      [PLAN_TIME_UNITS.WEEK]: 'weeks',
      [PLAN_TIME_UNITS.MONTH]: 'months',
      [PLAN_TIME_UNITS.YEAR]: 'years',
    }[unit];
    return plural ? `Every ${every} ${plural}` : '';
  }

  if (triggerType === 'METER') {
    const every = Number(formData.meterEvery);
    if (!every || every <= 0 || formData.meterType !== PLAN_METER_TYPES.OPERATING_HOURS) {
      return '';
    }
    return `Every ${every} operating hours`;
  }

  const eventLabel = {
    [PLAN_EVENT_TYPES.COMPLETION_REVIEW]: 'Completion Review',
    [PLAN_EVENT_TYPES.INSPECTION_COMPLETED]: 'Inspection Completed',
    [PLAN_EVENT_TYPES.WORK_ORDER_COMPLETED]: 'Work Order Completed',
    [PLAN_EVENT_TYPES.MAINTENANCE_COMPLETED]: 'Maintenance Completed',
  }[formData.eventType];

  return eventLabel ? `After ${eventLabel}` : '';
}
