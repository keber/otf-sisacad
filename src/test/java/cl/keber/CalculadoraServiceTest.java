package cl.keber;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraServiceTest {

    @Test
    void testSumaConMock() {
        Calculadora mockCalc = mock(Calculadora.class);
        when(mockCalc.sumar(2, 3)).thenReturn(5);

        int resultado = mockCalc.sumar(2, 3);
        assertEquals(5, resultado);

        verify(mockCalc).sumar(2, 3);
    }
}

