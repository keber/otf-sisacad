package cl.keber;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

class DatabaseMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.flyway.default-schema}")
    private String schema;

    @ParameterizedTest
    @ValueSource(strings = {
        "training_program",
        "client",
        "facilitator",
        "facilitator_qualification"
    })
    void tableShouldExist(String tableName) {
        String sql = "SELECT EXISTS (" +
                     "  SELECT FROM information_schema.tables " +
                     "  WHERE table_schema = ? " +
                     "  AND table_name = ?" +
                     ")";
        String msg = String.format("Table '%s' should exist.", tableName);
        Boolean exists = jdbcTemplate.queryForObject(
            sql, Boolean.class, schema, tableName
        );
        assertTrue(exists, msg);
    }
}
