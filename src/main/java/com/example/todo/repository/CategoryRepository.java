package com.example.todo.repository;

import com.example.todo.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE LOWER(c.label) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> searchByLabel(@Param("keyword") String keyword, Pageable pageable);
}
