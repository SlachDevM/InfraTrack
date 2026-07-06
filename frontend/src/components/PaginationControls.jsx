export default function PaginationControls({
  page,
  totalPages,
  onPrevious,
  onNext,
  loading = false,
}) {
  const safeTotalPages = Math.max(totalPages, 1);
  const displayPage = page + 1;

  return (
    <div className="pagination-controls" aria-busy={loading}>
      <button
        type="button"
        className="pagination-btn"
        data-testid="pagination-previous"
        onClick={onPrevious}
        disabled={loading || page <= 0}
        aria-label="Previous page"
      >
        Previous
      </button>
      <span className="pagination-status" aria-live="polite">
        Page {displayPage} of {safeTotalPages}
        {loading ? ' (loading)' : ''}
      </span>
      <button
        type="button"
        className="pagination-btn"
        data-testid="pagination-next"
        onClick={onNext}
        disabled={loading || page >= safeTotalPages - 1}
        aria-label="Next page"
      >
        Next
      </button>
    </div>
  );
}
