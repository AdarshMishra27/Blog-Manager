package com.example.BlogManager.services;


import com.example.BlogManager.exceptions.ResourceNotFoundCustomException;
import com.example.BlogManager.objects.Blog;
import com.example.BlogManager.objects.Comment;
import com.example.BlogManager.objects.UserEntity;
import com.example.BlogManager.objects.UserType;
import com.example.BlogManager.repositories.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BlogService blogService;

    @InjectMocks
    private CommentService commentService;


    private Blog testingBlog;
    private UserEntity creatorOfComment;

    // Run this before every test to set up data
    @BeforeEach
    void setup() {
        testingBlog = new Blog("Testing Blog Title", "This is content of the testing blog");
        creatorOfComment = new UserEntity("John", "john", "$%^&*", UserType.USER);
    }

    @Test
    void test_create_comment() {
        //Given
        when(blogService.findById(1L)).thenReturn(testingBlog);
        when(blogService.fetchUserDetailsFromDB("john")).thenReturn(creatorOfComment);

        Comment comment = Comment.builder().id(1L).content("this is testing comment").userEntity(creatorOfComment).blog(testingBlog).build();
        when(commentRepository.save(comment)).thenReturn(comment);

        //When
        Comment checkComment = commentService.save(comment, "john", 1L);

        //then
        assertThat(checkComment.getContent()).isEqualTo("this is testing comment");
        verify(commentRepository).save(argThat(entity ->
                entity.getContent().equals("this is testing comment")));
    }

    @Test
    void test_deleteComment_success_when_user_is_comment_creator() {
        // Given
        Long commentId = 1L;

        // 1. Setup IDs to ensure the .equals() check works
        String username = "john";
        UserEntity blogOwner = new UserEntity("Owner", "owner", "***", UserType.USER);
        // 2. Setup hierarchy: Comment -> Blog -> BlogOwner
        testingBlog.setUserEntity(blogOwner);

        // 3. Create the comment owned by 'john' (creatorOfComment)
        Comment commentToDelete = Comment.builder()
                .id(commentId)
                .content("To be deleted")
                .userEntity(creatorOfComment) // Matches the user trying to delete
                .blog(testingBlog)
                .build();

        // 4. Mocks
        when(blogService.fetchUserDetailsFromDB(username)).thenReturn(creatorOfComment);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentToDelete));

        // When
        Comment result = commentService.deleteComment(commentId, username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(commentId);

        // Verify repository delete was actually called
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void test_deleteBlog_success_when_user_is_comment_owner() {
        // Given
        Long commentId = 1L;
        String username = "blog_owner_user";

        // 1. Setup Users
        UserEntity commenter = new UserEntity("Commenter", "cmtr", "***", UserType.USER);

        UserEntity blogOwner = new UserEntity("Owner", username, "***", UserType.USER);

        // 2. Setup Blog owned by the blogOwner
        testingBlog.setUserEntity(blogOwner);

        // 3. Create comment owned by SOMEONE ELSE
        Comment commentToDelete = Comment.builder()
                .id(commentId)
                .userEntity(commenter) // Not the user deleting
                .blog(testingBlog)     // But the blog belongs to the user deleting
                .build();

        // 4. Mocks
        when(blogService.fetchUserDetailsFromDB(username)).thenReturn(blogOwner);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(commentToDelete));

        // When
        Comment result = commentService.deleteComment(commentId, username);

        // Then
        assertThat(result).isNotNull();
        verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    void test_deleteComment_fail_unauthorized_user() {
        // Given
        Long commentId = 1L;
        String username = "random_hacker";

        // 1. Setup Users with distinct IDs
        UserEntity randomUser = new UserEntity("Hacker", username, "***", UserType.USER);

        UserEntity blogOwner = new UserEntity("Owner", "owner", "***", UserType.USER);


        testingBlog.setUserEntity(blogOwner);

        Comment comment = Comment.builder()
                .id(commentId)
                .userEntity(creatorOfComment)
                .blog(testingBlog)
                .build();

        // 2. Mocks
        when(blogService.fetchUserDetailsFromDB(username)).thenReturn(randomUser);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // When
        Comment result = commentService.deleteComment(commentId, username);

        // Then
        // Should return null based on your logic "else { return null; }"
        assertThat(result).isNull();

        // CRITICAL: Verify delete was NEVER called
        verify(commentRepository, never()).deleteById(anyLong());
    }

    @Test
    void test_deleteComment_fail_comment_not_found() {
        // Given
        Long commentId = 99L;
        String username = "john";

        when(blogService.fetchUserDetailsFromDB(username)).thenReturn(creatorOfComment);
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.deleteComment(commentId, username))
                .isInstanceOf(ResourceNotFoundCustomException.class)
                .hasMessageContaining("no comment with id: " + commentId);

        verify(commentRepository, never()).deleteById(anyLong());
    }
}
