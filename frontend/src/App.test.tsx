import { render, screen } from '@testing-library/react';
import App from './App';

test('renders Registrar Programa title', () => {
  render(<App />);
  const someElement = screen.getByText(/Registrar Programa/i);
  expect(someElement).toBeInTheDocument();
});
