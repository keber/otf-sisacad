[![codecov](https://codecov.io/gh/keber/otf-sisacad/graph/badge.svg?token=9SP56NUD2K)](https://codecov.io/gh/keber/otf-sisacad)
[![Java CI](https://github.com/keber/otf-sisacad/actions/workflows/test.yml/badge.svg)](https://github.com/keber/otf-sisacad/actions/workflows/test.yml)
[![Tests](https://img.shields.io/endpoint?url=https%3A%2F%2Fgist.githubusercontent.com%2Fkeber%2Fbf1bff0a38948277a263377401536440%2Fraw%2Fotf-sisacad-junit-tests.json)](https://keber.github.io/otf-sisacad/tests/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=coverage)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=keber_otf-sisacad&metric=bugs)](https://sonarcloud.io/summary/new_code?id=keber_otf-sisacad)

# OTEC TuFuturo - Academic System

## Introduction and Context
OTEC TuFuturo is a recently created organization whose purpose is to provide virtual training to workers in occupational health and safety in mining operations.

To this end, the organization has requested the development of an academic system that allows recording and managing the training activities delivered, including training programs, their editions, client organizations, facilitators and students. The system must also record each student's individual participation, their attendance, scores in diagnostic and final assessments, as well as the final pass/fail status of the course.

## Business Characteristics
Each training program corresponds to a specific subject (for example, Law 16.744, Law 21.643, Use and handling of fire extinguishers, among others) and may have multiple versions, as updates are required due to changes in regulations or pedagogical content.

Programs are offered through course editions, which can be delivered several times a year and be contracted by one or more client organizations. The same client may request multiple editions during the year, but mixing students from different clients in the same class is not allowed.

Since each class admits a maximum of 20 participants, a client's students are split into one or more sections, as appropriate. Each section has a facilitator, who must be qualified to deliver one or more training programs. The same facilitator may be in charge of several sections, as long as they do not overlap in schedule.

- [ Functional Requirements Document ](docs/frd.md)
- [ Backlog ](docs/backlog.md)
- [ Class diagram ](docs/diag-class.md)
- [ ER diagram ](docs/diag-er.md)
- [ Sprint 0 – Initial configuration log](docs/sprint-0.md)
- [ Sprint 1: 44 Register training programs: 106 Create the TrainingProgram class](docs/106.md)
- [ Sprint 1: 44 Register training programs: 107 Create the TrainingProgramRepository repository](docs/107.md)
- [ Sprint 1: 44 Register training programs: 108 Create the TrainingProgramService service](docs/108.md)
- [ Sprint 1: 44 Register training programs: 109 Create the TrainingProgramController controller](docs/109.md)
- [ Sprint 1: 44 Register training programs: 110 Create the TrainingProgram DTO and Mapper](docs/110.md)
- [ Sprint 1: 44 Register training programs: 112 Create the frontend for TrainingProgram](docs/112.md)
- [ Sprint 1: 44 Register training programs: 111 Technical Debt](docs/111.md)

## Architecture

From Milestone 3, the domain model is separated from the JPA representation.
`TrainingProgram` is no longer a persistence entity; JPA lives in
`infrastructure.persistence`. The REST contract under `/programs` is unchanged.

The code is organised by architectural responsibility — `domain`, `application`
and `infrastructure` — with dependencies pointing inward, and the boundary is
enforced by ArchUnit tests rather than convention alone.

- [ Clean Architecture: layering and the dependency rule ](docs/architecture/clean-architecture.md)
- [ Package structure and dependency rules ](docs/architecture/package-dependencies.md)
- [ The TrainingProgram domain model ](docs/architecture/domain-model.md)
- [ Persistence: domain entity, JPA entity and the adapter ](docs/architecture/persistence.md)

The per-task documents above (AB#106 to AB#110) remain as historical
traceability and are annotated where they describe the previous design.

## Project Setup Instructions

### Pre requirements

- [x] Operating System: Windows or Linux (WSL included)
- [x] Java JDK 25 + Maven installed
  * Linux Installation (Ubuntu, WSL)
  ```bash
  sudo apt update
  sudo apt install openjdk-25-jdk maven
  ```
- [x] Clone the project
  ```bash
  git clone https://github.com/keber/otf-sisacad.git
  ```

### Configuration

* Create .env file in project root folder and load the following vars with actual values for your postgresql database:
  ```bash
  DB_HOST=
  DB_PORT=
  DB_DATABASE=
  DB_USERNAME=
  DB_PASSWORD=
  ```
* Load the .env file
    * in bash:
    ```bash
    export $(cat .env | xargs)
    ```
    * in Powershell: 
    ```powershell
    Get-Content .env | ForEach-Object {
        if ($_ -match "^(.*?)=(.*)$") { [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2]) }
    }
    ```

#### Back-end
* Build 
  ```bash
  mvn clean compile
  ```
* Run tests
  ```bash
  mvn clean verify
  ```
* Run app
  ```bash
  export $(cat .env | xargs) mvn spring-boot:run 
  ```

#### Front-end
* Build
  ```
  cd frontend
  npm install
  ```
* Run tests
  ```
  npm test
  ```
* Run app
  ```
  npm start
  ```
