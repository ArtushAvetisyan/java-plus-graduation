package ru.yandex.practicum.core.rating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.rating.service.EventReactionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
@Validated
public class InternalRatingController {

    private final EventReactionService eventReactionService;

    @PostMapping("/ratings")
    public Map<Long, Long> getRatingsForEvents(@Valid @RequestBody List<Long> eventIds) {
        return eventReactionService.getRatingsForEvents(eventIds);
    }

    @GetMapping("/{eventId}/ratings")
    public Long getRatingsForEvent(@PathVariable("eventId") Long eventId) {
        return eventReactionService.getRatingsForEvent(eventId);
    }

    @GetMapping("/users/{userId}/favorites")
    public List<Long> getFavoriteEventIds(@PathVariable("userId") Long userId) {
        return eventReactionService.getFavoriteEventIds(userId);
    }

    @GetMapping("/top")
    public List<Long> getTopEventIds(@RequestParam(name = "limit") Integer limit,
                                     @RequestParam(name = "order") String order) {
        return eventReactionService.getTopEventIds(limit, order);
    }
}