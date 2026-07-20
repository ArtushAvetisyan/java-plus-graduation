package ru.yandex.practicum.core.user.service;


import ru.yandex.practicum.core.interaction.dto.users.NewUserRequest;
import ru.yandex.practicum.core.interaction.dto.users.UserDto;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;

import java.util.List;

public interface UserService {

    UserShortDto getUserById(Long userId);

    UserDto createUser(NewUserRequest newUserRequest);

    List<UserShortDto> getUsersByIds(List<Long> ids);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}