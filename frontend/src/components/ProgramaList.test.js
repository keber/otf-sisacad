import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ProgramaList from './ProgramaList';

describe('ProgramaList', () => {
  beforeEach(() => {
    let programas = [
      { id: 1, codigo: 'PF001', nombre: 'Programa A', fechaInicio: '2024-01-01', fechaFin: '2024-12-31', estado: 'Activo' },
      { id: 2, codigo: 'PF002', nombre: 'Programa B', fechaInicio: '2024-02-01', fechaFin: '2024-11-30', estado: 'Activo' }
    ];

    global.fetch = jest.fn((url, options) => {
      
      if (!options || options.method === 'GET') {
        return Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve(programas)
        });
      }

      if (options.method === 'DELETE') {
        programas = programas.filter(p => p.id !== 1);
        return Promise.resolve({ ok: true });
      }

      return Promise.reject(new Error('Método no soportado'));
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('muestra la lista de programas formativos', async () => {
    render(<ProgramaList />);

    // Espera que los elementos se muestren
    await waitFor(() => {
      expect(screen.getByText('Programa A')).toBeInTheDocument();
      expect(screen.getByText('Programa B')).toBeInTheDocument();
    });
  });

  it('abre el modal de confirmación al hacer clic en Eliminar', async () => {
    render(<ProgramaList />);
    const botonEliminar = await screen.findAllByRole('button', { name: /eliminar/i });
    fireEvent.click(botonEliminar[0]);

    await waitFor(() => {
      expect(screen.getByText(/¿estás seguro/i)).toBeInTheDocument();
    });
  });

  it('realiza la petición DELETE y recarga la lista al confirmar', async () => {
    render(<ProgramaList />);
    const botonesEliminar = await screen.findAllByRole('button', { name: /eliminar/i });
    fireEvent.click(botonesEliminar[0]);

    const botonConfirmar = await screen.findByTestId('confirmar-eliminar');
    fireEvent.click(botonConfirmar);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('http://localhost:8080/programas/1'),
        expect.objectContaining({ method: 'DELETE' })
      );
    });

    const alerta = await screen.findByRole('alert');
    expect(alerta).toHaveTextContent(/programa eliminado/i);
  });
});
