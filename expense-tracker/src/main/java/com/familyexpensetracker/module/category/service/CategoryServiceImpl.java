package com.familyexpensetracker.module.category.service;

import com.familyexpensetracker.common.mapper.CategoryMapper;
import com.familyexpensetracker.exception.BadRequestException;
import com.familyexpensetracker.exception.DuplicateResourceException;
import com.familyexpensetracker.exception.ResourceNotFoundException;
import com.familyexpensetracker.module.category.dto.CategoryRequest;
import com.familyexpensetracker.module.category.dto.CategoryResponse;
import com.familyexpensetracker.module.category.entity.Category;
import com.familyexpensetracker.module.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Prevent duplicate category names (case-insensitive)
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new DuplicateResourceException("Category", "name", request.getName().trim());
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Soft protection: warn when modifying default categories
        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new BadRequestException("Default category '" + category.getName()
                    + "' cannot be modified. Please create a custom category instead.");
        }

        // Check for duplicate name, excluding the current category
        categoryRepository.findByNameIgnoreCase(request.getName().trim())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException("Category", "name", request.getName().trim());
                    }
                });

        categoryMapper.updateEntity(category, request);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Soft protection: prevent deletion of default categories
        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new BadRequestException("Default category '" + category.getName()
                    + "' cannot be deleted.");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return categoryMapper.toResponse(category);
    }
}
