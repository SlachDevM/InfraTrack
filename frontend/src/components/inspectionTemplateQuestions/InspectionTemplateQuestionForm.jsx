import { INSPECTION_TEMPLATE_QUESTION_TYPE_OPTIONS } from '../../constants/inspectionTemplateQuestionTypes';

export default function InspectionTemplateQuestionForm({
  editingId,
  formData,
  submitting,
  unitsOfMeasure,
  onChange,
  onSubmit,
  onCancelEdit,
}) {
  return (
    <section className="reference-form-section">
      <h2>{editingId ? 'Edit Question' : 'Create Question'}</h2>
      <form className="reference-form" onSubmit={onSubmit}>
        <div className="form-row">
          <label htmlFor="code">Question Code</label>
          <input
            id="code"
            name="code"
            type="text"
            value={formData.code}
            onChange={onChange}
            required={!editingId}
            disabled={submitting || Boolean(editingId)}
            readOnly={Boolean(editingId)}
          />
          {!editingId && (
            <p className="field-hint">
              Suggested automatically from question text. You can edit it before saving.
            </p>
          )}
          {editingId && (
            <p className="field-hint">
              Business codes are read-only after creation to protect downstream integrations.
            </p>
          )}
        </div>
        <div className="form-row">
          <label htmlFor="questionText">Question Text</label>
          <input
            id="questionText"
            name="questionText"
            type="text"
            value={formData.questionText}
            onChange={onChange}
            required
            disabled={submitting}
          />
        </div>
        <div className="form-row">
          <label htmlFor="helpText">Help Text</label>
          <textarea
            id="helpText"
            name="helpText"
            value={formData.helpText}
            onChange={onChange}
            disabled={submitting}
            rows={2}
          />
        </div>
        <div className="form-row">
          <label htmlFor="questionType">Question Type</label>
          <select
            id="questionType"
            name="questionType"
            value={formData.questionType}
            onChange={onChange}
            required
            disabled={submitting}
          >
            {INSPECTION_TEMPLATE_QUESTION_TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label htmlFor="required">
            <input
              id="required"
              name="required"
              type="checkbox"
              checked={formData.required}
              onChange={onChange}
              disabled={submitting}
            />{' '}
            Required
          </label>
        </div>
        {formData.questionType === 'NUMBER' && (
          <>
            <div className="form-row">
              <label htmlFor="unitOfMeasureId">Unit of Measure</label>
              <select
                id="unitOfMeasureId"
                name="unitOfMeasureId"
                value={formData.unitOfMeasureId}
                onChange={onChange}
                disabled={submitting}
              >
                <option value="">No unit</option>
                {unitsOfMeasure.map((unit) => (
                  <option key={unit.id} value={unit.id}>
                    {unit.symbol} — {unit.name} ({unit.quantityType})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <label htmlFor="minValue">Minimum Value</label>
              <input
                id="minValue"
                name="minValue"
                type="number"
                step="any"
                value={formData.minValue}
                onChange={onChange}
                disabled={submitting}
              />
            </div>
            <div className="form-row">
              <label htmlFor="maxValue">Maximum Value</label>
              <input
                id="maxValue"
                name="maxValue"
                type="number"
                step="any"
                value={formData.maxValue}
                onChange={onChange}
                disabled={submitting}
              />
            </div>
            <div className="form-row">
              <label htmlFor="decimalPlaces">Decimal Places</label>
              <input
                id="decimalPlaces"
                name="decimalPlaces"
                type="number"
                min="0"
                max="6"
                value={formData.decimalPlaces}
                onChange={onChange}
                disabled={submitting}
              />
            </div>
          </>
        )}
        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Saving...' : editingId ? 'Update Question' : 'Create Question'}
          </button>
          {editingId && (
            <button
              type="button"
              className="btn-secondary"
              onClick={onCancelEdit}
              disabled={submitting}
            >
              Cancel Edit
            </button>
          )}
        </div>
      </form>
    </section>
  );
}
