package cl.keber;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgramaFormativo{
    private String codigo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    public ProgramaFormativo(String codigo, String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado) {

        if (!fechaInicio.isBefore(fechaFin)) {
            throw new IllegalArgumentException("La fecha de término debe ser posterior a la de inicio");
        }

        this.codigo = codigo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }
}