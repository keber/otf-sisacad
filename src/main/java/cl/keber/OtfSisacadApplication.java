package cl.keber;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;


@SpringBootApplication
public class OtfSisacadApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OtfSisacadApplication.class);

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(OtfSisacadApplication.class, args); 
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Datasource: " + dataSource.getConnection().getMetaData().getURL());

        try (Connection connection = dataSource.getConnection()) {
            log.info("✅ Conexión OK: " + connection.getMetaData().getURL());
        } catch (Exception e) {
            log.error("❌ Error de conexión: " + e.getMessage());
        }
    }
}