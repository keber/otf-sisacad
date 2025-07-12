import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import ProgramaList from './ProgramaList';

describe('ProgramaList', () => {
  beforeEach(() => {
    global.fetch = jest.fn(() => 
      Promise.resolve({
        json: () =>
          Promise.resolve([
            { id: 1, codigo: 'PF001', nombre: 'Programa A' },
            { id: 2, codigo: 'PF002', nombre: 'Programa B' }
          ]),
      })
    );
  });

  
  afterEach(() => {
    fetch.mockClear();
  });

  it('muestra la lista de programas formativos', async () => {
    render(<ProgramaList />);

    // Espera que los elementos se muestren
    await waitFor(() => {
      expect(screen.getByText('Programa A')).toBeInTheDocument();
      expect(screen.getByText('Programa B')).toBeInTheDocument();
    });
  });
});
