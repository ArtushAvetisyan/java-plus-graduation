package ru.yandex.practicum.core.rating.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.core.interaction.dto.reaction.ReactionType;
import ru.yandex.practicum.core.interaction.dto.users.UserRatingStatsDto;
import ru.yandex.practicum.core.interaction.dto.users.UserShortDto;
import ru.yandex.practicum.core.rating.service.EventReactionService;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/ratings")
@RequiredArgsConstructor
@Validated
public class AdminRatingController {

    private final EventReactionService eventReactionService;

    @GetMapping("/by-events")
    public List<UserShortDto> getUsersByReactionOrByEventIds(
            @RequestParam(name = "eventIds") List<Long> eventIds,
            @RequestParam(name = "reaction") ReactionType reaction,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero Integer size) {
        return eventReactionService.getUsersByReaction(eventIds, reaction, from, size);
    }

    @GetMapping("/by-users")
    public List<UserRatingStatsDto> getReactionsByUsersIds(@RequestParam(name = "usersIds") List<Long> usersIds) {
        return eventReactionService.getUsersRatingStats(usersIds);
    }
}