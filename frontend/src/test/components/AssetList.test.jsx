import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import AssetList from '../../components/assets/AssetList';

describe('AssetList', () => {
  it('renders assets in the table', () => {
    render(
      <AssetList
        assets={[
          {
            id: 1,
            name: 'Central Playground',
            departmentName: 'Parks',
            assetCategoryName: 'Playground',
            location: 'Memorial Park',
            status: 'ACTIVE',
            registrationDate: '2026-06-01',
          },
        ]}
      />
    );

    expect(screen.getByText('Central Playground')).toBeInTheDocument();
    expect(screen.getByText('Parks')).toBeInTheDocument();
    expect(screen.getByText('Memorial Park')).toBeInTheDocument();
  });

  it('shows empty state when no assets', () => {
    render(<AssetList assets={[]} />);
    const message = screen.getByText('No assets registered yet.');
    expect(message).toBeInTheDocument();
    expect(message).toHaveClass('empty-state');
  });
});
