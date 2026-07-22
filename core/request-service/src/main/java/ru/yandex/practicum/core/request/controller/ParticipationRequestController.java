package ru.yandex.practicum.core.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.interaction.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.core.request.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class ParticipationRequestController {

    private final ParticipationRequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable("userId") Long userId,
                                                           @RequestParam("eventId") Long eventId) {
        return requestService.addParticipationRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelParticipationRequest(@PathVariable("userId") Long userId,
                                                              @PathVariable("requestId") Long requestId) {
        return requestService.cancelParticipationRequest(userId, requestId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getCurrentUserRequests(@PathVariable("userId") Long userId) {
        return requestService.getCurrentUserRequests(userId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable("eventId") Long eventId) {
        return requestService.getRequestsByEvent(eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable("eventId") Long eventId,
                                                              @RequestParam("participantLimit") Integer participantLimit,
                                                              @RequestParam("requestModeration") Boolean requestModeration,
                                                              @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return requestService.updateRequestStatus(eventId, participantLimit, requestModeration, updateRequest);
    }
}