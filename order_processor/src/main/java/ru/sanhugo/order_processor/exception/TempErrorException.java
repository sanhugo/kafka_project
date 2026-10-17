package ru.sanhugo.order_processor.exception;

public class TempErrorException extends RuntimeException {
    public TempErrorException(String message) {
        super(message);
    }
}
