package com.online.bookms.service.impl;

import com.online.bookms.dto.category.CategoryResponse;
import com.online.bookms.dto.category.MainCategoryRequest;
import com.online.bookms.exception.DuplicateResourceException;
import com.online.bookms.model.category.Category;
import com.online.bookms.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createMainCategorySavesTrimmedValues() {
        MainCategoryRequest request = new MainCategoryRequest("  Fiction  ", "  Story books  ");
        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Fiction");
        savedCategory.setDescription("Story books");

        when(categoryRepository.findByParentCategoryIsNullAndNameIgnoreCase("Fiction"))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.createMainCategory(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Fiction");
        assertThat(response.description()).isEqualTo("Story books");
        assertThat(response.parentCategoryId()).isNull();
    }

    @Test
    void createMainCategoryRejectsDuplicateName() {
        MainCategoryRequest request = new MainCategoryRequest("Fiction", "Story books");
        Category existingCategory = new Category();
        existingCategory.setId(99L);
        existingCategory.setName("Fiction");

        when(categoryRepository.findByParentCategoryIsNullAndNameIgnoreCase("Fiction"))
                .thenReturn(Optional.of(existingCategory));

        assertThatThrownBy(() -> categoryService.createMainCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Main category already exists with name: Fiction");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateMainCategoryChangesExistingMainCategory() {
        MainCategoryRequest request = new MainCategoryRequest("Science", "Science and technology");
        Category existingCategory = new Category();
        existingCategory.setId(5L);
        existingCategory.setName("Old Name");
        existingCategory.setDescription("Old description");

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByParentCategoryIsNullAndNameIgnoreCase("Science"))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

        CategoryResponse response = categoryService.updateMainCategory(5L, request);

        assertThat(existingCategory.getName()).isEqualTo("Science");
        assertThat(existingCategory.getDescription()).isEqualTo("Science and technology");
        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.name()).isEqualTo("Science");
    }

    @Test
    void deleteMainCategoryRemovesCategoryWithoutChildren() {
        Category existingCategory = new Category();
        existingCategory.setId(7L);
        existingCategory.setName("Children");

        when(categoryRepository.findById(7L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByParentCategoryId(7L)).thenReturn(false);

        categoryService.deleteMainCategory(7L);

        verify(categoryRepository).delete(existingCategory);
    }
}
