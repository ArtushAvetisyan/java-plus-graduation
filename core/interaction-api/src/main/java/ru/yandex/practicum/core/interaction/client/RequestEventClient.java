package ru.yandex.practicum.core.interaction.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.interaction.dto.requests.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service")
public interface RequestEventClient {

    // ParticipationRequest API
    @GetMapping("/users/events/{eventId}/requests")
    List<ParticipationRequestDto> getRequestsByEvent(@PathVariable("eventId") Long eventId);

    @PatchMapping("/users/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateRequestStatus(@PathVariable("eventId") Long eventId,
                                                       @RequestParam("participantLimit") Integer participantLimit,
                                                       @RequestParam("requestModeration") Boolean requestModeration,
                                                       @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest);

    //Internal API
    @PostMapping("/internal/events/requests/count")
    Map<Long, Long> getConfirmedRequestsCounts(@Valid @RequestBody @NotEmpty() List<Long> eventIds);

    @GetMapping("/internal/events/{eventId}/requests/count")
    Long getConfirmedRequestsCount(@PathVariable("eventId") Long eventId);

    @GetMapping("/internal/events/{eventId}/requests/{userId}")
    Boolean existsByRequesterIdAndEventIdAndStatusConfirmed(@PathVariable("userId") Long userId,
                                                            @PathVariable("eventId") Long eventId);
}