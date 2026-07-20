package ru.yandex.practicum.core.rating.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.core.interaction.dto.event.EventFullDto;
import ru.yandex.practicum.core.interaction.dto.reaction.EventReactionDto;
import ru.yandex.practicum.core.interaction.dto.reaction.ReactionType;
import ru.yandex.practicum.core.rating.service.EventReactionService;

import java.util.List;

@RestController
@RequestMapping("/v1/{userId}/ratings")
@RequiredArgsConstructor
public class PrivateRatingController {

    private final EventReactionService eventReactionService;

    @PostMapping("/{eventId}/{reaction}")
    @ResponseStatus(HttpStatus.CREATED)
    public EventReactionDto saveReaction(@PathVariable("eventId") Long eventId,
                                         @PathVariable("userId") Long userId,
                                         @PathVariable("reaction") ReactionType reaction) {
        return eventReactionService.addReaction(userId, eventId, reaction);
    }

    @DeleteMapping("/{eventId}/{reaction}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReaction(@PathVariable("eventId") Long eventId,
                               @PathVariable("userId") Long userId,
                               @PathVariable("reaction") ReactionType reaction) {
        eventReactionService.deleteReaction(userId, eventId, reaction);
    }

    @GetMapping
    public List<EventFullDto> getEventsUserLiked(@PathVariable("userId") Long userId) {
        return eventReactionService.getFavoriteEvents(userId);
    }
}