package ru.yandex.practicum.stats.analyzer.repository.projections;

public interface SimilarEventProjection {
    Long getEventId();

    Double getScore();
}