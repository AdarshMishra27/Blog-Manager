package com.example.BlogManager.controllers;

import com.example.BlogManager.objects.Blog;
import com.example.BlogManager.objects.Comment;
import com.example.BlogManager.response.ApiResponseWrapper;
import com.example.BlogManager.services.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentsService;

    public CommentController(CommentService commentsService) {
        this.commentsService = commentsService;
    }

    @PostMapping("/{blogId}")
    public ResponseEntity<ApiResponseWrapper<Comment>> createBlog(@RequestBody Comment comment, HttpServletRequest request, @PathVariable Long blogId) {
        Comment savedComment = commentsService.save(comment, request, blogId);
        System.out.println("created -> savedComment " + savedComment.getId());

        return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.CREATED.value(), "Successfully Created!", null, savedComment), HttpStatusCode.valueOf(HttpStatus.CREATED.value()));
    }

    //comment can be deleted either by the creator of the blog post OR by the creator of the comment only
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteBlog(@PathVariable Long commentId, HttpServletRequest request) {

        Comment comment = commentsService.deleteBlog(commentId, request);
        if (comment == null) {
            ApiResponseWrapper<Void> response = new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "comment can be deleted either by the creator of the blog post or by the creator of the comment only", null, null);

            return new ResponseEntity<>(response, HttpStatus.valueOf(401));
        }

        String message = "Deleted user with id: " + commentId;
        System.out.println(message);
        return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.NO_CONTENT.value(), message, null, null), HttpStatusCode.valueOf(200));

    }

}
