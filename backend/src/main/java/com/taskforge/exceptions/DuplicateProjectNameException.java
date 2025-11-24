package com.taskforge.exceptions;

public class DuplicateProjectNameException extends RuntimeException {
    public DuplicateProjectNameException(String message) {
        super(message);
    }
}