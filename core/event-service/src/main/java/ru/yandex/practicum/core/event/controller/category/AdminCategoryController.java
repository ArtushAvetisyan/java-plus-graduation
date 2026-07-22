package ru.yandex.practicum.core.event.controller.category;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.event.service.category.CategoryService;
import ru.yandex.practicum.core.interaction.dto.category.CategoryRequest;
import ru.yandex.practicum.core.interaction.dto.category.CategoryResponse;

@RestController
@RequestMapping("/admin/categories")
@AllArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        return categoryService.save(categoryRequest);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("catId") Long catId) {
        categoryService.delete(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryResponse update(@PathVariable("catId") Long catId, @Valid @RequestBody CategoryRequest categoryRequest) {
        return categoryService.update(catId, categoryRequest);
    }
}