package com.aminah.elearning.utils;


import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

@ControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

//    @ExceptionHandler(Exception.class)
//    @ResponseBody
//    public ResponseEntity<?> handleAll(Exception ex) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", ex.getMessage()));
//    }
}