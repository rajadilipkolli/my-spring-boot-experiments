package com.example.archunit.architecture;

import static com.example.archunit.architecture.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.example.archunit.architecture.ArchitectureConstants.MODEL_PACKAGE;
import static com.example.archunit.architecture.CommonRules.fieldsShouldHaveGetterRule;
import static com.example.archunit.architecture.CommonRules.methodsShouldBePublicRule;
import static com.example.archunit.architecture.CommonRules.publicAndFinalFieldsAreNotAllowedRule;
import static com.example.archunit.architecture.CommonRules.springAnnotationsClassesAreNotAllowedRule;
import static com.example.archunit.architecture.CommonRules.staticMethodsAreNotAllowedRule;
import static com.example.archunit.architecture.CustomConditions.HAVE_EQUALS_AND_HASH_CODE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.thirdparty.com.google.common.collect.Maps;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class ModelRulesTest {
	
	//Classes
    @ArchTest
    static final ArchRule classesShouldOverrideEqualsAndHashCode = classes().that()
            .resideInAnyPackage(MODEL_PACKAGE)
            .and().areNotMemberClasses()
            .should(HAVE_EQUALS_AND_HASH_CODE)
            .because("Model classes should override equals and hashCode methods");

    @ArchTest
    static final ArchRule springAnnotationsAreNotAllowed = springAnnotationsClassesAreNotAllowedRule(MODEL_PACKAGE);
	
	//Fields
    @ArchTest
    static final ArchRule fieldsShouldHaveGetter = fieldsShouldHaveGetterRule(
            Maps.newHashMap(), MODEL_PACKAGE);
	
    @ArchTest
    static final ArchRule publicAndFinalFieldsAreNotAllowed = publicAndFinalFieldsAreNotAllowedRule(MODEL_PACKAGE);
	
	//Methods
    @ArchTest
    static final ArchRule methodsShouldBePublic = methodsShouldBePublicRule(MODEL_PACKAGE);

    @ArchTest
    static final ArchRule staticMethodsAreNotAllowed = staticMethodsAreNotAllowedRule(MODEL_PACKAGE);

}
