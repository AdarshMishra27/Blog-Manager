package com.example.BlogManager.services;

import com.example.BlogManager.dto.UserDTO;
import com.example.BlogManager.objects.JwtUtil;
import com.example.BlogManager.objects.User;
import com.example.BlogManager.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder;

    //constructor injection
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        encoder = new BCryptPasswordEncoder();
    }

    public UserDTO register(UserDTO user) {
        user.setPassword(encoder.encode(user.getPassword())); //encode password and save it in the object given
        String username = user.getUserId();
        if(!userRepository.existsByUserId(username)) {
            User newUser = new User(user.getName(), username, user.getPassword(), user.getUserType());
            userRepository.save(newUser);
            System.out.println("created -> savedUser " + newUser.getId());
            return user;
        }else {
            //will recommend 5 unique user ids
            List<String> list = generateUniqueUsername(username);
            user.setRecommendedUsernames(list);
            return user;
        }
    }

    public String login(UserDTO loginRequest) throws Exception {
        String token;
        try{
            token =  userRepository.findByUserId(loginRequest.getUserId())
                    .filter(user -> encoder.matches(loginRequest.getPassword(), user.getPassword()))
                    .map(user -> JwtUtil.generateToken(user.getUserId()))
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        }catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return token;
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

    public Optional<User> findById(String id) {
        return userRepository.findByUserId(id);
    }

    public User deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(getUser -> {
            userRepository.deleteById(id);
            System.out.println("user with id " + id + " deleted");
            return getUser;
        }).orElse(null);
    }
}
