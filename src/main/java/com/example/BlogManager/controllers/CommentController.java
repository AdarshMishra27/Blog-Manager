package com.example.BlogManager.controllers;

import com.example.BlogManager.objects.Comment;
import com.example.BlogManager.response.ApiResponseWrapper;
import com.example.BlogManager.services.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentsService;

    public CommentController(CommentService commentsService) {
        this.commentsService = commentsService;
    }

    @PostMapping("/{blogId}")
    public ResponseEntity<ApiResponseWrapper<Comment>> createBlog(@RequestBody Comment comment, @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long blogId) {
        Comment savedComment = commentsService.save(comment, userDetails.getUsername(), blogId);
//        System.out.println("created -> savedComment " + savedComment.getId());
        log.info("Created comment with id: {}", savedComment.getId());
        return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.CREATED.value(), "Successfully Created!", null, savedComment), HttpStatusCode.valueOf(HttpStatus.CREATED.value()));
    }

    //comment can be deleted either by the creator of the blog post OR by the creator of the comment only
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseWrapper<Void>> deleteBlog(@PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails) {

        Comment comment = commentsService.deleteBlog(commentId, userDetails.getUsername());
        if (comment == null) {
            ApiResponseWrapper<Void> response = new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "comment can be deleted either by the creator of the blog post or by the creator of the comment only", null, null);

            return new ResponseEntity<>(response, HttpStatus.valueOf(401));
        }

        String message = "Deleted user with id: " + commentId;
//        System.out.println(message);
        log.info(message);
        return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.NO_CONTENT.value(), message, null, null), HttpStatusCode.valueOf(200));

    }

}
