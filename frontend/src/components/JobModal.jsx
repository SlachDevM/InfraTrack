import { useState, useEffect } from 'react';
import '../styles/JobModal.css';

const API_BASE = 'http://localhost:4000';

export const JOB_TYPES = [
  'FULL_REGUTTER',
  'PARTIAL_REGUTTER',
  'FULL_REFASCIA',
  'PARTIAL_REFASCIA',
  'RESCREW',
  'PARTIAL_RESHEET',
  'FULL_RESHEET',
];

const EMPTY_FORM = {
  clientName: '',
  clientPhone: '',
  clientAddress: '',
  details: '',
  notes: '',
  priorityLevel: '1',
  jobDate: '',
  jobStartHour: '07:50',
};

function formatJobTypeLabel(type) {
  return type.replace(/_/g, ' ');
}

function timestampToDateInput(timestamp) {
  if (!timestamp) return '';
  const d = new Date(timestamp);
  const offset = d.getTimezoneOffset();
  const local = new Date(d.getTime() - offset * 60 * 1000);
  return local.toISOString().split('T')[0];
}

function dateInputToTimestamp(dateStr) {
  if (!dateStr) return null;
  const [year, month, day] = dateStr.split('-').map(Number);
  const d = new Date(year, month - 1, day);
  return d.getTime();
}

function toPhotoSrc(photo) {
  if (!photo) return null;
  if (typeof photo === 'string') {
    return photo.startsWith('data:') ? photo : `data:image/jpeg;base64,${photo}`;
  }
  return null;
}

