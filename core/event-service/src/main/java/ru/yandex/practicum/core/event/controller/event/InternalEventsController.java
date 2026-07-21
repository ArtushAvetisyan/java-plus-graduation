package ru.yandex.practicum.core.event.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.core.event.service.event.EventService;
import ru.yandex.practicum.core.interaction.dto.event.EventFullDto;
import ru.yandex.practicum.core.interaction.dto.event.EventShortDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Validated
public class InternalEventsController {

    private final EventService eventService;

    @PostMapping("/internal/events")
    public List<EventFullDto> getFullEventsByEventIds(@Valid @RequestBody @NotEmpty List<Long> eventIds) {
        return eventService.getEventsByIds(eventIds);
    }

    @PostMapping("/internal/events/short")
    public List<EventShortDto> getShortEventsByEventIds(@Valid @RequestBody @NotEmpty List<Long> eventIds) {
        return eventService.getShortEventsByIds(eventIds);
    }

    @PostMapping("/internal/events/short/by-initiator")
    public List<EventShortDto> getShortEventsByInitiatorIds(@Valid @RequestBody @NotEmpty List<Long> userIds) {
        return eventService.getShortEventsByInitiatorIds(userIds);
    }
}