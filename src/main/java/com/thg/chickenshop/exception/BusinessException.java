package com.thg.chickenshop.exception;

public class BusinessException extends RuntimeException {

    private final Object[] args;

    public BusinessException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}