export default function JobModal({
  isOpen,
  onClose,
  onSuccess,
  token,
  job = null,
  prefilledDate = null,
  canManage = false,
}) {
  const isEdit = Boolean(job?.id);
  const [form, setForm] = useState(EMPTY_FORM);
  const [selectedTypes, setSelectedTypes] = useState([]);
  const [selectedWorkers, setSelectedWorkers] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [beforePreview, setBeforePreview] = useState(null);
  const [afterPreview, setAfterPreview] = useState(null);
  const [beforePhoto, setBeforePhoto] = useState(null);
  const [afterPhoto, setAfterPhoto] = useState(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [loadingJob, setLoadingJob] = useState(false);

  useEffect(() => {
    if (!isOpen) return;

    const loadWorkers = async () => {
      if (!canManage) return;
      try {
        const res = await fetch(`${API_BASE}/api/users/workers`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (res.ok) {
          setWorkers(await res.json());
        }
      } catch (err) {
        console.error('Failed to load workers:', err);
      }
    };

    loadWorkers();
  }, [isOpen, token, canManage]);

  useEffect(() => {
    if (!isOpen) return;

    const resetForm = () => {
      setForm({
        ...EMPTY_FORM,
        jobDate: prefilledDate ? timestampToDateInput(prefilledDate) : '',
      });
      setSelectedTypes([]);
      setSelectedWorkers([]);
      setBeforePreview(null);
      setAfterPreview(null);
      setBeforePhoto(null);
      setAfterPhoto(null);
      setError('');
    };

    if (!isEdit) {
      resetForm();
      return;
    }

    const loadJob = async () => {
      setLoadingJob(true);
      try {
        const res = await fetch(`${API_BASE}/api/jobs/${job.id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        if (!res.ok) throw new Error('Failed to load job');
        const fullJob = await res.json();

        setForm({
          clientName: fullJob.clientName || '',
          clientPhone: fullJob.clientPhone || '',
          clientAddress: fullJob.clientAddress || '',
          details: fullJob.details || '',
          notes: fullJob.notes || '',
          priorityLevel: String(fullJob.priorityLevel || 1),
          jobDate: prefilledDate
            ? timestampToDateInput(prefilledDate)
            : timestampToDateInput(fullJob.jobDate),
          jobStartHour: fullJob.jobStartHour || '07:50',
        });
        setSelectedTypes(fullJob.jobTypes ? fullJob.jobTypes.split(',').filter(Boolean) : []);
        setSelectedWorkers(
          fullJob.assignedWorkers ? fullJob.assignedWorkers.split(',').filter(Boolean) : []
        );
        setBeforePreview(toPhotoSrc(fullJob.beforePhoto));
        setAfterPreview(toPhotoSrc(fullJob.afterPhoto));
        setBeforePhoto(null);
        setAfterPhoto(null);
      } catch (err) {
        console.error(err);
        setError('Failed to load job details.');
      } finally {
        setLoadingJob(false);
      }
    };

    loadJob();
  }, [isOpen, isEdit, job?.id, prefilledDate, token]);

  if (!isOpen) return null;

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const toggleJobType = (type) => {
    setSelectedTypes((prev) =>
      prev.includes(type) ? prev.filter((t) => t !== type) : [...prev, type]
    );
  };

  const toggleWorker = (name) => {
    setSelectedWorkers((prev) =>
      prev.includes(name) ? prev.filter((w) => w !== name) : [...prev, name]
    );
  };

  const handlePhotoChange = (e, type) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result;
      const base64 = dataUrl.split(',')[1];
      if (type === 'before') {
        setBeforePreview(dataUrl);
        setBeforePhoto(base64);
      } else {
        setAfterPreview(dataUrl);
        setAfterPhoto(base64);
      }
    };
    reader.readAsDataURL(file);
  };

  const handleClose = () => {
    setError('');
    onClose();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!form.clientName.trim() || !form.clientPhone.trim() || !form.clientAddress.trim()) {
      setError('Client name, phone, and address are required.');
      return;
    }

    if (selectedTypes.length === 0) {
      setError('Select at least one job type.');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        clientName: form.clientName.trim(),
        clientPhone: form.clientPhone.trim(),
        clientAddress: form.clientAddress.trim(),
        jobTypes: selectedTypes.join(','),
        priorityLevel: parseInt(form.priorityLevel, 10),
        details: form.details.trim() || null,
        notes: form.notes.trim() || null,
      };

      if (canManage) {
        const jobDate = dateInputToTimestamp(form.jobDate);
        if (jobDate) {
          payload.jobDate = jobDate;
          payload.jobStartHour = form.jobStartHour || '07:50';
        }
        payload.assignedWorkers = selectedWorkers.join(',');
      }

      if (beforePhoto) payload.beforePhoto = beforePhoto;
      if (afterPhoto) payload.afterPhoto = afterPhoto;

      const url = isEdit ? `${API_BASE}/api/jobs/${job.id}` : `${API_BASE}/api/jobs`;
      const method = isEdit ? 'PUT' : 'POST';

      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        setError(isEdit ? 'Failed to update job.' : 'Failed to create job.');
        return;
      }

      const savedJob = await response.json();
      onSuccess(savedJob);
      onClose();
    } catch (err) {
      console.error(err);
      setError(isEdit ? 'Failed to update job.' : 'Failed to create job.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="job-modal-overlay" onClick={handleClose}>
      <div className="job-modal" onClick={(e) => e.stopPropagation()}>
        <h3>{isEdit ? 'Edit Job' : 'Create Job'}</h3>

        {loadingJob ? (
          <p className="job-modal-loading">Loading job...</p>
        ) : (
          <form onSubmit={handleSubmit}>
            <label>
              Client Name *
              <input
                name="clientName"
                value={form.clientName}
                onChange={handleChange}
                placeholder="John Smith"
                required
                disabled={!canManage}
              />
            </label>

            <label>
              Phone *
              <input
                name="clientPhone"
                value={form.clientPhone}
                onChange={handleChange}
                placeholder="0412 345 678"
                required
                disabled={!canManage}
              />
            </label>

            <label>
              Address *
              <input
                name="clientAddress"
                value={form.clientAddress}
                onChange={handleChange}
                placeholder="123 Main St, Sydney"
                required
                disabled={!canManage}
              />
            </label>

            <fieldset className="job-types-fieldset">
              <legend>Job Types *</legend>
              <div className="job-types-grid">
                {JOB_TYPES.map((type) => (
                  <label key={type} className="job-type-option">
                    <input
                      type="checkbox"
                      checked={selectedTypes.includes(type)}
                      onChange={() => toggleJobType(type)}
                      disabled={!canManage}
                    />
                    {formatJobTypeLabel(type)}
                  </label>
                ))}
              </div>
            </fieldset>

            <label>
              Priority
              <select name="priorityLevel" value={form.priorityLevel} onChange={handleChange} disabled={!canManage}>
                <option value="1">1 - Low</option>
                <option value="2">2 - Medium</option>
                <option value="3">3 - High</option>
                <option value="4">4 - Urgent</option>
              </select>
            </label>

            <label>
              Details
              <textarea
                name="details"
                value={form.details}
                onChange={handleChange}
                placeholder="Additional job details..."
                rows={3}
                disabled={!canManage}
              />
            </label>

            <label>
              Notes
              <textarea
                name="notes"
                value={form.notes}
                onChange={handleChange}
                placeholder="Internal notes..."
                rows={2}
                disabled={!canManage}
              />
            </label>

            {(canManage || isEdit) && (
              <>
                <div className="job-modal-section">
                  <h4>Schedule</h4>
                  <div className="job-modal-row">
                    <label>
                      Start Date
                      <input
                        type="date"
                        name="jobDate"
                        value={form.jobDate}
                        onChange={handleChange}
                        disabled={!canManage}
                      />
                    </label>
                    <label>
                      Start Hour
                      <input
                        type="time"
                        name="jobStartHour"
                        value={form.jobStartHour}
                        onChange={handleChange}
                        disabled={!canManage}
                      />
                    </label>
                  </div>
                </div>

                <fieldset className="job-types-fieldset">
                  <legend>Assign Workers</legend>
                  {workers.length === 0 ? (
                    <p className="job-modal-hint">No workers available.</p>
                  ) : (
                    <div className="job-types-grid">
                      {workers.map((worker) => (
                        <label key={worker.id} className="job-type-option">
                          <input
                            type="checkbox"
                            checked={selectedWorkers.includes(worker.name)}
                            onChange={() => toggleWorker(worker.name)}
                            disabled={!canManage}
                          />
                          {worker.name}
                        </label>
                      ))}
                    </div>
                  )}
                </fieldset>

                <div className="job-modal-section">
                  <h4>Photos</h4>
                  <div className="job-modal-row">
                    <label className="photo-upload">
                      Before Photo
                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => handlePhotoChange(e, 'before')}
                        disabled={!canManage}
                      />
                      {beforePreview && (
                        <img src={beforePreview} alt="Before" className="photo-preview" />
                      )}
                    </label>
                    <label className="photo-upload">
                      After Photo
                      <input
                        type="file"
                        accept="image/*"
                        onChange={(e) => handlePhotoChange(e, 'after')}
                        disabled={!canManage}
                      />
                      {afterPreview && (
                        <img src={afterPreview} alt="After" className="photo-preview" />
                      )}
                    </label>
                  </div>
                </div>
              </>
            )}

            {error && <p className="job-modal-error">{error}</p>}

            <div className="job-modal-actions">
              {canManage && (
                <button type="submit" disabled={submitting || loadingJob}>
                  {submitting ? (isEdit ? 'Saving...' : 'Creating...') : isEdit ? 'Save Job' : 'Create Job'}
                </button>
              )}
              <button type="button" onClick={handleClose} disabled={submitting}>
                {canManage ? 'Cancel' : 'Close'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
