package ru.yandex.practicum.core.event.repository;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.core.event.model.Event;
import ru.yandex.practicum.core.interaction.dto.event.EventSearchFilterAdmin;
import ru.yandex.practicum.core.interaction.dto.event.EventSearchFilterPublic;


import java.util.List;

public interface EventQuerydslRepository {
    List<Event> searchAdmin(EventSearchFilterAdmin filter, Pageable pageable);

    List<Event> searchPublic(EventSearchFilterPublic filter, Pageable pageable);
}