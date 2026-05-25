package com.familyexpensetracker.module.category.service;

import com.familyexpensetracker.module.category.dto.CategoryRequest;
import com.familyexpensetracker.module.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);
}
