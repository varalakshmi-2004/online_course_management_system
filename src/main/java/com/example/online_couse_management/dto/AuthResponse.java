package com.example.online_couse_management.dto;

import com.example.online_couse_management.entity.Role;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private Long profileId; // studentId or instructorId
    private String name;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username, String role, Long profileId, String name) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.profileId = profileId;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role != null ? role.name() : null;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
