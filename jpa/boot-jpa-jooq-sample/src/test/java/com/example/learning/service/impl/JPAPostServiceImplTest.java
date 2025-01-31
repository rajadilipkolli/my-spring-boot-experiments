package com.example.learning.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.learning.entities.Post;
import com.example.learning.exception.PostNotFoundException;
import com.example.learning.mapper.PostMapper;
import com.example.learning.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JPAPostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private JPAPostServiceImpl jpaPostService;

    @Test
    void deletePostByUserNameAndTitle_WhenPostExists_ShouldDeleteSuccessfully() {
        // Arrange
        String userName = "testUser";
        String title = "Test Title";
        Post post = new Post();
        given(postRepository.existsByTitleAndDetails_CreatedBy(title, userName)).willReturn(true);

        // Act
        jpaPostService.deletePostByUserNameAndTitle(userName, title);

        // Assert
        verify(postRepository).existsByTitleAndDetails_CreatedBy(title, userName);
        verify(postRepository).deleteByTitleAndCreatedBy(title, userName);
    }

    @Test
    void deletePostByUserNameAndTitle_WhenPostDoesNotExist_ShouldThrowException() {
        // Arrange
        String userName = "testUser";
        String title = "Test Title";
        given(postRepository.existsByTitleAndDetails_CreatedBy(title, userName)).willReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> jpaPostService.deletePostByUserNameAndTitle(userName, title))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("Post with title 'Test Title' not found for user 'testUser'");
        verify(postRepository).existsByTitleAndDetails_CreatedBy(title, userName);
        verify(postRepository, never()).deleteByTitleAndCreatedBy(title, userName);
    }
}
