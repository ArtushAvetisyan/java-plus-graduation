package ru.yandex.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.core.event.model.Category;
import ru.yandex.practicum.core.interaction.dto.category.CategoryRequest;
import ru.yandex.practicum.core.interaction.dto.category.CategoryResponse;


@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toCategory(CategoryRequest categoryRequest);

    CategoryResponse toCategoryResponse(Category category);
}