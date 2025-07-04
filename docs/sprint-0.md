# 🏁 Registro Sprint 0 – Proyecto OTF SISACAD

## 🎯 Objetivo del Sprint
Configurar el entorno base del proyecto y asegurar una integración funcional con la base de datos, incluyendo control de versiones de esquema con Flyway.

## 🚧 Actividades previas
* Clonación de repo a partir de template propio con github actions incorporado
* Configuración de entorno AzureDevops e integración con repositorio github
* Análisis y Diseño del proyecto, registro del backlog en AzureDevops

## ✅ Actividades realizadas

| ID  | Tarea                                                        | Estado     | Detalles                                                                 |
|-----|--------------------------------------------------------------|------------|--------------------------------------------------------------------------|
| 78  | Crear proyecto en Railway                                    | ✅ Cerrado | Base de datos PostgreSQL creada en Railway                               |
| 79  | Obtener URL, usuario, contraseña y puerto                    | ✅ Cerrado | Datos integrados como variables de entorno                               |
| 80  | Configurar `application.properties`                          | ✅ Cerrado | Usa `${}` con variables de entorno                                       |
| 81  | Verificar conexión                                           | ✅ Cerrado | Aplicación Spring Boot conecta correctamente a PostgreSQL                |
| 82  | Agregar dependencias Flyway al `pom.xml`                    | ✅ Cerrado | Se añadió `flyway-core`, sin versión explícita por uso de Spring Boot starter parent |
| 83  | Crear carpeta `db/migration`                                 | ✅ Cerrado | En `src/main/resources/db/migration`                                     |
| 84  | Verificar arranque correcto de aplicación con Flyway         | ✅ Cerrado | Flyway ejecuta migración `V1__create_dummy_table.sql` automáticamente    |
| 85  | Crear y probar migración de tabla dummy                      | ✅ Cerrado | Tabla creada y validada al iniciar la aplicación                         |
| 91  | Resolver observaciones Sonar (deuda técnica)                 | ✅ Cerrado | Uso de logger en lugar de `System.out`, try-with-resources, entre otros  |


## 🛠️ Deuda técnica registrada y resuelta

Se creó la tarea **91** para resolver observaciones detectadas por **SonarQube** durante la configuración inicial del proyecto.

### Mejoras implementadas:
- Reemplazo de `System.out.println` por `Logger` (`org.slf4j.Logger`)
- Uso de `try-with-resources` para manejo seguro de conexiones JDBC
- Eliminación de código redundante y prácticas no recomendadas

🔒 Esta tarea fue cerrada tras confirmar que las observaciones habían sido resueltas, dejando el proyecto con una base más limpia para los próximos sprints.


## 🧪 Incidentes resueltos

- ⚠️ **Error de checksum Flyway** al ejecutar en GitHub Actions, causado por diferencias en los finales de línea (CRLF ↔ LF).
- 🧼 Se solucionó con:
  - Configuración de `.gitattributes` para forzar uso de `LF`
  - Ejecución puntual de `mvn flyway:repair` usando variables de entorno
- 🔧 El plugin `flyway-maven-plugin` se usó temporalmente y fue retirado del `pom.xml` al no ser necesario de forma continua.


## ⚙️ Configuración destacada

- `application.properties` configurado para leer variables de entorno (`${DB_HOST}`, `${DB_USERNAME}`, etc.)
- GitHub Actions inyecta estas variables desde los **secrets**
- Spring Boot ejecuta automáticamente las migraciones de Flyway en tiempo de arranque


## 📦 Estado final del Sprint

- Proyecto funcional y conectado a base de datos
- Migraciones controladas y aplicadas correctamente
- CI/CD funcionando sin errores
- Código limpio y alineado con buenas prácticas


## 📁 Archivos relevantes

- `.env` (local, no versionado)
- `.gitattributes`
- `src/main/resources/application.properties`
- `src/main/resources/db/migration/V1__create_dummy_table.sql`




