export default function InspectionTemplateChoicePanel({
  questionCode,
  choices,
  choiceForm,
  editingChoiceId,
  choiceSubmitting,
  onClose,
  onFormChange,
  onSubmit,
  onEditChoice,
  onDeactivateChoice,
  onMoveChoice,
  onCancelEdit,
}) {
  return (
    <section className="reference-form-section">
      <h2>Manage Choices — {questionCode}</h2>
      <button type="button" className="btn-secondary" onClick={onClose}>
        Close
      </button>
      <form className="reference-form" onSubmit={onSubmit}>
        <div className="form-row">
          <label htmlFor="choiceCode">Choice Code</label>
          <input
            id="choiceCode"
            name="code"
            type="text"
            value={choiceForm.code}
            onChange={onFormChange}
            required={!editingChoiceId}
            disabled={choiceSubmitting || Boolean(editingChoiceId)}
            readOnly={Boolean(editingChoiceId)}
          />
        </div>
        <div className="form-row">
          <label htmlFor="choiceLabel">Label</label>
          <input
            id="choiceLabel"
            name="label"
            type="text"
            value={choiceForm.label}
            onChange={onFormChange}
            required
            disabled={choiceSubmitting}
          />
        </div>
        <div className="form-actions">
          <button type="submit" className="btn-primary" disabled={choiceSubmitting}>
            {choiceSubmitting ? 'Saving...' : editingChoiceId ? 'Update Choice' : 'Add Choice'}
          </button>
          {editingChoiceId && (
            <button
              type="button"
              className="btn-secondary"
              onClick={onCancelEdit}
              disabled={choiceSubmitting}
            >
              Cancel Edit
            </button>
          )}
        </div>
      </form>
      <table className="reference-table">
        <thead>
          <tr>
            <th>Order</th>
            <th>Code</th>
            <th>Label</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {choices.map((choice) => (
            <tr key={choice.id}>
              <td>{choice.displayOrder}</td>
              <td>{choice.code}</td>
              <td>{choice.label}</td>
              <td>{choice.active ? 'Active' : 'Inactive'}</td>
              <td>
                {choice.active ? (
                  <>
                    <button type="button" className="btn-link" onClick={() => onEditChoice(choice)}>
                      Edit
                    </button>{' '}
                    <button
                      type="button"
                      className="btn-link"
                      onClick={() => onDeactivateChoice(choice.id)}
                    >
                      Deactivate
                    </button>{' '}
                    <button
                      type="button"
                      className="btn-link"
                      disabled={choiceSubmitting}
                      onClick={() => onMoveChoice(choice.id, 'up')}
                    >
                      Move Up
                    </button>{' '}
                    <button
                      type="button"
                      className="btn-link"
                      disabled={choiceSubmitting}
                      onClick={() => onMoveChoice(choice.id, 'down')}
                    >
                      Move Down
                    </button>
                  </>
                ) : (
                  '-'
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
