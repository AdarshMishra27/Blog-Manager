package com.example.BlogManager.repositories;

import com.example.BlogManager.objects.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
