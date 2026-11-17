package com.example.online_couse_management.dto;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String contact;
    private String role; // "ROLE_STUDENT" or "ROLE_INSTRUCTOR" or "STUDENT" or "INSTRUCTOR"
    private String specialization; // for instructor

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password, String contact, String role, String specialization) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.contact = contact;
        this.role = role;
        this.specialization = specialization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
