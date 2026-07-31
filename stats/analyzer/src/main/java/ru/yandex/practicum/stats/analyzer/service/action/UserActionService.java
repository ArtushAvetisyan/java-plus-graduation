package ru.yandex.practicum.stats.analyzer.service.action;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionService {
    void processAction(UserActionAvro action);
}