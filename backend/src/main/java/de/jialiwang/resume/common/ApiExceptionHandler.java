package de.jialiwang.resume.common;

import de.jialiwang.resume.ai.AiUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) { return problem(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage()); }

    @ExceptionHandler(AiUnavailableException.class)
    ProblemDetail ai(AiUnavailableException e) { return problem(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", e.getMessage()); }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) { return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(x -> x.getField() + ": " + x.getDefaultMessage()).findFirst().orElse("输入无效");
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    private ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle(code); p.setType(URI.create("https://resume.local/problems/" + code.toLowerCase()));
        p.setProperty("code", code); return p;
    }
}
