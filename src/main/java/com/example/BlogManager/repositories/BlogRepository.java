package com.example.BlogManager.repositories;

import com.example.BlogManager.objects.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findByUserEntityIdAndId(Long user_id, Long blogId); // find a blog which has a long blog id and the specific long user_id
}
