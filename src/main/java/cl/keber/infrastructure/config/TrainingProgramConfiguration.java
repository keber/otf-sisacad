package cl.keber.infrastructure.config;

import cl.keber.application.service.TrainingProgramApplicationService;
import cl.keber.domain.repository.TrainingProgramRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free application layer into Spring (decision D10).
 *
 * <p>All Spring knowledge about the use cases lives here. {@code application/**} and
 * {@code domain/**} carry no framework import at all, which is what makes them testable
 * with plain JUnit and replaceable without touching the web layer.
 */
@Configuration
public class TrainingProgramConfiguration {

    /**
     * One instance satisfies all five use case interfaces.
     *
     * <p>{@link TrainingProgramApplicationService} implements every one of them, and the
     * declared return type is the concrete class, so Spring registers a single bean whose
     * type hierarchy includes {@code CreateTrainingProgramUseCase},
     * {@code GetTrainingProgramUseCase}, {@code ListTrainingProgramsUseCase},
     * {@code UpdateTrainingProgramUseCase} and {@code DeleteTrainingProgramUseCase}. A
     * constructor asking for any of those interfaces resolves to this bean by type.
     *
     * @param repository the domain port, supplied by the JPA adapter in
     *     {@code infrastructure.persistence}
     */
    @Bean
    public TrainingProgramApplicationService trainingProgramApplicationService(
            TrainingProgramRepository repository) {
        return new TrainingProgramApplicationService(repository);
    }
}
