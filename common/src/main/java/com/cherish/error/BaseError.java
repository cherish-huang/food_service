package com.cherish.error;

import lombok.Getter;

@Getter
public class BaseError extends RuntimeException{

    private int code;
    private String message;

    private BaseError(BaseErr baseErr) {
        this.code = baseErr.getCode();
        this.message = baseErr.getMessage();
    }

    public static BaseError newBaseError(BaseErr baseErr){
        return new BaseError(baseErr);
    }


}
