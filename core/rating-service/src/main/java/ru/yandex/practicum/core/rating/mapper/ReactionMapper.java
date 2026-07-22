package ru.yandex.practicum.core.rating.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.core.interaction.dto.reaction.EventReactionDto;
import ru.yandex.practicum.core.interaction.dto.reaction.ReactionType;
import ru.yandex.practicum.core.rating.model.EventReaction;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface ReactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reactorId", source = "userId")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "reactionType", source = "reactionType")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    EventReaction toReaction(Long userId, Long eventId, ReactionType reactionType);

    EventReactionDto toReactionDto(EventReaction reaction);
}