package br.edu.canteen.security;

import br.edu.canteen.config.CanteenProperties;
import br.edu.canteen.exception.LoginBlockedException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptGuard {

    private final Map<String, Attempts> records = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration lockDuration;

    public LoginAttemptGuard(CanteenProperties properties) {
        this.maxAttempts = properties.login().maxAttempts();
        this.lockDuration = Duration.ofMinutes(properties.login().lockMinutes());
    }

    public void checkLockout(String email) {
        Attempts attempts = records.get(key(email));
        if (attempts == null || attempts.blockedUntil == null) {
            return;
        }
        Instant now = Instant.now();
        if (now.isBefore(attempts.blockedUntil)) {
            long minutes = Math.max(1, Duration.between(now, attempts.blockedUntil).toMinutes() + 1);
            throw new LoginBlockedException(minutes);
        }
        records.remove(key(email));
    }

    public void recordFailure(String email) {
        records.compute(key(email), (k, current) -> {
            Attempts attempts = current == null ? new Attempts() : current;
            attempts.failures++;
            if (attempts.failures >= maxAttempts) {
                attempts.blockedUntil = Instant.now().plus(lockDuration);
            }
            return attempts;
        });
    }

    public void recordSuccess(String email) {
        records.remove(key(email));
    }

    private String key(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static class Attempts {
        private int failures;
        private Instant blockedUntil;
    }
}
