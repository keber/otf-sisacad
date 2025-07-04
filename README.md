[![codecov](https://codecov.io/gh/keber/otf-sisacad/graph/badge.svg?token=9SP56NUD2K)](https://codecov.io/gh/keber/otf-sisacad)
[![Java CI](https://github.com/keber/otf-sisacad/actions/workflows/test.yml/badge.svg)](https://github.com/keber/otf-sisacad/actions/workflows/test.yml)
[![Tests](https://img.shields.io/endpoint?url=https%3A%2F%2Fgist.githubusercontent.com%2Fkeber%2Fbf1bff0a38948277a263377401536440%2Fraw%2Fotf-sisacad-junit-tests.json)](https://keber.github.io/otf-sisacad/tests/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=coverage)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=bugs)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)

# OTEC TuFuturo - Sistema Académico

## Introducción y Contexto
## Introducción y Contexto
OTEC TuFuturo es una organización recientemente creada cuyo propósito es brindar formación virtual a trabajadores en materias de salud y seguridad ocupacional en operaciones mineras.

Con este objetivo, la organización ha solicitado el desarrollo de un sistema académico que permita registrar y gestionar las actividades de capacitación impartidas, incluyendo programas formativos, sus ediciones, mandantes, facilitadores y alumnos. El sistema deberá registrar además la participación individual de los alumnos, su asistencia, puntajes en evaluaciones diagnósticas y finales, así como el estado final de aprobación o reprobación del curso.

## Características del Negocio
Cada programa formativo corresponde a una materia específica (por ejemplo, Ley 16.744, Ley 21.643, Uso y manejo de extintores, entre otros) y podrá tener múltiples versiones, según se requiera su actualización por cambios en la normativa o contenidos pedagógicos.

Los programas se ofrecen a través de ediciones de curso, que pueden impartirse varias veces al año y ser contratadas por uno o varios mandantes. Un mismo mandante puede solicitar múltiples ediciones durante el año, pero no se permitirá mezclar alumnos de distintos mandantes en una misma clase.

Dado que cada clase admite un máximo de 20 participantes, los alumnos de un mandante serán divididos en una o más secciones, según corresponda. Cada sección contará con un facilitador, quien debe estar habilitado para impartir uno o varios programas formativos. Un mismo facilitador puede estar a cargo de varias secciones, siempre que no se superpongan en el horario.

---

# Documento de Visión Funcional – Sistema Académico OTEC TuFuturo

## 1. Nombre del proyecto

**Sistema Académico para Gestión de Capacitación – OTEC TuFuturo**


## 2. Descripción general

**OTEC TuFuturo** es un organismo técnico de capacitación recientemente creado, cuyo objetivo es ofrecer formación virtual a trabajadores en materias de salud y seguridad ocupacional en operaciones mineras.

Con este sistema se busca apoyar la gestión académica de los procesos de capacitación, facilitando la administración de programas formativos, cursos impartidos, facilitadores, alumnos y empresas clientes, así como el registro de la participación, evaluación y resultados de los alumnos.


## 3. Objetivo del sistema

Desarrollar una plataforma web interna para registrar, organizar y consultar información asociada a los cursos impartidos por el OTEC, incluyendo:

* Definición de programas formativos y sus versiones.
* Creación de ediciones de curso específicas para empresas clientes.
* División de cursos en secciones según el número de alumnos.
* Asignación de facilitadores habilitados a las secciones.
* Registro de matrícula de alumnos por sección.
* Registro de asistencia, evaluaciones y resultados de los alumnos.


## 4. Actores y usuarios esperados

| Rol                         | Descripción                                                                                                                     |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| **Administrador Académico** | Usuario interno responsable de gestionar programas, cursos, secciones, facilitadores y alumnos. Accede a toda la funcionalidad. |
| **Facilitador**             | Encargado de impartir secciones. Registra asistencia y notas. Puede ver solo sus cursos asignados.                              |
| **Empresa Cliente**         | Entidad que solicita la capacitación. No interactúa directamente con el sistema en esta etapa.                                  |
| **Alumno**                  | Persona capacitada. Su información es administrada por el sistema, pero no tiene acceso directo en esta versión.                |


## 5. Funcionalidades incluidas en el MVP

* Gestión de **programas formativos** y sus versiones.
* Gestión de **facilitadores** y su habilitación para programas específicos.
* Gestión de **empresas clientes**.
* Creación de **ediciones de curso** (instancias anuales/por cliente).
* División de ediciones en **secciones** según número de alumnos.
* Asignación de **facilitadores a secciones**.
* Registro de **alumnos** por parte del administrador.
* **Matrícula** de alumnos en ediciones y asignación automática a secciones.
* Registro de **asistencia y evaluaciones ** por parte del facilitador.
* Cálculo del **estado final del alumno** (aprobado/reprobado).


## 6. Funcionalidades excluidas (fuera de alcance MVP)

* Portal de autoservicio para alumnos o empresas clientes.
* Notificaciones por correo o mensajería.
* Reportes avanzados o descargables.
* Control de versiones legales automatizado.
* Integración con SENCE u otras plataformas externas.
* Gestión de usuarios, autenticación y roles con privilegios diferenciados (se asumirá acceso pleno por ahora).


## 7. Restricciones técnicas

* Se priorizará una solución web funcional, sin dependencias externas complejas.
* El sistema puede operar en modo local o desplegarse en un servidor básico (VPS o nube).
* No se contempla por ahora el uso de frameworks pesados o arquitecturas distribuidas.


## 8. Valor para la organización

* Centralización y trazabilidad de los cursos impartidos.
* Mejora en la asignación de recursos humanos (facilitadores).
* Facilita el cumplimiento normativo en capacitación de trabajadores.
* Permite escalar las operaciones y responder a múltiples clientes con control interno.


## 9. Indicadores de éxito del MVP

* Se puede crear un programa formativo, una edición, secciones y matrículas sin intervención técnica.
* Se pueden registrar y consultar evaluaciones de los alumnos.
* El sistema permite distinguir qué facilitadores están habilitados para cada programa.
* El equipo interno puede operar el sistema con mínima capacitación.

---

# Backlog Inicial - Sistema Académico OTEC TuFuturo

Este documento contiene las historias de usuario organizadas en 4 sprints, alineadas con el documento de visión funcional. Cada historia incluye su redacción en formato estándar, prioridad y criterios de aceptación.


## Sprint 1 – Fundaciones del sistema

### 1. Registrar programas formativos

**Historia**:
Como administrador académico, quiero crear programas formativos con su nombre, versión y vigencia, para tener una base estructurada sobre la cual ofrecer cursos.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se puede crear, editar, listar y desactivar un programa formativo.
* Se registra su versión y estado de vigencia.

### 2. Registrar empresas clientes

**Historia**:
Como administrador académico, quiero registrar empresas clientes, para poder asociarlas a cursos impartidos.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se pueden registrar clientes con nombre, RUT y contacto.
* El listado debe mostrar empresas activas.

### 3. Registrar facilitadores

**Historia**:
Como administrador académico, quiero registrar facilitadores, para luego asignarlos a secciones de cursos.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se pueden registrar facilitadores con nombre, RUT, correo.
* Se puede ver un listado de facilitadores disponibles.

### 4. Habilitar facilitadores para programas

**Historia**:
Como administrador académico, quiero asignar a cada facilitador los programas para los cuales está habilitado, para asegurar que sólo puedan impartir cursos acordes a su experiencia.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se puede seleccionar un programa para un facilitador con fecha y estado de habilitación.
* Se puede consultar qué programas tiene habilitado un facilitador.


## Sprint 2 – Cursos y secciones

### 1. Crear ediciones de curso

**Historia**:
Como administrador académico, quiero crear ediciones de curso a partir de un programa y asociarlas a una empresa cliente, para planificar su ejecución.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se puede seleccionar un programa, una empresa cliente y definir fechas.
* La edición queda asociada a la versión del programa vigente.

### 2. Dividir edición de curso en secciones

**Historia**:
Como administrador académico, quiero dividir una edición en secciones según el número de alumnos esperados, para respetar el aforo por clase.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se puede ingresar el número total de alumnos y el sistema sugiere cantidad de secciones (20 por sección).
* Se pueden crear manualmente secciones con identificador y horario.

### 3. Asignar facilitadores a secciones

**Historia**:
Como administrador académico, quiero asignar un facilitador habilitado a cada sección, para asegurar que imparta el contenido autorizado.
**Prioridad**: Alta
**Criterios de aceptación**:

* Sólo se pueden seleccionar facilitadores habilitados para ese programa.
* Se valida que no haya superposición horaria en secciones asignadas.


## Sprint 3 – Matrícula de alumnos

### 1. Registrar alumnos

**Historia**:
Como administrador académico, quiero registrar los datos personales de los alumnos enviados por la empresa cliente, para luego asignarlos a secciones.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se pueden registrar nombre, RUT, correo y empresa del alumno.
* Se pueden importar varios alumnos desde un archivo (CSV u otro).

### 2. Matricular alumnos en edición de curso

**Historia**:
Como administrador académico, quiero matricular alumnos en una edición de curso, para que puedan ser asignados a secciones.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se puede seleccionar una edición y matricular a uno o más alumnos.
* El sistema almacena la matrícula con fecha y estado.

### 3. Asignar automáticamente alumnos a secciones

**Historia**:
Como sistema, quiero distribuir automáticamente a los alumnos matriculados en las secciones de la edición, para balancear los grupos y respetar el aforo.
**Prioridad**: Media
**Criterios de aceptación**:

* Cada sección tiene máximo 20 alumnos.
* Se puede revisar y ajustar manualmente las asignaciones.


## Sprint 4 – Evaluación y resultados

### 1. Registrar asistencia y evaluaciones

**Historia**:
Como facilitador, quiero registrar la asistencia, evaluación diagnóstica y nota final de los alumnos, para reflejar su desempeño en el curso.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se puede ingresar asistencia en porcentaje.
* Se registran notas diagnóstica y final.
* Solo se pueden editar datos de secciones asignadas al facilitador.

### 2. Determinar estado final del alumno

**Historia**:
Como sistema, quiero calcular automáticamente si un alumno aprueba o reprueba, para facilitar la generación de certificados o reportes.
**Prioridad**: Alta
**Criterios de aceptación**:

* Se define un criterio (por ejemplo: asistencia ≥ 75% y nota final ≥ 60).
* El estado final se actualiza al registrar las notas.

### 3. Consultar resultados por sección

**Historia**:
Como administrador, quiero ver un resumen con los alumnos de una sección y sus estados finales, para evaluar el resultado del curso.
**Prioridad**: Media
**Criterios de aceptación**:

* Se puede ver la lista de alumnos con su asistencia, notas y estado.
* Se puede filtrar por aprobado / reprobado.

---

- [ Documento de Requerimientos funcionales ](docs/frd.md)
- [ Backlog ](docs/backlog.md)
- [ Diagrama de clases ](doc/diag-class.md)
- [ Diagrama ER ](doc/diag-er.md)
- [ Sprint 0 – Registro de configuración inicial](docs/sprint-0.md)