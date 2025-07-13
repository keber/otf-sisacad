import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';

const ProgramaForm = ({ onSubmit, programaEnEdicion  }) => {
  const [formData, setFormData] = useState({
    id: null,
    codigo: '',
    nombre: '',
    fechaInicio: '',
    fechaFin: '',
    estado: 'Activo',
  });

  const [errors, setErrors] = useState({});
  const [mensaje, setMensaje] = useState('');

  useEffect(() => {
    if (programaEnEdicion) {
      setFormData(programaEnEdicion);
    }
  }, [programaEnEdicion]);

  const handleChange = (e) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.codigo.trim()) newErrors.codigo = 'Campo obligatorio';
    if (!formData.nombre.trim()) newErrors.nombre = 'Campo obligatorio';
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    setErrors(validationErrors);
    setMensaje('');

    if (Object.keys(validationErrors).length === 0) {
      await onSubmit(formData);
      setFormData({
        id: null,
        codigo: '',
        nombre: '',
        fechaInicio: '',
        fechaFin: '',
        estado: 'Activo',
    });
    setMensaje('Programa registrado');
    }
  };

  return (
    <div className="card mt-4">
      <div className="card-body">
        <h4 className="card-title mb-3">{formData.id ? 'Editar Programa' : 'Registrar Programa'}</h4>
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label" htmlFor="codigo">Código *</label>
              <input
                type="text"
                name="codigo"
                id="codigo"
                value={formData.codigo}
                onChange={handleChange}
                className={`form-control ${errors.codigo ? 'is-invalid' : ''}`}
              />
              {errors.codigo && <div className="invalid-feedback">{errors.codigo}</div>}
            </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="nombre">Nombre *</label>
            <input
              type="text"
              name="nombre"
              id="nombre"
              value={formData.nombre}
              onChange={handleChange}
              className={`form-control ${errors.nombre ? 'is-invalid' : ''}`}
            />
            {errors.nombre && <div className="invalid-feedback">{errors.nombre}</div>}
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="fechaInicio">Fecha Inicio</label>
            <input
              type="date"
              name="fechaInicio"
              id="fechaInicio"
              value={formData.fechaInicio}
              onChange={handleChange}
              className="form-control"
            />
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="FechaFin">Fecha Fin</label>
            <input
              type="date"
              name="fechaFin"
              id="fechaFin"
              value={formData.fechaFin}
              onChange={handleChange}
              className="form-control"
            />
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="estado">Estado</label>
            <select name="estado" id="estado" value={formData.estado} onChange={handleChange} className="form-select">
                <option value="Activo">Activo</option>
                <option value="Inactivo">Inactivo</option>
            </select>
          </div>

          <button type="submit" className="btn btn-primary">Guardar</button>
        </form>

        {mensaje && <div className="alert alert-success mt-3">{mensaje}</div>}
      </div>
    </div>

  );
};

ProgramaForm.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  programaEnEdicion: PropTypes.object
};

export default ProgramaForm;
