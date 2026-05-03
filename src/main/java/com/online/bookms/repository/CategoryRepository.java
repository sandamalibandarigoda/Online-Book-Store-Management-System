package com.online.bookms.repository;

import com.online.bookms.model.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByParentCategoryIsNullAndNameIgnoreCase(String name);

    boolean existsByParentCategoryId(Long parentCategoryId);
}
