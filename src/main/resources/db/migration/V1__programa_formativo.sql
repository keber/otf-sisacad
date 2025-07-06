CREATE TABLE programa_formativo (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(100),
    version INT,
    fecha_vigencia_desde DATE,
    fecha_vigencia_hasta DATE,
    estado VARCHAR(50)
);