package com.example.BlogManager.controllers;

import com.example.BlogManager.dto.UserDTO;
import com.example.BlogManager.exceptions.ResourceNotFoundCustomException;
import com.example.BlogManager.objects.UserEntity;
import com.example.BlogManager.response.ApiResponseWrapper;
import com.example.BlogManager.services.UserService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Slf4j
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
                log.info("User registered with the username given: {}", savedUser.getUserId());
//                System.out.println("User registered with the username given: " + savedUser.getUserId());
                return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.CREATED.value(), "Successfully Created!", null, savedUser), HttpStatusCode.valueOf(HttpStatus.CREATED.value()));
            } else {
                String message = "user can't be created with the given username";
                log.info(message);
//                System.out.println(message);
                return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.CONFLICT.value(), "username already exists", message, savedUser.getRecommendedUsernames()), HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()));
            }

        } catch (Exception e) {
            log.error("exception caught in UserController", e);
            throw new Exception(e);
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponseWrapper<String>> login(@RequestBody UserDTO loginRequest) throws Exception {
        String token = userService.login(loginRequest);
        log.info("generated token for the username: {} is = {}", loginRequest.getUserId(), token);
//        System.out.println("generated token for the username: " + loginRequest.getUserId() + " is = " + token);
        return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.OK.value(), "token generated", null, token), HttpStatusCode.valueOf(200));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<UserDTO>> getUserById(@PathVariable String userId) {
        Optional<UserEntity> user = userService.findById(userId);
        if (user.isPresent()) {
            String message = "Fetched user with id: {}" + userId;
            log.info(message);
            System.out.println(message);
            UserDTO ret = new UserDTO(user.get().getName(), userId, null, user.get().getUserType(), null);
            return new ResponseEntity<>(new ApiResponseWrapper<>(LocalDateTime.now(), HttpStatus.OK.value(), message, null, ret), HttpStatusCode.valueOf(200));
        } else {
            String message = "user with id: " + userId + " not present in database! ";
            log.info(message);
//            System.out.println(message);
            throw new ResourceNotFoundCustomException(message);
        }
    }

}
