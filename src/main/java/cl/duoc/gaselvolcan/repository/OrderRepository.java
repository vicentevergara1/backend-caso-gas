package cl.duoc.gaselvolcan.repository;

import cl.duoc.gaselvolcan.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByOrderByCreatedAtDesc();
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
