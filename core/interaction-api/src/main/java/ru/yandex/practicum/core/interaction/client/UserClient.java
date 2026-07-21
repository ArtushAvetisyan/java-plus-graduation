package ru.yandex.practicum.core.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/admin/users/{userId}")
    UserShortDto getUserById(@PathVariable(value = "userId") Long userId);

    @PostMapping("/internal/users")
    List<UserShortDto> getUsersByIds(@Valid @RequestBody List<Long> ids);
}