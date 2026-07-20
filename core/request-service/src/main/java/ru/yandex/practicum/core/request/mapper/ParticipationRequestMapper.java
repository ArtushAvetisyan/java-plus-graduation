package ru.yandex.practicum.core.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.core.interaction.dto.requests.ParticipationRequestDto;
import ru.yandex.practicum.core.request.model.ParticipationRequest;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requesterId", source = "requesterId")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "created", ignore = true)
    ParticipationRequest toRequest(Long requesterId, Long eventId);

    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "event", source = "eventId")
    ParticipationRequestDto toRequestDto(ParticipationRequest request);
}