import { buildTriggerConfiguration, normalizePlanCode } from '../../utils/planTriggerConfiguration';

export const DEFAULT_FORM = {
  planCode: '',
  version: '1',
  name: '',
  description: '',
  assetId: '',
  status: 'DRAFT',
  priority: 'MEDIUM',
  targetAction: 'CREATE_INSPECTION',
  inspectionTemplateId: '',
  triggerType: 'TIME',
  timeEvery: '1',
  timeUnit: 'MONTH',
  meterType: 'OPERATING_HOURS',
  meterEvery: '250',
  eventType: 'COMPLETION_REVIEW',
  triggerActive: true,
};

export function formatTimestamp(timestamp) {
  if (!timestamp) {
    return '-';
  }
  return new Date(timestamp).toLocaleString();
}

export function buildPayload(formData, editing) {
  const payload = {
    name: formData.name.trim(),
    description: formData.description.trim() || undefined,
    assetId: Number(formData.assetId),
    status: formData.status,
    priority: formData.priority,
    targetAction: formData.targetAction,
    inspectionTemplateId: formData.inspectionTemplateId
      ? Number(formData.inspectionTemplateId)
      : undefined,
    businessTrigger: {
      triggerType: formData.triggerType,
      configurationJson: buildTriggerConfiguration(formData.triggerType, formData),
      active: formData.triggerActive,
    },
  };
  if (editing) {
    payload.version = Number(formData.version);
  } else {
    payload.planCode = normalizePlanCode(formData.planCode);
    if (formData.version) {
      payload.version = Number(formData.version);
    }
  }
  return payload;
}
