package com.example.BlogManager.controllers;

import com.example.BlogManager.exceptions.ResourceNotFoundCustomException;
import com.example.BlogManager.objects.Blog;
import com.example.BlogManager.response.ApiResponseWrapper;
import com.example.BlogManager.services.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/blog")
public class BlogController {
    //    @Autowired -> old
    private final BlogService blogService;

    //constructor injection -> new
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }


    @PostMapping("/")
    public ResponseEntity<ApiResponseWrapper<Blog>> createBlog(@RequestBody Blog blog, @AuthenticationPrincipal UserDetails userDetails) {
        Blog savedBlog = blogService.save(blog, userDetails.getUsername());
        System.out.println("created -> savedBlog " + savedBlog.getId());

        return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.CREATED.value(), "Successfully Created!", null, savedBlog), HttpStatusCode.valueOf(HttpStatus.CREATED.value()));
    }

    //fetches blog under the logged in user_id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Blog>> getBlog(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return blogService.findById(id, userDetails.getUsername()).map(blog -> {
            String message = "Fetched blog with id: {}" + id;
            System.out.println(message);
            return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.OK.value(), message, null, blog), HttpStatusCode.valueOf(200));
        }).orElseGet(() -> {
            String message = "Blog not found with id: {}" + id + " and under the user logged in " + userDetails.getUsername();
            System.out.println(message);
            throw new ResourceNotFoundCustomException(message);
        });
    }

    //independent of user -> everyone can access blogs
    @GetMapping("/all")
    public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> getAllBlogs(@RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "3") int size,
                                                                               @RequestParam(defaultValue = "title") String sortBy,
                                                                               @AuthenticationPrincipal UserDetails userDetails) {
        // PAGES are index of pages (0 - indexes) and size is no. of blogs in each page
        //total items is total no. of all the blogs in the db
        Map<String, Object> blogs = blogService.findAll(page, size, sortBy, userDetails.getUsername());
        if (blogs == null) {
            ApiResponseWrapper<Map<String, Object>> response = new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Only Admins can access all blogs", null, null);

            return new ResponseEntity<>(response, HttpStatus.valueOf(401));
        }

        return ResponseEntity.ok(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.OK.value(), "map of data returned", null,
                blogs));
    }

    //    Only Admins can delete the blog or the user who created it
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteBlog(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Blog blog = blogService.deleteBlog(id, userDetails.getUsername());
        if (blog == null) {
            ApiResponseWrapper<Void> response = new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Only Admins can delete the blog or the user who created it", null, null);

            return new ResponseEntity<>(response, HttpStatus.valueOf(401));
        }

        String message = "Deleted user with id: {}" + id;
        System.out.println(message);
        return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.NO_CONTENT.value(), null, message, null), HttpStatusCode.valueOf(200));

    }

    //Only the user who created the blog can update
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Blog>> updateBlog(@PathVariable Long id, @RequestBody Blog updatedBlog, @AuthenticationPrincipal UserDetails userDetails) {

        Blog blog = blogService.updateBlog(id, updatedBlog, userDetails.getUsername());
        if (blog == null) {
            ApiResponseWrapper<Blog> response = new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Only the user who created the blog can update it", null, null);

            return new ResponseEntity<>(response, HttpStatus.valueOf(401));
        }
        String message = "Fully updated blog with id: {}" + id;
        System.out.println(message);
        return ResponseEntity.ok(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.OK.value(), null, message, blog));

    }

    //Only the user who created the blog can update
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<Blog>> partiallyUpdateBlog(@PathVariable Long id, @RequestBody Map<String, Object> patchRequest, @AuthenticationPrincipal UserDetails userDetails) {

        Blog blog = blogService.partialUpdate(id, patchRequest, userDetails.getUsername());

        if (blog == null) {
            ApiResponseWrapper<Blog> response = new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Only the user who created the blog can partial update it", null, null);

            return new ResponseEntity<>(response, HttpStatus.valueOf(401));
        }
        String message = "Blog partially updated blog with id: {}" + id;
        System.out.println(message);
        return ResponseEntity.ok(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.OK.value(), null, message, blog));

    }


}
