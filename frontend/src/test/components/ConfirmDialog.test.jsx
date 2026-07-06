import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ConfirmDialog from '../../components/ConfirmDialog';

afterEach(cleanup);

describe('ConfirmDialog', () => {
  it('exposes dialog semantics and destructive confirm label', () => {
    render(
      <ConfirmDialog
        title="Delete document"
        message="This cannot be undone."
        confirmLabel="Delete"
        isDangerous
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
      />
    );

    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByRole('dialog')).toHaveAttribute('aria-modal', 'true');
    expect(screen.getByRole('button', { name: 'Delete — destructive action' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel confirmation' })).toBeInTheDocument();
  });

  it('calls onCancel when Escape is pressed', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();

    render(
      <ConfirmDialog
        title="Confirm"
        message="Are you sure?"
        confirmLabel="Confirm"
        onConfirm={vi.fn()}
        onCancel={onCancel}
      />
    );

    await user.keyboard('{Escape}');
    expect(onCancel).toHaveBeenCalledTimes(1);
  });
});
