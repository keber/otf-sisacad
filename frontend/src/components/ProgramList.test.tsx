import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ProgramList from './ProgramList';
import { TrainingProgram } from '../types';

const buildPrograms = (): TrainingProgram[] => [
  { id: 1, code: 'PF001', name: 'Programa A', startDate: '2024-01-01', endDate: '2024-12-31', status: 'Activo' },
  { id: 2, code: 'PF002', name: 'Programa B', startDate: '2024-02-01', endDate: '2024-11-30', status: 'Activo' },
];

describe('ProgramList', () => {
  let programs: TrainingProgram[];

  beforeEach(() => {
    programs = buildPrograms();
    let remaining = buildPrograms();

    global.fetch = jest.fn((_url: RequestInfo | URL, options?: RequestInit) => {
      if (!options || options.method === 'GET') {
        return Promise.resolve({ ok: true, json: () => Promise.resolve(remaining) });
      }

      if (options.method === 'DELETE') {
        remaining = remaining.filter((p) => p.id !== 1);
        return Promise.resolve({ ok: true });
      }

      return Promise.reject(new Error('Unsupported method'));
    }) as jest.Mock;
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('shows the list of training programs', async () => {
    render(<ProgramList programs={programs} onEdit={jest.fn()} refresh={jest.fn()} />);

    // Wait for the elements to be displayed
    await waitFor(() => {
      expect(screen.getByText('Programa A')).toBeInTheDocument();
    });
    expect(screen.getByText('Programa B')).toBeInTheDocument();
  });

  it('opens the confirmation modal when clicking Eliminar', async () => {
    render(<ProgramList programs={programs} onEdit={jest.fn()} refresh={jest.fn()} />);
    const deleteButtons = await screen.findAllByRole('button', { name: /eliminar/i });
    fireEvent.click(deleteButtons[0]);

    await waitFor(() => {
      expect(screen.getByText(/¿estás seguro/i)).toBeInTheDocument();
    });
  });

  it('sends the DELETE request and reloads the list on confirm', async () => {
    render(<ProgramList programs={programs} onEdit={jest.fn()} refresh={jest.fn()} />);
    const deleteButtons = await screen.findAllByRole('button', { name: /eliminar/i });
    fireEvent.click(deleteButtons[0]);

    const confirmButton = await screen.findByTestId('confirm-delete');
    fireEvent.click(confirmButton);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('http://localhost:8080/programs/1'),
        expect.objectContaining({ method: 'DELETE' })
      );
    });

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent(/programa eliminado/i);
  });
});
