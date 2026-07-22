package ru.yandex.practicum.core.event.service.compilation;


import ru.yandex.practicum.core.interaction.dto.compilation.CompilationRequest;
import ru.yandex.practicum.core.interaction.dto.compilation.CompilationResponse;
import ru.yandex.practicum.core.interaction.dto.compilation.GetCompilationListDto;
import ru.yandex.practicum.core.interaction.dto.compilation.UpdateCompilationRequest;

import java.util.Collection;

public interface CompilationService {

    CompilationResponse save(CompilationRequest compilationRequest);

    CompilationResponse update(UpdateCompilationRequest compilationRequest, Long compId);

    void delete(Long compId);

    CompilationResponse findById(Long compId);

    Collection<CompilationResponse> findNeededCompilation(GetCompilationListDto getCompilationListDto);
}