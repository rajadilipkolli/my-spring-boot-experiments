package com.example.archunit.architecture;

import static com.example.archunit.architecture.ArchitectureConstants.*;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = DEFAULT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

    private static final String CONTROLLER = "Controller";
    private static final String MODEL = "Model";
    private static final String REPOSITORY = "Repository";;
    private static final String SERVICE = "Service";


    @ArchTest
    static final ArchRule layeredArchitectureRule = layeredArchitecture()
            .layer(CONTROLLER).definedBy(CONTROLLER_PACKAGE)
            .layer(MODEL).definedBy(MODEL_PACKAGE)
            .layer(REPOSITORY).definedBy(REPOSITORY_PACKAGE)
            .layer(SERVICE).definedBy(SERVICE_PACKAGE)

            .whereLayer(CONTROLLER).mayNotBeAccessedByAnyLayer()
            .whereLayer(MODEL).mayOnlyBeAccessedByLayers(REPOSITORY, SERVICE)
            .whereLayer(REPOSITORY).mayOnlyBeAccessedByLayers(SERVICE)
            .whereLayer(SERVICE).mayOnlyBeAccessedByLayers(CONTROLLER, SERVICE);
}