package com.taskforge.exceptions;

public class DuplicateUserStoryTitleException extends RuntimeException {
    public DuplicateUserStoryTitleException(String message) {
        super(message);
    }
}
