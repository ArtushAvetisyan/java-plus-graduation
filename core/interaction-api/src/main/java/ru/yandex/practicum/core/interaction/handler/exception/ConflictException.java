package ru.yandex.practicum.core.interaction.handler.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}