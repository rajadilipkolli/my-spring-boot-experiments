package com.example.archunit.architecture;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public class CustomConditions {

    private static final String HAVE_GETTER_DESCRIPTION = "have getter";
    private static final String HAVE_GETTER_AND_SETTER_DESCRIPTION = "have getter and setter";
    private static final String GETTER_OR_SETTER_NOT_PRESENT_ERROR_MESSAGE = "Field %s of %s does not have %s";
    private static final String GETTER_PREFIX = "get";
    private static final String IS_PREFIX = "is";
    private static final String SETTER_PREFIX = "set";
    private static final String BOOLEAN_TYPE = "boolean";

    private static final String EQUALS_AND_HASH_CODE_DESCRIPTION = "have equals and hashCode";
    private static final String EQUALS_OR_HASH_CODE_NOT_PRESENT_ERROR_MESSAGE = "%s not found in %s";
    private static final String EQUALS_METHOD = "equals";
    private static final String HASH_CODE_METHOD = "hashCode";

    public static final ArchCondition<JavaClass> HAVE_EQUALS_AND_HASH_CODE = buildClassHaveEqualsAndHashCodeCondition();
    public static final ArchCondition<JavaClass> HAVE_NOT_STATIC_METHODS = buildStaticMethodsAreNotAllowedCondition();

    private CustomConditions() {

    }

    public static ArchCondition<JavaField> haveGetter(Map<String, String> exclusions) {
        return buildFieldHaveGetterAndSetterCondition(false, exclusions);
    }

    public static ArchCondition<JavaField> haveGetterAndSetter(Map<String, String> exclusions) {
        return buildFieldHaveGetterAndSetterCondition(true, exclusions);
    }

    private static ArchCondition<JavaField> buildFieldHaveGetterAndSetterCondition(boolean forceSetters,
            Map<String, String> exclusions) {
        return new ArchCondition<JavaField>(
                forceSetters ? HAVE_GETTER_AND_SETTER_DESCRIPTION : HAVE_GETTER_DESCRIPTION) {

            @Override
            public void check(JavaField field, ConditionEvents events) {
                Set<String> publicMethods = field.getOwner().getMethods().stream()
                        .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC)).map(JavaMember::getName)
                        .collect(Collectors.toSet());

                String name = field.getName();

                if (exclusions.containsKey(name)) {
                    String className = exclusions.get(name);
                    if (field.getOwner().getName().equals(className)) {
                        return;
                    }
                }

                String getter = calculateGetterPrefix(field.reflect().getType().getName()) + capitalize(name);

                if (!publicMethods.contains(getter)) {
                    String message = String
                            .format(GETTER_OR_SETTER_NOT_PRESENT_ERROR_MESSAGE, field.getName(),
                                    field.getOwner().getName(),
                                    GETTER_PREFIX);
                    events.add(SimpleConditionEvent.violated(field, message));
                }

                if (forceSetters) {
                    String setter = SETTER_PREFIX + capitalize(name);

                    if (!publicMethods.contains(setter)) {
                        String message = String
                                .format(GETTER_OR_SETTER_NOT_PRESENT_ERROR_MESSAGE, field.getName(),
                                        field.getOwner().getName(),
                                        SETTER_PREFIX);
                        events.add(SimpleConditionEvent.violated(field, message));
                    }
                }

            }
        };
    }

    private static ArchCondition<JavaClass> buildClassHaveEqualsAndHashCodeCondition() {
        return new ArchCondition<JavaClass>(EQUALS_AND_HASH_CODE_DESCRIPTION) {

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                Optional<JavaMethod> equalsMethod = findPublicMethodFromClass(javaClass, EQUALS_METHOD);
                Optional<JavaMethod> hashCodeMethod = findPublicMethodFromClass(javaClass, HASH_CODE_METHOD);

                if (!equalsMethod.isPresent()) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            String.format(EQUALS_OR_HASH_CODE_NOT_PRESENT_ERROR_MESSAGE, EQUALS_METHOD,
                                    javaClass.getName())));
                }

                if (!hashCodeMethod.isPresent()) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                            String.format(EQUALS_OR_HASH_CODE_NOT_PRESENT_ERROR_MESSAGE, HASH_CODE_METHOD,
                                    javaClass.getName())));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> buildStaticMethodsAreNotAllowedCondition() {
        return new ArchCondition<JavaClass>(EQUALS_AND_HASH_CODE_DESCRIPTION) {

            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethods().stream()
                        .filter(m -> m.getModifiers().contains(JavaModifier.STATIC))
                        .forEach(m -> SimpleConditionEvent.violated(javaClass, String
                                .format("Static method %s in %s is not allowed", m.getName(), javaClass.getName())));
            }
        };
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private static String calculateGetterPrefix(String type) {
        return !type.equals(BOOLEAN_TYPE) ? GETTER_PREFIX : IS_PREFIX;
    }

    private static Optional<JavaMethod> findPublicMethodFromClass(JavaClass javaClass, String methodName) {
        return javaClass.getMethods().stream()
                .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC) && methodName.equals(m.getName()))
                .findFirst();
    }
}
