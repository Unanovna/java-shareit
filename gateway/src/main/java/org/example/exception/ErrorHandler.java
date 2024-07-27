package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice("ru.practicum.shareit")
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException e) {
        String strError = String.format("Validation Error: %s", e.getMessage());
        log.info(strError);
        return new ErrorResponse(strError);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        String strError = e.getMessage();
        String strSubString = "default message";
        int index = strError.lastIndexOf(strSubString);
        String strMessage = index == 0 ? "" : strError.substring(index + strSubString.length());
        strError = String.format("Method argument not valid: %s", strMessage.isBlank() ? strError : strMessage);
        log.info(strError);
        return new ErrorResponse(strError);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        String strError = String.format("Required object wasn't found: %s", e.getMessage());
        log.info(strError);
        return new ErrorResponse(strError);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }
}
