export const JOB_TYPES = [
  'FULL_REGUTTER',
  'PARTIAL_REGUTTER',
  'FULL_REFASCIA',
  'PARTIAL_REFASCIA',
  'RESCREW',
  'PARTIAL_RESHEET',
  'FULL_RESHEET',
];

export const PRIORITY_LEVELS = [
  { value: '1', label: '1 - Low' },
  { value: '2', label: '2 - Medium' },
  { value: '3', label: '3 - High' },
  { value: '4', label: '4 - Urgent' },
];

export const JOB_STATUSES = {
  PENDING: 'PENDING',
  SCHEDULED: 'SCHEDULED',
  IN_PROGRESS: 'IN_PROGRESS',
  READY_FOR_CONFIRMATION: 'READY_FOR_CONFIRMATION',
  DONE: 'DONE',
  TO_BE_FIXED: 'TO_BE_FIXED',
  ARCHIVED: 'ARCHIVED',
};

export const DEFAULT_START_HOUR = '07:50';

export const EMPTY_JOB_FORM = {
  clientName: '',
  clientPhone: '',
  clientAddress: '',
  details: '',
  notes: '',
  priorityLevel: '1',
  jobDate: '',
  jobStartHour: DEFAULT_START_HOUR,
};

export const PRIORITY_COLORS = {
  1: '#28a745',
  2: '#ffc107',
  3: '#fd7e14',
  4: '#dc3545',
};

export const API_ENDPOINTS = {
  JOBS: '/api/jobs',
  JOBS_PENDING: '/api/jobs/pending',
  JOBS_DONE: '/api/jobs/done',
  JOBS_ARCHIVED: '/api/jobs/archived',
  JOBS_SCHEDULED: '/api/jobs/scheduled',
  USERS_WORKERS: '/api/users/workers',
  AUTH_LOGIN: '/api/auth/login',
  NOTIFICATIONS: '/api/notifications',
};
