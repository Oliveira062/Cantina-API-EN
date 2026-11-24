package br.edu.canteen.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "canteen")
public record CanteenProperties(String timeZone, Jwt jwt, Login login, Admin admin, Cors cors) {

    public record Jwt(String secret, long expirationMinutes, String issuer) {
    }

    public record Login(int maxAttempts, long lockMinutes) {
    }

    public record Admin(String email, String password) {
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
