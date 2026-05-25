package com.familyexpensetracker.module.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String icon;
    private String colorHex;

    @JsonProperty("isDefault")
    private Boolean isDefault;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
