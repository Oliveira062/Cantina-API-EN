package br.edu.canteen.service;

import br.edu.canteen.dto.StaffRequest;
import br.edu.canteen.dto.UserResponse;
import br.edu.canteen.exception.InvalidDataException;
import br.edu.canteen.exception.EmailAlreadyRegisteredException;
import br.edu.canteen.exception.ResourceNotFoundException;
import br.edu.canteen.model.User;
import br.edu.canteen.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final int SEARCH_LIMIT = 20;

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse createStaff(StaffRequest data) {
        if (repository.existsByEmailIgnoreCase(data.email().trim())) {
            throw new EmailAlreadyRegisteredException();
        }
        User user = new User(data.name(), data.email(), encoder.encode(data.password()),
                data.role(), null);
        return UserResponse.from(repository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return repository.findAllByOrderByNameAsc().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchCustomers(String search) {
        String term = search == null ? "" : search.trim();
        return repository.searchActiveCustomers(term).stream()
                .limit(SEARCH_LIMIT)
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse changeStatus(Long id, boolean active, User changedBy) {
        if (id.equals(changedBy.getId()) && !active) {
            throw new InvalidDataException("You cannot deactivate your own account.");
        }
        User user = find(id);
        user.setActive(active);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public User find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with code " + id + " not found."));
    }
}
