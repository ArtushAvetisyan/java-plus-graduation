package ru.yandex.practicum.core.interaction.dto.event;

import java.time.LocalDateTime;
import java.util.List;

public record EventSearchFilterAdmin(
        List<Long> users,
        List<EventState> states,
        List<Long> categories,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd
) {
}