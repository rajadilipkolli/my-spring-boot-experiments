package com.example.highrps;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
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

        ApplicationModule postcomment = MODULES.getModuleByName("postcomment")
                .orElseThrow(() -> new AssertionError("Module 'postcomment' not found"));
        ApplicationModule shared =
                MODULES.getModuleByName("shared").orElseThrow(() -> new AssertionError("Module 'shared' not found"));

        // Confirm postcomment has no inbound dependents (no module depends on postcomment)
        long postcommentInboundDependents = MODULES.stream()
                .filter(m -> !m.equals(postcomment))
                .filter(m -> m.getDirectDependencies(MODULES).contains(postcomment))
                .count();
        assertThat(postcommentInboundDependents)
                .as("postcomment should have no inbound dependents")
                .isZero();

        // Confirm shared has no outgoing module dependencies
        assertThat(shared.getDirectDependencies(MODULES).isEmpty())
                .as("shared module should have no outgoing module dependencies")
                .isTrue();
    }

    @Test
    @Tag("modulith-docs")
    void documentsModuleStructure() {
        // Generates C4 and PlantUML diagrams
        new Documenter(MODULES).writeDocumentation().writeIndividualModulesAsPlantUml();
    }
}
