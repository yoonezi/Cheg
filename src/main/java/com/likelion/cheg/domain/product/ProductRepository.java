package com.likelion.cheg.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(value = "SELECT * FROM Product p ORDER BY p.id DESC",nativeQuery = true)
    List<Product> findAllDesc();



}
