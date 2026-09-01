## Class Diagram

Conceptual domain model of the full academic system. For how the implemented
`TrainingProgram` slice is actually structured in code, see
[Implementation: the TrainingProgram slice](#implementation-the-trainingprogram-slice)
below.

```mermaid
classDiagram
    class TrainingProgram {
        +int id
        +string code
        +string name
        +string description
        +int revision
        +date validFrom
        +date validTo
        +string status
    }

    class CourseEdition {
        +int id
        +string code
        +date startDate
        +date endDate
        +int programRevision
    }

    class Client {
        +int id
        +string legalName
        +string taxId
        +string contact
    }

    class Section {
        +int id
        +int number
        +string schedule
        +date materialsAccessDeadline
    }

    class Facilitator {
        +int id
        +string name
        +string taxId
        +string email
    }

    class FacilitatorQualification {
        +int id
        +date qualificationDate
        +string grantedBy
        +string status  // active, expired, suspended
        +string notes
    }

    class Student {
        +int id
        +string name
        +string taxId
        +string email
        +string company
    }

    class Enrollment {
        +float attendance
        +float diagnosticScore
        +float finalScore
        +string finalStatus
    }


    %% Relationships
    TrainingProgram "1" --> "0..*" CourseEdition
    CourseEdition "1" --> "1" Client
    CourseEdition "1" --> "0..*" Section
    Facilitator "1" --> "0..*" Section
    Section "1" --> "0..*" Enrollment
    Enrollment "1" --> "1" Student

    Facilitator "1" --> "0..*" FacilitatorQualification
    TrainingProgram "1" --> "0..*" FacilitatorQualification
```

---

## Implementation: the TrainingProgram slice

The diagram above is the **conceptual domain model** of the full academic
system. Most of it is not implemented yet.

`TrainingProgram` *is* implemented, and from Milestone 3 it is built as a Clean
Architecture vertical slice. The single `TrainingProgram` class of the diagram
above is now realised as two distinct classes — the domain model and its JPA
representation — separated by a port and an adapter.

See [`architecture/clean-architecture.md`](architecture/clean-architecture.md)
for the reasoning.

```mermaid
classDiagram
    direction LR

    class TrainingProgram {
        <<domain entity>>
        -Long id
        -TrainingProgramCode code
        -TrainingProgramName name
        -TrainingPeriod period
        -TrainingProgramStatus status
        +create(code, name, period, status) TrainingProgram$
        +restore(id, code, name, period, status) TrainingProgram$
        +rename(TrainingProgramName)
        +reschedule(TrainingPeriod)
        +changeStatus(TrainingProgramStatus)
    }

    class TrainingProgramCode {
        <<value object>>
        +String value
    }
    class TrainingProgramName {
        <<value object>>
        +String value
    }
    class TrainingPeriod {
        <<value object>>
        +LocalDate startDate
        +LocalDate endDate
    }
    class TrainingProgramStatus {
        <<value object>>
        +String value
    }

    class TrainingProgramRepository {
        <<interface>>
        +save(TrainingProgram) TrainingProgram
        +findById(Long) Optional~TrainingProgram~
        +findAll() List~TrainingProgram~
        +existsById(Long) boolean
        +deleteById(Long)
    }

    class CreateTrainingProgramUseCase {
        <<interface>>
    }
    class ListTrainingProgramsUseCase {
        <<interface>>
    }
    class UpdateTrainingProgramUseCase {
        <<interface>>
    }
    class DeleteTrainingProgramUseCase {
        <<interface>>
    }

    class TrainingProgramApplicationService {
        <<application>>
        -TrainingProgramRepository repository
    }

    class TrainingProgramJpaEntity {
        <<JPA entity>>
        -Long id
        -String code
        -String name
        -LocalDate startDate
        -LocalDate endDate
        -String status
    }

    class JpaTrainingProgramRepositoryAdapter {
        <<infrastructure>>
    }

    class SpringDataTrainingProgramRepository {
        <<interface>>
    }

    class TrainingProgramPersistenceMapper {
        <<infrastructure>>
        +toDomain(TrainingProgramJpaEntity) TrainingProgram
        +toJpaEntity(TrainingProgram) TrainingProgramJpaEntity
    }

    class TrainingProgramController {
        <<REST adapter>>
    }

    class TrainingProgramDto {
        <<DTO>>
        -Long id
        -String code
        -String name
        -LocalDate startDate
        -LocalDate endDate
        -String status
    }

    class TrainingProgramMapper {
        <<web mapper>>
        +toDto(TrainingProgram) TrainingProgramDto$
        +toDomain(TrainingProgramDto) TrainingProgram$
    }

    %% domain composition
    TrainingProgram *-- TrainingProgramCode
    TrainingProgram *-- TrainingProgramName
    TrainingProgram *-- TrainingPeriod
    TrainingProgram *-- TrainingProgramStatus

    %% dependency inversion
    TrainingProgramApplicationService ..> TrainingProgramRepository : depends on the port
    JpaTrainingProgramRepositoryAdapter ..|> TrainingProgramRepository : implements

    %% persistence adapter
    JpaTrainingProgramRepositoryAdapter --> SpringDataTrainingProgramRepository
    JpaTrainingProgramRepositoryAdapter --> TrainingProgramPersistenceMapper
    SpringDataTrainingProgramRepository ..> TrainingProgramJpaEntity
    TrainingProgramPersistenceMapper ..> TrainingProgramJpaEntity
    TrainingProgramPersistenceMapper ..> TrainingProgram

    %% REST adapter
    TrainingProgramController ..> CreateTrainingProgramUseCase
    TrainingProgramController ..> ListTrainingProgramsUseCase
    TrainingProgramController ..> UpdateTrainingProgramUseCase
    TrainingProgramController ..> DeleteTrainingProgramUseCase
    TrainingProgramApplicationService ..|> CreateTrainingProgramUseCase
    TrainingProgramApplicationService ..|> ListTrainingProgramsUseCase
    TrainingProgramApplicationService ..|> UpdateTrainingProgramUseCase
    TrainingProgramApplicationService ..|> DeleteTrainingProgramUseCase
    TrainingProgramController ..> TrainingProgramDto : binds and returns
    TrainingProgramController ..> TrainingProgramMapper
    TrainingProgramMapper ..> TrainingProgramDto
    TrainingProgramMapper ..> TrainingProgram
```

Reading the diagram:

- `TrainingProgram` (domain) holds Value Objects, not primitives, and changes
  only through `rename` / `reschedule` / `changeStatus`.
- `TrainingProgramJpaEntity` holds primitives and is the only class mapped to
  the `training_program` table. It is anemic on purpose.
- `TrainingProgramRepository` is declared in `domain` and implemented in
  `infrastructure`. That single inverted arrow is what keeps the domain free of
  Spring and JPA.
- The controller depends on the four use case interfaces it routes, never on
  the class implementing them and never on a repository. A fifth interface,
  `GetTrainingProgramUseCase`, exists but is deliberately not injected — no
  `GET /programs/{id}` route has ever existed — so it is omitted here.
- `TrainingProgramDto` and `TrainingProgramJpaEntity` look alike — both are flat
  and anemic — but they answer to different masters: the DTO to the JSON
  contract, the JPA entity to the table. Its field order is the response field
  order, so it is not arbitrary.
- Note what `TrainingProgram` does *not* expose: there is no `startDate` or
  `endDate` accessor. The dates are reachable only through `getPeriod()`, which
  is what stops anyone setting one without the other.

The **ER diagram** in [`diag-er.md`](diag-er.md) is unchanged: the domain / JPA
split is a Java-side concern and the `training_program` table and columns are
exactly as Flyway `V1`–`V5` left them. No migration was added for this refactor.
