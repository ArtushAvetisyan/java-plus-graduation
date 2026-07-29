package ru.yandex.practicum.stats.analyzer.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.proto.dashboard.RecommendedEventProto;
import ru.yandex.practicum.stats.analyzer.model.EventSimilarity;
import ru.yandex.practicum.stats.analyzer.model.UserAction;
import ru.yandex.practicum.stats.analyzer.repository.EventSimilarityRepository;
import ru.yandex.practicum.stats.analyzer.repository.UserActionRepository;
import ru.yandex.practicum.stats.analyzer.repository.projections.EventWeightSumProjection;
import ru.yandex.practicum.stats.analyzer.repository.projections.SimilarEventProjection;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {
    private static final int DEFAULT_K_NEAREST_NEIGHBORS = 10;

    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        List<UserAction> allUserActions = userActionRepository.findByUserId(userId);
        if (allUserActions.isEmpty()) return Collections.emptyList();

        Map<Long, Double> userRatings = allUserActions.stream()
                .collect(Collectors.toMap(
                        UserAction::getEventId,
                        UserAction::getMaxWeight,
                        Math::max));

        Set<Long> allInteractedEventIds = userRatings.keySet();

        List<UserAction> recentActions = allUserActions.stream()
                .sorted(Comparator.comparing(UserAction::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(maxResults)
                .toList();

        Set<Long> recentEventIds = recentActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        List<EventSimilarity> recentSimilarities = eventSimilarityRepository.findAllByEventIds(recentEventIds);

        Map<Long, Double> candidateMaxSimilarity = new HashMap<>();
        for (EventSimilarity sim : recentSimilarities) {
            long a = sim.getEventA();
            long b = sim.getEventB();

            boolean aIsRecent = recentEventIds.contains(a);
            boolean bIsRecent = recentEventIds.contains(b);

            if (aIsRecent && !allInteractedEventIds.contains(b))
                candidateMaxSimilarity.merge(b, sim.getScore(), Math::max);

            if (bIsRecent && !allInteractedEventIds.contains(a))
                candidateMaxSimilarity.merge(a, sim.getScore(), Math::max);
        }

        List<Long> topCandidates = candidateMaxSimilarity.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .toList();

        if (topCandidates.isEmpty()) return Collections.emptyList();

        List<EventSimilarity> allSimilarities = eventSimilarityRepository.findAllByEventIds(topCandidates);
        Set<Long> topCandidatesSet = new HashSet<>(topCandidates);
        Map<Long, List<EventSimilarity>> similaritiesByCandidate = new HashMap<>();

        for (EventSimilarity sim : allSimilarities) {
            if (topCandidatesSet.contains(sim.getEventA()))
                similaritiesByCandidate.computeIfAbsent(sim.getEventA(), k -> new ArrayList<>()).add(sim);

            if (topCandidatesSet.contains(sim.getEventB()))
                similaritiesByCandidate.computeIfAbsent(sim.getEventB(), k -> new ArrayList<>()).add(sim);
        }

        List<RecommendedEventProto> recommendations = new ArrayList<>();

        for (Long candidateId : topCandidates) {
            List<EventSimilarity> candidateSimilarities = similaritiesByCandidate.getOrDefault(candidateId, Collections.emptyList());

            List<EventSimilarity> topKNeighbors = candidateSimilarities.stream()
                    .filter(sim -> {
                        long otherId = (Objects.equals(sim.getEventA(), candidateId)) ? sim.getEventB() : sim.getEventA();
                        return allInteractedEventIds.contains(otherId);
                    })
                    .sorted((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()))
                    .limit(DEFAULT_K_NEAREST_NEIGHBORS)
                    .toList();

            double weightedSum = 0.0;
            double similaritySum = 0.0;

            for (EventSimilarity sim : topKNeighbors) {
                long neighborId = (Objects.equals(sim.getEventA(), candidateId)) ? sim.getEventB() : sim.getEventA();
                double simScore = sim.getScore();
                double userRating = userRatings.get(neighborId);

                weightedSum += simScore * userRating;
                similaritySum += simScore;
            }

            if (similaritySum > 0) {
                double predictedRating = weightedSum / similaritySum;
                recommendations.add(RecommendedEventProto.newBuilder()
                        .setEventId(candidateId)
                        .setScore(predictedRating)
                        .build());
            }
        }

        return recommendations.stream()
                .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        List<SimilarEventProjection> similarEvents = eventSimilarityRepository.findSimilarEventsForUser(
                eventId,
                userId,
                Limit.of(maxResults));

        return similarEvents.stream()
                .map(similarEventProjection -> RecommendedEventProto.newBuilder()
                        .setEventId(similarEventProjection.getEventId())
                        .setScore(similarEventProjection.getScore())
                        .build())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Collections.emptyList();

        Map<Long, Double> weightSums = userActionRepository.sumWeightsByEventIds(eventIds).stream()
                .collect(Collectors.toMap(
                        EventWeightSumProjection::getEventId,
                        EventWeightSumProjection::getTotalWeight
                ));

        return eventIds.stream()
                .map(eventId -> RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(weightSums.getOrDefault(eventId, 0.0))
                        .build())
                .toList();
    }
}