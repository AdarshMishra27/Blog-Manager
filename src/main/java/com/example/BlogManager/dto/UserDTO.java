package com.example.BlogManager.dto;

import com.example.BlogManager.objects.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String name;
    private String userId;
    private String password;

    @NotNull(message = "userType must not be null")
    private UserType userType;

    private List<String> recommendedUsernames;

}
