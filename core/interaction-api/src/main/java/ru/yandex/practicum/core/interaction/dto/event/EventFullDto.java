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
public class EventFullDto {

    private String annotation;
    private CategoryResponse category;
    private Long confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
    private Long id;
    private UserShortDto initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventState state;
    private String title;
    private Long rating;
    private Long views;
}