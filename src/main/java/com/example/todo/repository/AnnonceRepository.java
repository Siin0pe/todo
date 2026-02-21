package com.example.todo.repository;

import com.example.todo.model.Annonce;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

    @EntityGraph(attributePaths = {"author", "category"})
    @Query("SELECT a FROM Annonce a WHERE a.id = :id")
    Optional<Annonce> findDetailedById(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"author", "category"})
    Page<Annonce> findAll(@Nullable Specification<Annonce> spec, Pageable pageable);

    @Query("""
            SELECT a
            FROM Annonce a
            WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Annonce> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
