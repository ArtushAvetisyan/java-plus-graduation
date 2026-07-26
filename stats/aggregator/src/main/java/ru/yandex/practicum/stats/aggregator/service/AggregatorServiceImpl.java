package ru.yandex.practicum.stats.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.stats.aggregator.mapper.ActionWeightMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {

    private final Map<Long, Map<Long, Double>> eventUserWeights = new ConcurrentHashMap<>();
    private final Map<Long, Double> eventWeightsSums = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    @Override
    public List<EventSimilarityAvro> processUserAction(UserActionAvro userAction) {
        long userId = userAction.getUserId();
        long eventA = userAction.getEventId();
        double newWeight = ActionWeightMapper.getWeight(userAction.getActionType());

        Map<Long, Double> usersForEventA = eventUserWeights.computeIfAbsent(eventA, k -> new ConcurrentHashMap<>());
        double oldWeight = usersForEventA.getOrDefault(userId, 0.0);

        if (newWeight <= oldWeight) return Collections.emptyList();

        double deltaWeight = newWeight - oldWeight;
        usersForEventA.put(userId, newWeight);

        double sumA = eventWeightsSums.getOrDefault(eventA, 0.0) + deltaWeight;
        eventWeightsSums.put(eventA, sumA);

        List<EventSimilarityAvro> result = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : eventUserWeights.entrySet()) {
            long eventB = entry.getKey();
            if (eventB == eventA) continue;

            Map<Long, Double> usersForEventB = entry.getValue();
            Double weightB = usersForEventB.get(userId);
            if (weightB == null) continue;

            double oldMin = Math.min(oldWeight, weightB);
            double newMin = Math.min(newWeight, weightB);
            double deltaMin = newMin - oldMin;

            double currentSmin = getMinWeightSum(eventA, eventB);
            double newSmin = currentSmin + deltaMin;

            if (deltaMin > 0.0) putMinWeightSum(eventA, eventB, newSmin);

            double sumB = eventWeightsSums.getOrDefault(eventB, 0.0);
            double similarity = 0.0;
            if (sumA > 0 && sumB > 0) similarity = newSmin / (sumA * sumB);

            long first = Math.min(eventA, eventB);
            long second = Math.max(eventA, eventB);

            EventSimilarityAvro simAvro = EventSimilarityAvro.newBuilder()
                    .setEventA(first)
                    .setEventB(second)
                    .setScore(similarity)
                    .setTimestamp(userAction.getTimestamp())
                    .build();

            result.add(simAvro);
        }
        return result;
    }

    private void putMinWeightSum(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .put(second, sum);
    }

    private double getMinWeightSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .getOrDefault(second, 0.0);
    }
}