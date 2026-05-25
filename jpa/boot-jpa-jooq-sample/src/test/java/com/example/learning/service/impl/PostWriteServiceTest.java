package com.example.learning.service.impl;

import static org.mockito.Mockito.verify;

import com.example.learning.mapper.PostMapper;
import com.example.learning.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostWriteServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostWriteServiceImpl postWriteService;

    @Test
    void deletePostByUserNameAndTitleWhenPostExistsShouldDeleteSuccessfully() {
        // Arrange
        String userName = "testUser";
        String title = "Test Title";
        // Act
        postWriteService.deletePostByUserNameAndTitle(userName, title);

        // Assert
        verify(postRepository).deleteByTitleAndCreatedBy(title, userName);
    }
}
