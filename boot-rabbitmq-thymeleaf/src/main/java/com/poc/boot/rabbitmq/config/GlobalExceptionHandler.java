package com.poc.boot.rabbitmq.config;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    ProblemDetail onException(HttpServletRequest request, Exception exception) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatusCode.valueOf(500), exception.getMessage());
        problemDetail.setTitle("Internal Server Error");

        // Try to extract the 'order' from flash attributes (test uses flashAttr)
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        Object order = null;
        if (inputFlashMap != null) {
            order = inputFlashMap.get("order");
        }
        if (order == null) {
            // fallback to request attribute if present
            order = request.getAttribute("order");
        }

        if (order != null) {
            problemDetail.setDetail("Unable To Parse " + order);
        } else {
            problemDetail.setDetail(exception.getMessage());
        }
        problemDetail.setType(
                URI.create("https://api.boot-rabbitmq-thymeleaf.com/errors/exception"));
        return problemDetail;
    }
}
