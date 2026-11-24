package br.edu.canteen.repository;

import br.edu.canteen.model.Order;
import br.edu.canteen.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByIdDesc(Long customerId);

    List<Order> findByCustomerIdAndStatusOrderByIdDesc(Long customerId, OrderStatus status);

    List<Order> findByStatusOrderByIdDesc(OrderStatus status);

    List<Order> findByStatusInOrderByIdAsc(Collection<OrderStatus> statuses);

    List<Order> findAllByOrderByIdDesc();
}
