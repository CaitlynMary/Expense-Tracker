package com.familyexpensetracker.config;

import com.familyexpensetracker.module.category.entity.Category;
import com.familyexpensetracker.module.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initDefaultCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                List<Category> defaultCategories = List.of(
                        Category.builder()
                                .name("Food & Dining")
                                .icon("🍔")
                                .colorHex("#FF6B35")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Transportation")
                                .icon("🚗")
                                .colorHex("#4ECDC4")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Shopping")
                                .icon("🛒")
                                .colorHex("#FF6B6B")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Entertainment")
                                .icon("🎬")
                                .colorHex("#C44DFF")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Bills & Utilities")
                                .icon("💡")
                                .colorHex("#FFE66D")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Healthcare")
                                .icon("🏥")
                                .colorHex("#06D6A0")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Education")
                                .icon("📚")
                                .colorHex("#118AB2")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Groceries")
                                .icon("🥬")
                                .colorHex("#2ECC71")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Rent & Housing")
                                .icon("🏠")
                                .colorHex("#E17055")
                                .isDefault(true)
                                .build(),
                        Category.builder()
                                .name("Others")
                                .icon("📦")
                                .colorHex("#636E72")
                                .isDefault(true)
                                .build()
                );

                categoryRepository.saveAll(defaultCategories);
                log.info("✅ Default categories initialized successfully ({} categories)", defaultCategories.size());
            } else {
                log.info("ℹ️ Categories already exist, skipping default initialization");
            }
        };
    }
}
