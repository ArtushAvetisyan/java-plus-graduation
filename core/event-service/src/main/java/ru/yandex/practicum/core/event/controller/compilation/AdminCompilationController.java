package ru.yandex.practicum.core.event.controller.compilation;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.event.service.compilation.CompilationService;
import ru.yandex.practicum.core.interaction.dto.compilation.CompilationRequest;
import ru.yandex.practicum.core.interaction.dto.compilation.CompilationResponse;
import ru.yandex.practicum.core.interaction.dto.compilation.UpdateCompilationRequest;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Validated
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationResponse save(@Valid @RequestBody CompilationRequest compilationRequest) {
        return compilationService.save(compilationRequest);
    }

    @PatchMapping("/{compId}")
    public CompilationResponse update(@Valid @RequestBody UpdateCompilationRequest compilationRequest,
                                      @PathVariable("compId") Long compId) {
        return compilationService.update(compilationRequest, compId);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("compId") Long compId) {
        compilationService.delete(compId);
    }
}