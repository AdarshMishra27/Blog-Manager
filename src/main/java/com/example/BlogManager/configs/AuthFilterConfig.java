package com.example.BlogManager.configs;

import com.example.BlogManager.objects.JwtFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AuthFilterConfig {
    //disable spring's security
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())           // disable CSRF for APIs
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // allow all endpoints
        return http.build();
    }

    //custom Jwtfilter for AUTH
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilter() {
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns("/api/*"); // apply to all endpoints
        return registrationBean;
    }
}
