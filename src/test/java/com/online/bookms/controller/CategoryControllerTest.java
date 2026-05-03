package com.online.bookms.controller;

import com.online.bookms.dto.category.CategoryResponse;
import com.online.bookms.dto.category.MainCategoryRequest;
import com.online.bookms.exception.GlobalExceptionHandler;
import com.online.bookms.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest {

    private MockMvc mockMvc;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = Mockito.mock(CategoryService.class);
        CategoryController controller = new CategoryController(categoryService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createMainCategoryReturnsCreatedResponse() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Fiction", "Story books", null);

        when(categoryService.createMainCategory(any(MainCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/categories/main")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Fiction",
                                  "description": "Story books"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fiction"))
                .andExpect(jsonPath("$.description").value("Story books"));
    }

    @Test
    void updateMainCategoryReturnsUpdatedResponse() throws Exception {
        CategoryResponse response = new CategoryResponse(3L, "Science", "Science books", null);

        when(categoryService.updateMainCategory(eq(3L), any(MainCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/categories/main/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Science",
                                  "description": "Science books"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Science"));
    }

    @Test
    void deleteMainCategoryReturnsNoContent() throws Exception {
        doNothing().when(categoryService).deleteMainCategory(4L);

        mockMvc.perform(delete("/api/v1/categories/main/4"))
                .andExpect(status().isNoContent());
    }
}
