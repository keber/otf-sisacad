package cl.keber;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;


@SpringBootApplication
public class OtfSisacadApplication implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(OtfSisacadApplication.class, args); 
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Datasource: " + dataSource.getConnection().getMetaData().getURL());

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ Conexión OK: " + connection.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("❌ Error de conexión: " + e.getMessage());
        }
    }
}