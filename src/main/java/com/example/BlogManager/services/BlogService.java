package com.example.BlogManager.services;

import com.example.BlogManager.exceptions.ResourceNotFoundCustomException;
import com.example.BlogManager.objects.Blog;
import com.example.BlogManager.objects.UserEntity;
import com.example.BlogManager.objects.UserType;
import com.example.BlogManager.repositories.BlogRepository;
import com.example.BlogManager.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BlogService {
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    private BlogService(BlogRepository blogRepository, UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    //TODO ->>> this method should be inside @UserService
    public UserEntity fetchUserDetailsFromDB(String userId) { // username or userId is same across the app
        System.out.println("current user -> " + userId);
        //now using the username complete user details are fetched from the db and then added in the blog
        Optional<UserEntity> user = userRepository.findByUserId(userId);
        return user.get();
    }

    //create
    public Blog save(Blog blog, String username) {
        UserEntity userEntity = fetchUserDetailsFromDB(username);
        blog.setUserEntity(userEntity);
        return blogRepository.save(blog);
    }

    //find only by blogId
    public Blog findById(Long id) {
        Optional<Blog> blog = blogRepository.findById(id);
        if (blog.isPresent()) {
            return blog.get();
        } else {
            throw new ResourceNotFoundCustomException("blog not found with blog id: " + id);
        }
    }

    //find by both blogId and Userid
    public Optional<Blog> findById(Long id, String username) {
        UserEntity userEntity = fetchUserDetailsFromDB(username);
        return blogRepository.findByUserEntityIdAndId(userEntity.getId(), id);
    }

    public Map<String, Object> findAll(int page, int size, String sortBy, String username) {
        UserEntity userEntity = fetchUserDetailsFromDB(username);
        if (userEntity.getUserType() != UserType.ADMIN) { // Only ADMIN can access all the blogs
            return null;
        }

        Sort sort = Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Blog> res = blogRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", res.getContent());
        response.put("currentPage", res.getNumber());
        response.put("totalItems", res.getTotalElements());
        response.put("totalPages", res.getTotalPages());
        return response;
    }


    public Blog deleteBlog(Long id, String username) {
        UserEntity userEntity = fetchUserDetailsFromDB(username);
        Optional<Blog> checkBlog = blogRepository.findById(id);
        if (checkBlog.isEmpty()) throw new ResourceNotFoundCustomException("no blog with id: " + id);

        // Only ADMIN can delete a blog or the user who created it
        if (userEntity.getUserType() == UserType.ADMIN || checkBlog.get().getUserEntity().getUserId().equals(userEntity.getUserId())) {
            return checkBlog.map(getBlog -> {
                blogRepository.deleteById(id);
                System.out.println("blog with id " + id + " deleted");
                return getBlog;
            }).orElse(null);
        } else {
            return null;
        }
    }

    //complete update
    public Blog updateBlog(Long id, Blog updatedBlog, String username) {
        //only USER who created the blog can update the blog
        UserEntity userEntity = fetchUserDetailsFromDB(username);
        Optional<Blog> checkBlog = blogRepository.findById(id);
        if (checkBlog.isEmpty()) throw new ResourceNotFoundCustomException("no blog with id: " + id);

        if (checkBlog.get().getUserEntity().getUserId().equals(userEntity.getUserId())) {
            return checkBlog
                    .map(blog -> {
                        blog.setTitle(updatedBlog.getTitle());
                        blog.setContent(updatedBlog.getContent());

                        Blog savedBlog = blogRepository.save(blog);
                        return blog;
                    })
                    .orElse(null);

        } else {
            return null;
        }
    }

    //partial update
    public Blog partialUpdate(Long id, Map<String, Object> updates, String username) {
        //only USER who created the blog can partially update the blog
        UserEntity userEntity = fetchUserDetailsFromDB(username);
        Optional<Blog> checkBlog = blogRepository.findById(id);
        if (checkBlog.isEmpty()) throw new ResourceNotFoundCustomException("no blog with id: " + id);

        if (checkBlog.get().getUserEntity().getUserId().equals(userEntity.getUserId())) {

            updates.forEach((fieldName, value) -> {
                try {
                    Field field = checkBlog.get().getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(checkBlog.get(), value);
                } catch (NoSuchFieldException e) {
                    // field does not exist in the entity → ignore
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to update field: " + fieldName, e);
                }
            });

            return blogRepository.save(checkBlog.get());
        } else {
            return null;
        }

    }
}
