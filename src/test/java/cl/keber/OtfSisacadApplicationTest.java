package cl.keber;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OtfSisacadApplicationTests {

    @Test
    void contextLoads() {
        // Esta prueba verifica que el contexto de Spring se inicia correctamente.
    }
}