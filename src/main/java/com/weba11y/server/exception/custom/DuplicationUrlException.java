package com.weba11y.server.exception.custom;

import lombok.Getter;

@Getter
public class DuplicationUrlException extends RuntimeException{
    private Long urlId;

    public DuplicationUrlException (String message, Long urlId){
        super(message);
        this.urlId = urlId;
    }
}
