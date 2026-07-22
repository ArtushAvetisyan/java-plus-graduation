package ru.yandex.practicum.core.interaction.dto.event;

import lombok.*;
import ru.yandex.practicum.core.interaction.dto.category.CategoryResponse;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryResponse category;
    private UserShortDto initiator;
    private Long confirmedRequests;
    private Long views;
    private Boolean paid;
    private Long rating;
    private LocalDateTime eventDate;
}