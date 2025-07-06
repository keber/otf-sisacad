package cl.keber.model;

import java.time.LocalDate;
import jakarta.persistence.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "programa_formativo")
public class ProgramaFormativo{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;

    private static final Logger log = LoggerFactory.getLogger(ProgramaFormativo.class);

    private String validarTexto(String campo, String nombreCampo) {
        if (campo == null || campo.isBlank()) {
            throw new IllegalArgumentException("El " + nombreCampo + " no puede ser nulo ni vacío");
        }
        return campo;
    }
    private LocalDate validarFecha(LocalDate fecha, String nombreCampo) {
        if (fecha == null) {
            throw new IllegalArgumentException("La " + nombreCampo + " no puede ser nula");
        }
        return fecha;
    }
    @Override
    public String toString() {
        return "ProgramaFormativo{" +
            "codigo='" + codigo + '\'' +
            ", nombre='" + nombre + '\'' +
            ", fechaInicio=" + fechaInicio +
            ", fechaFin=" + fechaFin +
            ", estado='" + estado + '\'' +
            '}';
    }

    public ProgramaFormativo(String codigo, String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado) {

        this.codigo = validarTexto(codigo, "código");
        this.nombre = validarTexto(nombre, "nombre");
        this.estado = validarTexto(estado, "estado");
        this.fechaInicio = validarFecha(fechaInicio, "fecha de inicio");
        this.fechaFin = validarFecha(fechaFin, "fecha de término");
        
        if (!this.fechaInicio.isBefore(this.fechaFin)) {
            throw new IllegalArgumentException("La fecha de término debe ser posterior a la de inicio");
        }

        log.debug("ProgramaFormativo creado: {}", this);
  
    }
}