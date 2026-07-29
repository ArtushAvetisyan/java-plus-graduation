package ru.yandex.practicum.stats.analyzer.repository.projections;

public interface EventWeightSumProjection {

    Long getEventId();

    Double getTotalWeight();
}