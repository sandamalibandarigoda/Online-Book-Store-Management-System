package com.online.bookms.service.impl;

import com.online.bookms.dto.category.CategoryResponse;
import com.online.bookms.dto.category.MainCategoryRequest;
import com.online.bookms.exception.DuplicateResourceException;
import com.online.bookms.exception.InvalidCategoryException;
import com.online.bookms.exception.ResourceNotFoundException;
import com.online.bookms.model.category.Category;
import com.online.bookms.repository.CategoryRepository;
import com.online.bookms.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryResponse createMainCategory(MainCategoryRequest request) {
        SanitizedCategoryData sanitizedData = validateAndNormalize(request);

        categoryRepository.findByParentCategoryIsNullAndNameIgnoreCase(sanitizedData.name())
                .ifPresent(category -> {
                    throw new DuplicateResourceException(
                            "Main category already exists with name: " + sanitizedData.name()
                    );
                });

        Category category = new Category();
        category.setName(sanitizedData.name());
        category.setDescription(sanitizedData.description());

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateMainCategory(Long categoryId, MainCategoryRequest request) {
        SanitizedCategoryData sanitizedData = validateAndNormalize(request);
        Category category = getMainCategory(categoryId);

        categoryRepository.findByParentCategoryIsNullAndNameIgnoreCase(sanitizedData.name())
                .filter(existingCategory -> !existingCategory.getId().equals(categoryId))
                .ifPresent(existingCategory -> {
                    throw new DuplicateResourceException(
                            "Main category already exists with name: " + sanitizedData.name()
                    );
                });

        category.setName(sanitizedData.name());
        category.setDescription(sanitizedData.description());

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteMainCategory(Long categoryId) {
        Category category = getMainCategory(categoryId);

        if (categoryRepository.existsByParentCategoryId(categoryId)) {
            throw new InvalidCategoryException("Cannot delete a main category that still has subcategories.");
        }

        categoryRepository.delete(category);
    }

    private Category getMainCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        if (category.getParentCategory() != null) {
            throw new InvalidCategoryException("Category with id " + categoryId + " is not a main category.");
        }

        return category;
    }

    private SanitizedCategoryData validateAndNormalize(MainCategoryRequest request) {
        if (request == null) {
            throw new InvalidCategoryException("Request body is required.");
        }

        String name = request.getName() == null ? null : request.getName().trim();
        String description = request.getDescription() == null ? null : request.getDescription().trim();

        if (!StringUtils.hasText(name)) {
            throw new InvalidCategoryException("Category name is required.");
        }

        if (name.length() > NAME_MAX_LENGTH) {
            throw new InvalidCategoryException(
                    "Category name must not exceed " + NAME_MAX_LENGTH + " characters."
            );
        }

        if (description != null && description.isEmpty()) {
            description = null;
        }

        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new InvalidCategoryException(
                    "Category description must not exceed " + DESCRIPTION_MAX_LENGTH + " characters."
            );
        }

        return new SanitizedCategoryData(name, description);
    }

    private CategoryResponse toResponse(Category category) {
        Long parentCategoryId = category.getParentCategory() == null
                ? null
                : category.getParentCategory().getId();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                parentCategoryId
        );
    }

    private record SanitizedCategoryData(String name, String description) {
    }
}
