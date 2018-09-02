package com.sldlt.controller;

import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleConflict(final Exception ex, final WebRequest request) throws Exception {
        logger.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, Collections.singletonMap("error", "Something went wrong."), new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
