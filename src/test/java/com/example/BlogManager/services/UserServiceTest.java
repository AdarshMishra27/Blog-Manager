package com.example.BlogManager.services;

import com.example.BlogManager.dto.UserDTO;
import com.example.BlogManager.objects.UserEntity;
import com.example.BlogManager.objects.UserType;
import com.example.BlogManager.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @Test
    void register_NewUser_SavesAndEncodesPassword() {
        // Given
        UserDTO input = new UserDTO("John", "john", "pass123", UserType.USER);
        String encoded = "$2a$10$encodedHash";
        when(passwordEncoder.encode("pass123")).thenReturn(encoded);
        when(userRepository.existsByUserId("john")).thenReturn(false);

        UserEntity savedEntity = new UserEntity("John", "john", encoded, UserType.USER);
        savedEntity.setId(1L);  // Assuming it sets ID on save
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // When
        UserDTO result = userService.register(input);

        // Then
        assertThat(result.getPassword()).isEmpty();
        assertThat(result.getName()).isEqualTo("John");
        verify(passwordEncoder).encode("pass123");
        verify(userRepository).save(argThat(entity ->
                entity.getUserId().equals("john") && entity.getPassword().equals(encoded)));
    }

    @Test
    void register_ExistingUser_ReturnsRecommendations() {
        // Given
        UserDTO input = new UserDTO("John", "john", "pass123", UserType.USER);
        when(userRepository.existsByUserId("john")).thenReturn(true);

        // When
        UserDTO result = userService.register(input);

        // Then
        assertThat(result.getPassword()).isEmpty();
        assertThat(result.getRecommendedUsernames()).isNotEmpty().hasSize(5);
    }

    @Test
    void login_Successful_ReturnsToken() {
        // Given
        UserDTO loginReq = new UserDTO(null, "john", "pass123", null);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        // When
        String token = userService.login(loginReq);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
        verify(authenticationManager).authenticate(argThat(tokenArg ->
                tokenArg.getPrincipal().equals("john") &&
                        tokenArg.getCredentials().equals("pass123")));
    }

    @Test
    void login_Failure_ThrowsException() {
        // Given
        UserDTO loginReq = new UserDTO(null, "john", "wrongpass", null);
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        // When & Then
        assertThatThrownBy(() -> userService.login(loginReq))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Authentication failed");
    }
}
