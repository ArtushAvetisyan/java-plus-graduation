package ru.yandex.practicum.core.interaction.handler;

import feign.Response;
import feign.codec.ErrorDecoder;
import ru.yandex.practicum.core.interaction.handler.exception.BadRequestException;
import ru.yandex.practicum.core.interaction.handler.exception.ConflictException;
import ru.yandex.practicum.core.interaction.handler.exception.NotFoundException;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new BadRequestException("Некорректный запрос к микросервису");
            case 404 -> new NotFoundException("Ресурс в микросервисе не найден");
            case 409 -> new ConflictException("Конфликт бизнес-логики в микросервисе");
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}