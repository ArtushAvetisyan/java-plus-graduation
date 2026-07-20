package ru.yandex.practicum.core.event.service.category;


import ru.yandex.practicum.core.interaction.dto.category.CategoryFilter;
import ru.yandex.practicum.core.interaction.dto.category.CategoryRequest;
import ru.yandex.practicum.core.interaction.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse save(CategoryRequest categoryRequest);

    CategoryResponse update(Long catId, CategoryRequest categoryRequest);

    void delete(Long catId);

    List<CategoryResponse> getNeeded(CategoryFilter categoryFilter);

    CategoryResponse findCategoryById(Long catId);
}