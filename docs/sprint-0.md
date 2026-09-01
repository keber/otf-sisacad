# 🏁 Sprint 0 Log – OTF SISACAD Project

## 🎯 Sprint goal
Set up the project's base environment and ensure a working integration with the database, including schema version control with Flyway.

## 🚧 Preliminary activities
* Cloned the repo from an in-house template with GitHub Actions already included
* Set up the Azure DevOps environment and integrated it with the GitHub repository
* Project analysis and design; backlog recorded in Azure DevOps

## ✅ Activities completed

| ID  | Task                                                        | Status     | Details                                                                 |
|-----|--------------------------------------------------------------|------------|--------------------------------------------------------------------------|
| 78  | Create project in Railway                                   | ✅ Closed  | PostgreSQL database created in Railway                                  |
| 79  | Obtain URL, username, password and port                     | ✅ Closed  | Data wired in as environment variables                                 |
| 80  | Configure `application.properties`                          | ✅ Closed  | Uses `${}` with environment variables                                  |
| 81  | Verify connection                                           | ✅ Closed  | Spring Boot application connects correctly to PostgreSQL               |
| 82  | Add Flyway dependencies to `pom.xml`                        | ✅ Closed  | Added `flyway-core`, with no explicit version thanks to the Spring Boot starter parent |
| 83  | Create `db/migration` folder                                | ✅ Closed  | At `src/main/resources/db/migration`                                   |
| 84  | Verify the application starts correctly with Flyway         | ✅ Closed  | Flyway runs migration `V1__create_dummy_table.sql` automatically      |
| 85  | Create and test a dummy-table migration                     | ✅ Closed  | Table created and validated on application startup                     |
| 91  | Resolve Sonar findings (technical debt)                     | ✅ Closed  | Use of a logger instead of `System.out`, try-with-resources, among others |

## 🛠️ Technical debt recorded and resolved

Task **91** was created to resolve findings detected by **SonarQube** during the project's initial setup.

### Improvements implemented:
- Replaced `System.out.println` with a `Logger` (`org.slf4j.Logger`)
- Used `try-with-resources` for safe handling of JDBC connections
- Removed redundant code and discouraged practices

🔒 This task was closed after confirming the findings had been resolved, leaving the project with a cleaner base for the next sprints.

## 🧪 Incidents resolved

- ⚠️ **Flyway checksum error** when running in GitHub Actions, caused by line-ending differences (CRLF ↔ LF).
- 🧼 Fixed with:
  - `.gitattributes` configuration to force `LF`
  - A one-off run of `mvn flyway:repair` using environment variables
- 🔧 The `flyway-maven-plugin` was used temporarily and removed from `pom.xml` since it was not needed on an ongoing basis.

## ⚙️ Notable configuration

- `application.properties` configured to read environment variables (`${DB_HOST}`, `${DB_USERNAME}`, etc.)
- GitHub Actions injects these variables from the **secrets**
- Spring Boot automatically runs the Flyway migrations at startup

## 📦 Final sprint status

- Project working and connected to the database
- Migrations controlled and applied correctly
- CI/CD running without errors
- Clean code aligned with good practices

## 📁 Relevant files

- `.env` (local, not versioned)
- `.gitattributes`
- `src/main/resources/application.properties`
- `src/main/resources/db/migration/V1__create_dummy_table.sql`
