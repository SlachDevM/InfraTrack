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
    <div className="pagination-controls">
      <button
        type="button"
        className="pagination-btn"
        data-testid="pagination-previous"
        onClick={onPrevious}
        disabled={loading || page <= 0}
      >
        Previous
      </button>
      <span className="pagination-status">
        Page {displayPage} of {safeTotalPages}
      </span>
      <button
        type="button"
        className="pagination-btn"
        data-testid="pagination-next"
        onClick={onNext}
        disabled={loading || page >= safeTotalPages - 1}
      >
        Next
      </button>
    </div>
  );
}
