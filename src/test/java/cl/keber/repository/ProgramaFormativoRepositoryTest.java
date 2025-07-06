package cl.keber.repository;

import cl.keber.model.ProgramaFormativo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProgramaFormativoRepositoryTest {

    @Autowired
    private ProgramaFormativoRepository repository;

    @Test
    @DisplayName("Debe guardar y recuperar un ProgramaFormativo por ID")
    void debeGuardarYRecuperarProgramaFormativo() {
        // Arrange
        ProgramaFormativo programa = new ProgramaFormativo(
            "PF001", "Seguridad y Salud Ocupacional", 
            LocalDate.of(2025, 7, 1), 
            LocalDate.of(2025, 7, 15), 
            "VIGENTE"
        );

        // Act
        ProgramaFormativo guardado = repository.save(programa);
        Optional<ProgramaFormativo> recuperado = repository.findById(guardado.getId());

        // Assert
        assertTrue(recuperado.isPresent());
        assertEquals("PF001", recuperado.get().getCodigo());
        assertEquals("Seguridad y Salud Ocupacional", recuperado.get().getNombre());
    }
}
