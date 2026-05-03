package com.online.bookms.dto.category;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Long parentCategoryId
) {
}
