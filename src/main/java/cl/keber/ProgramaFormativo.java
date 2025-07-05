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

    private static final Logger log = LoggerFactory.getLogger(ProgramaFormativo.class);

    public ProgramaFormativo(String codigo, String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado) {

        log.debug("Fecha Inicio: {}", fechaInicio);
        log.debug("Fecha Término: {}", fechaFin);

        if (!fechaInicio.isBefore(fechaFin)) {
            throw new IllegalArgumentException("La fecha de término debe ser posterior a la de inicio");
        }

        this.codigo = codigo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;

        log.debug("codigo: {}", this.codigo);
        log.debug("nombre: {}", this.nombre);
        log.debug("fechaInicio: {}", this.fechaInicio);
        log.debug("fechaFin: {}", this.fechaFin);
        log.debug("estado: {}", this.estado);

    }
}