package com.online.bookms.service;

import com.online.bookms.dto.category.CategoryResponse;
import com.online.bookms.dto.category.MainCategoryRequest;

public interface CategoryService {

    CategoryResponse createMainCategory(MainCategoryRequest request);

    CategoryResponse updateMainCategory(Long categoryId, MainCategoryRequest request);

    void deleteMainCategory(Long categoryId);
}
