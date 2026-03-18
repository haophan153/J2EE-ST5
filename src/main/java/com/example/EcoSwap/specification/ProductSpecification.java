package com.example.EcoSwap.specification;

import com.example.EcoSwap.dto.ProductFilterDTO;
import com.example.EcoSwap.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFilters(ProductFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), "AVAILABLE"));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                        "%" + filter.getKeyword().toLowerCase().trim() + "%"));
            }
            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }
            if (filter.getCondition() != null && !filter.getCondition().isBlank()) {
                predicates.add(cb.equal(root.get("condition"), filter.getCondition()));
            }
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            if (filter.getLocation() != null && !filter.getLocation().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")),
                        "%" + filter.getLocation().toLowerCase().trim() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
