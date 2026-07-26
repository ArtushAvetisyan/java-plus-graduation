package ru.yandex.practicum.stats.analyzer.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.dashboard.RecommendedEventProto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        return List.of();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        return List.of();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        return List.of();
    }
}