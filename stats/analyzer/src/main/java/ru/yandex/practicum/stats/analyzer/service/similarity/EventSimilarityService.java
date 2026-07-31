package ru.yandex.practicum.stats.analyzer.service.similarity;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventSimilarityService {
    void processSimilarity(EventSimilarityAvro eventSimilarity);
}