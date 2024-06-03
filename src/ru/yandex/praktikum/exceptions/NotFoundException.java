package ru.yandex.praktikum.exceptions;

public class NotFoundException extends RuntimeException {

    private final int statusCode;

    public NotFoundException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
