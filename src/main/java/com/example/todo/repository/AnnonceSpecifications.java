package com.example.todo.repository;

import com.example.todo.model.Annonce;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public final class AnnonceSpecifications {
    private AnnonceSpecifications() {
    }

    public static Specification<Annonce> keywordInFields(String keyword, Set<String> fields) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank() || fields == null || fields.isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            ArrayList<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                predicates.add(cb.like(cb.lower(root.get(field)), pattern));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Annonce> hasStatus(Annonce.Status status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Annonce> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? cb.conjunction() : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Annonce> hasAuthorId(Long authorId) {
        return (root, query, cb) -> authorId == null ? cb.conjunction() : cb.equal(root.get("author").get("id"), authorId);
    }

    public static Specification<Annonce> fromDate(Instant fromDate) {
        return (root, query, cb) -> fromDate == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("date"), Timestamp.from(fromDate));
    }

    public static Specification<Annonce> toDate(Instant toDate) {
        return (root, query, cb) -> toDate == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("date"), Timestamp.from(toDate));
    }
}
