package com.tenniscourts.exceptions;

public class ParameterTypeMismatchException extends RuntimeException {

    public ParameterTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}