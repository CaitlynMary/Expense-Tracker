package com.familyexpensetracker.common.mapper;

import com.familyexpensetracker.module.category.dto.CategoryRequest;
import com.familyexpensetracker.module.category.dto.CategoryResponse;
import com.familyexpensetracker.module.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.getName().trim())
                .icon(request.getIcon().trim())
                .colorHex(request.getColorHex().toUpperCase().trim())
                .isDefault(false)
                .build();
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .colorHex(category.getColorHex())
                .isDefault(category.getIsDefault())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    public void updateEntity(Category category, CategoryRequest request) {
        category.setName(request.getName().trim());
        category.setIcon(request.getIcon().trim());
        category.setColorHex(request.getColorHex().toUpperCase().trim());
    }
}
