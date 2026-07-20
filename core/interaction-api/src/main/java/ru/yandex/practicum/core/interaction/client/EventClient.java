package ru.yandex.practicum.core.interaction.client;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.core.interaction.dto.event.EventFullDto;
import ru.yandex.practicum.core.interaction.dto.event.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventClient {

    // Public API
    @GetMapping("/events/{id}")
    EventFullDto getEventPublicById(@PathVariable("id") Long id);

    // Internal API
    @PostMapping("/internal/events")
    List<EventFullDto> getFullEventsByEventIds(@RequestBody @NotEmpty List<Long> eventIds);

    @PostMapping("/internal/events/short")
    List<EventShortDto> getShortEventsByEventIds(@RequestBody @NotEmpty List<Long> eventIds);

    @PostMapping("/internal/events/short/by-initiator")
    List<EventShortDto> getShortEventsByInitiatorIds(@RequestBody @NotEmpty List<Long> userIds);
}