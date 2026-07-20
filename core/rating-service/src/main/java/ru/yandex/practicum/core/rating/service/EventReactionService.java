package ru.yandex.practicum.core.rating.service;


import ru.yandex.practicum.core.interaction.dto.event.EventFullDto;
import ru.yandex.practicum.core.interaction.dto.reaction.EventReactionDto;
import ru.yandex.practicum.core.interaction.dto.event.EventShortDto;
import ru.yandex.practicum.core.interaction.dto.reaction.ReactionType;
import ru.yandex.practicum.core.interaction.dto.users.UserRatingStatsDto;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;

import java.util.List;
import java.util.Map;

public interface EventReactionService {

    EventReactionDto addReaction(Long userId, Long eventId, ReactionType reactionType);

    void deleteReaction(Long userId, Long eventId, ReactionType reactionType);

    List<UserRatingStatsDto> getUsersRatingStats(List<Long> userIds);

    List<UserShortDto> getUsersByReaction(List<Long> eventIds, ReactionType reactionType, Integer from, Integer size);

    List<EventFullDto> getFavoriteEvents(Long userId);

    List<EventShortDto> getTopEventsByRating(String sort, Integer size);

    Map<Long, Long> getRatingsForEvents(List<Long> eventIds);

    Long getRatingsForEvent(Long eventId);

    List<Long> getFavoriteEventIds(Long userId);

    List<Long> getTopEventIds(Integer limit, String order);
}