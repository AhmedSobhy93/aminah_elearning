package com.aminah.elearning.utils;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = ResponseStatusException.class, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView handleHtmlStatus(ResponseStatusException exception) {
        return new ModelAndView("error", Map.of("status", exception.getStatusCode().value()),
                exception.getStatusCode());
    }
}
