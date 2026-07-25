package ru.yandex.practicum.core.event.controller.event;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.event.service.event.EventService;
import ru.yandex.practicum.core.interaction.dto.event.EventFullDto;
import ru.yandex.practicum.core.interaction.dto.event.EventSearchFilterPublic;
import ru.yandex.practicum.core.interaction.dto.event.EventShortDto;
import ru.yandex.practicum.core.interaction.dto.event.PublicEventSort;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Validated
public class PublicEventsController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsPublic(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "categories", required = false) List<Long> categories,
            @RequestParam(name = "users", required = false) List<Long> users,
            @RequestParam(name = "paid", required = false) Boolean paid,
            @RequestParam(name = "rangeStart", required = false) LocalDateTime rangeStart,
            @RequestParam(name = "rangeEnd", required = false) LocalDateTime rangeEnd,
            @RequestParam(name = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(name = "sort", required = false) PublicEventSort sort,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero Integer size) {
        EventSearchFilterPublic filter = new EventSearchFilterPublic(text, categories, users, paid, rangeStart,
                rangeEnd, onlyAvailable, sort);
        return eventService.getEventsPublic(filter, from, size);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventPublicById(@RequestHeader(value = "X-EWM-USER-ID", required = false) Long userId,
                                           @PathVariable("id") Long id) {
        return eventService.getPublicEventById(id, userId);
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getEventRecommendations(@RequestHeader(name = "X-EWM-USER-ID") Long userId,
                                                       @RequestParam(name = "size", required = false) Integer size) {
        return eventService.getEventRecommendations(userId, size);
    }

    @PutMapping("/{eventId}/like")
    public void addLike(@RequestHeader(name = "X-EWM-USER-ID") Long userId,
                        @PathVariable("eventId") Long eventId) {
        eventService.addLike(userId, eventId);
    }
}