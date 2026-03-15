package com.example.highrps;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Test to verify Spring Modulith structure and boundaries.
 */
class ModulithStructureTest {

    private static final ApplicationModules MODULES = ApplicationModules.of(HighRpsApplication.class);

    @Test
    void verifiesModularStructure() {
        // This will verify:
        // - No cyclic dependencies between modules
        // - Proper module boundaries respected
        // - Only allowed dependencies are present
        MODULES.verify();
    }

    @Test
    @Tag("modulith-docs")
    void documentsModuleStructure() {
        // Generates C4 and PlantUML diagrams
        new Documenter(MODULES).writeDocumentation().writeIndividualModulesAsPlantUml();
    }
}
