package com.example.sashabf.exception;



import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorMessage resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMessage badRequestException(BadRequestException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
    }
    @ExceptionHandler(PasswordInvalidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMessage passwordInvalidException(PasswordInvalidException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
    }
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage resourceAlreadyExistsException(ResourceAlreadyExistsException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage globalExceptionHandler(Exception ex, WebRequest request) {
        log.error("Error no controlado: ", ex); // Para que tú lo veas en la consola
        return new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(),
                "Se ha producido un error interno en el servidor. Inténtelo de nuevo más tarde.",
                request.getDescription(false));
    }
    @ExceptionHandler(GuardiaYaAsignadaException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT) // Mantenemos el 409 Conflict
    public ErrorMessage manejarGuardiaAsignada(GuardiaYaAsignadaException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false));
    }
}
