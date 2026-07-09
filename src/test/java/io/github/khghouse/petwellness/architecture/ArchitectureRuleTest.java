package io.github.khghouse.petwellness.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AnalyzeClasses(
        packages = "io.github.khghouse.petwellness",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRuleTest {

    private static final Pattern DOMAIN_PACKAGE_PATTERN =
            Pattern.compile("(^|\\.)domain\\.([^.]+)(\\.|$)");

    @ArchTest
    static final ArchRule controller_should_not_depend_on_repository =
            noClasses()
                    .that()
                    .resideInAPackage("..controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule layered_architecture_should_be_respected =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Controller")
                    .definedBy("..controller..")
                    .layer("Service")
                    .definedBy("..service..")
                    .layer("Repository")
                    .definedBy("..repository..")
                    .whereLayer("Controller")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Service")
                    .mayOnlyBeAccessedByLayers("Controller", "Service")
                    .whereLayer("Repository")
                    .mayOnlyBeAccessedByLayers("Service");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_other_domain_repository =
            classes()
                    .that()
                    .resideInAPackage("..domain..")
                    .should(notDependOnOtherDomainRepository());

    private static ArchCondition<JavaClass> notDependOnOtherDomainRepository() {
        return new ArchCondition<>("not depend on another domain repository") {

            @Override
            public void check(JavaClass originClass, ConditionEvents events) {
                Optional<String> originDomain = extractDomainName(originClass.getPackageName());

                if (originDomain.isEmpty()) {
                    return;
                }

                for (Dependency dependency : originClass.getDirectDependenciesFromSelf()) {
                    JavaClass targetClass = dependency.getTargetClass();
                    Optional<String> targetDomain = extractDomainName(targetClass.getPackageName());

                    if (targetDomain.isEmpty()
                            || originDomain.get().equals(targetDomain.get())
                            || !isRepositoryPackage(targetClass, targetDomain.get())) {
                        continue;
                    }

                    String message =
                            "%s depends on another domain repository: %s"
                                    .formatted(originClass.getName(), dependency.getDescription());
                    events.add(SimpleConditionEvent.violated(originClass, message));
                }
            }
        };
    }

    private static Optional<String> extractDomainName(String packageName) {
        Matcher matcher = DOMAIN_PACKAGE_PATTERN.matcher(packageName);

        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(matcher.group(2));
    }

    private static boolean isRepositoryPackage(JavaClass javaClass, String domainName) {
        return javaClass.getPackageName().contains(".domain.%s.repository".formatted(domainName));
    }
}
