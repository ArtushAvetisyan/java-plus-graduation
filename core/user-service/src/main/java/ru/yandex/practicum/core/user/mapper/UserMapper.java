package ru.yandex.practicum.core.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.core.interaction.dto.users.NewUserRequest;
import ru.yandex.practicum.core.interaction.dto.users.UserDto;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;
import ru.yandex.practicum.core.user.model.User;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto userToUserDto(User user);

    UserShortDto userToUserShortDto(User user);

    @Mapping(target = "id", ignore = true)
    User newUserRequestToUser(NewUserRequest newUserRequest);
}