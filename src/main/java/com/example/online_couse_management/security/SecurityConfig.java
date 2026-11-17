package com.example.online_couse_management.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/auth/**",
                                "/error",
                                "/",
                                "/index.html",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico"
                        ).permitAll()
                        // Anyone authenticated can browse courses
                        .requestMatchers(HttpMethod.GET, "/courses/**").permitAll()
                        // Only Instructors / Admin can create, modify, delete courses and course content
                        .requestMatchers(HttpMethod.POST, "/courses/**").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/courses/**").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/courses/**").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        // Instructor specific routes
                        .requestMatchers("/instructor/**").hasAnyAuthority("ROLE_INSTRUCTOR", "ROLE_ADMIN")
                        // Student specific routes
                        .requestMatchers("/students/**").hasAnyAuthority("ROLE_STUDENT", "ROLE_ADMIN", "ROLE_INSTRUCTOR")
                        // Enrollment routes
                        .requestMatchers("/enrollments/**").authenticated()
                        // Notification routes
                        .requestMatchers("/notifications/**").authenticated()
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
