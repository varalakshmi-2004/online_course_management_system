package com.example.online_couse_management.service;

import com.example.online_couse_management.dto.AuthRequest;
import com.example.online_couse_management.dto.AuthResponse;
import com.example.online_couse_management.dto.RegisterRequest;
import com.example.online_couse_management.entity.Instructor;
import com.example.online_couse_management.entity.Role;
import com.example.online_couse_management.entity.Student;
import com.example.online_couse_management.entity.User;
import com.example.online_couse_management.repository.InstructorRepository;
import com.example.online_couse_management.repository.StudentRepository;
import com.example.online_couse_management.repository.UserRepository;
import com.example.online_couse_management.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       InstructorRepository instructorRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(email)) {
            throw new RuntimeException("An account with email " + email + " already exists.");
        }

        Role userRole = Role.ROLE_STUDENT;
        if (request.getRole() != null) {
            String roleStr = request.getRole().trim().toUpperCase();
            if (roleStr.contains("INSTRUCTOR")) {
                userRole = Role.ROLE_INSTRUCTOR;
            } else if (roleStr.contains("ADMIN")) {
                userRole = Role.ROLE_ADMIN;
            }
        }

        User user = new User(email, passwordEncoder.encode(request.getPassword()), userRole);
        user = userRepository.save(user);

        Long profileId = null;
        String profileName = request.getName();

        if (userRole == Role.ROLE_STUDENT) {
            Student student = new Student(request.getName(), email, request.getContact());
            student.setUser(user);
            student = studentRepository.save(student);
            profileId = student.getId();
        } else if (userRole == Role.ROLE_INSTRUCTOR) {
            Instructor instructor = new Instructor(request.getName(), email, request.getContact(),
                    request.getSpecialization() != null ? request.getSpecialization() : "General");
            instructor.setUser(user);
            instructor = instructorRepository.save(instructor);
            profileId = instructor.getId();
        }

        String token = jwtUtil.generateToken(user, userRole.name());
        return new AuthResponse(token, user.getUsername(), userRole.name(), profileId, profileName);
    }

    public AuthResponse login(AuthRequest request) {
        String username = request.getUsername().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user, user.getRole().name());

        Long profileId = null;
        String name = user.getUsername();

        if (user.getRole() == Role.ROLE_STUDENT) {
            var studentOpt = studentRepository.findByEmail(username);
            if (studentOpt.isPresent()) {
                profileId = studentOpt.get().getId();
                name = studentOpt.get().getName();
            }
        } else if (user.getRole() == Role.ROLE_INSTRUCTOR) {
            var instructorOpt = instructorRepository.findByEmail(username);
            if (instructorOpt.isPresent()) {
                profileId = instructorOpt.get().getId();
                name = instructorOpt.get().getName();
            }
        }

        return new AuthResponse(token, user.getUsername(), user.getRole().name(), profileId, name);
    }
}
