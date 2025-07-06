[![codecov](https://codecov.io/gh/keber/otf-sisacad/graph/badge.svg?token=9SP56NUD2K)](https://codecov.io/gh/keber/otf-sisacad)
[![Java CI](https://github.com/keber/otf-sisacad/actions/workflows/test.yml/badge.svg)](https://github.com/keber/otf-sisacad/actions/workflows/test.yml)
[![Tests](https://img.shields.io/endpoint?url=https%3A%2F%2Fgist.githubusercontent.com%2Fkeber%2Fbf1bff0a38948277a263377401536440%2Fraw%2Fotf-sisacad-junit-tests.json)](https://keber.github.io/otf-sisacad/tests/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=coverage)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=bugs)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)

# OTEC TuFuturo - Sistema Académico

## Introducción y Contexto
OTEC TuFuturo es una organización recientemente creada cuyo propósito es brindar formación virtual a trabajadores en materias de salud y seguridad ocupacional en operaciones mineras.

Con este objetivo, la organización ha solicitado el desarrollo de un sistema académico que permita registrar y gestionar las actividades de capacitación impartidas, incluyendo programas formativos, sus ediciones, mandantes, facilitadores y alumnos. El sistema deberá registrar además la participación individual de los alumnos, su asistencia, puntajes en evaluaciones diagnósticas y finales, así como el estado final de aprobación o reprobación del curso.

## Características del Negocio
Cada programa formativo corresponde a una materia específica (por ejemplo, Ley 16.744, Ley 21.643, Uso y manejo de extintores, entre otros) y podrá tener múltiples versiones, según se requiera su actualización por cambios en la normativa o contenidos pedagógicos.

Los programas se ofrecen a través de ediciones de curso, que pueden impartirse varias veces al año y ser contratadas por uno o varios mandantes. Un mismo mandante puede solicitar múltiples ediciones durante el año, pero no se permitirá mezclar alumnos de distintos mandantes en una misma clase.

Dado que cada clase admite un máximo de 20 participantes, los alumnos de un mandante serán divididos en una o más secciones, según corresponda. Cada sección contará con un facilitador, quien debe estar habilitado para impartir uno o varios programas formativos. Un mismo facilitador puede estar a cargo de varias secciones, siempre que no se superpongan en el horario.

- [ Documento de Requerimientos funcionales ](docs/frd.md)
- [ Backlog ](docs/backlog.md)
- [ Diagrama de clases ](docs/diag-class.md)
- [ Diagrama ER ](docs/diag-er.md)
- [ Sprint 0 – Registro de configuración inicial](docs/sprint-0.md)
- [ Sprint 1: 44 Registrar programas formativos: 106 Crear clase ProgramaFormativo](docs/106.md)