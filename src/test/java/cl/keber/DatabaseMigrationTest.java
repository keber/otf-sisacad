package cl.keber;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

class DatabaseMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ParameterizedTest
    @ValueSource(strings = {
        "programa_formativo",
        "cliente",
        "facilitador",
        "habilitacion_facilitador"
    })
    void tablaDebeExistir(String nombreTabla) {
        String sql = "SELECT EXISTS (" +
                     "  SELECT FROM information_schema.tables " +
                     "  WHERE table_schema = 'public' " +
                     "  AND table_name = ?" +
                     ")";
        String msg = String.format("La tabla '%s' debería existir.",nombreTabla);
        Boolean existe = jdbcTemplate.queryForObject(sql, Boolean.class, nombreTabla);
        assertTrue(existe, msg);
    }
}
