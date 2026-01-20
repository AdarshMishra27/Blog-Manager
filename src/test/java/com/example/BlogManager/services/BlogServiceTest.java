package com.example.BlogManager.services;


import com.example.BlogManager.exceptions.ResourceNotFoundCustomException;
import com.example.BlogManager.objects.Blog;
import com.example.BlogManager.objects.UserEntity;
import com.example.BlogManager.objects.UserType;
import com.example.BlogManager.repositories.BlogRepository;
import com.example.BlogManager.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlogServiceTest {
    @Mock
    private BlogRepository blogRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BlogService blogService;


    private UserEntity adminUser;
    private UserEntity regularUser;
    private Blog testBlog;

    @BeforeEach
    void setUp() {
        adminUser = new UserEntity();
        adminUser.setUserId("admin123");
        adminUser.setUserType(UserType.ADMIN);
        adminUser.setId(3L);

        regularUser = new UserEntity();
        regularUser.setUserId("user123");
        regularUser.setUserType(UserType.USER);
        regularUser.setId(2L);

        testBlog = new Blog("Test Title", "Test Content");
        testBlog.setId(1L);
        testBlog.setUserEntity(regularUser);
    }

    @Test
    void save_ShouldSaveBlogWithUser() {
        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(regularUser));
        when(blogRepository.save(any(Blog.class))).thenReturn(testBlog);

        Blog result = blogService.save(testBlog, "user123");

        assertThat(result).isNotNull();
        assertThat(result.getUserEntity()).isEqualTo(regularUser);
        verify(blogRepository).save(testBlog);
        verify(userRepository).findByUserId("user123");
    }

    @Test
    void findById_BlogExists_ShouldReturnBlog() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(testBlog));

        Blog result = blogService.findById(1L);

        assertThat(result).isEqualTo(testBlog);
        verify(blogRepository).findById(1L);
    }

    @Test
    void findById_BlogNotFound_ShouldThrowException() {
        when(blogRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.findById(999L))
                .isInstanceOf(ResourceNotFoundCustomException.class)
                .hasMessageContaining("blog not found with blog id: 999");
        verify(blogRepository).findById(999L);
    }

    @Test
    void findByIdWithUser_ShouldReturnBlog() {
        regularUser.setBlogs(Collections.singletonList(testBlog));
        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(regularUser));
        when(blogRepository.findByUserEntityIdAndId(2L, 1L)).thenReturn(Optional.of(testBlog));

        Optional<Blog> result = blogService.findById(1L, "user123");

        assertThat(result).isPresent().contains(testBlog);
        verify(blogRepository).findByUserEntityIdAndId(2L, 1L);
    }

    @Test
    void findAll_AdminUser_ShouldReturnPagedData() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Page<Blog> mockPage = new PageImpl<>(Collections.singletonList(testBlog));
        when(blogRepository.findAll(pageable)).thenReturn(mockPage);
        when(userRepository.findByUserId("admin123")).thenReturn(Optional.of(adminUser));

        Map<String, Object> result = blogService.findAll(0, 10, "title", "admin123");

        assertThat(result).isNotNull();
        result.containsKey("data");
        assertThat(((List<?>) result.get("data")).size()).isEqualTo(1);
        verify(userRepository).findByUserId("admin123");
        verify(blogRepository).findAll(pageable);
    }

    @Test
    void findAll_NonAdminUser_ShouldReturnNull() {
        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(regularUser));

        Map<String, Object> result = blogService.findAll(0, 10, "title", "user123");

        assertThat(result).isNull();
        verify(userRepository).findByUserId("user123");
        verifyNoInteractions(blogRepository);
    }

    @Test
    void deleteBlog_AdminUser_ShouldDeleteAndReturnBlog() {
        when(userRepository.findByUserId("admin123")).thenReturn(Optional.of(adminUser));
        when(blogRepository.findById(1L)).thenReturn(Optional.of(testBlog));

        Blog result = blogService.deleteBlog(1L, "admin123");

        assertThat(result).isEqualTo(testBlog);
        verify(blogRepository).deleteById(1L);
    }

    @Test
    void deleteBlog_OwnerUser_ShouldDeleteAndReturnBlog() {
        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(regularUser));
        when(blogRepository.findById(1L)).thenReturn(Optional.of(testBlog));

        Blog result = blogService.deleteBlog(1L, "user123");

        assertThat(result).isEqualTo(testBlog);
        verify(blogRepository).deleteById(1L);
    }

    @Test
    void deleteBlog_NotAuthorized_ShouldReturnNull() {
        UserEntity unauthorizedUser = new UserEntity();
        unauthorizedUser.setUserId("unauth123");
        when(userRepository.findByUserId("unauth123")).thenReturn(Optional.of(unauthorizedUser));
        when(blogRepository.findById(1L)).thenReturn(Optional.of(testBlog));

        Blog result = blogService.deleteBlog(1L, "unauth123");

        assertThat(result).isNull();
        verify(blogRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateBlog_OwnerUser_ShouldUpdateAndSave() {
        Blog updatedBlog = new Blog("Updated Title", "Updated Content");
        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(regularUser));
        when(blogRepository.findById(1L)).thenReturn(Optional.of(testBlog));
        when(blogRepository.save(any(Blog.class))).thenReturn(testBlog);

        Blog result = blogService.updateBlog(1L, updatedBlog, "user123");

        assertThat(result).isEqualTo(testBlog);
        assertThat(testBlog.getTitle()).isEqualTo("Updated Title");
        verify(blogRepository).save(testBlog);
    }

    @Test
    void partialUpdate_OwnerUser_ShouldUpdateFields() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", "Partial Updated Title");

        when(userRepository.findByUserId("user123")).thenReturn(Optional.of(regularUser));
        when(blogRepository.findById(1L)).thenReturn(Optional.of(testBlog));
        when(blogRepository.save(any(Blog.class))).thenReturn(testBlog);

        Blog result = blogService.partialUpdate(1L, updates, "user123");

        assertThat(result).isEqualTo(testBlog);
        assertThat(testBlog.getTitle()).isEqualTo("Partial Updated Title");
        verify(blogRepository).save(testBlog);
    }
}
