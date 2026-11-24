package br.edu.canteen.dto;

public record TokenResponse(String token, String type, long expiresInSeconds, UserResponse user) {
}
