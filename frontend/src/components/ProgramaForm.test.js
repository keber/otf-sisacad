import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ProgramaForm from './ProgramaForm';

const mockOnSubmit = jest.fn();

global.fetch = jest.fn(() =>
  Promise.resolve({
    json: () => Promise.resolve({ id: 1, codigo: 'PF001', nombre: 'Programa A' }),
  })
);

describe('ProgramaForm', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  it('envía el formulario con los datos y muestra mensaje de éxito', async () => {
    render(<ProgramaForm onSubmit={mockOnSubmit} />);

    // Completar inputs
    fireEvent.change(screen.getByLabelText(/código/i), {
      target: { value: 'PF001' },
    });
    fireEvent.change(screen.getByLabelText(/nombre/i), {
      target: { value: 'Programa A' },
    });

    // Enviar formulario
    fireEvent.click(screen.getByRole('button', { name: /guardar/i }));

    // Esperar mensaje de éxito
    await waitFor(() =>
      expect(screen.getByText(/programa registrado/i)).toBeInTheDocument()
    );

    // Asegura que se hizo fetch con POST
    expect(mockOnSubmit).toHaveBeenCalledWith({
        codigo: 'PF001',
        nombre: 'Programa A',
        fechaInicio: '',
        fechaFin: '',
        estado: 'Activo',
    });

  });
});
