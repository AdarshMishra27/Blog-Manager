package com.example.BlogManager.objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    // Inject UserDetailsService to load user roles/permissions from DB
    public JwtFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String token = null;

        // 1. Check if token exists
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = JwtUtil.validateTokenAndGetUsername(token);
            } catch (Exception e) {
                // Token invalid - we ignore it and let Spring Security handle the 403 later
                System.out.println("Token validation failed: " + e.getMessage());
            }
        }

        // 2. If token is valid and no authentication is set in context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load full user details (including roles) from DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 3. Create Authentication Token
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 4. Set the user in the Security Context
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Continue the filter chain
        chain.doFilter(request, response);
    }
}
