package com.example.learning.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.learning.exception.PostAlreadyExistsException;
import com.example.learning.exception.PostNotFoundException;
import com.example.learning.model.request.PostRequest;
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

    @Test
    void createPostWhenPostAlreadyExistsShouldThrowException() {
        // Arrange
        String userName = "testUser";
        PostRequest postRequest = new PostRequest("Existing Title", "Content", false, null, null, null);
        given(postReadService.existsByTitleAndDetailsCreatedBy(postRequest.title(), userName))
                .willReturn(true);
        // Act & Assert
        assertThatThrownBy(() -> postServiceFacade.createPost(postRequest, userName))
                .isInstanceOf(PostAlreadyExistsException.class)
                .hasMessageContaining("Existing Title");
        verify(postWriteService, never()).createPost(any(PostRequest.class), anyString());
    }

    @Test
    void createPostWhenPostDoesNotExistShouldSucceed() {
        // Arrange
        String userName = "testUser";
        PostRequest postRequest = new PostRequest("New Title", "Content", false, null, null, null);
        given(postReadService.existsByTitleAndDetailsCreatedBy(postRequest.title(), userName))
                .willReturn(false);
        // Act
        postServiceFacade.createPost(postRequest, userName);
        // Assert
        verify(postReadService).existsByTitleAndDetailsCreatedBy(postRequest.title(), userName);
        verify(postWriteService).createPost(postRequest, userName);
    }

    @Test
    void deletePostByUserNameAndTitleWhenPostExistsShouldSucceed() {
        // Arrange
        String userName = "testUser";
        String title = "Test Title";
        given(postReadService.existsByTitleAndDetailsCreatedBy(title, userName)).willReturn(true);
        // Act
        postServiceFacade.deletePostByUserNameAndTitle(userName, title);
        // Assert
        verify(postReadService).existsByTitleAndDetailsCreatedBy(title, userName);
        verify(postWriteService).deletePostByUserNameAndTitle(userName, title);
    }
}
