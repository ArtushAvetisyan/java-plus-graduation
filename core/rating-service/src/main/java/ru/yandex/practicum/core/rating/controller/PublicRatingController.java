package ru.yandex.practicum.core.rating.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.core.interaction.dto.event.EventShortDto;
import ru.yandex.practicum.core.rating.service.EventReactionService;

import java.util.List;

@RestController
@RequestMapping("/v1/ratings")
@RequiredArgsConstructor
@Validated
public class PublicRatingController {

    private final EventReactionService eventReactionService;

    @GetMapping
    public List<EventShortDto> getSortedEvents(
            @RequestParam(name = "sort", defaultValue = "DESC") String sort,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return eventReactionService.getTopEventsByRating(sort, size);
    }
}