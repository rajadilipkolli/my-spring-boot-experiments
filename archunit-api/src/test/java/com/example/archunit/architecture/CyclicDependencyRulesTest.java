package com.example.archunit.architecture;

import static com.example.archunit.architecture.ArchitectureConstants.DEFAULT_PACKAGE;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class CyclicDependencyRulesTest {

    @ArchTest
    public static final ArchRule noCyclesDependencies = slices().matching("..(*)..")
            .should().beFreeOfCycles();
}