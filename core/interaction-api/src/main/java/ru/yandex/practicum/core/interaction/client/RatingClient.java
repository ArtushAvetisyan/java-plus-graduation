package ru.yandex.practicum.core.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "rating-service")
public interface RatingClient {

    // Internal API
    @PostMapping("/internal/events/ratings")
    Map<Long, Long> getRatingsForEvents(@RequestBody List<Long> eventIds);

    @GetMapping("/internal/events/{eventId}/ratings")
    Long getRatingsForEvent(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/users/{userId}/favorites")
    List<Long> getFavoriteEventIds(@PathVariable("userId") Long userId);

    @GetMapping("/internal/events/top")
    List<Long> getTopEventIds(@RequestParam("limit") Integer limit,
                              @RequestParam("order") String order);
}