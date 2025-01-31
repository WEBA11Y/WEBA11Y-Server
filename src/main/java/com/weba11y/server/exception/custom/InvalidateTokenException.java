package com.weba11y.server.exception.custom;

public class InvalidateTokenException extends RuntimeException{
    public InvalidateTokenException(String message){
        super(message);
    }
}
