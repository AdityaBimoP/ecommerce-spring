package com.superstyleshop.repository;

import com.superstyleshop.model.CustomerOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<CustomerOrder> findByUser_IdOrderByOrderNumberAsc(Long userId);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    Optional<CustomerOrder> findTopByUser_IdOrderByOrderNumberDesc(Long userId);

    List<CustomerOrder> findByUser_IdOrderByCreatedAtAscIdAsc(Long userId);
}
