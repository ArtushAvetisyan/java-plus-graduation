package ru.yandex.practicum.stats.analyzer.service.recommendation;

import ru.practicum.ewm.stats.proto.dashboard.RecommendedEventProto;

import java.util.List;

public interface RecommendationService {
    List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults);

    List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults);

    List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds);
}