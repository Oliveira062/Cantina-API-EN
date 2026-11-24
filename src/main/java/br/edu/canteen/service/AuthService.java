package br.edu.canteen.service;

import br.edu.canteen.dto.LoginRequest;
import br.edu.canteen.dto.RegisterRequest;
import br.edu.canteen.dto.TokenResponse;
import br.edu.canteen.dto.UserResponse;
import br.edu.canteen.exception.InvalidCredentialsException;
import br.edu.canteen.exception.EmailAlreadyRegisteredException;
import br.edu.canteen.model.Role;
import br.edu.canteen.model.User;
import br.edu.canteen.repository.UserRepository;
import br.edu.canteen.security.LoginAttemptGuard;
import br.edu.canteen.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final LoginAttemptGuard attemptGuard;
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder encoder, JwtService jwtService,
                       LoginAttemptGuard attemptGuard) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.attemptGuard = attemptGuard;
        this.dummyHash = encoder.encode("nonexistent-password-for-comparison");
    }

    @Transactional
    public UserResponse registerCustomer(RegisterRequest data) {
        if (userRepository.existsByEmailIgnoreCase(data.email().trim())) {
            throw new EmailAlreadyRegisteredException();
        }
        User customer = new User(data.name(), data.email(), encoder.encode(data.password()),
                Role.CUSTOMER, data.studentId());
        return UserResponse.from(userRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest data) {
        String email = data.email().trim();
        attemptGuard.checkLockout(email);

        Optional<User> found = userRepository.findByEmailIgnoreCase(email);
        String hashToCompare = found.map(User::getPassword).orElse(dummyHash);
        boolean passwordMatches = encoder.matches(data.password(), hashToCompare);

        if (found.isEmpty() || !passwordMatches || !found.get().isActive()) {
            attemptGuard.recordFailure(email);
            throw new InvalidCredentialsException();
        }

        attemptGuard.recordSuccess(email);
        User user = found.get();
        return new TokenResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds(),
                UserResponse.from(user));
    }
}
