package org.example.duelmasters.Controllers;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatusCode().value());
        body.put("errors", List.of(ex.getReason()));

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }
}
