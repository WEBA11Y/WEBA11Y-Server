package com.weba11y.server.global.exception.custom;

public class DeactivatedMemberException extends RuntimeException {
    public DeactivatedMemberException(String message) {
        super(message);
    }
}
