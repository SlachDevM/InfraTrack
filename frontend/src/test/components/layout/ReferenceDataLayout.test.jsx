import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import ReferenceDataLayout from '../../../components/layout/ReferenceDataLayout';

const mockNavigate = vi.fn();
const mockLogout = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock('../../../context/AuthContext', () => ({
  useAuth: () => ({
    logout: mockLogout,
  }),
}));

vi.mock('../../../components/NotificationButton', () => ({
  default: () => <button type="button">Notifications</button>,
}));

describe('ReferenceDataLayout', () => {
  afterEach(cleanup);

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders title, navigation actions, and children in reference-content', () => {
    const { container } = render(
      <MemoryRouter>
        <ReferenceDataLayout title="Inspection Templates">
          <p>Page body</p>
        </ReferenceDataLayout>
      </MemoryRouter>
    );

    expect(screen.getByRole('heading', { name: 'Inspection Templates' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '← Back' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Notifications' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Logout' })).toBeInTheDocument();
    expect(screen.getByText('Page body')).toBeInTheDocument();
    expect(container.querySelector('.reference-data-page')).toBeInTheDocument();
    expect(container.querySelector('.reference-header')).toBeInTheDocument();
    expect(container.querySelector('.reference-content')).toContainElement(
      screen.getByText('Page body')
    );
  });

  it('navigates home on back and to login on logout by default', async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <ReferenceDataLayout title="Departments">
          <p>Content</p>
        </ReferenceDataLayout>
      </MemoryRouter>
    );

    await user.click(screen.getByRole('button', { name: '← Back' }));
    await user.click(screen.getByRole('button', { name: 'Logout' }));

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledWith('/login');
    expect(mockLogout).toHaveBeenCalled();
  });

  it('supports custom back handler and header actions', async () => {
    const user = userEvent.setup();
    const onBack = vi.fn();

    render(
      <MemoryRouter>
        <ReferenceDataLayout
          title="Template Questions"
          backLabel="← Back to Templates"
          onBack={onBack}
          headerActions={<button type="button">Extra Action</button>}
        >
          <p>Questions</p>
        </ReferenceDataLayout>
      </MemoryRouter>
    );

    await user.click(screen.getByRole('button', { name: '← Back to Templates' }));

    expect(onBack).toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalledWith('/');
    expect(screen.getByRole('button', { name: 'Extra Action' })).toBeInTheDocument();
  });
});
