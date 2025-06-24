import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders kanban board', () => {
  render(<App />);
  const loadingElement = screen.getByText(/loading tasks/i);
  expect(loadingElement).toBeInTheDocument();
});
