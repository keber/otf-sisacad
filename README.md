# OTEC TuFuturo - Sistema Académico

## Contexto
OTEC TuFuturo es una organización recientemente creada cuyo propósito es brindar formación virtual a trabajadores en materias de salud y seguridad ocupacional en operaciones mineras.

Con este objetivo, la organización ha solicitado el desarrollo de un sistema académico que permita registrar y gestionar las actividades de capacitación impartidas, incluyendo programas formativos, sus ediciones, mandantes, facilitadores y alumnos. El sistema deberá registrar además la participación individual de los alumnos, su asistencia, puntajes en evaluaciones diagnósticas y finales, así como el estado final de aprobación o reprobación del curso.

## Características del Negocio
Cada programa formativo corresponde a una materia específica (por ejemplo, Ley 16.744, Ley 21.643, Uso y manejo de extintores, entre otros) y podrá tener múltiples versiones, según se requiera su actualización por cambios en la normativa o contenidos pedagógicos.

Los programas se ofrecen a través de ediciones de curso, que pueden impartirse varias veces al año y ser contratadas por uno o varios mandantes. Un mismo mandante puede solicitar múltiples ediciones durante el año, pero no se permitirá mezclar alumnos de distintos mandantes en una misma clase.

Dado que cada clase admite un máximo de 20 participantes, los alumnos de un mandante serán divididos en una o más secciones, según corresponda. Cada sección contará con un facilitador, quien debe estar habilitado para impartir uno o varios programas formativos. Un mismo facilitador puede estar a cargo de varias secciones, siempre que no se superpongan en el horario.

## Diagrama de Clases

```mermaid
classDiagram
    class ProgramaFormativo {
        +int id
        +string nombre
        +string descripcion
        +int version
        +date fecha_vigencia_desde
        +date fecha_vigencia_hasta
        +string estado
    }

    class EdicionCurso {
        +int id
        +string codigo
        +date fecha_inicio
        +date fecha_termino
        +int version_programa
    }

    class Mandante {
        +int id
        +string razon_social
        +string rut
        +string contacto
    }

    class Seccion {
        +int id
        +int numero
        +string horario
        +date plazo_acceso_materiales
    }

    class Facilitador {
        +int id
        +string nombre
        +string rut
        +string correo
    }

    class HabilitacionFacilitador {
        +int id
        +date fecha_habilitacion
        +string otorgado_por
        +string estado  // vigente, caducada, suspendida
        +string observaciones
    }

    class Alumno {
        +int id
        +string nombre
        +string rut
        +string correo
        +string empresa
    }

    class ParticipacionAlumno {
        +float asistencia
        +float puntaje_diagnostico
        +float puntaje_final
        +string estado_final
    }


    %% Relaciones
    ProgramaFormativo "1" --> "0..*" EdicionCurso
    EdicionCurso "1" --> "1" Mandante
    EdicionCurso "1" --> "0..*" Seccion
    Facilitador "1" --> "0..*" Seccion
    Seccion "1" --> "0..*" ParticipacionAlumno
    ParticipacionAlumno "1" --> "1" Alumno

    Facilitador "1" --> "0..*" HabilitacionFacilitador
    ProgramaFormativo "1" --> "0..*" HabilitacionFacilitador
```

## Diagrama ER

```mermaid
erDiagram

%% =================== ENTIDADES ===================

ProgramaFormativo {
    int id PK
    string nombre
    string descripcion
    int version
    date fecha_vigencia_desde
    date fecha_vigencia_hasta
    string estado
}

EdicionCurso {
    int id PK
    string codigo
    date fecha_inicio
    date fecha_termino
    int version_programa
    int programa_formativo_id FK
    int mandante_id FK
}

Mandante {
    int id PK
    string razon_social
    string rut
    string contacto
}

Seccion {
    int id PK
    int numero
    string horario
    date plazo_acceso_materiales
    int edicion_curso_id FK
    int facilitador_id FK
}

Facilitador {
    int id PK
    string nombre
    string rut
    string correo
}

HabilitacionFacilitador {
    int id PK
    int facilitador_id FK
    int programa_formativo_id FK
    date fecha_habilitacion
    string otorgado_por
    string estado
    string observaciones
}

Alumno {
    int id PK
    string nombre
    string rut
    string correo
    string empresa
}

Matricula {
    int id PK
    int alumno_id FK
    int edicion_curso_id FK
    int seccion_id FK
    float asistencia
    float puntaje_diagnostico
    float puntaje_final
    string estado_final
}

%% =================== RELACIONES ===================

ProgramaFormativo ||--o{ EdicionCurso : ofrece
EdicionCurso }o--|| Mandante : contratado_por
Alumno ||--o{ Matricula : se_matricula
EdicionCurso ||--o{ Matricula : corresponde_a
Seccion ||--o{ Matricula : asignado_a
EdicionCurso ||--o{ Seccion : incluye
Facilitador ||--o{ Seccion : imparte
Facilitador ||--o{ HabilitacionFacilitador : habilitado_para
ProgramaFormativo ||--o{ HabilitacionFacilitador : asociado_a


```

[![codecov](https://codecov.io/gh/keber/otf-sisacad/graph/badge.svg?token=9SP56NUD2K)](https://codecov.io/gh/keber/otf-sisacad)
[![Java CI](https://github.com/keber/otf-sisacad/actions/workflows/test.yml/badge.svg)](https://github.com/keber/otf-sisacad/actions/workflows/test.yml)
[![Tests](https://img.shields.io/endpoint?url=https%3A%2F%2Fgist.githubusercontent.com%2Fkeber%2Fbf1bff0a38948277a263377401536440%2Fraw%2Fotf-sisacad-junit-tests.json)](https://keber.github.io/otf-sisacad/tests/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=coverage)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=bugs)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)

