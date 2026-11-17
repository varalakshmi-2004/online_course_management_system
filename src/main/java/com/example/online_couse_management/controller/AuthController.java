package com.example.online_couse_management.controller;

import com.example.online_couse_management.dto.AuthRequest;
import com.example.online_couse_management.dto.AuthResponse;
import com.example.online_couse_management.dto.RegisterRequest;
import com.example.online_couse_management.entity.User;
import com.example.online_couse_management.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/student")
    public ResponseEntity<AuthResponse> registerStudent(@RequestBody RegisterRequest request) {
        request.setRole("ROLE_STUDENT");
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/instructor")
    public ResponseEntity<AuthResponse> registerInstructor(@RequestBody RegisterRequest request) {
        request.setRole("ROLE_INSTRUCTOR");
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }
}
