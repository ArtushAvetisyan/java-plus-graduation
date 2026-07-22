package ru.yandex.practicum.core.request.service;


import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.interaction.dto.requests.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface ParticipationRequestService {

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getCurrentUserRequests(Long userId);

    List<ParticipationRequestDto> getRequestsByEvent(Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(Long eventId, Integer participantLimit,
                                                       Boolean requestModeration,
                                                       EventRequestStatusUpdateRequest updateRequest);

    Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds);

    Long getConfirmedRequestsCount(Long eventId);

    Boolean existsByRequesterIdAndEventIdAndStatusConfirmed(Long userId, Long eventId);
}