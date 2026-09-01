-- Rename tables and columns to English identifiers.
-- V1-V4 are left untouched (applied migrations are immutable); this migration
-- carries the schema forward.

ALTER TABLE programa_formativo RENAME TO training_program;
ALTER TABLE training_program RENAME COLUMN codigo TO code;
ALTER TABLE training_program RENAME COLUMN nombre TO name;
ALTER TABLE training_program RENAME COLUMN descripcion TO description;
ALTER TABLE training_program RENAME COLUMN version TO revision;
ALTER TABLE training_program RENAME COLUMN fecha_vigencia_desde TO valid_from;
ALTER TABLE training_program RENAME COLUMN fecha_vigencia_hasta TO valid_to;
ALTER TABLE training_program RENAME COLUMN estado TO status;

-- start_date / end_date are the columns the JPA entity maps. They are not part of
-- V1; add them here so a fresh database has them without relying on ddl-auto.
ALTER TABLE training_program ADD COLUMN IF NOT EXISTS start_date DATE;
ALTER TABLE training_program ADD COLUMN IF NOT EXISTS end_date DATE;

ALTER TABLE cliente RENAME TO client;
ALTER TABLE client RENAME COLUMN razon_social TO legal_name;
ALTER TABLE client RENAME COLUMN rut TO tax_id;
ALTER TABLE client RENAME COLUMN contacto TO contact;
ALTER TABLE client RENAME COLUMN correo TO email;

ALTER TABLE facilitador RENAME TO facilitator;
ALTER TABLE facilitator RENAME COLUMN nombre TO name;
ALTER TABLE facilitator RENAME COLUMN rut TO tax_id;
ALTER TABLE facilitator RENAME COLUMN correo TO email;
ALTER TABLE facilitator RENAME COLUMN profesion TO profession;

ALTER TABLE habilitacion_facilitador RENAME TO facilitator_qualification;
ALTER TABLE facilitator_qualification RENAME COLUMN programa_formativo_id TO training_program_id;
ALTER TABLE facilitator_qualification RENAME COLUMN fecha_habilitacion TO qualification_date;
ALTER TABLE facilitator_qualification RENAME COLUMN otorgado_por TO granted_by;
ALTER TABLE facilitator_qualification RENAME COLUMN estado TO status;
ALTER TABLE facilitator_qualification RENAME COLUMN observaciones TO notes;
