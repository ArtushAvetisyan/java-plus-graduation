package ru.yandex.practicum.core.event.service.event;

import ru.yandex.practicum.core.interaction.dto.event.*;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.interaction.dto.requests.ParticipationRequestDto;

import java.util.List;

public interface EventService {
    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);

    List<EventFullDto> getEventsByAdmin(EventSearchFilterAdmin filter,
                                        Integer from,
                                        Integer size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> getEventsPublic(EventSearchFilterPublic filter,
                                        Integer from,
                                        Integer size);

    EventFullDto getPublicEventById(Long eventId, Long userId);

    List<EventFullDto> getFavoriteEvents(Long userId);

    List<EventShortDto> getTopEventsByRating(Integer limit, String order);

    List<EventFullDto> getEventsByIds(List<Long> eventIds);

    List<EventShortDto> getShortEventsByIds(List<Long> eventIds);

    List<EventShortDto> getShortEventsByInitiatorIds(List<Long> userIds);

    List<EventShortDto> getEventRecommendations(Long userId, Integer size);

    void addLike(Long userId, Long eventId);
}