package cl.keber;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DatabaseMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void tablaProgramaFormativoDebeExistir() {
        String sql = "SELECT EXISTS (" +
                     "  SELECT FROM information_schema.tables " +
                     "  WHERE table_schema = 'public' " +
                     "  AND table_name = 'programa_formativo'" +
                     ")";
        Boolean existe = jdbcTemplate.queryForObject(sql, Boolean.class);
        assertTrue(existe, "La tabla 'programa_formativo' debería existir.");
    }

    @Test
    void tablaClienteDebeExistir() {
        String sql = "SELECT EXISTS (" +
                     "  SELECT FROM information_schema.tables " +
                     "  WHERE table_schema = 'public' " +
                     "  AND table_name = 'cliente'" +
                     ")";
        Boolean existe = jdbcTemplate.queryForObject(sql, Boolean.class);
        assertTrue(existe, "La tabla 'cliente' debería existir.");
    }

    @Test
    void tablaFacilitadorDebeExistir() {
        String sql = "SELECT EXISTS (" +
                     "  SELECT FROM information_schema.tables " +
                     "  WHERE table_schema = 'public' " +
                     "  AND table_name = 'facilitador'" +
                     ")";
        Boolean existe = jdbcTemplate.queryForObject(sql, Boolean.class);
        assertTrue(existe, "La tabla 'facilitador' debería existir.");
    }

    @Test
    void tablaHabilitacionFacilitadorDebeExistir() {
        String sql = "SELECT EXISTS (" +
                     "  SELECT FROM information_schema.tables " +
                     "  WHERE table_schema = 'public' " +
                     "  AND table_name = 'habilitacion_facilitador'" +
                     ")";
        Boolean existe = jdbcTemplate.queryForObject(sql, Boolean.class);
        assertTrue(existe, "La tabla 'habilitacion_facilitador' debería existir.");
    }
}
