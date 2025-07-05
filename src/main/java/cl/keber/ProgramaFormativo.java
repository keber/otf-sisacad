package cl.keber;

import java.time.LocalDate;

public class ProgramaFormativo{
    private String codigo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    public ProgramaFormativo(String codigo, String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }
}