package ru.yandex.practicum.core.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.core.event.model.Event;
import ru.yandex.practicum.core.event.model.Location;
import ru.yandex.practicum.core.interaction.dto.event.EventFullDto;
import ru.yandex.practicum.core.interaction.dto.event.EventShortDto;
import ru.yandex.practicum.core.interaction.dto.event.LocationDto;
import ru.yandex.practicum.core.interaction.dto.event.NewEventDto;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface EventMapper {

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "rating", source = "rating")
    EventShortDto toEventShortDto(Event event, UserShortDto initiator, Long confirmedRequests, Long views, Long rating);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiatorId", source = "initiatorId")
    @Mapping(target = "eventState", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    Event toEvent(NewEventDto newEventDto, Long initiatorId);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "state", source = "event.eventState")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "rating", source = "rating")
    EventFullDto toEventFullDto(Event event, UserShortDto initiator, Long confirmedRequests, Long views, Long rating);

    Location toLocation(LocationDto locationDto);

    LocationDto toLocationDto(Location location);
}