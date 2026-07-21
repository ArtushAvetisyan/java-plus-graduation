package ru.yandex.practicum.core.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.interaction.client.EventClient;
import ru.yandex.practicum.core.interaction.client.UserClient;
import ru.yandex.practicum.core.interaction.dto.event.EventFullDto;
import ru.yandex.practicum.core.interaction.dto.event.EventState;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.interaction.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.core.interaction.dto.requests.RequestStatus;
import ru.yandex.practicum.core.interaction.handler.exception.ConflictException;
import ru.yandex.practicum.core.interaction.handler.exception.NotFoundException;
import ru.yandex.practicum.core.request.mapper.ParticipationRequestMapper;
import ru.yandex.practicum.core.request.model.ParticipationRequest;
import ru.yandex.practicum.core.request.repository.ParticipationRequestRepository;
import ru.yandex.practicum.core.request.repository.RequestCountProjection;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestMapper requestMapper;
    private final EventClient eventClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        userClient.getUserById(userId);

        List<EventFullDto> events = eventClient.getFullEventsByEventIds(List.of(eventId));
        if (events.isEmpty()) {
            throw new NotFoundException("Событие с id - " + eventId + " не найдено");
        }
        EventFullDto event = events.getFirst();

        validateRequest(event, userId);

        ParticipationRequest request = requestMapper.toRequest(userId, eventId);

        if (event.getParticipantLimit() == 0 || Boolean.FALSE.equals(event.getRequestModeration())) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        userClient.getUserById(userId);
        ParticipationRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с ID - " + requestId + " не найден"));

        if (!Objects.equals(request.getRequesterId(), userId)) {
            throw new ConflictException("Только создатель заявки может отменить запрос");
        }

        if (RequestStatus.CONFIRMED.equals(request.getStatus())) {
            throw new ConflictException("Нельзя отменить уже подтверждённую заявку");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        return requestMapper.toRequestDto(request);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long eventId, Integer participantLimit, Boolean requestModeration,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        if (updateRequest.getRequestIds() == null || updateRequest.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        int limit = (participantLimit != null) ? participantLimit : 0;

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());
        for (ParticipationRequest request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
            }
        }

        List<ParticipationRequest> confirmedList = new ArrayList<>();
        List<ParticipationRequest> rejectedList = new ArrayList<>();

        if (updateRequest.getStatus() == RequestStatus.REJECTED) {
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedList.add(request);
            }
        } else if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
            long currentConfirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

            if (limit > 0 && currentConfirmedCount >= limit) {
                throw new ConflictException("Лимит участников для данного события уже исчерпан");
            }

            for (ParticipationRequest request : requests) {
                if (limit == 0 || currentConfirmedCount < limit) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedList.add(request);
                    currentConfirmedCount++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedList.add(request);
                }
            }
        }

        requestRepository.saveAll(requests);
        return new EventRequestStatusUpdateResult(
                confirmedList.stream().map(requestMapper::toRequestDto).toList(),
                rejectedList.stream().map(requestMapper::toRequestDto).toList()
        );
    }

    @Override
    public List<ParticipationRequestDto> getCurrentUserRequests(Long userId) {
        userClient.getUserById(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long eventId) {
        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();

        List<RequestCountProjection> projections = requestRepository
                .countConfirmedRequestsByEventIds(eventIds, RequestStatus.CONFIRMED);

        return projections.stream().collect(Collectors.toMap(
                RequestCountProjection::getEventId,
                RequestCountProjection::getConfirmedCount,
                (existing, replacement) -> existing
        ));
    }

    @Override
    public Long getConfirmedRequestsCount(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public Boolean existsByRequesterIdAndEventIdAndStatusConfirmed(Long userId, Long eventId) {
        return requestRepository.existsByRequesterIdAndEventIdAndStatus(userId, eventId, RequestStatus.CONFIRMED);
    }

    private void validateRequest(EventFullDto event, Long userId) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, event.getId())) {
            throw new ConflictException("Заявка на участие в этом событии уже подана");
        }

        Integer eventParticipationLimit = event.getParticipantLimit();
        Long currentConfirmedRequests =
                requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        if (event.getInitiator() != null &&
                Objects.equals(userId, event.getInitiator().getId())) {
            throw new ConflictException(
                    "Инициатор события не может подать заявку на участие в собственном событии"
            );
        }

        if (!EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException(
                    "Нельзя участвовать в неопубликованном событии. Текущий статус: " + event.getState());
        }

        if (eventParticipationLimit != null && eventParticipationLimit > 0 &&
                currentConfirmedRequests >= eventParticipationLimit) {
            throw new ConflictException(
                    String.format("Лимит участников в событии исчерпан. Лимит: %d, Текущее количество: %d",
                            event.getParticipantLimit(), currentConfirmedRequests));
        }
    }
}