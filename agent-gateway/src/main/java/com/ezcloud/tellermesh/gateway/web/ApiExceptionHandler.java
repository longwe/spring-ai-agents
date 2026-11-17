package com.ezcloud.tellermesh.gateway.web;

import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Problem-detail error responses for the gateway.
 *
 * <p>Extends {@link ResponseEntityExceptionHandler} rather than declaring a plain
 * {@code @ExceptionHandler}: Boot's own problem-details handler already handles
 * {@link MethodArgumentNotValidException} and would run first. Declaring this bean makes Boot
 * back off, while every other standard Spring MVC exception keeps its default problem detail.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    /** Names each invalid field in {@code detail}, e.g. {@code "message: must not be blank"}. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .sorted()
                .collect(Collectors.joining("; "));
        var body = ex.getBody();
        body.setDetail(detail);
        return handleExceptionInternal(ex, body, headers, status, request);
    }
}
