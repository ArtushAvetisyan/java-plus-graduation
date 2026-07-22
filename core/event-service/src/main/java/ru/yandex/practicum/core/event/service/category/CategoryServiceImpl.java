package ru.yandex.practicum.core.event.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.event.mapper.CategoryMapper;
import ru.yandex.practicum.core.event.model.Category;
import ru.yandex.practicum.core.event.repository.CategoryRepository;
import ru.yandex.practicum.core.event.repository.EventRepository;
import ru.yandex.practicum.core.interaction.dto.category.CategoryFilter;
import ru.yandex.practicum.core.interaction.dto.category.CategoryRequest;
import ru.yandex.practicum.core.interaction.dto.category.CategoryResponse;
import ru.yandex.practicum.core.interaction.handler.exception.AlreadyExistsException;
import ru.yandex.practicum.core.interaction.handler.exception.ConflictException;
import ru.yandex.practicum.core.interaction.handler.exception.NotFoundException;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryResponse save(CategoryRequest categoryRequest) {
        if (Boolean.TRUE.equals(categoryRepository.existsByName(categoryRequest.getName()))) {
            throw new AlreadyExistsException(String.format("Категория с названием=%s уже существует", categoryRequest.getName()));
        }
        Category newCategory = categoryRepository.save(categoryMapper.toCategory(categoryRequest));
        return categoryMapper.toCategoryResponse(newCategory);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long catId, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException(String.format("Категория с id=%s не найдена", catId)));
        if (Boolean.TRUE.equals(categoryRepository.existsByNameAndIdNot(categoryRequest.getName(), catId))) {
            throw new AlreadyExistsException(String.format("Категория с названием=%s уже существует", categoryRequest.getName()));
        }
        category.setName(categoryRequest.getName());
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException(String.format("Категория с id=%s не найдена", catId)));

        if (Boolean.TRUE.equals(eventRepository.existsByCategoryId(catId))) {
            throw new ConflictException("Невозможно удалить категорию, так как к ней привязаны некоторые события");
        }
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryResponse> getNeeded(CategoryFilter categoryFilter) {
        return categoryRepository.findNeededCategories(categoryFilter.getFrom(), categoryFilter.getSize())
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public CategoryResponse findCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException(String.format("Категория с id=%s не найдена", catId)));
        return categoryMapper.toCategoryResponse(category);
    }
}