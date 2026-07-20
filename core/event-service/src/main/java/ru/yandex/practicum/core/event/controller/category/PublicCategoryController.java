package ru.yandex.practicum.core.event.controller.category;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.event.service.category.CategoryService;
import ru.yandex.practicum.core.interaction.dto.category.CategoryFilter;
import ru.yandex.practicum.core.interaction.dto.category.CategoryResponse;

import java.util.List;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> getNeededCategories(@RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                      @RequestParam(defaultValue = "10") @Positive Integer size) {
        CategoryFilter categoryFilter = new CategoryFilter(from, size);
        return categoryService.getNeeded(categoryFilter);
    }

    @GetMapping("/{catId}")
    public CategoryResponse getCategory(@PathVariable("catId") Long catId) {
        return categoryService.findCategoryById(catId);
    }
}