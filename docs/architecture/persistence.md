# Persistence

How the domain reaches PostgreSQL without knowing that PostgreSQL exists.

## Two classes, two jobs

```text
TrainingProgram            = the business model
                             cl.keber.domain.model

TrainingProgramJpaEntity   = the Hibernate / PostgreSQL representation
                             cl.keber.infrastructure.persistence.entity
```

Before Milestone 3 these were the same class, which is why the domain could not
be compiled or tested without JPA on the classpath. Splitting them is the whole
change.

`TrainingProgramJpaEntity` is deliberately anemic — fields, a no-arg
constructor, getters and setters, nothing else:

```java
@Entity
@Table(name = "training_program")
public class TrainingProgramJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    // no-arg constructor, getters, setters
}
```

That it has no behaviour is not a smell here. It is not the domain model; it is
a row of a table with a Java shape. All the business rules live in
`TrainingProgram` and its Value Objects, and Hibernate never touches them.

### Not to be confused with the DTO

There are now **two** flat, anemic representations of a training program, and
they are not interchangeable:

| | `TrainingProgramJpaEntity` | `TrainingProgramDto` |
|---|---|---|
| Package | `infrastructure.persistence.entity` | `infrastructure.web.dto` |
| Shaped by | the `training_program` table | the JSON wire format |
| Serves | Hibernate | Jackson |
| Changes when | the schema changes | the API contract changes |

Both exist so that `TrainingProgram` has to answer to neither. Collapsing them
into one class would re-couple the database schema to the public API — a change
to either would force a change to the other, which is exactly the knot this
refactor unties.

## The pieces

| Class | Package | Role |
|---|---|---|
| `TrainingProgramJpaEntity` | `infrastructure.persistence.entity` | table mapping |
| `SpringDataTrainingProgramRepository` | `infrastructure.persistence.repository` | `extends JpaRepository<TrainingProgramJpaEntity, Long>` |
| `TrainingProgramPersistenceMapper` | `infrastructure.persistence.mapper` | domain <-> JPA entity |
| `JpaTrainingProgramRepositoryAdapter` | `infrastructure.persistence.adapter` | `implements` the domain port |

Every one of them lives in infrastructure, so every one of them is free to use
as much technology as it wants.

## The mapper

Translation happens in exactly one place, so the rest of the system only ever
sees one of the two representations:

```java
public static TrainingProgram toDomain(TrainingProgramJpaEntity entity) {
    return TrainingProgram.restore(
        entity.getId(),
        new TrainingProgramCode(entity.getCode()),
        new TrainingProgramName(entity.getName()),
        new TrainingPeriod(entity.getStartDate(), entity.getEndDate()),
        new TrainingProgramStatus(entity.getStatus()));
}
```

`toDomain` rehydrates through `TrainingProgram.restore`, which is exactly what
rehydration means: this program already exists and keeps its identity.
`toJpaEntity` goes the other way, reading the Value Objects' `value()`,
`startDate()` and `endDate()` accessors back out into plain columns.

Both methods are `static` on a `final` class — the mapper holds no state, so
there is nothing to inject.

Note this is *not* the only caller of `restore`. The web mapper also calls it,
for a request body that carries an `id`. What the two have in common is that
both are reconstructing a program that already exists; `create` is reserved for
one that does not.

## The adapter

The adapter is the class that inverts the dependency. It implements the domain
port, and it is the only class in the system that knows both worlds:

```java
@Repository
public class JpaTrainingProgramRepositoryAdapter implements TrainingProgramRepository {

    private final SpringDataTrainingProgramRepository repository;

    @Override
    public Optional<TrainingProgram> findById(Long id) {
        return repository.findById(id)
                         .map(TrainingProgramPersistenceMapper::toDomain);
    }
}
```

`@Repository` is the one framework annotation in the chain, and it sits on the
outermost class — the one whose whole job is to be replaceable.

```text
       TrainingProgramRepository            (domain port)
                  ^
                  | implements
   JpaTrainingProgramRepositoryAdapter
                  |
                  v
  SpringDataTrainingProgramRepository
                  |
                  v
             JpaRepository  ->  Hibernate  ->  PostgreSQL
```

Swapping PostgreSQL for anything else — another store, an in-memory fake for a
test — means writing a different implementation of the port. Nothing in
`domain` or `application` changes.

## The schema did not change

**This refactor introduced no Flyway migration.**

`TrainingProgramJpaEntity` maps the same `training_program` table and exactly the
same columns the old entity mapped: `id`, `code`, `name`, `start_date`,
`end_date`, `status`. The schema is still whatever migrations `V1` through `V5`
produced — `V1__programa_formativo.sql` created the table and
`V5__rename_to_english.sql` renamed the tables and columns to English and added
`start_date` / `end_date`.

The table also carries `description`, `revision`, `valid_from` and `valid_to`,
which belong to the conceptual model in [`../diag-class.md`](../diag-class.md)
but are **not mapped** by the entity and never were. Moving JPA into
infrastructure neither adopts nor drops them; they stay exactly as they are.
Mapping them would be a feature, not a refactor, so the JPA entity deliberately
mirrors the old entity's column set and nothing more. Whether to map or remove
them is a separate decision.

> **Known issue, not introduced here.** `spring.jpa.hibernate.ddl-auto=update` is
> active alongside Flyway, so Hibernate widens some column types on every
> startup on top of the migrated schema. That predates this refactor and is left
> untouched by it, but it should be resolved before production: Flyway should be
> the only thing that writes to the schema.

There is no architectural reason for separating the domain from JPA to touch the
database: the split is a Java-side concern. Keeping the schema fixed also means
the existing `@DataJpaTest` coverage and the deployed database stay valid, and
that the change is reversible without a down-migration.

The ER diagram in [`../diag-er.md`](../diag-er.md) is therefore unchanged and
still accurate.

## Testing

Persistence is tested where the technology is, with `@DataJpaTest` against H2:

```text
TrainingProgram  ->  adapter  ->  mapper  ->  JPA  ->  H2 / PostgreSQL
```

The test drives the **port**, passes domain objects in, gets domain objects
back, and so covers the entity mapping, the mapper and the adapter in one pass.
The JPA annotation assertions that used to live in the domain test now belong
here — the mapping is verified by actually persisting a row, which is a stronger
check than reflecting over annotations.
