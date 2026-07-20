package ru.yandex.practicum.core.interaction.handler;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    private static final Integer MAX_STACKTRACE_LENGTH = 10;

    @ExceptionHandler({MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final Exception e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "Неправильно составленный запрос");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handeNotFoundException(final RuntimeException e) {
        return handleException(e, HttpStatus.NOT_FOUND, "Требуемый объект не найден");
    }

    @ExceptionHandler({AlreadyExistsException.class, ConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final RuntimeException e) {
        return handleException(e, HttpStatus.CONFLICT, "Нарушено ограничение целостности");
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiError> handleFeignException(final FeignException e) {
        HttpStatus status = HttpStatus.resolve(e.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ApiError apiError = handleException(e, status, "Ошибка при межсервисном взаимодействии");
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleCircuitBreakerOpen(final CallNotPermittedException e) {
        return handleException(e, HttpStatus.SERVICE_UNAVAILABLE, "Сервис временно недоступен");
    }

    private ApiError handleException(final Throwable e, final HttpStatus status, @Nullable String reason) {
        log.warn("{} {}", status.value(), e.getMessage(), e);

        List<ErrorDetail> errors = new ArrayList<>();

        List<String> stackTrace = getStackTrace(e);

        ErrorDetail errorDetail = ErrorDetail.builder()
                .type(e.getClass().getSimpleName())
                .stackTrace(stackTrace)
                .build();

        errors.add(errorDetail);

        Throwable cause = e.getCause();
        if (cause != null) {
            List<String> causeStackTrace = getStackTrace(cause);

            ErrorDetail causeError = ErrorDetail.builder()
                    .type(cause.getClass().getSimpleName())
                    .stackTrace(causeStackTrace)
                    .build();

            errors.add(causeError);
        }

        return ApiError.builder()
                .status(status)
                .message(e.getMessage())
                .reason(reason != null ? reason : "Неизвестная причина ошибки")
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