package br.edu.canteen.dto;

import br.edu.canteen.model.Role;
import br.edu.canteen.model.User;

public record UserResponse(Long id, String name, String email, Role role, String studentId, boolean active) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.getStudentId(), user.isActive());
    }
}
