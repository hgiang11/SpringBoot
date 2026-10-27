package com.thg.chickenshop.exception;

import com.thg.chickenshop.controller.CartRestController;
import com.thg.chickenshop.service.MessageService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CartRestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    public record ApiError(String message) { }

    private final MessageService messageService;

    public ApiExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusiness(BusinessException ex) {
        return new ApiError(messageService.get(ex.getMessage(), ex.getArgs()));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex) {
        return new ApiError(messageService.get(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalid() {
        return new ApiError(messageService.get("cart.qtyRange", 20));
    }
}