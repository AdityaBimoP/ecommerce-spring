package com.superstyleshop.repository;

import com.superstyleshop.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select product from Product product where product.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    List<Product> findByCategoryIgnoreCaseOrderByNameAsc(String category);

    List<Product> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Product> findByCategoryIgnoreCaseAndNameContainingIgnoreCaseOrderByNameAsc(
            String category, String name);
}
