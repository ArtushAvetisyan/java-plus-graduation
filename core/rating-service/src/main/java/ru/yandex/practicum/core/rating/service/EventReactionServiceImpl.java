package ru.yandex.practicum.core.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.interaction.client.EventClient;
import ru.yandex.practicum.core.interaction.client.RequestEventClient;
import ru.yandex.practicum.core.interaction.client.UserClient;
import ru.yandex.practicum.core.interaction.dto.event.EventFullDto;
import ru.yandex.practicum.core.interaction.dto.event.EventShortDto;
import ru.yandex.practicum.core.interaction.dto.reaction.EventReactionDto;
import ru.yandex.practicum.core.interaction.dto.reaction.ReactionType;
import ru.yandex.practicum.core.interaction.dto.users.UserRatingStatsDto;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;
import ru.yandex.practicum.core.interaction.handler.exception.BadRequestException;
import ru.yandex.practicum.core.interaction.handler.exception.ConflictException;
import ru.yandex.practicum.core.interaction.handler.exception.NotFoundException;
import ru.yandex.practicum.core.rating.mapper.ReactionMapper;
import ru.yandex.practicum.core.rating.model.EventReaction;
import ru.yandex.practicum.core.rating.repository.EventReactionRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventReactionServiceImpl implements EventReactionService {

    private final EventReactionRepository reactionRepository;
    private final RequestEventClient requestClient;
    private final ReactionMapper reactionMapper;
    private final EventClient eventClient;
    private final UserClient userClient;

    @Override
    public List<UserShortDto> getUsersByReaction(List<Long> eventIds, ReactionType reactionType, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Long> reactorsIds = reactionRepository.findReactorIdsByEventIdAndReactionType(eventIds, reactionType, pageable);
        if (reactorsIds == null || reactorsIds.isEmpty()) return Collections.emptyList();

        return userClient.getUsersByIds(reactorsIds);
    }

    @Override
    public List<UserRatingStatsDto> getUsersRatingStats(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyList();

        List<EventShortDto> userEvents = eventClient.getShortEventsByInitiatorIds(userIds);
        if (userEvents == null || userEvents.isEmpty()) {
            return userIds.stream()
                    .map(id -> new UserRatingStatsDto(id, 0L, 0L))
                    .toList();
        }

        Map<Long, List<Long>> userEventIdsMap = userEvents.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getInitiator().getId(),
                        Collectors.mapping(EventShortDto::getId, Collectors.toList())
                ));

        List<Long> allEventIds = userEvents.stream().map(EventShortDto::getId).toList();
        List<EventReaction> reactions = reactionRepository.getRatingsByEventIds(allEventIds);

        Map<Long, Long> likesMap = new HashMap<>();
        Map<Long, Long> dislikesMap = new HashMap<>();

        for (EventReaction reaction : reactions) {
            if (reaction.getReactionType() == ReactionType.LIKE) {
                likesMap.merge(reaction.getEventId(), 1L, Long::sum);
            } else if (reaction.getReactionType() == ReactionType.DISLIKE) {
                dislikesMap.merge(reaction.getEventId(), 1L, Long::sum);
            }
        }

        List<UserRatingStatsDto> result = new ArrayList<>();
        for (Long userId : userIds) {
            List<Long> eventIds = userEventIdsMap.getOrDefault(userId, Collections.emptyList());
            long likes = eventIds.stream().mapToLong(id -> likesMap.getOrDefault(id, 0L)).sum();
            long dislikes = eventIds.stream().mapToLong(id -> dislikesMap.getOrDefault(id, 0L)).sum();
            result.add(new UserRatingStatsDto(userId, likes, dislikes));
        }
        return result;
    }

    @Override
    @Transactional
    public EventReactionDto addReaction(Long userId, Long eventId, ReactionType reactionType) {
        userClient.getUserById(userId);
        validateUserParticipant(userId, eventId);

        Optional<EventReaction> existingReaction = reactionRepository.findByReactorIdAndEventId(userId, eventId);

        EventReaction reaction;
        if (existingReaction.isPresent()) {
            reaction = existingReaction.get();

            if (reaction.getReactionType().equals(reactionType)) {
                throw new ConflictException("Вы уже оставили такую реакцию на это событие");
            }

            reaction.setReactionType(reactionType);
            reaction.setUpdatedAt(LocalDateTime.now());
        } else {
            EventReaction newReaction = reactionMapper.toReaction(userId, eventId, reactionType);
            reaction = reactionRepository.save(newReaction);
        }
        return reactionMapper.toReactionDto(reaction);
    }

    @Override
    @Transactional
    public void deleteReaction(Long userId, Long eventId, ReactionType reactionType) {
        userClient.getUserById(userId);

        EventReaction reaction = reactionRepository.findByReactorIdAndEventId(userId, eventId)
                .filter(r -> r.getReactionType().equals(reactionType))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Реакция с типом %s не найдена для данного события", reactionType)));

        reactionRepository.delete(reaction);
    }

    @Override
    public List<EventFullDto> getFavoriteEvents(Long userId) {
        userClient.getUserById(userId);

        List<Long> favoriteIds = reactionRepository.findFavoriteEventIdsByUserId(userId);
        if (favoriteIds == null || favoriteIds.isEmpty()) return Collections.emptyList();

        return eventClient.getFullEventsByEventIds(favoriteIds);
    }

    @Override
    public List<EventShortDto> getTopEventsByRating(String sort, Integer size) {
        List<Long> topIds = getTopEventIds(size, sort);
        if (topIds.isEmpty()) return Collections.emptyList();

        List<EventShortDto> events = eventClient.getShortEventsByEventIds(topIds);
        Map<Long, EventShortDto> eventMap = events.stream()
                .collect(Collectors.toMap(EventShortDto::getId, e -> e));

        return topIds.stream().map(eventMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Map<Long, Long> getRatingsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();

        List<EventReaction> results = reactionRepository.getRatingsByEventIds(eventIds);
        Map<Long, Long> ratingsMap = results.stream()
                .collect(Collectors.toMap(
                        EventReaction::getEventId,
                        er -> er.getReactionType() == ReactionType.LIKE ? 1L : -1L,
                        Long::sum
                ));

        for (Long eventId : eventIds) ratingsMap.putIfAbsent(eventId, 0L);

        return ratingsMap;
    }

    @Override
    public Long getRatingsForEvent(Long eventId) {
        Long rating = reactionRepository.getRatingByEventId(eventId);
        return rating != null ? rating : 0L;
    }

    @Override
    public List<Long> getFavoriteEventIds(Long userId) {
        userClient.getUserById(userId);
        return reactionRepository.findFavoriteEventIdsByUserId(userId);
    }

    @Override
    public List<Long> getTopEventIds(Integer limit, String order) {
        Pageable pageable = PageRequest.of(0, limit);
        if ("ASC".equalsIgnoreCase(order)) return reactionRepository.findTopEventIdsAsc(pageable);
        return reactionRepository.findTopEventIdsDesc(pageable);
    }

    private void validateUserParticipant(Long userId, Long eventId) {
        boolean isParticipant = requestClient.existsByRequesterIdAndEventIdAndStatusConfirmed(userId, eventId);

        if (!isParticipant) {
            throw new BadRequestException("Только подтвержденные участники могут оставлять реакции к событиям");
        }
    }
}