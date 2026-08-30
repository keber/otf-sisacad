package cl.keber.infrastructure.web.dto;

import java.time.LocalDate;

public class TrainingProgramDto {
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public TrainingProgramDto() {
        // Empty constructor required by Jackson and frameworks
    }

    public TrainingProgramDto(String code, String name, LocalDate startDate, LocalDate endDate, String status) {
        this.code = code;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
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
