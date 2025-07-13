import React from 'react';
import ProgramaForm from './components/ProgramaForm';
import ProgramaList from './components/ProgramaList';

function App() {
  return (
    <div className="App">
      <h1>Registrar Programa</h1>
      <ProgramaForm />
      <ProgramaList />
    </div>
  );
}

export default App;
