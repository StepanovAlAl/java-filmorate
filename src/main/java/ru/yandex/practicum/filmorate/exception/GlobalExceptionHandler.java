package ru.yandex.practicum.filmorate.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException ex) {
        log.warn("Объект не найден: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> {
            String message = err.getDefaultMessage();
            String field = err.getField();
            errors.put(field, message);
            log.warn("Ошибка валидации поля '{}': {}", field, message);
        });
        return errors;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException ex) {
        log.warn("Ошибка валидации: {}", ex.getMessage());
        return Map.of("error", ex.getMessage());
    }

    //@ExceptionHandler(ResponseStatusException.class)
    //@ResponseStatus(HttpStatus.NOT_FOUND)
    //public Map<String, String> handleNotFoundException(ResponseStatusException ex) {
    //    log.warn("Объект не найден: {}", ex.getReason());
    //    return Map.of("error", ex.getReason());
    //}

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidJson(HttpMessageNotReadableException ex) {
        log.warn("Невалидный JSON: {}", ex.getMessage());
        return Map.of("error", "Невалидный JSON");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleUnexpectedException(Exception ex) {
        log.error("Непредвиденная ошибка: {}", ex.getMessage(), ex);
        return Map.of("error", "Внутренняя ошибка сервера");
    }
}