import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import RegisterAssetForm from '../../components/assets/RegisterAssetForm';

const baseFormData = {
  name: 'Central Playground',
  departmentId: '1',
  assetCategoryId: '2',
  location: 'Memorial Park',
  status: 'ACTIVE',
  registrationDate: '2026-06-01',
};

describe('RegisterAssetForm', () => {
  it('renders key fields and calls onSubmit', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn((event) => event.preventDefault());

    render(
      <RegisterAssetForm
        formData={baseFormData}
        departments={[{ id: 1, name: 'Parks' }]}
        categories={[{ id: 2, name: 'Playground' }]}
        submitting={false}
        onChange={vi.fn()}
        onSubmit={onSubmit}
      />
    );

    expect(screen.getByLabelText('Asset name')).toBeInTheDocument();
    expect(screen.getByLabelText('Department')).toBeInTheDocument();
    expect(screen.getByLabelText('Location')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Register Asset' }));
    expect(onSubmit).toHaveBeenCalledTimes(1);
  });
});
