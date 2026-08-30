# Functional Vision Document – OTEC TuFuturo Academic System

## 1. Project name

**Academic System for Training Management – OTEC TuFuturo**


## 2. General description

**OTEC TuFuturo** is a recently created technical training body whose goal is to offer virtual training to workers in occupational health and safety in mining operations.

This system aims to support the academic management of training processes, making it easier to administer training programs, delivered courses, facilitators, students and client companies, as well as to record student participation, assessment and results.


## 3. System objective

Develop an internal web platform to record, organize and query information related to the courses delivered by the OTEC, including:

* Definition of training programs and their versions.
* Creation of course editions specific to client companies.
* Splitting courses into sections according to the number of students.
* Assignment of qualified facilitators to sections.
* Recording student enrollment per section.
* Recording student attendance, assessments and results.


## 4. Expected actors and users

| Role                        | Description                                                                                                                     |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| **Academic Administrator**  | Internal user responsible for managing programs, courses, sections, facilitators and students. Has access to all functionality. |
| **Facilitator**             | Responsible for delivering sections. Records attendance and grades. Can only see their assigned courses.                        |
| **Client Company**          | Entity that requests the training. Does not interact directly with the system at this stage.                                    |
| **Student**                 | Person being trained. Their information is managed by the system, but they have no direct access in this version.               |


## 5. Features included in the MVP

* Management of **training programs** and their versions.
* Management of **facilitators** and their qualification for specific programs.
* Management of **client companies**.
* Creation of **course editions** (yearly / per-client instances).
* Splitting editions into **sections** according to the number of students.
* Assignment of **facilitators to sections**.
* Registration of **students** by the administrator.
* **Enrollment** of students in editions and automatic assignment to sections.
* Recording of **attendance and assessments** by the facilitator.
* Calculation of the **student's final status** (passed/failed).


## 6. Features excluded (out of MVP scope)

* Self-service portal for students or client companies.
* Email or messaging notifications.
* Advanced or downloadable reports.
* Automated tracking of legal versions.
* Integration with SENCE or other external platforms.
* User management, authentication and roles with differentiated privileges (full access is assumed for now).


## 7. Technical constraints

* A functional web solution will be prioritized, with no complex external dependencies.
* The system can run locally or be deployed on a basic server (VPS or cloud).
* The use of heavyweight frameworks or distributed architectures is not considered for now.


## 8. Value for the organization

* Centralization and traceability of the courses delivered.
* Improved allocation of human resources (facilitators).
* Facilitates regulatory compliance in worker training.
* Enables scaling operations and serving multiple clients with internal control.


## 9. MVP success indicators

* A training program, an edition, sections and enrollments can be created without technical intervention.
* Student assessments can be recorded and queried.
* The system can distinguish which facilitators are qualified for each program.
* The internal team can operate the system with minimal training.
