package cl.keber;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OtfSisacadApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context starts correctly.
        assertTrue(true, "El contexto de Spring se cargó correctamente.");
    }

}
