package cl.keber.architecture;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

/**
 * Executable architecture rules for the Clean Architecture slice.
 *
 * <p>These rules are the machine-checked form of {@code docs/architecture}. They
 * are deliberately unweakened: there is no {@code freeze}, no allowance list and
 * no ignored violation. A failure here is a real architecture violation, not a
 * rule that needs relaxing.
 *
 * <p>Only production classes are analysed ({@code ImportOption.DoNotIncludeTests}).
 * Test classes deliberately reach across layers - a {@code @WebMvcTest} mocks use
 * cases, a {@code @DataJpaTest} drives the JPA adapter, and the characterization
 * suite boots the whole application - so including them would assert something
 * other than the production dependency graph.
 *
 * <p>None of these rules can pass vacuously. ArchUnit's default
 * {@code archRule.failOnEmptyShould} makes a rule that matched no class a
 * failure, and the layered rule reports an empty layer as a violation. That is
 * load-bearing here: ArchUnit must be new enough to read the project's Java 25
 * bytecode (class file major version 69). On an older ArchUnit every class is
 * skipped on import and all nine rules fail rather than silently succeeding.
 *
 * <p>{@code cl.keber.OtfSisacadApplication} carries {@code @SpringBootApplication}
 * and sits at the root, outside all three layers. It depends on no layer, so the
 * layered rule below is satisfied without an exception for it.
 */
@AnalyzeClasses(packages = "cl.keber", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String DOMAIN = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String INFRASTRUCTURE = "..infrastructure..";

    /** The domain is framework-free: no Spring anywhere below {@code cl.keber.domain}. */
    @ArchTest
    static final ArchRule domainMustNotDependOnSpring =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(DOMAIN)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..")
                    .because("the domain model must not know that Spring exists");

    /** The domain is persistence-free: no JPA and no Hibernate types leak into it. */
    @ArchTest
    static final ArchRule domainMustNotDependOnJpaOrHibernate =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(DOMAIN)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("jakarta.persistence..", "jakarta.persistence.*", "org.hibernate..")
                    .because("TrainingProgram is no longer a persistence entity; JPA lives in infrastructure.persistence");

    /** The domain is serialization-free: no Jackson annotations or types. */
    @ArchTest
    static final ArchRule domainMustNotDependOnJackson =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(DOMAIN)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("com.fasterxml.jackson..")
                    .because("the wire format is owned by infrastructure.web.dto, not by the domain");

    /** The dependency rule: application depends inwards on the domain, never outwards. */
    @ArchTest
    static final ArchRule applicationMustNotDependOnInfrastructure =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE)
                    .because("use cases depend on the domain port, never on the adapter that implements it");

    /** The application layer is framework-free: plain Java use cases and commands. */
    @ArchTest
    static final ArchRule applicationMustNotDependOnSpring =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..")
                    .because("bean wiring belongs to infrastructure.config.TrainingProgramConfiguration");

    /** The application layer never sees JPA or Spring Data. */
    @ArchTest
    static final ArchRule applicationMustNotDependOnJpaOrSpringData =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.persistence..", "jakarta.persistence.*", "org.springframework.data..")
                    .because("use cases receive the domain port by constructor and know no persistence technology");

    /** Controllers talk to use cases only: never to persistence, never to the repository port. */
    @ArchTest
    static final ArchRule controllersMustNotDependOnPersistenceOrTheRepositoryPort =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage("..web.controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..persistence..", "..domain.repository..")
                    .because("controllers never see repositories; they call use cases");

    /** Nothing inward knows about the web: no domain or application class depends on {@code ..web..}. */
    @ArchTest
    static final ArchRule domainAndApplicationMustNotDependOnWeb =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAnyPackage(DOMAIN, APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..web..")
                    .because("DTOs, controllers and the exception advice are delivery details");

    /**
     * The layering as a whole: domain is the innermost layer, application sits on
     * it, infrastructure sits on both, and nothing depends on infrastructure.
     */
    @ArchTest
    static final ArchRule layersAreRespected =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain")
                    .definedBy(DOMAIN)
                    .layer("Application")
                    .definedBy(APPLICATION)
                    .layer("Infrastructure")
                    .definedBy(INFRASTRUCTURE)
                    .whereLayer("Infrastructure")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Application")
                    .mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Domain")
                    .mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                    .because("dependencies point inwards only");
}
