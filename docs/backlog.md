# Initial Backlog - OTEC TuFuturo Academic System

This document contains the user stories organized into 4 sprints, aligned with the functional vision document. Each story includes its wording in the standard format, priority and acceptance criteria.


## Sprint 1 – System foundations

### 1. Register training programs

**Story**:
As an academic administrator, I want to create training programs with their name, version and validity period, so that I have a structured base on which to offer courses.
**Priority**: High
**Acceptance criteria**:

* A training program can be created, edited, listed and deactivated.
* Its version and validity status are recorded.

### 2. Register client companies

**Story**:
As an academic administrator, I want to register client companies, so that I can associate them with delivered courses.
**Priority**: High
**Acceptance criteria**:

* Clients can be registered with name, RUT and contact.
* The list must show active companies.

### 3. Register facilitators

**Story**:
As an academic administrator, I want to register facilitators, so that I can later assign them to course sections.
**Priority**: High
**Acceptance criteria**:

* Facilitators can be registered with name, RUT and email.
* A list of available facilitators can be viewed.

### 4. Qualify facilitators for programs

**Story**:
As an academic administrator, I want to assign each facilitator the programs for which they are qualified, to ensure they can only deliver courses that match their experience.
**Priority**: High
**Acceptance criteria**:

* A program can be selected for a facilitator with a qualification date and status.
* It is possible to query which programs a facilitator is qualified for.


## Sprint 2 – Courses and sections

### 1. Create course editions

**Story**:
As an academic administrator, I want to create course editions from a program and associate them with a client company, so that I can plan their execution.
**Priority**: High
**Acceptance criteria**:

* A program and a client company can be selected and dates defined.
* The edition is associated with the current version of the program.

### 2. Split a course edition into sections

**Story**:
As an academic administrator, I want to split an edition into sections according to the expected number of students, to respect the per-class capacity.
**Priority**: High
**Acceptance criteria**:

* The total number of students can be entered and the system suggests a number of sections (20 per section).
* Sections can be created manually with an identifier and schedule.

### 3. Assign facilitators to sections

**Story**:
As an academic administrator, I want to assign a qualified facilitator to each section, to ensure they deliver the authorized content.
**Priority**: High
**Acceptance criteria**:

* Only facilitators qualified for that program can be selected.
* It is validated that there is no schedule overlap in assigned sections.


## Sprint 3 – Student enrollment

### 1. Register students

**Story**:
As an academic administrator, I want to register the personal data of the students sent by the client company, so that I can later assign them to sections.
**Priority**: High
**Acceptance criteria**:

* Name, RUT, email and the student's company can be registered.
* Multiple students can be imported from a file (CSV or other).

### 2. Enroll students in a course edition

**Story**:
As an academic administrator, I want to enroll students in a course edition, so that they can be assigned to sections.
**Priority**: High
**Acceptance criteria**:

* An edition can be selected and one or more students enrolled.
* The system stores the enrollment with a date and status.

### 3. Automatically assign students to sections

**Story**:
As the system, I want to automatically distribute enrolled students across the edition's sections, to balance the groups and respect capacity.
**Priority**: Medium
**Acceptance criteria**:

* Each section has a maximum of 20 students.
* Assignments can be reviewed and adjusted manually.


## Sprint 4 – Assessment and results

### 1. Record attendance and assessments

**Story**:
As a facilitator, I want to record students' attendance, diagnostic assessment and final grade, to reflect their performance in the course.
**Priority**: High
**Acceptance criteria**:

* Attendance can be entered as a percentage.
* Diagnostic and final grades are recorded.
* Only data for sections assigned to the facilitator can be edited.

### 2. Determine the student's final status

**Story**:
As the system, I want to automatically calculate whether a student passes or fails, to facilitate the generation of certificates or reports.
**Priority**: High
**Acceptance criteria**:

* A criterion is defined (for example: attendance ≥ 75% and final grade ≥ 60).
* The final status is updated when the grades are recorded.

### 3. Query results by section

**Story**:
As an administrator, I want to see a summary of the students in a section and their final statuses, to evaluate the course outcome.
**Priority**: Medium
**Acceptance criteria**:

* The list of students with their attendance, grades and status can be viewed.
* It can be filtered by passed / failed.

---
