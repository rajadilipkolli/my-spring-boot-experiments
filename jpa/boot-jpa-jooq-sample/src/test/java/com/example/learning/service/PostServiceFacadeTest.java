package com.example.learning.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.learning.exception.PostNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PostServiceFacadeTest {

    @Mock
    private PostReadService postReadService;

    @Mock
    private PostWriteService postWriteService;

    @InjectMocks
    private PostServiceFacade postServiceFacade;

    @Test
    void deletePostByUserNameAndTitleWhenPostDoesNotExistShouldThrowException() {
        // Arrange
        String userName = "testUser";
        String title = "Test Title";
        given(postReadService.existsByTitleAndDetailsCreatedBy(title, userName)).willReturn(false);
        // Act & Assert
        assertThatThrownBy(() -> postServiceFacade.deletePostByUserNameAndTitle(userName, title))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("Post with title 'Test Title' not found for user 'testUser'");
        verify(postWriteService, never()).deletePostByUserNameAndTitle(userName, title);
    }
}
