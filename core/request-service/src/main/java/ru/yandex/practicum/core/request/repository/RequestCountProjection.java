package ru.yandex.practicum.core.request.repository;

public interface RequestCountProjection {
    Long getEventId();

    Long getConfirmedCount();
}