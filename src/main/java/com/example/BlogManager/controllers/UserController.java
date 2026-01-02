package com.example.BlogManager.controllers;

import com.example.BlogManager.dto.UserDTO;
import com.example.BlogManager.exceptions.ResourceNotFoundCustomException;
import com.example.BlogManager.objects.User;
import com.example.BlogManager.response.ApiResponseWrapper;
import com.example.BlogManager.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponseWrapper> createNewUser(@Valid @RequestBody UserDTO user) throws Exception {
        try {
            UserDTO savedUser = userService.register(user);

            if (savedUser.getRecommendedUsernames() == null) {
                System.out.println("User registered with the username given: " + savedUser.getUserId());
                return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.CREATED.value(), "Successfully Created!", null, savedUser), HttpStatusCode.valueOf(HttpStatus.CREATED.value()));
            } else {
                String message = "user can't be created with the given username";
                System.out.println(message);
                return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.CONFLICT.value(), "username already exists", message, savedUser.getRecommendedUsernames()), HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()));
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponseWrapper<String>> login(@RequestBody UserDTO loginRequest) throws Exception {
        String token = userService.login(loginRequest);
        System.out.println("generated token for the username: " + loginRequest.getUserId() + " is = " + token);
        return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.OK.value(), "token generated", null, token), HttpStatusCode.valueOf(200));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<UserDTO>> getUserById(@PathVariable String userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            String message = "Fetched user with id: {}" + userId;
            System.out.println(message);
            UserDTO ret = new UserDTO(user.get().getName(), userId, null, user.get().getUserType(), null);
            return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.OK.value(), message, null, ret), HttpStatusCode.valueOf(200));
        } else {
            String message = "user with id: " + userId + " not present in database! ";
            System.out.println(message);
            throw new ResourceNotFoundCustomException(message);
        }
    }

}
