package ru.yandex.practicum.core.event.controller.compilation;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.event.service.compilation.CompilationService;
import ru.yandex.practicum.core.interaction.dto.compilation.CompilationResponse;
import ru.yandex.practicum.core.interaction.dto.compilation.GetCompilationListDto;

import java.util.Collection;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    public CompilationResponse getById(@PathVariable("compId") Long compId) {
        return compilationService.findById(compId);
    }

    @GetMapping
    public Collection<CompilationResponse> getNeededCompilation(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            @RequestParam(defaultValue = "false") Boolean pinned) {
        return compilationService.findNeededCompilation(new GetCompilationListDto(size, pinned, from));
    }
}