package com.isaacmicheal.payflow.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        String role) {

    public static AuthResponse bearer(String token, String email, String role) {
        return new AuthResponse(token, "Bearer", email, role);
    }
}
