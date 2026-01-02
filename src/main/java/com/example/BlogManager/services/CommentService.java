package com.example.BlogManager.services;

import com.example.BlogManager.exceptions.ResourceNotFoundCustomException;
import com.example.BlogManager.objects.Blog;
import com.example.BlogManager.objects.Comment;
import com.example.BlogManager.objects.User;
import com.example.BlogManager.repositories.CommentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BlogService blogService;

    public CommentService(CommentRepository commentRepository, BlogService blogService) {
        this.commentRepository = commentRepository;
        this.blogService = blogService;
    }

    //create
    public Comment save(Comment comment, HttpServletRequest request, Long blogId) {
        Blog blog = blogService.findById(blogId);
        User user = blogService.fetchUserDetailsFromDB(request);
        comment.setUser(user);
        comment.setBlog(blog);
        return commentRepository.save(comment);
    }

    public Comment deleteBlog(Long commentId, HttpServletRequest request) {
        User user = blogService.fetchUserDetailsFromDB(request);
        Optional<Comment> checkComment = commentRepository.findById(commentId);
        if (checkComment.isEmpty()) throw new ResourceNotFoundCustomException("no comment with id: " + commentId);


        //comment can be deleted either by the creator of the blog post OR by the creator of the comment only
        if (checkComment.get().getBlog().getUser().getUserId().equals(user.getUserId()) //creator of the blog post check
                || checkComment.get().getUser().getUserId().equals(user.getUserId()) // creator of the comment check
        ) {
            return checkComment.map(getComment -> {
                commentRepository.deleteById(commentId);
                System.out.println("blog with id " + commentId + " deleted");
                return getComment;
            }).orElse(null);
        } else {
            return null;
        }
    }
}
