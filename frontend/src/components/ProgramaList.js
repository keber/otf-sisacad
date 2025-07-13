import React, { useEffect, useState } from 'react';
import * as bootstrap from 'bootstrap';
import PropTypes from 'prop-types';

const ProgramaList = ({ programas, onEditar, refrescar }) => {
  const [programaSeleccionado, setProgramaSeleccionado] = useState(null);
  const [alerta, setAlerta] = useState({ tipo: '', mensaje: '' });

  useEffect(() => {
    
    refrescar();
  }, []);

  const confirmarEliminacion = (programa) => {
    setProgramaSeleccionado(programa);
    const modal = new bootstrap.Modal(document.getElementById('confirmarModal'));
    modal.show();
  };

  const eliminarPrograma = async () => {
    if (!programaSeleccionado) return;

    try {
      const res = await fetch(`http://localhost:8080/programas/${programaSeleccionado.id}`, {
        method: 'DELETE',
      });

      if (!res.ok) throw new Error('No se pudo eliminar el programa');

      setProgramaSeleccionado(null);
      await refrescar();

      setAlerta({ tipo: 'success', mensaje: 'Programa eliminado' });
    } catch (error) {
      console.error('Error al eliminar el programa:', error);
      setAlerta({ tipo: 'danger', mensaje: 'Error al eliminar el programa' });
    }

      setTimeout(() => setAlerta({ tipo: '', mensaje: ''}), 3000);
  };

  return (
    <div className="mt-5">
      <h2 className="mb-3">Listado de Programas Formativos</h2>
      {alerta.mensaje && (
        <div className={`alert alert-${alerta.tipo} alert-dismissible fade show`} role="alert">
          {alerta.mensaje}
          <button
            type="button"
            className="btn-close"
            data-bs-dismiss="alert"
            aria-label="Cerrar"
            onClick={() => setAlerta({ tipo: '', mensaje: '' })}
          ></button>
        </div>
      )}

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
                    <button className="btn btn-sm btn-warning me-2" onClick={() => onEditar(p)}>
                      Actualizar
                    </button>
                    <button className="btn btn-sm btn-danger" onClick={() => confirmarEliminacion(p)}>
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Modal de Confirmación */}
        <div className="modal fade" id="confirmarModal" tabIndex="-1" aria-labelledby="confirmarModalLabel" aria-hidden="true">
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" id="confirmarModalLabel">Confirmar eliminación</h5>
                <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
              </div>
              <div className="modal-body">
                ¿Estás seguro que deseas eliminar el programa <strong>{programaSeleccionado?.nombre}</strong>?
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" className="btn btn-danger" data-bs-dismiss="modal" data-testid="confirmar-eliminar" onClick={eliminarPrograma}>Eliminar</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

ProgramaList.propTypes = {
  programas: PropTypes.array.isRequired,
  onEditar: PropTypes.func.isRequired,
  refrescar: PropTypes.func.isRequired
};

export default ProgramaList;
