package com.example.BlogManager.services;

import com.example.BlogManager.objects.Blog;
import com.example.BlogManager.objects.Comment;
import com.example.BlogManager.objects.User;
import com.example.BlogManager.repositories.CommentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

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
}
