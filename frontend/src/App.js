import React from 'react';
import ProgramaForm from './components/ProgramaForm';
import ProgramaList from './components/ProgramaList';

function App() {

  const handleCrearPrograma = async (nuevoPrograma) => {
    await fetch('http://localhost:8080/programas', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(nuevoPrograma),
    });

  };

  return (
    <div style={{ padding: '2rem' }}>
      <h1>Registrar Programa</h1>
      <ProgramaForm onSubmit={handleCrearPrograma} />
      <ProgramaList />
    </div>
  );
}

export default App;
