package com.example.highrps;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Test to verify Spring Modulith structure and boundaries.
 */
class ModulithStructureTest {

    ApplicationModules modules = ApplicationModules.of(HighRpsApplication.class);

    @Test
    void verifiesModularStructure() {
        // This will verify:
        // - No cyclic dependencies between modules
        // - Proper module boundaries respected
        // - Only allowed dependencies are present
        modules.verify();
    }

    @Test
    void documentsModuleStructure() {
        // Generates C4 and PlantUML diagrams
        new Documenter(modules).writeDocumentation().writeIndividualModulesAsPlantUml();
    }
}
