package com.example.BlogManager.objects;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "comments")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") //foreign key in Comment table
    @JsonBackReference(value = "user-comments")   // This prevents serialization of parent within each blog to avoid loop
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id") //foreign key in comment table
    @JsonBackReference(value = "blog-comments")   // This prevents serialization of parent within each blog to avoid loop
    private Blog blog;
}
