package com.example.BlogManager.services;

import com.example.BlogManager.dto.UserDTO;
import com.example.BlogManager.objects.JwtUtil;
import com.example.BlogManager.objects.UserEntity;
import com.example.BlogManager.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    // Update Constructor to ask Spring for them
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public UserDTO register(UserDTO user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); //encode password and save it in the object given
        String username = user.getUserId();
        if (!userRepository.existsByUserId(username)) {
            UserEntity newUserEntity = new UserEntity(user.getName(), username, user.getPassword(), user.getUserType());
            userRepository.save(newUserEntity);
            System.out.println("created -> savedUser " + newUserEntity.getId());

            // --- SECURITY FIX HERE ---
            // Clear the password before sending the object back to the user
            user.setPassword("");

            return user;
        } else {
            //will recommend 5 unique user ids
            List<String> list = generateUniqueUsername(username);
            user.setRecommendedUsernames(list);

            // Clear password here too just in case
            user.setPassword("");

            return user;
        }
    }

    public String login(UserDTO loginRequest) {
        // 1. Delegate to Spring Security (This calls your CustomUserDetailsService internally)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUserId(),
                        loginRequest.getPassword()
                )
        );

        // 2. If code reaches here, password is correct. Generate Token.
        if (authentication.isAuthenticated()) {
            return JwtUtil.generateToken(loginRequest.getUserId());
        } else {
            throw new RuntimeException("Authentication failed");
        }
    }

    private List<String> generateUniqueUsername(String baseName) {
        String candidate = baseName.replaceAll("\\s+", "").toLowerCase();
        Random random = new Random();
        int suffix = 0;
        List<String> ret = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        while (ret.size() < 5) {
            while (userRepository.existsByUserId(candidate + (suffix == 0 ? "" : suffix))
                    || set.contains(suffix)) {
                suffix = random.nextInt(10000); // Appends random digits if username exists
            }
            set.add(suffix); // to avoid generation of same no.
            ret.add(candidate + (suffix == 0 ? "" : suffix));
        }
        return ret;
    }

    public Optional<UserEntity> findById(String id) {
        return userRepository.findByUserId(id);
    }

    public UserEntity deleteUser(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        return user.map(getUser -> {
            userRepository.deleteById(id);
            System.out.println("user with id " + id + " deleted");
            return getUser;
        }).orElse(null);
    }
}
