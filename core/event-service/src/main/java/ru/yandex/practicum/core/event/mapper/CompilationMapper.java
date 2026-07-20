package ru.yandex.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.core.event.model.Compilation;
import ru.yandex.practicum.core.interaction.dto.compilation.CompilationRequest;
import ru.yandex.practicum.core.interaction.dto.compilation.CompilationResponse;

@Mapper(componentModel = "spring", uses = EventMapper.class)
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(CompilationRequest compilationRequest);

    CompilationResponse toCompilationResponse(Compilation compilation);
}