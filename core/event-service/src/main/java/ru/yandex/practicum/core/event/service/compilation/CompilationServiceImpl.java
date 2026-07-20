package ru.yandex.practicum.core.event.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.event.mapper.CompilationMapper;
import ru.yandex.practicum.core.event.model.Compilation;
import ru.yandex.practicum.core.event.model.Event;
import ru.yandex.practicum.core.event.repository.CompilationRepository;
import ru.yandex.practicum.core.event.repository.EventRepository;
import ru.yandex.practicum.core.interaction.dto.compilation.CompilationRequest;
import ru.yandex.practicum.core.interaction.dto.compilation.CompilationResponse;
import ru.yandex.practicum.core.interaction.dto.compilation.GetCompilationListDto;
import ru.yandex.practicum.core.interaction.dto.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.core.interaction.handler.exception.AlreadyExistsException;
import ru.yandex.practicum.core.interaction.handler.exception.NotFoundException;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationResponse save(CompilationRequest compilationRequest) {
        Set<Long> eventIds = new HashSet<>(compilationRequest.getEvents());
        boolean exists = compilationRepository.existsByTitleAndSameEvents(compilationRequest.getTitle(),
                eventIds,
                eventIds.size());

        if (exists) {
            throw new AlreadyExistsException("Подборка с таким же заголовком и событиями уже существует");
        }

        Set<Event> events = new HashSet<>(eventRepository.findAllById(eventIds));

        Compilation compilation = compilationRepository.save(compilationMapper.toCompilation(compilationRequest));
        compilation.setEvents(events);
        return compilationMapper.toCompilationResponse(compilation);
    }

    @Override
    @Transactional
    public CompilationResponse update(UpdateCompilationRequest compilationRequest, Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(String.format("Подборка с id=%s не найдена", compId)));

        if (compilationRequest.getPinned() != null && !(compilationRequest.getPinned()).equals(compilation.getPinned())) {
            compilation.setPinned(compilationRequest.getPinned());
        }
        if (compilationRequest.getTitle() != null && !(compilationRequest.getTitle()).equals(compilation.getTitle())) {
            compilation.setTitle(compilationRequest.getTitle());
        }

        if (compilationRequest.getEvents() != null) {
            Set<Long> requestEventIds = compilationRequest.getEvents();
            Set<Long> currentEventIds = compilation.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());

            if (!requestEventIds.equals(currentEventIds)) {
                List<Event> foundEvents = eventRepository.findAllById(requestEventIds);
                Set<Long> foundEventIds = foundEvents.stream()
                        .map(Event::getId)
                        .collect(Collectors.toSet());
                if (!foundEventIds.equals(requestEventIds)) {
                    throw new NotFoundException("Некоторые события не найдены");
                }
                compilation.setEvents(new HashSet<>(foundEvents));
            }

        }

        return compilationMapper.toCompilationResponse(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(String.format("Подборка с id=%s не найдена", compId)));
        compilationRepository.delete(compilation);
    }

    @Override
    public CompilationResponse findById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(String.format("Подборка с id=%s не найдена", compId)));
        return compilationMapper.toCompilationResponse(compilation);
    }

    @Override
    public Collection<CompilationResponse> findNeededCompilation(GetCompilationListDto getCompilationListDto) {
        int from = getCompilationListDto.getFrom();
        int size = getCompilationListDto.getSize();
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return compilationRepository.findNeededCompilations(getCompilationListDto.getPinned(), pageable)
                .stream()
                .map(compilationMapper::toCompilationResponse)
                .collect(Collectors.toList());
    }
}