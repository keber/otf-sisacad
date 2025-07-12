import React, { useEffect, useState } from 'react';

const ProgramaList = () => {
  const [programas, setProgramas] = useState([]);

  useEffect(() => {
    
    fetch('http://localhost:8080/programas')
      .then((res) => res.json())
      .then((data) => setProgramas(data));
  }, []);

  return (
    <ul>
      {programas.map((p) => (
        <li key={p.id}>{p.nombre}</li>
      ))}
    </ul>
  );
};

export default ProgramaList;
