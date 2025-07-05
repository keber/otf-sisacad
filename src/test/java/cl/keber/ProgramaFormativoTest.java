package cl.keber;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProgramaFormativoTest {

    @Test
    @DisplayName("Validación de fechas en ProgramaFormativo")
    void debeFallarSiFechaTerminoEsAntesDeInicio() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2024, 12, 31);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new ProgramaFormativo("PF001", "Programa de Prueba", inicio, fin, "Descripción");
        });

        assertEquals("La fecha de término debe ser posterior a la de inicio", exception.getMessage());
    }

    @ParameterizedTest(name = "Debe fallar si {0} es nulo")
    @MethodSource("proveedorDeCamposNulos")
    void debeFallarSiCampoEsNulo(String campo, ProgramaFormativoBuilder builderEsperado, String mensajeEsperado) {
        Executable accion = builderEsperado::build;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, accion);
        assertEquals(mensajeEsperado, ex.getMessage());
    }

    static Stream<Arguments> proveedorDeCamposNulos() {
        return Stream.of(
            Arguments.of("codigo", new ProgramaFormativoBuilder().conCodigo(null), "El código no puede ser nulo ni vacío"),
            Arguments.of("nombre", new ProgramaFormativoBuilder().conNombre(null), "El nombre no puede ser nulo ni vacío"),
            Arguments.of("fechaInicio", new ProgramaFormativoBuilder().conFechaInicio(null), "La fecha de inicio no puede ser nula"),
            Arguments.of("fechaFin", new ProgramaFormativoBuilder().conFechaFin(null), "La fecha de término no puede ser nula"),
            Arguments.of("estado", new ProgramaFormativoBuilder().conEstado(null), "El estado no puede ser nulo")
        );
    }

    
}

class ProgramaFormativoBuilder {
    private String codigo = "PF001";
    private String nombre = "Nombre válido";
    private LocalDate fechaInicio = LocalDate.of(2025, 1, 1);
    private LocalDate fechaFin = LocalDate.of(2025, 12, 31);
    private String estado = "activo";

    ProgramaFormativoBuilder conCodigo(String codigo) {
        this.codigo = codigo;
        return this;
    }

    ProgramaFormativoBuilder conNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    ProgramaFormativoBuilder conFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
        return this;
    }

    ProgramaFormativoBuilder conFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
        return this;
    }

    ProgramaFormativoBuilder conEstado(String estado) {
        this.estado = estado;
        return this;
    }

    ProgramaFormativo build() {
        return new ProgramaFormativo(codigo, nombre, fechaInicio, fechaFin, estado);
    }
}
