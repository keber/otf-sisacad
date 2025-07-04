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