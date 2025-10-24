package com.example.archunit.architecture;

import static com.example.archunit.architecture.CustomConditions.haveGetter;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

public class CommonRules {

    // Classes
    static ArchRule interfacesAreOnlyAllowedRule(String packageName, String... excludedPackages) {
        return classes()
                .that()
                .resideInAPackage(packageName)
                .and()
                .resideOutsideOfPackages(excludedPackages)
                .should()
                .beInterfaces()
                .because("Resources should be interfaces in %s".formatted(packageName));
    }

    static ArchRule componentAnnotationIsNotAllowedRule(String packageName) {
        return classes()
                .that()
                .resideInAPackage(packageName)
                .should()
                .notBeAnnotatedWith(Component.class)
                .because("Component annotation is not allowed in %s".formatted(packageName));
    }

    static ArchRule springAnnotationsClassesAreNotAllowedRule(String... packageNames) {
        return classes()
                .that()
                .resideInAnyPackage(packageNames)
                .should()
                .notBeAnnotatedWith(Service.class)
                .andShould()
                .notBeAnnotatedWith(Component.class)
                .andShould()
                .notBeAnnotatedWith(Configuration.class)
                .andShould()
                .notBeAnnotatedWith(ConfigurationProperties.class)
                .andShould()
                .notBeAnnotatedWith(Bean.class)
                .andShould()
                .notBeAnnotatedWith(Controller.class)
                .andShould()
                .notBeAnnotatedWith(RestController.class)
                .because("Classes in %s should not be annotated with Spring annotations"
                        .formatted(Arrays.toString(packageNames)));
    }

    // Fields
    static ArchRule fieldsShouldHaveGetterRule(Map<String, String> exclusions, String... packageNames) {
        return fields().that()
                .areDeclaredInClassesThat()
                .resideInAnyPackage(packageNames)
                .and()
                .areDeclaredInClassesThat()
                .areNotMemberClasses()
                .and()
                .arePrivate()
                .and()
                .areNotFinal()
                .and()
                .areNotStatic()
                .should(haveGetter(exclusions))
                .because("Private fields should have getters in %s" + Arrays.toString(packageNames));
    }

    static ArchRule fieldsShouldNotBePublic(String packageName) {
        return fields().that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                .should()
                .notBePublic()
                .because("Public fields are not allowed in %s".formatted(packageName));
    }

    static ArchRule publicAndFinalFieldsAreNotAllowedRule(String... packageNames) {
        return fields().that()
                .areDeclaredInClassesThat()
                .resideInAnyPackage(packageNames)
                .and()
                .doNotHaveName("serialVersionUID")
                .should()
                .notBeFinal()
                .andShould()
                .notBePublic()
                .because("Fields with public and final modifiers are not allowed in %s"
                        .formatted(Arrays.toString(packageNames)));
    }

    static ArchRule fieldsShouldHaveGetterRule(String... packageNames) {
        return fieldsShouldHaveGetterRule(new HashMap<>(), packageNames);
    }

    static ArchRule finalFieldsRule(String packageName, String... excludedPackages) {
        return fields().that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                .and()
                .areDeclaredInClassesThat()
                .resideOutsideOfPackages(excludedPackages)
                .and()
                .arePrivate()
                .and()
                .doNotHaveModifier(JavaModifier.SYNTHETIC)
                .should()
                .beFinal()
                .because("Private attributes should be instanced by constructor classes, or it should be static in %s"
                        .formatted(packageName));
    }

    // Constructors
    static ArchRule publicConstructorsRule(String packageName) {
        return constructors()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                .and()
                .areDeclaredInClassesThat()
                .areNotAnonymousClasses()
                .should()
                .bePublic()
                .because("Public constructors are only allowed in %s".formatted(packageName));
    }

    static ArchRule packagePrivateConstructorsRule(String packageName) {
        return constructors()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                .and()
                .areDeclaredInClassesThat()
                .areNotAnonymousClasses()
                .should()
                .bePackagePrivate()
                .because("Package Private constructors are only allowed in %s".formatted(packageName));
    }

    // Methods
    static ArchRule beanMethodsAreNotAllowedRule(String packageName) {
        return methods()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                .should()
                .notBeAnnotatedWith(Bean.class)
                .because("Bean methods are not allowed in %s".formatted(packageName));
    }

    static ArchRule privateMethodsAreNotAllowedRule(String packageName) {
        return methods()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                // exclude compiler-generated synthetic methods (e.g., lambda$...) from this rule
                .and()
                .doNotHaveModifier(JavaModifier.SYNTHETIC)
                .should()
                .notBePrivate()
                .because("Private methods are not allowed in %s".formatted(packageName));
    }

    static ArchRule publicMethodsAreNotAllowedRule(String packageName) {
        return methods()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                .and()
                .doNotHaveModifier(JavaModifier.SYNTHETIC)
                .should()
                .notBePublic()
                .because("Public methods are not allowed in %s".formatted(packageName));
    }

    static ArchRule staticMethodsAreNotAllowedRule(String packageName) {
        return methods()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                .and()
                .doNotHaveModifier(JavaModifier.SYNTHETIC)
                .should()
                .notBeStatic()
                .because("Static methods are not allowed in %s".formatted(packageName));
    }

    static ArchRule methodsShouldBePublicRule(String... packageNames) {
        return methods()
                .that()
                .areDeclaredInClassesThat()
                .resideInAnyPackage(packageNames)
                .should()
                .bePublic()
                .because("Public methods are only allowed in " + Arrays.toString(packageNames));
    }

    static ArchRule staticMethodsAreOnlyAllowedRule(String packageName) {
        return methods()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage(packageName)
                .should()
                .beStatic()
                .because("Static methods are only allowed in %s".formatted(packageName));
    }
}
