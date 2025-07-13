import React, { useEffect, useState } from 'react';

const ProgramaList = () => {
  const [programas, setProgramas] = useState([]);

  useEffect(() => {
    
    fetch('http://localhost:8080/programas')
      .then((res) => res.json())
      .then((data) => setProgramas(data));
  }, []);

  return (
    <div className="mt-5">
      <h2 className="mb-3">Listado de Programas Formativos</h2>
      <div className="table-responsive">
        <table className="table table-bordered table-hover align-middle">
          <thead className="table-dark">
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Fecha Inicio</th>
              <th>Fecha Fin</th>
              <th>Estado</th>
              <th style={{ width: '180px' }}>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {programas.length === 0 ? (
              <tr>
                <td colSpan="6" className="text-center">
                  No hay programas registrados.
                </td>
              </tr>
            ) : (
              programas.map((p) => (
                <tr key={p.id}>
                  <td>{p.codigo}</td>
                  <td>{p.nombre}</td>
                  <td>{p.fechaInicio}</td>
                  <td>{p.fechaFin}</td>
                  <td>{p.estado}</td>
                  <td>
                    <button className="btn btn-sm btn-warning me-2">
                      Actualizar
                    </button>
                    <button className="btn btn-sm btn-danger">
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ProgramaList;
