package ru.yandex.practicum.core.interaction.dto.event;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    private Double lat;
    private Double lon;
}