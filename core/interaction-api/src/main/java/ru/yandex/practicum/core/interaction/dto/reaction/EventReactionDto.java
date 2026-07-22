package ru.yandex.practicum.core.interaction.dto.reaction;

import lombok.*;


import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventReactionDto {

    private Long reactorId;
    private Long eventId;
    private ReactionType reactionType;
    private LocalDateTime createdAt;
}