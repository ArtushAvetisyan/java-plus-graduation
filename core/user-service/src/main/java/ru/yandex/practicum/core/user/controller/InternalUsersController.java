package ru.yandex.practicum.core.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;
import ru.yandex.practicum.core.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/internal/users")
@RequiredArgsConstructor
@Validated
public class InternalUsersController {

    private final UserService userService;

    @PostMapping
    public List<UserShortDto> getUsersByIds(@Valid @RequestBody List<Long> ids) {
        return userService.getUsersByIds(ids);
    }
}