package com.online.bookms.controller;

import com.online.bookms.dto.category.CategoryResponse;
import com.online.bookms.dto.category.MainCategoryRequest;
import com.online.bookms.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories/main")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createMainCategory(@RequestBody MainCategoryRequest request) {
        CategoryResponse response = categoryService.createMainCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse updateMainCategory(
            @PathVariable Long categoryId,
            @RequestBody MainCategoryRequest request
    ) {
        return categoryService.updateMainCategory(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteMainCategory(@PathVariable Long categoryId) {
        categoryService.deleteMainCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
