package ru.yandex.practicum.core.event.service.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatsViewDto;
import ru.yandex.practicum.core.event.mapper.EventMapper;
import ru.yandex.practicum.core.event.model.Category;
import ru.yandex.practicum.core.event.model.Event;
import ru.yandex.practicum.core.event.repository.CategoryRepository;
import ru.yandex.practicum.core.event.repository.EventRepository;
import ru.yandex.practicum.core.event.repository.specification.EventSpecification;
import ru.yandex.practicum.core.interaction.client.RatingClient;
import ru.yandex.practicum.core.interaction.client.RequestEventClient;
import ru.yandex.practicum.core.interaction.client.UserClient;
import ru.yandex.practicum.core.interaction.dto.event.*;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.core.interaction.dto.requests.EventRequestStatusUpdateResult;
import ru.yandex.practicum.core.interaction.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;
import ru.yandex.practicum.core.interaction.handler.exception.BadRequestException;
import ru.yandex.practicum.core.interaction.handler.exception.ConflictException;
import ru.yandex.practicum.core.interaction.handler.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.yandex.practicum.core.interaction.dto.event.EventState.PUBLISHED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final int MIN_HOURS_BEFORE_EVENT = 2;
    private static final int DEFAULT_STATS_RANGE_MONTHS = 6;

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final RequestEventClient requestClient;
    private final RatingClient ratingClient;
    private final UserClient userClient;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        userClient.getUserById(userId);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        return enrichShortDto(events);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        userClient.getUserById(userId);

        Event event = getEventByIdOrThrow(eventId);
        if (!event.getInitiatorId().equals(userId))
            throw new NotFoundException("Пользователь не является инициатором этого события");

        return requestClient.getRequestsByEvent(eventId);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        UserShortDto initiator = userClient.getUserById(userId);

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new BadRequestException(
                    String.format("Дата и время начала события не могут быть раньше, чем через %d часа от текущего момента",
                            MIN_HOURS_BEFORE_EVENT)
            );
        }

        Category category = getCategoryByIdOrThrow(newEventDto.getCategory());
        Event newEvent = eventMapper.toEvent(newEventDto, userId);

        newEvent.setCategory(category);

        if (newEvent.getPaid() == null) newEvent.setPaid(false);
        if (newEvent.getParticipantLimit() == null) newEvent.setParticipantLimit(0);
        if (newEvent.getRequestModeration() == null) newEvent.setRequestModeration(true);
        newEvent.setEventState(EventState.PENDING);
        Event createdEvent = eventRepository.save(newEvent);
        return eventMapper.toEventFullDto(
                createdEvent,
                initiator,
                0L,
                0L,
                0L);
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        UserShortDto initiator = userClient.getUserById(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id - " + eventId + " не найдено"));

        return enrichFullDto(List.of(event)).getFirst();
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        UserShortDto initiator = userClient.getUserById(userId);

        Event event = getEventByIdOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Пользователь не является инициатором этого события");
        }

        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
                throw new BadRequestException(
                        String.format("Дата и время начала события не могут быть раньше, чем через %d часа от текущего момента",
                                MIN_HOURS_BEFORE_EVENT)
                );
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (event.getEventState().equals(PUBLISHED)) {
            throw new ConflictException("Событие уже опубликовано");
        }

        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setEventState(EventState.PENDING);
            } else if (updateEventUserRequest.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                event.setEventState(EventState.CANCELED);
            }
        }

        if (updateEventUserRequest.getAnnotation() != null)
            event.setAnnotation(updateEventUserRequest.getAnnotation());

        if (updateEventUserRequest.getCategory() != null)
            event.setCategory(getCategoryByIdOrThrow(updateEventUserRequest.getCategory()));

        if (updateEventUserRequest.getDescription() != null)
            event.setDescription(updateEventUserRequest.getDescription());

        if (updateEventUserRequest.getLocation() != null)
            event.setLocation(eventMapper.toLocation(updateEventUserRequest.getLocation()));

        if (updateEventUserRequest.getPaid() != null)
            event.setPaid(updateEventUserRequest.getPaid());

        if (updateEventUserRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());

        if (updateEventUserRequest.getRequestModeration() != null)
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());

        if (updateEventUserRequest.getTitle() != null)
            event.setTitle(updateEventUserRequest.getTitle());

        Event savedEvent = eventRepository.save(event);

        return enrichFullDto(List.of(savedEvent)).getFirst();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        userClient.getUserById(userId);
        Event event = getEventByIdOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("Пользователь не является инициатором этого события");
        }

        return requestClient.updateRequestStatus(
                eventId,
                event.getParticipantLimit(),
                event.getRequestModeration(),
                updateRequest
        );
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(EventSearchFilterAdmin filter,
                                               Integer from,
                                               Integer size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Event> specification = EventSpecification.byAdminFilter(filter);

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();
        return enrichFullDto(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventByIdOrThrow(eventId);

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
                throw new BadRequestException(
                        String.format("Дата и время начала события не могут быть раньше, чем через %d часа от текущего момента",
                                MIN_HOURS_BEFORE_EVENT)
                );
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            if (request.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                if (!event.getEventState().equals(EventState.PENDING)) {
                    throw new ConflictException("Можно публиковать только события в состоянии ожидания публикации");
                }
                event.setEventState(PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                if (event.getEventState().equals(PUBLISHED)) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                event.setEventState(EventState.CANCELED);
            }
        }

        if (request.getAnnotation() != null)
            event.setAnnotation(request.getAnnotation());

        if (request.getCategory() != null)
            event.setCategory(getCategoryByIdOrThrow(request.getCategory()));

        if (request.getDescription() != null)
            event.setDescription(request.getDescription());

        if (request.getLocation() != null)
            event.setLocation(eventMapper.toLocation(request.getLocation()));

        if (request.getPaid() != null)
            event.setPaid(request.getPaid());

        if (request.getParticipantLimit() != null)
            event.setParticipantLimit(request.getParticipantLimit());

        if (request.getRequestModeration() != null)
            event.setRequestModeration(request.getRequestModeration());

        if (request.getTitle() != null)
            event.setTitle(request.getTitle());

        log.info("Событие с id - {} обновлено администратором", eventId);

        Event savedEvent = eventRepository.save(event);
        return enrichFullDto(List.of(savedEvent)).getFirst();
    }

    @Override
    public List<EventShortDto> getEventsPublic(EventSearchFilterPublic filter,
                                               Integer from,
                                               Integer size,
                                               HttpServletRequest request) {

        statsClient.hit(new HitRequestDto("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now()));

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Event> specification = EventSpecification.byPublicFilter(filter);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        return enrichShortDto(events);
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {
        Event event = getEventByIdOrThrow(eventId);

        if (!event.getEventState().equals(PUBLISHED)) {
            throw new NotFoundException("Событие с id - " + eventId + " не найдено");
        }

        statsClient.hit(new HitRequestDto("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now()));

        return enrichFullDto(List.of(event)).getFirst();
    }

    @Override
    public List<EventFullDto> getFavoriteEvents(Long userId) {
        userClient.getUserById(userId);

        List<Long> favoriteEventIds;
        try {
            favoriteEventIds = ratingClient.getFavoriteEventIds(userId);
        } catch (Exception exception) {
            log.error("Ошибка при получении избранных событий из rating-service: {}", exception.getMessage());
            return Collections.emptyList();
        }

        if (favoriteEventIds == null || favoriteEventIds.isEmpty()) return Collections.emptyList();
        List<Event> favoriteEvents = eventRepository.findAllById(favoriteEventIds);
        if (favoriteEvents.isEmpty()) return Collections.emptyList();

        return enrichFullDto(favoriteEvents);
    }

    @Override
    public List<EventShortDto> getTopEventsByRating(Integer limit, String order) {
        List<Long> topEventIds;
        try {
            topEventIds = ratingClient.getTopEventIds(limit, order);
        } catch (Exception exception) {
            log.error("Ошибка при получении топ-событий из rating-service: {}", exception.getMessage());
            return Collections.emptyList();
        }

        if (topEventIds == null || topEventIds.isEmpty()) return Collections.emptyList();
        List<Event> events = eventRepository.findAllById(topEventIds);
        if (events.isEmpty()) return Collections.emptyList();

        Map<Long, Event> eventMap = events.stream().collect(Collectors.toMap(Event::getId, Function.identity()));
        List<Event> sortedEvents = topEventIds.stream()
                .map(eventMap::get)
                .filter(Objects::nonNull)
                .toList();
        return enrichShortDto(sortedEvents);
    }

    @Override
    public List<EventFullDto> getEventsByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyList();

        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.isEmpty()) return Collections.emptyList();

        return enrichFullDto(events);
    }

    @Override
    public List<EventShortDto> getShortEventsByIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyList();

        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.isEmpty()) return Collections.emptyList();

        return enrichShortDto(events);
    }

    @Override
    public List<EventShortDto> getShortEventsByInitiatorIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyList();

        List<Event> events = eventRepository.findAllByInitiatorIdIn(userIds);
        if (events.isEmpty()) return Collections.emptyList();

        return enrichShortDto(events);
    }

    private Map<Long, Long> getViewsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        LocalDateTime earliestDateTime = eventRepository.findEarliestCreatedOnByEventIds(eventIds)
                .orElse(LocalDateTime.now().minusMonths(DEFAULT_STATS_RANGE_MONTHS));

        String start = earliestDateTime.format(FORMATTER);
        String end = LocalDateTime.now().format(FORMATTER);


        try {
            List<StatsViewDto> stats = statsClient.getStats(start, end, uris, true);

            if (stats == null || stats.isEmpty()) return Collections.emptyMap();

            return stats.stream()
                    .collect(Collectors.toMap(
                            statsDto -> {
                                String uri = statsDto.getUri();
                                return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                            },
                            StatsViewDto::getHits,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.warn("Сервис статистики недоступен. Возвращаю пустой список");
            return Collections.emptyMap();
        }
    }

    private List<EventFullDto> enrichFullDto(List<Event> events) {
        if (events.isEmpty()) return Collections.emptyList();

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Long> ratingsMap = getRatingsMap(eventIds);

        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).toList();
        Map<Long, UserShortDto> initiatorsMap = getUsersMap(initiatorIds);

        return events.stream()
                .map(event -> {
                    UserShortDto initiator = initiatorsMap.get(event.getInitiatorId());
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    Long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
                    Long rating = ratingsMap.getOrDefault(event.getId(), 0L);

                    return eventMapper.toEventFullDto(event, initiator, confirmedRequests, views, rating);
                })
                .toList();
    }

    private List<EventShortDto> enrichShortDto(List<Event> events) {
        if (events == null || events.isEmpty()) return Collections.emptyList();

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);
        Map<Long, Long> ratingsMap = getRatingsMap(eventIds);

        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).toList();
        Map<Long, UserShortDto> initiatorsMap = getUsersMap(initiatorIds);

        return events.stream()
                .map(event -> {
                    UserShortDto initiator = initiatorsMap.get(event.getInitiatorId());
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    Long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
                    Long rating = ratingsMap.getOrDefault(event.getId(), 0L);

                    return eventMapper.toEventShortDto(event, initiator, confirmedRequests, views, rating);
                })
                .toList();
    }

    private Category getCategoryByIdOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(
                        () -> new NotFoundException("Категория с id - " + categoryId + " не найдена")
                );
    }

    private Event getEventByIdOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(
                        () -> new NotFoundException("Событие с id - " + eventId + " не найдено")
                );
    }

    private Map<Long, UserShortDto> getUsersMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();
        try {
            return userClient.getUsersByIds(userIds).stream()
                    .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
        } catch (Exception exception) {
            log.error("Ошибка при получении пользователей из user-service: {}", exception.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();
        try {
            return requestClient.getConfirmedRequestsCounts(eventIds);
        } catch (Exception exception) {
            log.error("Ошибка при получении заявок из request-service: {}", exception.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, Long> getRatingsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();
        try {
            return ratingClient.getRatingsForEvents(eventIds);
        } catch (Exception exception) {
            log.error("Ошибка при получении рейтингов из rating-service: {}", exception.getMessage());
            return Collections.emptyMap();
        }
    }
}