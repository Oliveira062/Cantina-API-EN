package br.edu.canteen.repository;

import br.edu.canteen.model.Notification;
import br.edu.canteen.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByUserIdOrTargetRoleOrderByIdDesc(Long userId, Role targetRole);
}
