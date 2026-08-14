package com.example.highrps.post.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TagEntityTest {

    @Test
    void shouldCreateTagWithNaturalIdAtConstruction() {
        TagEntity tag = new TagEntity("java");

        assertThat(tag.getTagName()).isEqualTo("java");
    }

    @Test
    void shouldRejectNullOrBlankTagName() {
        assertThatThrownBy(() -> new TagEntity(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tagName");
    }
}
