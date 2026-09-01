package cl.keber.infrastructure.web.dto;

import java.time.LocalDate;

/**
 * The wire format of the {@code /programs} endpoints.
 *
 * <p>The field declaration order below is the response field order Jackson produces:
 * {@code id, code, name, startDate, endDate, status}. It matches what the JPA entity used
 * to serialise to and is pinned by the characterization tests - do not reorder it.
 */
public class TrainingProgramDto {
    private Long id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public TrainingProgramDto() {
        // Empty constructor required by Jackson and frameworks
    }

    public TrainingProgramDto(
            Long id, String code, String name, LocalDate startDate, LocalDate endDate, String status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
