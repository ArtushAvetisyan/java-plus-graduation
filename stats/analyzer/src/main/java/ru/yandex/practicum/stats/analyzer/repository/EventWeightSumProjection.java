package ru.yandex.practicum.stats.analyzer.repository;

public interface EventWeightSumProjection {

    Long getEventId();

    Double getTotalWeight();
}