package ru.yandex.practicum.core.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.core.interaction.dto.users.NewUserRequest;
import ru.yandex.practicum.core.interaction.dto.users.UserDto;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;
import ru.yandex.practicum.core.interaction.handler.exception.NotFoundException;
import ru.yandex.practicum.core.user.mapper.UserMapper;
import ru.yandex.practicum.core.user.model.User;
import ru.yandex.practicum.core.user.repository.UsersRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final UserMapper userMapper;

    @Override
    public UserShortDto getUserById(Long userId) {
        User user = usersRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id - " + userId + " не найден"));
        return userMapper.userToUserShortDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        User newUser = usersRepository.save(userMapper.newUserRequestToUser(newUserRequest));

        return userMapper.userToUserDto(newUser);
    }

    @Override
    public List<UserShortDto> getUsersByIds(List<Long> ids) {
        return usersRepository.findAllById(ids).stream()
                .map(userMapper::userToUserShortDto)
                .toList();
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        List<User> users = ids != null && !ids.isEmpty() ?
                usersRepository.findByIdIn(ids) :
                usersRepository.findAllWithOffsetLimit(from, size);

        return users.stream()
                .map(userMapper::userToUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = usersRepository.findByIdOrThrow(userId);
        usersRepository.delete(user);
    }
}