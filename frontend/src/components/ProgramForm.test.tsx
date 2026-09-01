import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ProgramForm from './ProgramForm';

const mockOnSubmit = jest.fn();

beforeAll(() => {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      json: () => Promise.resolve({ id: 1, code: 'PF001', name: 'Programa A' }),
    })
  ) as jest.Mock;
});

describe('ProgramForm', () => {
  beforeEach(() => {
    (global.fetch as jest.Mock).mockClear();
    mockOnSubmit.mockClear();
  });

  it('submits the form data and shows the success message', async () => {
    render(<ProgramForm onSubmit={mockOnSubmit} editingProgram={null} />);

    // Fill in inputs
    fireEvent.change(screen.getByLabelText(/código/i), {
      target: { value: 'PF001' },
    });
    fireEvent.change(screen.getByLabelText(/nombre/i), {
      target: { value: 'Programa A' },
    });

    // Submit form
    fireEvent.click(screen.getByRole('button', { name: /guardar/i }));

    // Wait for success message
    await waitFor(() =>
      expect(screen.getByText(/programa registrado/i)).toBeInTheDocument()
    );

    // Ensure onSubmit was called with the English-keyed payload
    expect(mockOnSubmit).toHaveBeenCalledWith({
      code: 'PF001',
      name: 'Programa A',
      startDate: '',
      endDate: '',
      id: null,
      status: 'Activo',
    });
  });
});
