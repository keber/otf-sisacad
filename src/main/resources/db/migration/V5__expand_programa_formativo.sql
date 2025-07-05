ALTER TABLE programa_formativo
    ADD descripcion TEXT,
    ADD version INT,
    ADD fecha_vigencia_desde DATE,
    ADD fecha_vigencia_hasta DATE,
    ADD estado VARCHAR(50);
