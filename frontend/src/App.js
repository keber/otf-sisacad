import React, { useState } from 'react';
import ProgramaForm from './components/ProgramaForm';
import ProgramaList from './components/ProgramaList';

function App() {
  const [programas, setProgramas] = useState([]);
  const [programaEnEdicion, setProgramaEnEdicion] = useState(null);

  const cargarProgramas = async () => {
    const res = await fetch('http://localhost:8080/programas');
    const data = await res.json();
    setProgramas(data);
  };

  const handleCrearPrograma = async (nuevoPrograma) => {
    await fetch('http://localhost:8080/programas', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(nuevoPrograma),
    });
    await cargarProgramas();
  };

  const handleActualizarPrograma = async (programaActualizado) => {
    await fetch(`http://localhost:8080/programas/${programaActualizado.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(programaActualizado),
    });
    await cargarProgramas();
    setProgramaEnEdicion(null); // Limpiar el formulario después de actualizar
  };

  return (
    <div style={{ padding: '2rem' }}>
      <ProgramaForm onSubmit={programaEnEdicion ? handleActualizarPrograma : handleCrearPrograma}
        programaEnEdicion={programaEnEdicion} />
      <ProgramaList 
        programas={programas}
        onEditar={setProgramaEnEdicion} 
        refrescar={cargarProgramas}
      />
    </div>
  );
}

export default App;
