import React, { useState } from 'react';

const ProgramaForm = () => {
  const [codigo, setCodigo] = useState('');
  const [nombre, setNombre] = useState('');
  const [mensaje, setMensaje] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();

    await fetch('http://localhost:8080/programas', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ codigo, nombre })
    });

    setMensaje('Programa registrado');
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label htmlFor="codigo">Código</label>
        <input
          id="codigo"
          value={codigo}
          onChange={(e) => setCodigo(e.target.value)}
        />
      </div>
      <div>
        <label htmlFor="nombre">Nombre</label>
        <input
          id="nombre"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
        />
      </div>
      <button type="submit">Registrar</button>
      {mensaje && <p>{mensaje}</p>}
    </form>
  );
};

export default ProgramaForm;
    