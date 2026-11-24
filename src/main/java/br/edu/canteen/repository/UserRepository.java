package br.edu.canteen.repository;

import br.edu.canteen.model.Role;
import br.edu.canteen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(Role role);

    List<User> findAllByOrderByNameAsc();

    @Query("""
            select u from User u
            where u.role = br.edu.canteen.model.Role.CUSTOMER and u.active = true
              and (lower(u.name) like lower(concat('%', :search, '%'))
                   or lower(u.email) like lower(concat('%', :search, '%'))
                   or u.studentId like concat('%', :search, '%'))
            order by u.name
            """)
    List<User> searchActiveCustomers(@Param("search") String search);
}
