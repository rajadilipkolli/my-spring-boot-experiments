package com.example.graphql.querydsl.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.graphql.querydsl.entities.Tag;
import com.example.graphql.querydsl.mapper.TagMapper;
import com.example.graphql.querydsl.model.response.TagResponse;
import com.example.graphql.querydsl.repositories.TagRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    @Test
    void findTagById() {
        // given
        given(tagRepository.findById(1L)).willReturn(Optional.of(getTag()));
        given(tagMapper.toResponse(any(Tag.class))).willReturn(getTagResponse());
        // when
        Optional<TagResponse> optionalTag = tagService.findTagById(1L);
        // then
        assertThat(optionalTag).isPresent();
        TagResponse tag = optionalTag.get();
        assertThat(tag.id()).isOne();
        assertThat(tag.name()).isEqualTo("junitTest");
    }

    @Test
    void deleteTagById() {
        // given
        willDoNothing().given(tagRepository).deleteById(1L);
        // when
        tagService.deleteTagById(1L);
        // then
        verify(tagRepository, times(1)).deleteById(1L);
    }

    private Tag getTag() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("junitTest");
        return tag;
    }

    private TagResponse getTagResponse() {
        return new TagResponse(1L, "junitTest");
    }
}
