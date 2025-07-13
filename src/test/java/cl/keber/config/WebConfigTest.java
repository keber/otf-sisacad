package cl.keber.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class WebConfigTest {

    @Test
    void shouldCreateCorsConfigurationSource() {
        WebConfig config = new WebConfig();
        WebMvcConfigurer source = config.corsConfigurer();
        assertNotNull(source);
    }
}
