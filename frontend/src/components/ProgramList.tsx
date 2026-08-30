import { useEffect, useState } from 'react';
import * as bootstrap from 'bootstrap';
import { TrainingProgram } from '../types';

interface ProgramListProps {
  programs: TrainingProgram[];
  onEdit: (program: TrainingProgram) => void;
  refresh: () => void | Promise<void>;
}

interface Notification {
  type: string;
  message: string;
}

const API_URL = 'http://localhost:8080/programs';

const ProgramList = ({ programs, onEdit, refresh }: ProgramListProps) => {
  const [selectedProgram, setSelectedProgram] = useState<TrainingProgram | null>(null);
  const [notification, setNotification] = useState<Notification>({ type: '', message: '' });

  useEffect(() => {
    refresh();
  }, [refresh]);

  const confirmDelete = (program: TrainingProgram): void => {
    setSelectedProgram(program);
    const modalElement = document.getElementById('confirmModal');
    if (modalElement) {
      new bootstrap.Modal(modalElement).show();
    }
  };

  const deleteProgram = async (): Promise<void> => {
    if (!selectedProgram) return;

    try {
      const res = await fetch(`${API_URL}/${selectedProgram.id}`, {
        method: 'DELETE',
      });

      if (!res.ok) throw new Error('No se pudo eliminar el programa');

      setSelectedProgram(null);
      await refresh();

      setNotification({ type: 'success', message: 'Programa eliminado' });
    } catch (error) {
      console.error('Error al eliminar el programa:', error);
      setNotification({ type: 'danger', message: 'Error al eliminar el programa' });
    }

    setTimeout(() => setNotification({ type: '', message: '' }), 3000);
  };

  return (
    <div className="mt-5">
      <h2 className="mb-3">Listado de Programas Formativos</h2>
      {notification.message && (
        <div className={`alert alert-${notification.type} alert-dismissible fade show`} role="alert">
          {notification.message}
          <button
            type="button"
            className="btn-close"
            data-bs-dismiss="alert"
            aria-label="Cerrar"
            onClick={() => setNotification({ type: '', message: '' })}
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
            {programs.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center">
                  No hay programas registrados.
                </td>
              </tr>
            ) : (
              programs.map((program) => (
                <tr key={program.id}>
                  <td>{program.code}</td>
                  <td>{program.name}</td>
                  <td>{program.startDate}</td>
                  <td>{program.endDate}</td>
                  <td>{program.status}</td>
                  <td>
                    <button className="btn btn-sm btn-warning me-2" onClick={() => onEdit(program)}>
                      Actualizar
                    </button>
                    <button className="btn btn-sm btn-danger" onClick={() => confirmDelete(program)}>
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Confirmation Modal */}
        <div className="modal fade" id="confirmModal" tabIndex={-1} aria-labelledby="confirmModalLabel" aria-hidden="true">
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title" id="confirmModalLabel">Confirmar eliminación</h5>
                <button type="button" className="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
              </div>
              <div className="modal-body">
                ¿Estás seguro que deseas eliminar el programa <strong>{selectedProgram?.name}</strong>?
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                <button type="button" className="btn btn-danger" data-bs-dismiss="modal" data-testid="confirm-delete" onClick={deleteProgram}>Eliminar</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProgramList;
