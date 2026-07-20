package ru.yandex.practicum.core.rating.controller;

import lombok.RequiredArgsConstructor;
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
public class PublicRatingController {

    private final EventReactionService eventReactionService;

    @GetMapping
    public List<EventShortDto> getSortedEvents(@RequestParam(defaultValue = "DESC") String sort,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return eventReactionService.getTopEventsByRating(sort, size);
    }
}