package br.edu.canteen.config;

import br.edu.canteen.model.Role;
import br.edu.canteen.model.User;
import br.edu.canteen.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialData implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialData.class);

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final CanteenProperties properties;

    public InitialData(UserRepository userRepository, PasswordEncoder encoder,
                        CanteenProperties properties) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        String email = properties.admin().email();
        String password = properties.admin().password();
        if (email == null || email.isBlank() || password == null || password.length() < 8) {
            log.warn("No administrator registered. Set ADMIN_EMAIL and ADMIN_PASSWORD (at least 8 characters) "
                    + "and restart the application.");
            return;
        }
        userRepository.save(new User("Administrator", email, encoder.encode(password), Role.ADMIN, null));
        log.info("Initial administrator created: {}", email.trim().toLowerCase());
    }
}
