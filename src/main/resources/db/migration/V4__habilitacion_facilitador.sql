CREATE TABLE habilitacion_facilitador (
    id SERIAL PRIMARY KEY,
    facilitador_id INT NOT NULL,
    programa_formativo_id INT NOT NULL,
    fecha_habilitacion DATE NOT NULL,
    otorgado_por VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    observaciones VARCHAR(100)
);

ALTER TABLE habilitacion_facilitador
ADD CONSTRAINT habilitacion_facilitador_programa_formativo_FK
FOREIGN KEY (programa_formativo_id) REFERENCES programa_formativo;

ALTER TABLE habilitacion_facilitador
ADD CONSTRAINT habilitacion_facilitador_facilitador_FK
FOREIGN KEY (facilitador_id) REFERENCES facilitador;