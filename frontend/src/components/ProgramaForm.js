import React, { useState } from 'react';

const ProgramaForm = ({ onSubmit }) => {
  const [formData, setFormData] = useState({
    codigo: '',
    nombre: '',
    fechaInicio: '',
    fechaFin: '',
    estado: 'Activo',
  });

  const [errors, setErrors] = useState({});
  const [mensaje, setMensaje] = useState('');

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

    if (Object.keys(validationErrors).length === 0) {
      onSubmit(formData);
      setFormData({
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
    <form onSubmit={handleSubmit} style={styles.form}>
      <div style={styles.field}>
        <label htmlFor="codigo">Código *</label>
        <input
          type="text"
          name="codigo"
          id="codigo"
          value={formData.codigo}
          onChange={handleChange}
        />
        {errors.codigo && <span style={styles.error}>{errors.codigo}</span>}
      </div>

      <div style={styles.field}>
        <label htmlFor="nombre">Nombre *</label>
        <input
          type="text"
          name="nombre"
          id="nombre"
          value={formData.nombre}
          onChange={handleChange}
        />
        {errors.nombre && <span style={styles.error}>{errors.nombre}</span>}
      </div>

      <div style={styles.field}>
        <label htmlFor="fechaInicio">Fecha Inicio</label>
        <input
          type="date"
          name="fechaInicio"
          id="fechaInicio"
          value={formData.fechaInicio}
          onChange={handleChange}
        />
      </div>

      <div style={styles.field}>
        <label htmlFor="FechaFin">Fecha Fin</label>
        <input
          type="date"
          name="fechaFin"
          id="fechaFin"
          value={formData.fechaFin}
          onChange={handleChange}
        />
      </div>

      <div style={styles.field}>
        <label htmlFor="estado">Estado</label>
        <select name="estado" if="estado" value={formData.estado} onChange={handleChange}>
            <option value="Activo">Activo</option>
            <option value="Inactivo">Inactivo</option>
        </select>
      </div>

      <button type="submit">Guardar</button>

      {mensaje && <p style={{ color: 'green' }}>{mensaje}</p>}

    </form>
  );
};

const styles = {
  form: {
    maxWidth: '500px',
    padding: '1rem',
    border: '1px solid #ccc',
    borderRadius: '8px',
    marginBottom: '2rem',
    backgroundColor: '#f9f9f9',
  },
  field: {
    display: 'flex',
    flexDirection: 'column',
    marginBottom: '1rem',
  },
  error: {
    color: 'red',
    fontSize: '0.8rem',
  },
};

export default ProgramaForm;
