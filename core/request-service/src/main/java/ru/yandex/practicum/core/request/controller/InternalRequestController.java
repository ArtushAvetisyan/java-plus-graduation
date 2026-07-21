package ru.yandex.practicum.core.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.request.service.ParticipationRequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
@Validated
public class InternalRequestController {
    private final ParticipationRequestService requestService;

    @PostMapping("/requests/count")
    public Map<Long, Long> getConfirmedRequestsCounts(@Valid @RequestBody @NotEmpty List<Long> eventIds) {
        return requestService.getConfirmedRequestsCounts(eventIds);
    }

    @GetMapping("/{eventId}/requests/count")
    public Long getConfirmedRequestsCount(@PathVariable("eventId") Long eventId) {
        return requestService.getConfirmedRequestsCount(eventId);
    }

    @GetMapping("/{eventId}/requests/{userId}")
    public Boolean existsByRequesterIdAndEventIdAndStatusConfirmed(@PathVariable("userId") Long userId,
                                                                   @PathVariable("eventId") Long eventId) {
        return requestService.existsByRequesterIdAndEventIdAndStatusConfirmed(userId, eventId);
    }
}