package com.taskforge.exceptions;

public class DuplicateTaskTitleException extends RuntimeException {
    public DuplicateTaskTitleException(String message) {
        super(message);
    }
}