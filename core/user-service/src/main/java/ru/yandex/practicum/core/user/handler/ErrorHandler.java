package ru.yandex.practicum.core.user.handler;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.yandex.practicum.core.interaction.handler.exception.AlreadyExistsException;
import ru.yandex.practicum.core.interaction.handler.exception.BadRequestException;
import ru.yandex.practicum.core.interaction.handler.exception.ConflictException;
import ru.yandex.practicum.core.interaction.handler.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    private static final Integer MAX_STACKTRACE_LENGTH = 10;

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class,
            MissingServletRequestParameterException.class,
            HandlerMethodValidationException.class,
            BadRequestException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final Exception e) {
        String message = e.getMessage();

        if (e instanceof MethodArgumentNotValidException ex) {
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(error -> String.format("Поле: %s. Ошибка: %s. Отклоненное значение: [%s]",
                            error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                    .collect(Collectors.joining("; "));
        }

        return handleException(e, HttpStatus.BAD_REQUEST, "Неправильно составленный запрос.", message);
    }

    @ExceptionHandler({
            EntityNotFoundException.class,
            NotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final RuntimeException e) {
        return handleException(e, HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден.", e.getMessage());
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            AlreadyExistsException.class,
            ConflictException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final RuntimeException e) {
        return handleException(e, HttpStatus.CONFLICT, "Нарушено ограничение целостности.", e.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера.", e.getMessage());
    }

    private ApiError handleException(final Throwable e, final HttpStatus status, String reason, String message) {
        log.warn("Перехвачено исключение: [{}] Статус: {}. Сообщение: {}", e.getClass().getSimpleName(), status.value(), message);

        List<ErrorDetail> errors = new ArrayList<>();

        errors.add(ErrorDetail.builder()
                .type(e.getClass().getSimpleName())
                .stackTrace(getStackTrace(e))
                .build());

        Throwable cause = e.getCause();
        if (cause != null) {
            errors.add(ErrorDetail.builder()
                    .type(cause.getClass().getSimpleName())
                    .stackTrace(getStackTrace(cause))
                    .build());
        }

        return ApiError.builder()
                .status(status)
                .message(message)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
    }

    private List<String> getStackTrace(Throwable e) {
        return Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .limit(MAX_STACKTRACE_LENGTH)
                .toList();
    }
}