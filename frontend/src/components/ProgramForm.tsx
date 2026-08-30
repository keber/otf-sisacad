import React, { useState, useEffect } from 'react';
import { TrainingProgram, TrainingProgramFields, TrainingProgramFormState } from '../types';

interface ProgramFormProps {
  onSubmit: (program: TrainingProgramFormState) => void | Promise<void>;
  editingProgram?: TrainingProgram | null;
}

type FieldErrors = Partial<Pick<TrainingProgramFields, 'code' | 'name'>>;

const EMPTY_FIELDS: TrainingProgramFields = {
  code: '',
  name: '',
  startDate: '',
  endDate: '',
  status: 'Activo',
};

const ProgramForm = ({ onSubmit, editingProgram }: ProgramFormProps) => {
  const [id, setId] = useState<number | null>(null);
  const [fields, setFields] = useState<TrainingProgramFields>(EMPTY_FIELDS);
  const [errors, setErrors] = useState<FieldErrors>({});
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (editingProgram) {
      const { id: editingId, ...editingFields } = editingProgram;
      setId(editingId);
      setFields(editingFields);
    }
  }, [editingProgram]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>): void => {
    const { name, value } = e.target;
    setFields((prev) => ({ ...prev, [name]: value }));
  };

  const validate = (): FieldErrors => {
    const newErrors: FieldErrors = {};
    if (!fields.code.trim()) newErrors.code = 'Campo obligatorio';
    if (!fields.name.trim()) newErrors.name = 'Campo obligatorio';
    return newErrors;
  };

  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    const validationErrors = validate();
    setErrors(validationErrors);
    setMessage('');

    if (Object.keys(validationErrors).length === 0) {
      await onSubmit({ ...fields, id });
      setId(null);
      setFields(EMPTY_FIELDS);
      setMessage('Programa registrado');
    }
  };

  return (
    <div className="card mt-4">
      <div className="card-body">
        <h4 className="card-title mb-3">{id ? 'Editar Programa' : 'Registrar Programa'}</h4>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label" htmlFor="code">Código *</label>
            <input
              type="text"
              name="code"
              id="code"
              value={fields.code}
              onChange={handleChange}
              className={`form-control ${errors.code ? 'is-invalid' : ''}`}
            />
            {errors.code && <div className="invalid-feedback">{errors.code}</div>}
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="name">Nombre *</label>
            <input
              type="text"
              name="name"
              id="name"
              value={fields.name}
              onChange={handleChange}
              className={`form-control ${errors.name ? 'is-invalid' : ''}`}
            />
            {errors.name && <div className="invalid-feedback">{errors.name}</div>}
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="startDate">Fecha Inicio</label>
            <input
              type="date"
              name="startDate"
              id="startDate"
              value={fields.startDate}
              onChange={handleChange}
              className="form-control"
            />
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="endDate">Fecha Fin</label>
            <input
              type="date"
              name="endDate"
              id="endDate"
              value={fields.endDate}
              onChange={handleChange}
              className="form-control"
            />
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="status">Estado</label>
            <select name="status" id="status" value={fields.status} onChange={handleChange} className="form-select">
              <option value="Activo">Activo</option>
              <option value="Inactivo">Inactivo</option>
            </select>
          </div>

          <button type="submit" className="btn btn-primary">Guardar</button>
        </form>

        {message && <div className="alert alert-success mt-3">{message}</div>}
      </div>
    </div>
  );
};

export default ProgramForm;
