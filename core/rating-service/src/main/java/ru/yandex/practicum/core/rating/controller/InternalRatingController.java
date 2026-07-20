package ru.yandex.practicum.core.rating.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.rating.service.EventReactionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalRatingController {

    private final EventReactionService eventReactionService;

    @PostMapping("/ratings")
    Map<Long, Long> getRatingsForEvents(@RequestBody List<Long> eventIds) {
        return eventReactionService.getRatingsForEvents(eventIds);
    }

    @GetMapping("/{eventId}/ratings")
    Long getRatingsForEvent(@PathVariable("eventId") Long eventId) {
        return eventReactionService.getRatingsForEvent(eventId);
    }

    @GetMapping("/users/{userId}/favorites")
    List<Long> getFavoriteEventIds(@PathVariable("userId") Long userId) {
        return eventReactionService.getFavoriteEventIds(userId);
    }

    @GetMapping("/top")
    List<Long> getTopEventIds(@RequestParam("limit") Integer limit,
                              @RequestParam("order") String order) {
        return eventReactionService.getTopEventIds(limit, order);
    }
}