package com.example.BlogManager.repositories;

import com.example.BlogManager.objects.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUserId(String userId);

    Optional<UserEntity> findByUserId(String userId);
}
