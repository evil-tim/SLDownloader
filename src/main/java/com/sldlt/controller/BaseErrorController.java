package com.sldlt.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.sldlt.metrics.annotation.Instrumented;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class BaseErrorController implements ErrorController {

    private static final String PATH = "/error";

    @GetMapping(value = PATH)
    @Instrumented
    public String handleError(final HttpServletRequest request) {
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            try {
                statusCode = Integer.valueOf(status.toString());
            } catch (NumberFormatException ex) {
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            }
        }

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return "error-404";
        } else {
            return "error";
        }
    }
}
