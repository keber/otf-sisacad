package cl.keber.model;

import java.time.LocalDate;
import jakarta.persistence.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "training_program")
public class TrainingProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    private static final Logger log = LoggerFactory.getLogger(TrainingProgram.class);

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getStatus() { return status; }

    private String validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value;
    }

    private LocalDate validateDate(LocalDate value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    @Override
    public String toString() {
        return "TrainingProgram{" +
            "code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", status='" + status + '\'' +
            '}';
    }

    public TrainingProgram() {
        // Required by JPA and Jackson
    }

    public TrainingProgram(String code, String name, LocalDate startDate, LocalDate endDate, String status) {

        this.code = validateText(code, "code");
        this.name = validateText(name, "name");
        this.status = validateText(status, "status");
        this.startDate = validateDate(startDate, "startDate");
        this.endDate = validateDate(endDate, "endDate");

        if (!this.startDate.isBefore(this.endDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        log.debug("TrainingProgram created: {}", this);

    }
}
