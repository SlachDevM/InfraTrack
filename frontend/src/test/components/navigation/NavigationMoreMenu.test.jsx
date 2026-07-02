import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import NavigationMoreMenu from '../../../components/navigation/NavigationMoreMenu';

describe('NavigationMoreMenu', () => {
  afterEach(cleanup);

  it('renders nothing when there are no overflow items', () => {
    const { container } = render(<NavigationMoreMenu items={[]} onNavigate={vi.fn()} />);

    expect(container).toBeEmptyDOMElement();
  });

  it('opens menu items and navigates on selection', async () => {
    const user = userEvent.setup();
    const onNavigate = vi.fn();

    render(
      <NavigationMoreMenu
        items={[{ path: '/departments', label: 'Departments' }]}
        onNavigate={onNavigate}
      />
    );

    await user.click(screen.getByRole('button', { name: 'More navigation' }));
    await user.click(screen.getByRole('menuitem', { name: 'Departments' }));

    expect(onNavigate).toHaveBeenCalledWith('/departments');
    expect(screen.queryByRole('menuitem', { name: 'Departments' })).not.toBeInTheDocument();
  });

  it('closes the menu when Escape is pressed', async () => {
    const user = userEvent.setup();

    render(
      <NavigationMoreMenu
        items={[{ path: '/departments', label: 'Departments' }]}
        onNavigate={vi.fn()}
      />
    );

    await user.click(screen.getByRole('button', { name: 'More navigation' }));
    expect(screen.getByRole('menuitem', { name: 'Departments' })).toBeInTheDocument();

    await user.keyboard('{Escape}');
    expect(screen.queryByRole('menuitem', { name: 'Departments' })).not.toBeInTheDocument();
  });
});
