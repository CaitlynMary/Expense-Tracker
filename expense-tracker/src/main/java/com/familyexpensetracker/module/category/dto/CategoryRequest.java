package com.familyexpensetracker.module.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Icon is required")
    @Size(max = 50, message = "Icon must not exceed 50 characters")
    private String icon;

    @NotBlank(message = "Color hex is required")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color hex must be a valid hex color (e.g., #FF5733)")
    private String colorHex;
}
